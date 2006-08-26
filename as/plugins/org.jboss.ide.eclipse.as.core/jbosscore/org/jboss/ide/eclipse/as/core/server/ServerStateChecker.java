/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.client.TwiddleLauncher;
import org.jboss.ide.eclipse.as.core.client.TwiddleLauncher.TwiddleLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ProcessData;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class ServerStateChecker extends Thread implements IServerProcessListener {

	public static final boolean UP = true;
	public static final boolean DOWN = false;
	
	
	private static final int STATE_TRUE = 1;
	private static final int STATE_FALSE = 2;
	private static final int STATE_EXCEPTION = 3;
	
	
	
	private static int max;
	private static int delay = 1000;
	private int current = 0;
	
	private boolean expectedState;
	private boolean startProcessesTerminated = false;
	private ProcessData[] processDatas = new ProcessData[0];
	
	private JBossServerBehavior behavior;
	private JBossServer jbServer;
	
	private ProcessLogEvent eventLog;
	private ServerProcessModelEntity ent;
	private boolean canceled = false;
	
	
	public ServerStateChecker(JBossServerBehavior behavior, boolean expectedState) {
		this.behavior = behavior;
		this.expectedState = expectedState;
		jbServer = (JBossServer)behavior.getServer().loadAdapter(JBossServer.class, null);
		
		// load our timeouts
		if( expectedState ) { 
			max = jbServer.getAttributeHelper().getStartTimeout();
		} else {
			max = jbServer.getAttributeHelper().getStopTimeout();
		}
	}
	
	public void cancel() {
		canceled = true;
	}
	
	public void run() {
		int jndiPort = jbServer.getDescriptorModel().getJNDIPort();
		String host = jbServer.getServer().getHost();
		String args = "-s " + host + ":" + jndiPort +  " -a jmx/rmi/RMIAdaptor " + 
						"get \"jboss.system:type=Server\" Started";
		
		ent =  ServerProcessModel.getDefault().getModel(jbServer.getServer().getId());
		eventLog = new StateCheckerLogEvent(expectedState);
		ent.getEventLog().newMajorEvent(eventLog);
		
		ent.addSPListener(this);
		
		boolean twiddleResults = !expectedState;
		while( current < max && twiddleResults != expectedState && !canceled) {
			// short circuit
			if( jbServer.getServer().getServerState() == IServer.STATE_STOPPED || 
					jbServer.getServer().getServerState() == IServer.STATE_STARTED) { 
				ASDebug.p("server state is " + jbServer.getServer().getServerState(), this);
				canceled = true; 
			}

			if( ServerProcessModel.allProcessesTerminated(ent.getProcesses(ServerProcessModel.START_PROCESSES))) {
				// this thread may have started early, before the other thread was launched. 
				// Give it 5 seconds
				if( current > 5000 ) {
					ASDebug.p("canceled - processes terminated", this);
					canceled = true;
				}
			}
			
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
			ent.getEventLog().branchChanged();
		}

		// If we canceled, die in that fashion
		if( canceled ) {
			dieCanceled();
			return;
		}
		
		boolean success = (expectedState && twiddleResults == UP) || (!expectedState && twiddleResults == DOWN);
		
		// If we timed out while trying to start the server, and we failed to start, die that way.
		if( current >= max && !success && expectedState) {
			dieTimeoutFailure();
			return;
		}
		
		
		
		StateCheckerLogEvent finalEvent = new StateCheckerLogEvent(twiddleResults, expectedState);
		finalEvent.setTime(current);
		eventLog.addChild(finalEvent, ProcessLogEvent.ADD_END);
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
		//behavior.setServerState(true, false);
		ent.removeSPListener(this);
		
	}
	
	private void dieCanceled() {
		eventLog.addChild(new StateCheckerLogEvent(StateCheckerLogEvent.SERVER_STATE_CHANGE_CANCELED));
		eventLog.setComplete();
		ent.getEventLog().branchChanged();
		ent.removeSPListener(this);
		behavior.setServerState(expectedState, !expectedState);
	}
	
	private void dieTimeoutFailure() {
		ASDebug.p("Timeout on startup", this);
		boolean abortOrIgnore = jbServer.getAttributeHelper().getTimeoutBehavior();
		if( abortOrIgnore == ServerAttributeHelper.TIMEOUT_ABORT ) {
			behavior.setServerState(true, false);;
		} else {
			behavior.setServerState(true, true);;
		}
		
		eventLog.addChild(new StateCheckerLogEvent(StateCheckerLogEvent.SERVER_STATE_CHANGE_TIMEOUT));
		eventLog.setComplete();
		ent.getEventLog().branchChanged();
		ent.removeSPListener(this);
	}
	
	public static class StateCheckerLogEvent extends ProcessLogEvent {
		public static final String EXPECTED_STATE = "_EXPECTED_STATE_";
		public static final String CURRENT_STATE = "_CURRENT_STATE_";
		public static final String SUCCESS = "_SUCCESS_";
		public static final String TWIDDLE_DATA = "_TWIDDLE_DATA_";
		public static final String EVENT_TYPE = "_EVENT_TYPE_";
		public static final String TIME = "_TIME_";
		
		public static final int SERVER_STATE_CHANGE_TIMEOUT = 0;
		public static final int SERVER_STATE_CHANGE_CANCELED = 1;
		public static final int SERVER_STARTING = 2;
		public static final int SERVER_STOPPING = 3;
		public static final int SERVER_UP = 4;
		public static final int SERVER_DOWN = 5;

		public static final int BEFORE = 6;
		public static final int DURING = 7;
		public static final int AFTER = 8;
		
		public StateCheckerLogEvent(boolean currentState, boolean expectedState) {
			super(AFTER);
			setProperty(SUCCESS, new Boolean(currentState == expectedState));
			setProperty(EXPECTED_STATE, new Boolean(expectedState));
			if( currentState ) 
				setProperty(CURRENT_STATE, new Integer(SERVER_UP));
			else 
				setProperty(CURRENT_STATE, new Integer(SERVER_DOWN));
		}
		
		public StateCheckerLogEvent(boolean expectedState) {
			super(BEFORE);
			setProperty(EXPECTED_STATE, new Boolean(expectedState));
		}
		
		public StateCheckerLogEvent(TwiddleLogEvent e, int type) {
			super(DURING);
			//setProperty(TWIDDLE_DATA, e);
			setProperty(TwiddleLogEvent.ARGS, e.getArgs());
			setProperty(TwiddleLogEvent.OUT, e.getOut());
			setProperty(CURRENT_STATE, new Integer(type));
		}
		
		public StateCheckerLogEvent(int type) {
			super(type);
			setProperty(CURRENT_STATE, new Integer(type));
		}
		
		public String[] getAvailableProperties() {
			if( getEventType() == BEFORE ) return new String[] {EXPECTED_STATE};
			if( getEventType() == DURING ) return new String[] {CURRENT_STATE, TIME, TwiddleLogEvent.ARGS, TwiddleLogEvent.OUT};
			if( getEventType() == AFTER ) return new String[] {EXPECTED_STATE, CURRENT_STATE, SUCCESS, TIME};
			if( getEventType() == SERVER_STATE_CHANGE_CANCELED ) return new String[] {};
			if( getEventType() == SERVER_STATE_CHANGE_TIMEOUT ) return new String[] {};
			
			return new String[] {};
		}
		
		public int getCurrentState() {
			try {
				return ((Integer)getProperty(CURRENT_STATE)).intValue();
			} catch( Exception e ) {}
			return -1;
		}

		public boolean getExpectedState() {
			try {
				return ((Boolean)getProperty(EXPECTED_STATE)).booleanValue();
			} catch( Exception e ) {}
			return false;
		}
		
		public boolean isSuccess() {
			if( getCurrentState() == SERVER_UP && getExpectedState()) return true;
			if( getCurrentState() == SERVER_DOWN && !getExpectedState()) return true;
			return false;
		}
		
		public TwiddleLogEvent getTwiddleLogEvent() {
			try {
				return (TwiddleLogEvent)getProperty(TWIDDLE_DATA);
			} catch( Exception e ) {}
			return null;
		}

		public void setTime(int time) {
			setProperty(TIME, new Integer(time));
		}

	}

	/**
	 * Executes the twiddle launch and returns the state of the server
	 * @param ent
	 * @param jbServer
	 * @param args
	 * @return
	 */
	private int getTwiddleResults( ServerProcessModelEntity ent, JBossServer jbServer, String args ) {

		// Log stuff
		TwiddleLauncher launcher = new TwiddleLauncher(max-current, delay);
		TwiddleLogEvent launchEvent = launcher.getTwiddleResults(ent, jbServer, args);
		StateCheckerLogEvent newEvent;
		
		int retval;
		if( launchEvent.getOut().startsWith("Started=true")) {
			newEvent = new StateCheckerLogEvent(launchEvent, StateCheckerLogEvent.SERVER_UP);
			retval = STATE_TRUE;
		} else if(launchEvent.getOut().startsWith("Started=false")) {
			retval = STATE_FALSE;
			if( expectedState == true ) {
				newEvent = new StateCheckerLogEvent(launchEvent, StateCheckerLogEvent.SERVER_STARTING);
			} else { 
				newEvent = new StateCheckerLogEvent(launchEvent, StateCheckerLogEvent.SERVER_STOPPING);
			}
		} else {
			newEvent = new StateCheckerLogEvent(launchEvent, StateCheckerLogEvent.SERVER_DOWN);
			retval = STATE_EXCEPTION;
		}

		current += launcher.getDuration();

		newEvent.setTime(current);
		
		eventLog.addChild(newEvent);
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
