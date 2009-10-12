/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
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
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JBossServerConnectionProvider;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXClassLoaderRepository;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.launch.JBossServerStartupLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.internal.launch.StopLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

/**
 * 
 * @author Rob Stryker
 *
 */
public class JBossServerBehavior extends DeployableServerBehavior {
	
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
					pollThread.cancel(Messages.STOP_FAILED_MESSAGE);
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
				JBossServerCorePlugin.PLUGIN_ID, IEventCodes.BEHAVIOR_FORCE_STOP_FAILED, 
				Messages.FORCE_TERMINATE_FAILED, e);
		ServerLogger.getDefault().log(getServer(), status);
	}
	protected void addForceStopEvent() {
		IStatus status = new Status(IStatus.ERROR,
				JBossServerCorePlugin.PLUGIN_ID, IEventCodes.BEHAVIOR_FORCE_STOP, 
				Messages.FORCE_TERMINATED, null);
		ServerLogger.getDefault().log(getServer(), status);
	}
	
	protected void addProcessTerminatedEvent() {
		IStatus status = new Status(IStatus.INFO,
				JBossServerCorePlugin.PLUGIN_ID, IEventCodes.BEHAVIOR_PROCESS_TERMINATED, 
				Messages.TERMINATED, null);
		ServerLogger.getDefault().log(getServer(), status);
	}
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		JBossServerStartupLaunchConfiguration.setupLaunchConfiguration(workingCopy, getServer());
	}

	
	protected transient IDebugEventSetListener processListener;
	public void setProcess(final IProcess newProcess) {
		if (process != null) { 
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
		this.pollThread = new PollThread(Messages.ServerPollerThreadName, expectedState, this);
		pollThread.start();
	}
	

	protected void publishStart(final IProgressMonitor monitor) throws CoreException {
		super.publishStart(monitor);
		if( shouldSuspendScanner()) {
			JMXClassLoaderRepository.getDefault().addConcerned(getServer(), this);
			IJMXRunnable r = new IJMXRunnable() {
				public void run(MBeanServerConnection connection) throws Exception {
					suspendDeployment(connection, monitor);
				}
			};
			try {
				JBossServerConnectionProvider.run(getServer(), r);
			} catch( JMXException jmxe ) {
				IStatus status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.SUSPEND_DEPLOYMENT_SCANNER, Messages.JMXPauseScannerError, jmxe);
				ServerLogger.getDefault().log(getServer(), status);
			}
		}
	}
	
	protected void publishFinish(final IProgressMonitor monitor) throws CoreException {
		if( shouldSuspendScanner()) {
			IJMXRunnable r = new IJMXRunnable() {
				public void run(MBeanServerConnection connection) throws Exception {
					resumeDeployment(connection, monitor);
				}
			};
			try {
				JBossServerConnectionProvider.run(getServer(), r);
			} catch( JMXException jmxe ) {
				IStatus status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.RESUME_DEPLOYMENT_SCANNER, Messages.JMXResumeScannerError, jmxe);
				ServerLogger.getDefault().log(getServer(), status);
			} finally {
				JMXClassLoaderRepository.getDefault().removeConcerned(getServer(), this);
			}
		}
		super.publishFinish(monitor);
	}

	protected boolean shouldSuspendScanner() {
//		if( getServer().getServerType().getId().equals(IConstants.AS_50))
//			return false;
		if( getServer().getServerState() != IServer.STATE_STARTED)
			return false;
		return true;
	}
	
	protected void suspendDeployment(final MBeanServerConnection connection, IProgressMonitor monitor) throws Exception {
		ObjectName name = new ObjectName(IJBossRuntimeConstants.DEPLOYMENT_SCANNER_MBEAN_NAME);
		launchDeployCommand(connection, name, IJBossRuntimeConstants.STOP, monitor);
	}
	

	
	protected void resumeDeployment(final MBeanServerConnection connection, IProgressMonitor monitor) throws Exception {
		ObjectName name = new ObjectName(IJBossRuntimeConstants.DEPLOYMENT_SCANNER_MBEAN_NAME);
		launchDeployCommand(connection, name, IJBossRuntimeConstants.START, monitor);
	}
	
	protected void launchDeployCommand(final MBeanServerConnection connection, final ObjectName objectName, 
			final String methodName, IProgressMonitor monitor) throws Exception {
		final Exception[] e = new Exception[1];
		Thread t = new Thread() {
			public void run() {
				try {
					executeDeploymentCommand(connection, objectName, methodName);
				} catch( Exception ex ) {
					e[0] = ex;
				}
			}
		};
		t.start();
		int count = 0;
		while(t.isAlive() && !monitor.isCanceled() && count <= 4000) {
			count+= 100;
			Thread.sleep(100);
		}
		if( t.isAlive()) {
			t.interrupt();
			IStatus status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.DEPLOYMENT_SCANNER_TRANSITION_CANCELED, Messages.JMXScannerCanceled, null);
			ServerLogger.getDefault().log(getServer(), status);
		}
	}
		
	protected void executeDeploymentCommand(MBeanServerConnection connection, ObjectName objectName, String methodName) throws Exception {
		connection.invoke(objectName, methodName, new Object[] {  }, new String[] {});
	}

}
