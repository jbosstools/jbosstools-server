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

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Module;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerType;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.module.PathModuleFactory;
import org.jboss.ide.eclipse.as.core.publishers.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.publishers.JstPublisher;
import org.jboss.ide.eclipse.as.core.publishers.NullPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PathPublisher;
import org.jboss.ide.eclipse.as.core.runtime.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.runtime.server.IServerPollerTimeoutListener;
import org.jboss.ide.eclipse.as.core.runtime.server.internal.TwiddlePoller;
import org.jboss.ide.eclipse.as.core.server.attributes.IServerPollingAttributes;

public class JBossServerBehavior extends ServerBehaviourDelegate {
	public static final String LAUNCH_CONFIG_DEFAULT_CLASSPATH = "__JBOSS_SERVER_BEHAVIOR_LAUNCH_CONFIG_DEFAULT_CLASSPATH__";
	
	
	private PollThread pollThread = null;
	public JBossServerBehavior() {
		super();
	}

	public void stop(boolean force) {
		if( force ) {
			forceStop();
			return;
		}
		
		// If the server's already terminated via processes, just abort
		IProcess[] startProcesses = 
			ServerProcessModel.getDefault().getModel(getServer().getId()).getProcesses(JBossServerLaunchConfiguration.START);
		if( ServerProcessModel.allProcessesTerminated(startProcesses)) {
			forceStop();
			return;
		}
			
		// if we're starting up or shutting down and they've tried again, 
		// then force it to stop. 
		int state = getServer().getServerState();
		if( state == IServer.STATE_STARTING || state == IServer.STATE_STOPPING || state == IServer.STATE_STOPPED) {
			pollThread.cancel();
			forceStop();
			return;
		}
		
		
		// Otherwise execute a shutdown attempt
		try {
			// Set up our launch configuration for a STOP call (to shutdown.jar)
			ILaunchConfiguration wc = JBossServerLaunchConfiguration.setupLaunchConfiguration(getServer(), JBossServerLaunchConfiguration.STOP);
			wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		} catch( Exception e ) {
		}
	}
	
