/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.LocalStopLaunchConfigurator;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.ThreadUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerShutdownController;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;

public class LocalLegacyShutdownController extends AbstractSubsystemController
		implements IServerShutdownController {

	// The launch configuration ID for shutdown
	private static final String STOP_LAUNCH_TYPE = "org.jboss.ide.eclipse.as.core.server.stopLaunchConfiguration"; //$NON-NLS-1$

	@Override
	public IStatus canStop() {
		return Status.OK_STATUS;
	}

	@Override
	public void stop(boolean force) {
		stop(force, true);
	}
	
	protected void stop(boolean force, boolean removeScanners) {
		if( removeScanners ) {
			removeScanners();
		}
		boolean ignoreLaunch = false;
		try {
			ILaunchConfiguration config = getServer().getLaunchConfiguration(true, new NullProgressMonitor());
			ignoreLaunch = LaunchCommandPreferences.isIgnoreLaunchCommand(config);
		} catch(CoreException ce) {
			JBossServerCorePlugin.log(ce.getStatus());
		}
		
		if(ignoreLaunch) {
			((ControllableServerBehavior)getControllableBehavior()).setServerStopped();
			return;
		}
		
		stopImpl(force);
	}
	

	protected void removeScanners() {
		boolean removeScanners = getServer().getAttribute(IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, true);
		if( removeScanners ) {
			JBossExtendedProperties properties = (JBossExtendedProperties)getServer().loadAdapter(JBossExtendedProperties.class, null);
			if( properties != null ) {
				properties.getDeploymentScannerModifier().removeAddedDeploymentScanners(getServer());
			}		
		}
	}
	
	protected IProcess getProcess() {
		IProcess existing = (IProcess)getControllableBehavior().getSharedData(IDeployableServerBehaviorProperties.PROCESS);
		return existing;
	}
	
	protected boolean isProcessRunning() {
		boolean isProcessRunning = getProcess() != null && !getProcess().isTerminated();
		return isProcessRunning;
	}
	
	protected void clearProcess() {
		getControllableBehavior().putSharedData(IDeployableServerBehaviorProperties.PROCESS, null);
	}
	
	protected PollThread getPollThread() {
		return (PollThread)getControllableBehavior().getSharedData(IDeployableServerBehaviorProperties.POLL_THREAD);
	}
	
	protected void clearPollThread() {
		getControllableBehavior().putSharedData(IDeployableServerBehaviorProperties.POLL_THREAD, null);
	}
	
	protected boolean getRequiresForce() {
		Object o = getControllableBehavior().getSharedData(IDeployableServerBehaviorProperties.NEXT_STOP_REQUIRES_FORCE);
		return o == null ? false : ((Boolean)o).booleanValue();
	}
	
	protected void setNextStopRequiresForce(boolean val) {
		getControllableBehavior().putSharedData(IDeployableServerBehaviorProperties.NEXT_STOP_REQUIRES_FORCE, val);
	}
	
	protected boolean shouldUseForce() {
		int state = getServer().getServerState();
		boolean useForce = !isProcessRunning() || state == IServer.STATE_STOPPED || getRequiresForce(); 
		return useForce;
	}
	
	public void stopImpl(boolean force) {
		int state = getServer().getServerState();
		if (force || shouldUseForce()) {
			forceStop();
		} else if (state == IServer.STATE_STARTING
				|| state == IServer.STATE_STOPPING) {
			// if we're starting up or shutting down and they've tried again,
			// then force it to stop.
			cancelPolling(null);
			forceStop();
		} else {
			((ControllableServerBehavior)getControllableBehavior()).setServerStopping();
			IStatus result = gracefullStop();
			if (!result.isOK()) {
				setNextStopRequiresForce(true);
				((ControllableServerBehavior)getControllableBehavior()).setServerStarted();
			}
		}
	}

	/**
	 * The graceful stop implementation for as < 7 is to use a special launch configuration
	 * which runs the java command to shutdown the server
	 * 
	 * @return
	 */
	protected IStatus gracefullStop() {
		new Thread() {
			
			@Override
			public void run() {
				try {
					ILaunchConfigurationWorkingCopy wc = 
							LaunchConfigUtils.createLaunchConfigurationWorkingCopy("Stop JBoss Server", STOP_LAUNCH_TYPE);  //$NON-NLS-1$
					new LocalStopLaunchConfigurator(getServer()).configure(wc);
					ILaunch launch = wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
					IProcess stopProcess = launch.getProcesses()[0];
					ThreadUtils.sleepWhileRunning(stopProcess);
					if (stopProcess.getExitValue() != 0) {
						// TODO: correct concurrent access to process, pollThread and nextStopRequiresForce
						// Stop process exit value was NOT zero, so the stop process failed
						((ControllableServerBehavior)getControllableBehavior()).setServerStarted();
						cancelPolling(Messages.STOP_FAILED_MESSAGE);
						setNextStopRequiresForce(true);
					}
				} catch( CoreException ce ) {
					JBossServerCorePlugin.getDefault().getLog().log(ce.getStatus());
				}
				
			}
		}.start();
		return Status.OK_STATUS;
	}
	
	protected void forceStop() {
		// Only synchronize on this for fast methods blocking on the process
		// Calls to parent should not be synchronized for fear of deadlock
		synchronized(this) {
			// just terminate the process.
			if( isProcessRunning()) {
				try {
					getProcess().terminate();
					addForceStopEvent();
				} catch( DebugException e ) {
					addForceStopFailedEvent(e);
				}
			}
			clearProcess();
			setNextStopRequiresForce(false);
		}
		((ControllableServerBehavior)getControllableBehavior()).setServerStopped();
	}
	

	protected void cancelPolling(String message) {
		PollThreadUtils.cancelPolling(message, getPollThread());
		clearPollThread();
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
	
}
