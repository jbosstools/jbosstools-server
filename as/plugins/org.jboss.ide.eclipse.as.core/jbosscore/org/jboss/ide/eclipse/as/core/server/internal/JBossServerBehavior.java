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
package org.jboss.ide.eclipse.as.core.server.internal;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JBossServerConnectionProvider;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.launch.JBossServerStartupLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.internal.launch.StopLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.jmx.core.IJMXRunnable;

/**
 * 
 * @author Rob Stryker
 *
 */
public class JBossServerBehavior extends DeployableServerBehavior {
	private static final String STOP_FAILED_MESSAGE = 
		"Command to stop server failed. The next attempt will forcefully terminate the process.";
	private static final String FORCE_TERMINATED = "The server was shutdown forcefully. All processes were terminated.";
	private static final String TERMINATED = "Server processes have been terminated.";
	private static final String FORCE_TERMINATE_FAILED = "Killing the server process has failed. The process may still be running.";
	
	private PollThread pollThread = null;
	protected IProcess process;
	protected boolean nextStopRequiresForce = false;
	public JBossServerBehavior() {
		super();
	}

	public void stop(boolean force) {
		int state = getServer().getServerState();
		if( force || process == null || process.isTerminated() || state == IServer.STATE_STOPPED || nextStopRequiresForce) {
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
		
		serverStopping();
		new Thread() {public void run() {
			boolean success = StopLaunchConfiguration.stop(getServer());
			if( !success ) {
				if( process != null && !process.isTerminated() ) { 
					setServerStarted();
					pollThread.cancel(STOP_FAILED_MESSAGE);
					nextStopRequiresForce = true;
				}
			}
		}}.start();
	}
	
	public void forceStop() {
		// just terminate the process.
		if( process != null && !process.isTerminated()) {
			try {
				process.terminate();
				addForceStopEvent();
			} catch( DebugException e ) {
				addForceStopFailedEvent(e);
			}
		}
		process = null;
		setServerStopped();
	}
	
	protected void addForceStopFailedEvent(DebugException e) {
		IStatus status = new Status(IStatus.ERROR,
				JBossServerCorePlugin.PLUGIN_ID, 0, 
				FORCE_TERMINATE_FAILED, e);
		ServerLogger.getDefault().log(getServer(), status);
	}
	protected void addForceStopEvent() {
		IStatus status = new Status(IStatus.ERROR,
				JBossServerCorePlugin.PLUGIN_ID, 0, 
				FORCE_TERMINATED, null);
		ServerLogger.getDefault().log(getServer(), status);
	}
	
	protected void addProcessTerminatedEvent() {
		IStatus status = new Status(IStatus.INFO,
				JBossServerCorePlugin.PLUGIN_ID, 0, 
				TERMINATED, null);
		ServerLogger.getDefault().log(getServer(), status);
	}
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		JBossServerStartupLaunchConfiguration.setupLaunchConfiguration(workingCopy, getServer());
	}

	
	protected transient IDebugEventSetListener processListener;
	public void setProcess(final IProcess newProcess) {
		if (process != null) { 
			System.out.println(process.isTerminated());
			return;
		}
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
							forceStop();
							addProcessTerminatedEvent();
						}
					}
				}
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(processListener);
	}
	
	
	public void setRunMode(String mode) {
		setMode(mode);
	}
	
	public void serverStarting() {
		nextStopRequiresForce = false;
		setServerStarting();
		pollServer(IServerStatePoller.SERVER_UP);
	}
	
	public void serverStopping() {
		setServerStopping();
		pollServer(IServerStatePoller.SERVER_DOWN);
	}
	
	public IProcess getProcess() {
		return process;
	}
	
	protected void pollServer(final boolean expectedState) {
		if( this.pollThread != null ) {
			pollThread.cancel();
		}
		this.pollThread = new PollThread("Server Poller", expectedState, this);
		pollThread.start();
	}
	

	protected void publishStart(IProgressMonitor monitor) throws CoreException {
		super.publishStart(monitor);
		suspendDeployment();
		ensureDeployLocationAdded();
	}
	
	protected void ensureDeployLocationAdded() {
		final IDeployableServer ds = ServerConverter.getDeployableServer(getServer());
		IJMXRunnable r = new IJMXRunnable() {
			public void run(MBeanServerConnection connection) throws Exception {
				ObjectName name = new ObjectName("jboss.deployment:flavor=URL,type=DeploymentScanner");
				connection.invoke(name, "addURL", new Object[] { "file:" + ds.getDeployFolder() }, new String[] {String.class.getName()});
			}
		};
		try {
			if( getServer().getServerState() == IServer.STATE_STARTED)
				JBossServerConnectionProvider.run(getServer(), r);
		} catch( CoreException ce) {}
	}

	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		resumeDeployment();
		super.publishFinish(monitor);
	}
	
	protected boolean shouldSuspendScanner() {
		if( getServer().getServerType().getId().equals("org.jboss.ide.eclipse.as.50"))
			return false;
		if( getServer().getServerState() != IServer.STATE_STARTED)
			return false;
		return true;
	}
	
	protected void suspendDeployment() {
		IJMXRunnable r = new IJMXRunnable() {
			public void run(MBeanServerConnection connection) throws Exception {
				ObjectName name = new ObjectName("jboss.deployment:flavor=URL,type=DeploymentScanner");
				connection.invoke(name, "stop", new Object[] {  }, new String[] {});
			}
		};
		try {
			if( shouldSuspendScanner() )
				JBossServerConnectionProvider.run(getServer(), r);
		} catch( CoreException ce) {} // ignore
	}
	
	protected void resumeDeployment() {
		IJMXRunnable r = new IJMXRunnable() {
			public void run(MBeanServerConnection connection) throws Exception {
				ObjectName name = new ObjectName("jboss.deployment:flavor=URL,type=DeploymentScanner");
				connection.invoke(name, "start", new Object[] {  }, new String[] {});
			}
		};
		try {
			if( shouldSuspendScanner() )
				JBossServerConnectionProvider.run(getServer(), r);
		} catch( CoreException ce) {} // ignore
	}
		

}
