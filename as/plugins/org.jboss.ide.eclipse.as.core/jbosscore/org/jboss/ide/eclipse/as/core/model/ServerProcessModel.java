package org.jboss.ide.eclipse.as.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEventRoot;
import org.jboss.ide.eclipse.as.core.server.IServerLogListener;
import org.jboss.ide.eclipse.as.core.server.IServerProcessListener;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.ServerProcessEvent;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

/**
 * This model keeps track of each server and the processes that 
 * are currently running in association with it. 
 * 
 * This includes start processes (a running jboss server), as 
 * well as stop and twiddle launches trying to communicate with 
 * this particular server. 
 * 
 * @author rstryker
 *
 */
public class ServerProcessModel implements IServerLifecycleListener {
	
	public static final String START_PROCESSES = "__START_PROCESSES__";
	public static final String STOP_PROCESSES = "__STOP_PROCESSES__";
	public static final String TWIDDLE_PROCESSES = "__TWIDDLE_PROCESSES__";
	public static final String TERMINATED_PROCESSES = "__TERMINATED__PROCESSES__";
	
	
	private static ServerProcessModel instance;
	
	/**
	 * There is a singleton instance of the ServerProcessModel.
	 * @return
	 */
	public static ServerProcessModel getDefault() {
		if( instance == null ) {
			instance = new ServerProcessModel();
		}
		return instance;
	}
	