	protected void forceStop() {
		// just terminate the processes. All of them
		try {
			ServerProcessModel.getDefault().getModel(getServer().getId()).clearAll();
			setServerStopped();
		} catch( Throwable t ) {
			t.printStackTrace();
		}
	}

	
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		JBossServerLaunchConfiguration.setupLaunchConfiguration(workingCopy, getServer(), JBossServerLaunchConfiguration.START);
	}

	
	
	
	
	public void serverStarting() {
		setServerStarting();
		pollServer(IServerStatePoller.SERVER_UP);
	}
	
	public void serverStopping() {
		setServerStopping();
		pollServer(IServerStatePoller.SERVER_DOWN);
	}
	
	
	public class PollThread extends Thread {
		private boolean expectedState;
		private IServerStatePoller poller;
		private boolean abort;
		public PollThread(String name, IServerStatePoller poller, boolean expectedState) {
			super(name);
			this.expectedState = expectedState;
			this.poller = poller;
			this.abort = false;
		}
		
		public void cancel() {
			abort = true;
		}

		
		// Getting the timeouts. First from plugin.xml as default, or from user settings.
		public int getTimeout() {
			int timeout;
			JBossServer jbs = ((JBossServer)getServer().loadAdapter(JBossServer.class, null));
			ServerAttributeHelper helper = (ServerAttributeHelper)jbs.getAttributeHelper();
			if( expectedState == IServerStatePoller.SERVER_UP) {
				int def = ((ServerType)getServer().getServerType()).getStartTimeout();
				timeout = helper.getAttribute(IServerPollingAttributes.START_TIMEOUT, def);
			} else {
				int def = ((ServerType)getServer().getServerType()).getStopTimeout();
				timeout = helper.getAttribute(IServerPollingAttributes.STOP_TIMEOUT, def);
			}
			return timeout;
		}
		
		
		public void run() {
			int maxWait = getTimeout();

			long startTime = new Date().getTime();
			boolean done = false;
			poller.beginPolling(getServer(), expectedState);
			while( !abort && !done && new Date().getTime() < startTime + maxWait ) {
				try {
					Thread.sleep(100);
					done = poller.isComplete();
				} catch( InterruptedException ie ) { }
			}
			boolean currentState = !expectedState;
			if( abort ) {
				poller.cancel(IServerStatePoller.CANCEL);
				poller.cleanup();
			} else {
			
				if( done ) {
					// the poller has an answer
					currentState = poller.getState();
					poller.cleanup();
				} else {
					// we timed out.  get response from preferences
					poller.cancel(IServerStatePoller.TIMEOUT_REACHED);
					currentState = getTimeoutBehavior();
					poller.cleanup();
					fireTimeoutEvent();
				}
				
				if( currentState != expectedState ) {
					// it didnt work... cancel all processes!
					forceStop();
				} else {
					if( currentState == IServerStatePoller.SERVER_UP ) 
						setServerStarted();
					else
						setServerStopped();
				}
			}
		}
		protected boolean getTimeoutBehavior() {
			// timeout has been reached, so let the user's preferences override
			JBossServer jbs = ((JBossServer)getServer().loadAdapter(JBossServer.class, null));
			ServerAttributeHelper helper = (ServerAttributeHelper)jbs.getAttributeHelper();
				
			boolean behavior = helper.getAttribute(IServerPollingAttributes.TIMEOUT_BEHAVIOR, true);
			if( behavior == IServerPollingAttributes.TIMEOUT_ABORT ) 
				return !expectedState;

			return expectedState;
		}
		
		protected void fireTimeoutEvent() {
			IServerPollerTimeoutListener[] listeners = 
				JBossServerCore.getDefault().getTimeoutListeners(poller.getClass().getName());
			for( int i = 0; i < listeners.length; i++ ) {
				listeners[i].serverTimedOut(getServer(), expectedState);
			}
		}
	}

	protected void pollServer(final boolean expectedState) {
		if( this.pollThread != null ) {
			pollThread.cancel();
		}
		this.pollThread = new PollThread("Server Poller", new TwiddlePoller(), expectedState);
//		this.pollThread = new PollThread("Server Poller", new TimeoutPoller(), expectedState);
		pollThread.start();
	}
	
	
	// By default, goes to check if the members are all the same or any changes
	public IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
		return ((Server)getServer()).getPublishedResourceDelta(module);
	}
	
	protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
		// kind = [incremental, full, auto, clean] = [1,2,3,4]
		// delta = [no_change, added, changed, removed] = [0,1,2,3]

		System.out.print("publishing module: ");
		switch( kind ) {
			case 1: System.out.print("incremental, "); break;
			case 2: System.out.print("full, "); break;
			case 3: System.out.print("auto, "); break;
			case 4: System.out.print("clean, "); break;
		}
		switch( deltaKind ) {
			case 0: System.out.print("no change"); break;
			case 1: System.out.print("added"); break;
			case 2: System.out.print("changed"); break;
			case 3: System.out.print("removed"); break;
		}
		System.out.println("");
		
		if( module.length == 0 ) return;

		IJBossServerPublisher publisher;

		/**
		 * If our modules are already packaged as ejb jars, wars, aop files, 
		 * then go ahead and publish
		 */
		if( hasPackagingConfiguration(module) ) {
			// will be changed
			publisher = new NullPublisher();
		} else if( arePathModules(module)) {
			publisher = new PathPublisher(JBossServerCore.getServer(getServer()), this);
		} else if( areJstModules(module)){
			publisher = new JstPublisher(JBossServerCore.getServer(getServer()));
		} else {
			publisher = new NullPublisher();
		}
		
		publisher.publishModule(kind, deltaKind, module, monitor);
		setModulePublishState(module, publisher.getPublishState());
	}
	
	public boolean arePathModules(IModule[] module) {
		if( module.length == 1 && module[0] instanceof Module ) {
			ModuleFactoryDelegate delegate = 
				((Module)module[0]).getModuleFactory().getDelegate(new NullProgressMonitor());
			if( delegate instanceof PathModuleFactory ) return true;
		}
		return false;
	}
	
	/* Temporary and will need to be fixed */
	protected boolean areJstModules(IModule[] module) {
		String type;
		for( int i = 0; i < module.length; i++ ) {
			type = module[i].getModuleType().getId();
			if( type.equals("jst.ejb") || type.equals("jst.client") || type.equals("jst.web") || type.equals("jst.ear")) 
				continue;
			return false;
		}
		return true;
	}
	/* Temporary and will need to be fixed */
	protected boolean hasPackagingConfiguration(IModule[] module) {
		return false;
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

	
}
