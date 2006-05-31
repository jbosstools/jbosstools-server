package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.debug.core.model.IProcess;
import org.jboss.ide.eclipse.as.core.client.TwiddleLauncher;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.IProcessLogVisitor;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ProcessData;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;

public class ServerStateChecker extends Thread implements IServerProcessListener {

	public static final boolean UP = true;
	public static final boolean DOWN = false;
	
	
	private static final int STATE_TRUE = 1;
	private static final int STATE_FALSE = 2;
	private static final int STATE_EXCEPTION = 3;
	
	
	
	private static int max = 120000;
	private static int delay = 1000;
	private int current = 0;
	
	private boolean expectedState;
	private boolean startProcessesTerminated = false;
	private ProcessData[] processDatas = new ProcessData[0];
	
	private JBossServerBehavior behavior;
	
	private ProcessLogEvent eventLog;
	private ServerProcessModelEntity ent;
	
	
	public ServerStateChecker(JBossServerBehavior behavior, boolean expectedState) {
		this.behavior = behavior;
		this.expectedState = expectedState;
	}
	
	public void run() {
		JBossServer jbServer = (JBossServer)behavior.getServer().loadAdapter(JBossServer.class, null);
		int jndiPort = jbServer.getDescriptorModel().getJNDIPort();
		String host = jbServer.getRuntimeConfiguration().getHost();
		String args = "-s " + host + ":" + jndiPort +  " -a jmx/rmi/RMIAdaptor " + 
						"get \"jboss.system:type=Server\" Started";
		
		ent =  ServerProcessModel.getDefault().getModel(jbServer.getServer().getId());

		
		// To be sent to the log 
		int logEventType = expectedState ? ProcessLogEvent.SERVER_STARTING : ProcessLogEvent.SERVER_STOPPING;
		String action = (expectedState ? "Starting Server" : "Stopping Server");

		eventLog = ent.getEventLog().newMajorEvent(action, logEventType);
		
		ent.addSPListener(this);
		
		boolean twiddleResults = !expectedState;
		while( current < max && twiddleResults != expectedState ) {
			int res = getTwiddleResults(ent, jbServer, args);
			if( res == STATE_TRUE ) {
				twiddleResults = UP;
			} else if( res == STATE_EXCEPTION) {
				twiddleResults = DOWN;
			} else if( res == STATE_FALSE ) {
				// we're stuck in a middle-state. Do nothing, just wait.
				// Eventually the server will complete its startup or shutdown
				// and return true or generate an exception
			}
			//System.out.println("Results at time " + current + ": " + twiddleResults);
		}

		int state = twiddleResults == UP ? ProcessLogEvent.SERVER_UP : ProcessLogEvent.SERVER_DOWN;
		boolean success = (expectedState && twiddleResults == UP) || (!expectedState && twiddleResults == DOWN);
		String text = "Server " + (success ? "is now " : "failed to ") 
		+ (expectedState ? "started " : "shut down");
		eventLog.addChild(text, state, ProcessLogEvent.ADD_END);

		
		if( expectedState == DOWN && success ) {
			// wait until the processes are actually terminated too.
			while( !startProcessesTerminated && current < max ) {
				try {
					current += delay;
					Thread.sleep(delay);
				} catch(InterruptedException ie) {
				}
			}
		}

		eventLog.setComplete();
		ent.getEventLog().branchChanged();
		
		behavior.setServerState(expectedState, twiddleResults);
		ent.removeSPListener(this);
		
	}

	
	private int getTwiddleResults( ServerProcessModelEntity ent, JBossServer jbServer, String args ) {
		TwiddleLauncher launcher = new TwiddleLauncher(max-current, delay);
		ProcessLogEvent launchEvent = launcher.getTwiddleResults(ent, jbServer, args);
		current += launcher.getDuration();
		
		final Boolean found = new Boolean(false);
		
		IProcessLogVisitor visitor = new IProcessLogVisitor() {
			private int ret = -1;
			private boolean found = false;
			public boolean visit(ProcessLogEvent event) {
				if( found ) return false;
				
				if( event.getText().startsWith("Started=true")) {
					found = true;
					ret = STATE_TRUE;
				} else if( event.getText().startsWith("Started=false")) {
					found = true;
					ret = STATE_FALSE;
				}
				return true;
			} 
			
			public Integer getResult() {
				if( !found ) 
					return new Integer(STATE_EXCEPTION);
				return new Integer(ret);
			}
			
		};
		launchEvent.accept( visitor );
		int retval = ((Integer)visitor.getResult()).intValue(); 
		
		// Modify the type from a twiddle execution to one of state
		if( retval == STATE_EXCEPTION) {
			launchEvent.setEventType(ProcessLogEvent.SERVER_DOWN);
		} else if( retval == STATE_TRUE ) {
			launchEvent.setEventType(ProcessLogEvent.SERVER_UP);
		} else if( retval == STATE_FALSE ) {
			if( expectedState == true ) 
				launchEvent.setEventType(ProcessLogEvent.SERVER_STARTING);
			else 
				launchEvent.setEventType(ProcessLogEvent.SERVER_STOPPING);
		}
		
		eventLog.addChild(launchEvent);
		if( eventLog.getRoot() != null ) 
			eventLog.getRoot().branchChanged();
		return  retval;
		
	}
	
	public void ServerProcessEventFired(ServerProcessEvent event) {
		//ASDebug.p("Serverprocessevent: " + event.getProcessType() + " and " + event.getEventType(), this);
		if( event.getProcessType().equals(ServerProcessModel.TWIDDLE_PROCESSES)) {
			if( event.getEventType().equals(IServerProcessListener.PROCESS_ADDED)) {
				ProcessData[] processDatas = event.getProcessDatas();
				for( int i = 0; i < processDatas.length; i++ ) {
					processDatas[i].startListening();
				}
				this.processDatas = processDatas; 
			}
		} else if( event.getProcessType().equals(ServerProcessModel.TERMINATED_PROCESSES)) {
			ProcessData[] datas = ent.getProcessDatas(ServerProcessModel.START_PROCESSES);
			if( datas.length == 0 ) {
				this.startProcessesTerminated = true;
			} else {
				IProcess[] p = new IProcess[datas.length];
				for( int i = 0; i < datas.length; i++ ) {
					p[i]=datas[i].getProcess();
				}
				if( ServerProcessModel.allProcessesTerminated(p)) {
					this.startProcessesTerminated = true;
				}
			}
		}
	}



}
