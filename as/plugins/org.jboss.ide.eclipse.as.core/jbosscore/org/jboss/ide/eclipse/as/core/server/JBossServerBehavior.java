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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.model.EventLogModel;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.runtime.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.PollThread;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.TwiddlePoller;
import org.jboss.ide.eclipse.as.core.server.stripped.DeployableServerBehavior;

public class JBossServerBehavior extends DeployableServerBehavior {
	private PollThread pollThread = null;
	protected transient IProcess process;
	public JBossServerBehavior() {
		super();
	}

	public void stop(boolean force) {
		int state = getServer().getServerState();
		if( force || process.isTerminated() || state == IServer.STATE_STOPPED) {
			forceStop();
			return;
		}
		
		// if we're starting up or shutting down and they've tried again, 
		// then force it to stop. 
		if( state == IServer.STATE_STARTING || state == IServer.STATE_STOPPING ) {
			pollThread.cancel();
			forceStop();
			return;
		}
		
		Thread t = new Thread() {
			public void run() {
				// Otherwise execute a shutdown attempt
				try {
					// Set up our launch configuration for a STOP call (to shutdown.jar)
					ILaunchConfiguration wc = JBossServerLaunchConfiguration.setupLaunchConfiguration(getServer(), JBossServerLaunchConfiguration.STOP);
					wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
				} catch( Exception e ) {
				}
			}
		};
		t.start();
	}
	
	protected void forceStop() {
		forceStop(true);
	}
	protected void forceStop(boolean addEvent) {
		// just terminate the process.
		try {
			if( process != null ) 
				process.terminate();
			process = null;
			setServerStopped();
			if( addEvent ) {
				EventLogTreeItem tpe = new ForceShutdownEvent();
				EventLogModel.markChanged(tpe.getEventRoot());
			}
		} catch( Throwable t ) {
			t.printStackTrace();
		}
	}

	public static final String FORCE_SHUTDOWN_EVENT_KEY = "org.jboss.ide.eclipse.as.core.server.JBossServerBehavior.forceShutdown";
	public class ForceShutdownEvent extends EventLogTreeItem {
		public ForceShutdownEvent() {
			super(EventLogModel.getModel(getServer()).getRoot(), PollThread.SERVER_STATE_MAJOR_TYPE, FORCE_SHUTDOWN_EVENT_KEY);
		}
	}
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		JBossServerLaunchConfiguration.setupLaunchConfiguration(workingCopy, getServer(), JBossServerLaunchConfiguration.START);
	}

	
	protected transient IDebugEventSetListener processListener;
	public void setProcess(final IProcess newProcess) {
		if (process != null)
			return;

		process = newProcess;
		if (processListener != null)
			DebugPlugin.getDefault().removeDebugEventListener(processListener);
		if (newProcess == null)
			return;
		
		processListener = new IDebugEventSetListener() {
			public void handleDebugEvents(DebugEvent[] events) {
				if (events != null) {
					int size = events.length;
					for (int i = 0; i < size; i++) {
						if (process != null && process.equals(events[i].getSource()) && events[i].getKind() == DebugEvent.TERMINATE) {
							DebugPlugin.getDefault().removeDebugEventListener(this);
							forceStop(false);
						}
					}
				}
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(processListener);
	}
	
	
	
	public void serverStarting() {
		setServerStarting();
		pollServer(IServerStatePoller.SERVER_UP);
	}
	
	public void serverStopping() {
		setServerStopping();
		pollServer(IServerStatePoller.SERVER_DOWN);
	}
	
	

	protected void pollServer(final boolean expectedState) {
		if( this.pollThread != null ) {
			pollThread.cancel();
		}
		this.pollThread = new PollThread("Server Poller", new TwiddlePoller(), expectedState, this);
		pollThread.start();
	}
	
}