	/**
	 * Static method to check if all of some process array are terminated.
	 * @param processes
	 * @return
	 */
	public static boolean allProcessesTerminated(IProcess[] processes) {
		for( int i = 0; i < processes.length; i++ ) {
			if( !(processes[i].isTerminated())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Will map server ID's to a server process entity.
	 * Each server has it's own entity instance.
	 */
	private HashMap map;
	private ArrayList logListeners;

	
	public ServerProcessModel() {
		map = new HashMap();
		logListeners = new ArrayList();
		initialize();
	}
	
	private void initialize() {
		ServerCore.addServerLifecycleListener(this);
		JBossServer[] servers = JBossServerCore.getAllJBossServers();
		for( int i = 0; i < servers.length; i++ ) {
			getModel(servers[i].getServer().getId(), true);
		}
	}
	
	
	/**
	 * Each JBoss Server instance has it's own sub-model
	 * @param key
	 * @return
	 */
	public ServerProcessModelEntity getModel(String key) {
		return getModel(key, false);
	}
	
	
	private ServerProcessModelEntity getModel(String key, boolean create) {
		Object o = map.get(key);
		if( o == null ) {
			if( !create ) return null;
			o = new ServerProcessModelEntity(key);
			map.put(key, (ServerProcessModelEntity)o);
			processModelChanged((ServerProcessModelEntity)o);
		}
		return ((ServerProcessModelEntity)o);
	}
	
	
	
	public ServerProcessModelEntity[] getModels() {
		ArrayList list = new ArrayList();
		Iterator i = map.keySet().iterator();
		while(i.hasNext()) {
			list.add(map.get(i.next()));
		}
		ServerProcessModelEntity[] models = new ServerProcessModelEntity[list.size()];
		list.toArray(models);
		return models;
	}
	
	public void addLogListener(IServerLogListener listener) {
		logListeners.add(listener);
	}
	public void removeLogListener(IServerLogListener listener) {
		logListeners.remove(listener);
	}

	public void processModelChanged(ServerProcessModelEntity ent) {
		Iterator i = logListeners.iterator();
		while(i.hasNext()) {
			((IServerLogListener)i.next()).logChanged(ent);
		}
	}
	/**
	 * Completely shut down all JBoss Servers, as well as all 
	 * start, stop, and twiddle launches associated with them.
	 */
	public void terminateAllProcesses() {
		Iterator i = map.values().iterator();
		ServerProcessModelEntity entity;
		while(i.hasNext()) {
			entity = (ServerProcessModelEntity)i.next();
			entity.clearAll();
		}
	}
	
	
	
	
	
	/**
	 * Process model for one jboss server
	 */
	public class ServerProcessModelEntity implements IDebugEventSetListener {
	
		private HashMap allProcesses;
		private String serverID;
		private ArrayList processListeners;
		private ArrayList removeOnTerminate;
		private ProcessLogEventRoot eventLog;
		private ServerConsoleLogger logger;
		
		public ServerProcessModelEntity(String serverID) {
			allProcesses = new HashMap();
			processListeners = new ArrayList();
			this.serverID = serverID;
			removeOnTerminate = new ArrayList();
			DebugPlugin.getDefault().addDebugEventListener(this);
			eventLog = new ProcessLogEventRoot(serverID);
			logger = new ServerConsoleLogger(serverID); 
			addSPListener(logger);
		}
		
		public ProcessData[] getProcessDatas(String processType) {
			Iterator i = allProcesses.keySet().iterator();
			Set s = new HashSet();
			ProcessData data;
			while(i.hasNext()) {
				data = (ProcessData)(allProcesses.get(i.next()));
				if( data.getProcesType().equals(processType)) {
					s.add(data);
				}
			}
			ProcessData[] ret = new ProcessData[s.size()];
			s.toArray(ret);
			return ret;
		}
		
		public IProcess[] getProcesses(String processType) {
			ProcessData[] datas = getProcessDatas(processType);
			IProcess[] ret = new IProcess[datas.length];
			for( int i = 0; i < datas.length; i++ ) {
				ret[i] = datas[i].getProcess();
			}
			return ret;
		}
		
		
		public void clearAll() {
			ArrayList datas = new ArrayList(allProcesses.values());
			Iterator it = datas.iterator();
			ProcessData d = null;
			while(it.hasNext()) {
				d = (ProcessData)it.next();
				completelyRemove(new ProcessData[] { d }, d.getProcesType());
			}
		}
		
		public void clear( String processType ) {
			ArrayList l = new ArrayList();
			l.addAll(allProcesses.values());
			
			Iterator i = l.iterator();
			ProcessData pd = null;
			while(i.hasNext()) {
				pd = (ProcessData)i.next();
				if( (pd.getProcesType().equals(processType))) {
					completelyRemove( new ProcessData[] {pd}, pd.getProcesType());
				}
			}
		}
		
		public void clearTerminated() {
			ArrayList l = new ArrayList();
			l.addAll(allProcesses.values());
			
			Iterator i = l.iterator();
			ProcessData pd = null;
			while(i.hasNext()) {
				pd = (ProcessData)i.next();
				if( pd.getProcess().isTerminated()) {
					completelyRemove( new ProcessData[] {pd}, ServerProcessModel.TERMINATED_PROCESSES);
				}
			}
		}
		
		/**
		 * Completely remove the processes from the model,
		 * close / destroy their streams, and fire events to 
		 * any listeners.
		 * 
		 * @param pd
		 * @param processType
		 */
		private void completelyRemove(ProcessData[] pd, String processType) {
			// Terminate anything that's not already terminated. 
			for( int i = 0; i < pd.length; i++ ) {

				if( pd[i] == null ) continue;
				
				if( !pd[i].getProcess().isTerminated()) {
					try {
						pd[i].getProcess().terminate();
						pd[i].destroy();
					} catch ( DebugException de ) {
					}
				}
				allProcesses.remove(pd[i].getProcess());
				removeOnTerminate.remove(pd[i].getProcess());
			}
			fireEvents(IServerProcessListener.PROCESS_REMOVED, processType, pd, null);
		}
		
		public void add( IProcess[] processes, String processType, ILaunchConfiguration config ) {
			ArrayList pds = new ArrayList();
			ProcessData d = null;
			for( int i = 0; i < processes.length; i++ ) {
				d = new ProcessData(processes[i], processType);
				allProcesses.put(processes[i],d);
				pds.add(d);
			}
			
			
			ProcessData[] pdsArray = new ProcessData[pds.size()];
			pds.toArray(pdsArray);
			
			fireEvents(IServerProcessListener.PROCESS_ADDED, processType, pdsArray, config);
		}
		
		public void fireEvents(String eventType, String processType, ProcessData[] processes, ILaunchConfiguration config) {
			ServerProcessEvent event = new ServerProcessEvent(eventType, processType, processes, config);
			Iterator i = processListeners.iterator();
			while(i.hasNext() ) {
				((IServerProcessListener)i.next()).ServerProcessEventFired(event);
			}
		}
		
		public void addSPListener(IServerProcessListener listener) {
			processListeners.add(listener);
		}
		
		public void removeSPListener(IServerProcessListener listener) {
			processListeners.remove(listener);
		}
		

		public String getServerID() {
			return serverID;
		}
		
		/**
		 * The model is free to remove these processes upon termination.
		 * This designates that no one is listening for their output 
		 * streams, or that they have already gotten what they were
		 * listening for. 
		 * 
		 * @param processes
		 */
		public void removeProcessOnTerminate(IProcess[] processes) {
			for( int i = 0; i < processes.length; i++ ) {
				ProcessData data = (ProcessData)allProcesses.get(processes[i]);
				removeOnTerminate.add(processes[i]);
			}
			recheckTerminatedProcesses();
		}
		

		/**
		 * If a process is terminated and it's in the 
		 * remove-on-terminate list, then completely remove it.
		 */
		public void handleDebugEvents(DebugEvent[] events) {
			boolean recheckTerminated = false;
			for( int i = 0; i < events.length; i++ ) {
				if( events[i].getKind() == DebugEvent.TERMINATE ) {
					recheckTerminatedProcesses();
					return;
				}
			}
		}
		/**
		 * Go through a list of launches allowed to be removed upon
		 * termination and check if any have terminated yet.
		 *
		 */
		public void recheckTerminatedProcesses() {
			Iterator i = removeOnTerminate.iterator();
			IProcess proc;
			ArrayList l = new ArrayList();
			ProcessData data;
			while(i.hasNext()) {
				proc = (IProcess)i.next();
				if( proc.isTerminated()) {
					data = (ProcessData)allProcesses.get(proc);
					if( data == null ) {
						removeOnTerminate.remove(proc);
					} else {
						l.add(data);
					}
				}
			}
			ProcessData[] pdArray = new ProcessData[l.size()];
			l.toArray(pdArray);
			completelyRemove(pdArray, ServerProcessModel.TERMINATED_PROCESSES);
		}

		public ProcessLogEventRoot getEventLog() {
			return eventLog;
		}

	
	
	}
	
	
	private class ServerConsoleLogger implements IServerProcessListener, IStreamListener {
		private IStreamMonitor out, err;
		private ProcessLogEventRoot eventLogRoot;
		private String serverID;
		
		public ServerConsoleLogger(String serverID) {
			try {
				this.serverID = serverID;
			} catch( Exception e ) {
				ASDebug.p("Error", this);
				e.printStackTrace();
			}
		}
		
		public void ServerProcessEventFired(ServerProcessEvent event) {
			try {
			if( event.getProcessType().equals(START_PROCESSES) && event.getProcessDatas().length == 1 ) {
				IStreamsProxy proxy = event.getProcessDatas()[0].getProcess().getStreamsProxy();
				out = proxy.getOutputStreamMonitor();
				err = proxy.getErrorStreamMonitor();
				out.addListener(this);
				err.addListener(this);
				
			}
			} catch( Exception e ) {
				ASDebug.p("Error", this);
				e.printStackTrace();
			}
		}
		
		private ProcessLogEvent getLatestConsole() {
			ProcessLogEvent major = getEventLog().getLatestMajorEvent(true);
			ProcessLogEvent[] children = major.getChildren();
			for( int i = 0; i < children.length; i++ ) {
				if( children[i].getEventType() == ProcessLogEvent.SERVER_CONSOLE ) {
					return children[i];
				}
			}
			ProcessLogEvent newConsole = new ProcessLogEvent("Console Output", ProcessLogEvent.SERVER_CONSOLE);
			major.addChild(newConsole, ProcessLogEvent.ADD_BEGINNING);
			return newConsole;
		}
		private ProcessLogEventRoot getEventLog() {
			if( eventLogRoot == null ) {
				this.eventLogRoot = ServerProcessModel.getDefault().
				getModel(serverID).getEventLog();
			}
			return eventLogRoot;
		}
		

		public void streamAppended(String text, IStreamMonitor monitor) {
			if( monitor.equals(out)) {
				outAppended(text, monitor);
			} else if(monitor.equals(err)) {
				errAppended(text, monitor);
			}
			getEventLog().branchChanged();
		}
		
		protected void outAppended(String text, IStreamMonitor monitor) {
			String[] textSplit = text.split("\r\n|\r|\n");
			for( int i = 0; i < textSplit.length; i++ )
				getLatestConsole().addChild(textSplit[i], ProcessLogEvent.STDOUT);
		}
		protected void errAppended(String text, IStreamMonitor monitor) {
			String[] textSplit = text.split("\r\n|\r|\n");
			for( int i = 0; i < textSplit.length; i++ )
				getLatestConsole().addChild(textSplit[i], ProcessLogEvent.STDERR);
		}
	}
	
	
	/**
	 * A class that can monitor a process's standard out and
	 * standard error, as well as keep track of the process's 
	 * arguments that it was launched with. 
	 * 
	 * @author rstryker
	 *
	 */
	public static class ProcessData implements IStreamListener {
		private String args;
		private String processType;

		private IProcess process;
		private IStreamMonitor outMonitor;
		private IStreamMonitor errMonitor;
		
		private String out, err;

		public ProcessData(IProcess process, String processType) {
			this(null, process, processType);
			try {
				this.args = process.getLaunch().getLaunchConfiguration().
					getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
			} catch( CoreException ce ) {
			}

		}
		
		public ProcessData(String args, IProcess process, String processType) {
			this.args = args;
			this.process = process;
			this.processType = processType;
			
			//ASDebug.p("process is " + process, this);
			//ASDebug.p("Stream Proxy is " + process.getStreamsProxy(), this);
			this.outMonitor = process.getStreamsProxy().getOutputStreamMonitor();
			this.errMonitor = process.getStreamsProxy().getErrorStreamMonitor();
			
			out = "";
			err = "";
		}
		
		public void startListening() {
			outMonitor.addListener(this);
			errMonitor.addListener(this);
		}
		
		public void stopListening() {
			outMonitor.removeListener(this);
			errMonitor.removeListener(this);
		}
		
		public void resetStrings() {
			out = "";
			err = "";
		}
		
		public void destroy() {
			stopListening();
			resetStrings();
		}

		public void streamAppended(String text, IStreamMonitor monitor) {
			if( monitor == outMonitor ) {
				out += text;
			} else if( monitor == errMonitor ) {
				err += text;
			}
		}

		public String getArgs() {
			return args;
		}

		public String getErr() {
			return err;
		}

		public String getOut() {
			return out;
		}
		
		public String getProcesType() {
			return processType;
		}
		
		public IProcess getProcess() {
			return process;
		}

	}


	public void serverAdded(IServer server) {
		if( JBossServerCore.getServer(server) != null ) {
			// init
			getModel(server.getId());
		}
	}

	public void serverChanged(IServer server) {
		// TODO Auto-generated method stub
		
	}

	public void serverRemoved(IServer server) {
		if( JBossServerCore.getServer(server) != null ) {
			ServerProcessModelEntity ent = getModel(server.getId());
			map.remove(server.getId());
			processModelChanged(ent);
		}
	}
	
}
