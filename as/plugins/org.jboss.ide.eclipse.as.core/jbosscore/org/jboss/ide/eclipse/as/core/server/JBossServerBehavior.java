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

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jst.server.tomcat.core.internal.FileUtil;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.model.ModuleModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ProcessData;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.publishers.IJbossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.publishers.JstPublisher;
import org.jboss.ide.eclipse.as.core.server.publishers.PackagedPublisher;
import org.jboss.ide.eclipse.as.core.server.runtime.AbstractServerRuntimeDelegate;
import org.jboss.ide.eclipse.as.core.server.runtime.IJBossServerRuntimeDelegate;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossRuntimeConfiguration;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class JBossServerBehavior extends ServerBehaviourDelegate {
	public static final String ATTR_ACTION = "__ACTION__";
	public static final String ACTION_STOPPING = "__ACTION_STOPPING__";
	public static final String ACTION_STARTING = "__ACTION_STARTING__";
	public static final String ACTION_TWIDDLE = "__ACTION_TWIDDLE__";
	
	private JBossServer jbServer = null;
	private ProcessLogEvent log = null;
	
	

	public JBossServerBehavior() {
		super();
	}

	/**
	 * Probally called when it shouldn't be, but, 
	 * ensures that a stopped server is indeed stopped and 
	 * all process references are terminated and removed from
	 * the serverProcessModel.
	 *
	 * Ensures no rogue unreferenced jboss processes are left
	 * executing in the background draining resources. 
	 */
	public void forceStop() {
		getJBossServer().getProcessModel().clearAll();
		setServerStopped();
		return;
	}
	
	public void stop(boolean force) {

		int state = getServer().getServerState();
		if (state == IServer.STATE_STOPPED || force ) {
			forceStop();
			return;
		}

		
		
		JBossServer jbServer = getJBossServer();
		JBossServerRuntime runtime = jbServer.getJBossRuntime();
		AbstractServerRuntimeDelegate runtimeDelegate = runtime.getVersionDelegate();
		ServerProcessModelEntity processModel = jbServer.getProcessModel();


		
		// If we have no reference to any start processes, give up
		if( processModel.getProcessDatas(ServerProcessModel.START_PROCESSES).length == 0 ) {
			// no start processes exist. Give it up.
			setServerStopped(); 
			return;
		}
		
		// If we do have referneces but they're all terminated already, give up
		ProcessData[] datas = processModel.getProcessDatas(ServerProcessModel.START_PROCESSES);
		boolean alive = false;
		for( int i = 0; i < datas.length; i++ ) {
			if( !datas[i].getProcess().isTerminated()) {
				alive = true;
			}
		}

		// and clear it for good measure
		if( !alive ) {
			forceStop();
			return;			
		}
		
		
		
		// Now we actually have to shut it down. Oh well.

		try {
			// Set up our launch configuration for a STOP call (to shutdown.jar)
			ILaunchConfiguration launchConfig = ((Server)getServer()).getLaunchConfiguration(true, null);
			ILaunchConfigurationWorkingCopy wc = launchConfig.getWorkingCopy();
			
			List cp = getJBossServer().getJBossRuntime().getVersionDelegate().getRuntimeClasspath(jbServer, IJBossServerRuntimeDelegate.ACTION_SHUTDOWN);
			String args = runtimeDelegate.getStopArgs(jbServer);
			wc.setAttribute(ATTR_ACTION, ACTION_STOPPING);

			// Set our attributes from our runtime configuration
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, cp);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, 
					getJBossServer().getRuntimeConfiguration().getServerHome());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, 
					getJBossServer().getRuntimeConfiguration().getVMArgs());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, runtimeDelegate.getStopMainType(jbServer));
			
			wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
			
			int maxWait = 30000;
			int soFar = 0;
			
			// waiting for our stop process to be created
			while( processModel.getProcesses(ServerProcessModel.STOP_PROCESSES) == null ) {
				soFar += 250;
				Thread.sleep(250);
			}
			
			// Now waiting for them to all finish execution
			while( !ServerProcessModel.allProcessesTerminated(processModel.getProcesses(ServerProcessModel.STOP_PROCESSES)) 
					&& soFar < maxWait) {
				soFar += 100;
				try {
					Thread.sleep(100);
				} catch( InterruptedException ie ) {
				}
			}
			
			// We're going to stop the server and get rid of all processes, 
			// so grab a reference to them all first!

			IProcess[] stopProcesses = processModel.getProcesses(ServerProcessModel.STOP_PROCESSES);
			IProcess[] startProcesses = processModel.getProcesses(ServerProcessModel.START_PROCESSES);
			processModel.clear(ServerProcessModel.TWIDDLE_PROCESSES);
			

			if( soFar >= maxWait ) {
				// we timed out... even our stop thread didn't finish yet
				// time to manually terminate EVERYTHING
				forceStop();
				return;
			} else {
				// The stop process ended. Let's see if it ended successfully.
				
				if( stopProcesses[0].isTerminated()) {
					if( stopProcesses[0].getExitValue() != 0 ) {
						// Our stop process ended with exceptions. That means the server is still running.
						// We need to shut it down.  

						String text = "\n\n" + 
								"The server shutdown script failed. The server has been terminated manually.\n" + 
								"The most common cause of this is that a minimal configuration does not respond to the shutdown script\n" +
								"The shutdown script's exception trace can be seen above \n\n";
//						JBossServerCore.getDefault().sendToConsole(text);
						forceStop();
						return;
						
					}
				}

			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Ok... if we didn't switch to forceStop already, then that means
		 * the server is in the process of shutting down. We can terminate the 
		 * stop launch (the process executing shutdown.jar) but we must let 
		 * the start processes (the server) to terminate on its own.
		 * We can, however, tell the model to delete those processes upon completion.
		 */
		processModel.clear(ServerProcessModel.STOP_PROCESSES);
		processModel.removeProcessOnTerminate(processModel.getProcesses(ServerProcessModel.START_PROCESSES));

	}


	
	
	/*
	 * Note:  creating a launch in the following manner:
	 * 	        ((Server)getServer()).getLaunchConfiguration(true, null);
	 *        will send execution below into setupLaunchConfiguration. 
	 *        
	 *        Creating it as shown here will not:
	 *           
	 *      ILaunchConfigurationType type =  DebugPlugin.getDefault().getLaunchManager().
	 * 		        getLaunchConfigurationType("org.jboss.ide.eclipse.as.core.jbossLaunch");
	 * 		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, name);
	 * 
	 */
	
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		JBossServerRuntime runtime = getJBossServer().getJBossRuntime();
		AbstractServerRuntimeDelegate runtimeDelegate = runtime.getVersionDelegate();
		JBossRuntimeConfiguration configuration = getJBossServer().getRuntimeConfiguration();
		
		String action = workingCopy.getAttribute(ATTR_ACTION, ACTION_STARTING);
		if( action.equals(ACTION_STARTING)) {
			try {
				List classpath = runtimeDelegate.getRuntimeClasspath(getJBossServer(), IJBossServerRuntimeDelegate.ACTION_START);
				workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, 
						getJBossServer().getRuntimeConfiguration().getServerHome());
				workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, configuration.getStartArgs());
				workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, configuration.getVMArgs());
				workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, configuration.getStartMainType());
				workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
				
		        workingCopy.setAttribute(
		                IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
		                configuration.getServerHome() + Path.SEPARATOR + "bin");

				
				
				workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} else {
			// do nothing
		}
	}
	
	/**
	 * Outline that the server is starting.
	 * Launch a continuous batch of twiddles to determine 
	 * when the server is up.
	 */
	public void serverStarting() {
		setServerStarting();
		
		ServerStateChecker t = new ServerStateChecker(this, ServerStateChecker.UP);
		t.start();
	}
	
	/**
	 * Outline that the server is stopping.
	 * Launch a continuous batch of twiddles to determine 
	 * when the server is down.
	 */
	public void serverStopping() {
		setServerStopping();

		ServerStateChecker t = new ServerStateChecker(this, ServerStateChecker.DOWN);
		t.start();
	}
	
	
	/*
	 * Change the state of the server
	 */
	private void setServerStarted() {
		setServerState(IServer.STATE_STARTED);
	}
	
	private void setServerStarting() {
		setServerState(IServer.STATE_STARTING);
	}
	
	private void setServerStopped() {
		setServerState(IServer.STATE_STOPPED);
	}
	
	private void setServerStopping() {
		setServerState(IServer.STATE_STOPPING);
	}

	/**
	 * This is where the ServerStateChecker responds. 
	 * @param serverUp whether the server is up or not. 
	 */
	public void setServerState(boolean waitingFor, boolean serverUp) {
		if( !serverUp ) {
			/* Fail safe... if the server times out but it IS starting 
			 * but not quick enough, clear / destroy all generated processes.
			 * 
			 * Otherwise, there are start processes but the gui has 'stop' greyed
			 * out and unavailable for selection. 
			 * 
			 * Included inside if statement so it doesn't prematurely
			 * shut down the server while it's in the process of shutting down.
			 */
			if( waitingFor == ServerStateChecker.UP ) {
				getJBossServer().getProcessModel().clearAll();
			}
			setServerStopped();
		} else {
			setServerStarted();
		}
	}

	private JBossServer getJBossServer() {
		if( jbServer == null ) {
			jbServer = (JBossServer)getServer().loadAdapter(JBossServer.class, null);
		}
		return jbServer;
	}
	
	

	/**
	 * Here I go with the default implementatiion for MOST of the situations, 
	 * but I first check my ModuleModel to see if I have any pressing / overriding
	 * designations. 
	 */
	public IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
		// if my model has any reference to them, use that.
		ModuleModel model = ModuleModel.getDefault();
		IModuleResourceDelta[] deltas = model.getDeltaModel().getRecentDeltas(module, getServer());
		if( deltas.length != 0 )
			return deltas;

		return ((Server)getServer()).getPublishedResourceDelta(module);
	}
	
	
	protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
		if( module.length == 0 ) return;
			
		IJbossServerPublisher publisher;
		
		/**
		 * If our modules are already packaged as ejb jars, wars, aop files, 
		 * then go ahead and publish
		 */
		if( ModuleModel.arePackagedModules(module)) {
			publisher = new PackagedPublisher(getJBossServer(), this);
		} else {
			publisher = new JstPublisher(getJBossServer());
		}
		
		
		publisher.publishModule(kind, deltaKind, module, monitor);
		setModulePublishState(module, publisher.getPublishState());
		log.addChildren(publisher.getLogEvents());
	}
	
	

	
	
	/**
	 * Logging information for log listeners
	 */
	protected void publishStart(IProgressMonitor monitor) throws CoreException {
		ServerProcessModelEntity e = ServerProcessModel.getDefault().getModel(getServer().getId());
		log = e.getEventLog().newMajorEvent("Publish Event", ProcessLogEvent.SERVER_PUBLISH);
	}

	/**
	 * Logging information for log listeners
	 */
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		if( log.getChildren().length > 0 ) {
			log.getRoot().branchChanged();
		} else {
			log.getParent().deleteChild(log);
		}
		
		log = null;
	}

	public void restart(String launchMode) throws CoreException {
		ASDebug.p("restart", this);
		 throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, "Could not restart", null));
	}

	
	
	public boolean canControlModule(IModule[] module) {
		ASDebug.p("canControlModule", this);
		return false;
	}

	public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
	}

	public void stopModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
	}

	public int getJndiPort() {
		return getJBossServer().getDescriptorModel().getJNDIPort();
	}

}
