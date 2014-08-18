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
package org.jboss.ide.eclipse.as.wtp.core.server.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IUserPrompter;
import org.jboss.ide.eclipse.as.core.server.UserPrompter;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.Messages;
import org.jboss.ide.eclipse.as.wtp.core.Trace;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;

/**
 * This is a launch configuration delegate for use with local jboss servers. 
 * It will launch the configuration, update server state, and 
 * register a process termination listener. 
 * 
 * We also kick off the polling mechanism from here as part of the launch
 */
public abstract class AbstractStartJavaServerLaunchDelegate extends AbstractJavaServerLaunchDelegate {
	
	/**
	 * A key for shared data, the value should be an {@link IProcess}
	 */
	public static final String PROCESS = "AbstractStartJavaServerLaunchDelegate.Process"; //$NON-NLS-1$
	/**
	 * A key for shared data, the value should be a PollThread
	 */
	public static final String DEBUG_LISTENER = "AbstractStartJavaServerLaunchDelegate.DebugListener"; //$NON-NLS-1$\
	
	/**
	 * A key for shared data, the value should be a Boolean
	 */
	public static final String NEXT_STOP_REQUIRES_FORCE = "DeployableServerBehavior.RequiresForceStop"; //$NON-NLS-1$\

	
	private static final String PROCESS_TERMINATION_DELAY_PREF_KEY = "org.jboss.ide.eclipse.as.wtp.core.server.launch.PROCESS_TERMINATION_DELAY_PREF_KEY"; //$NON-NLS-1$
	
	/**
	 * Get the fail-safe delay for ensuring process termination after a server shutdown
	 * @return
	 */
	protected int getProcessTerminationDelay() {
		return Platform.getPreferencesService().getInt(ASWTPToolsPlugin.PLUGIN_ID, 
				PROCESS_TERMINATION_DELAY_PREF_KEY, 10000, null);
	}


	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		if( LaunchCommandPreferences.isIgnoreLaunchCommand(server)) {
			return;
		}
		beforeVMRunner(configuration, mode, launch, monitor);
		actualLaunch(configuration, mode, launch, monitor);
		afterVMRunner(configuration, mode, launch, monitor);
	}

	protected void actualLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		fireVMRunner(configuration, mode, launch, monitor);
	}
	
	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		IStatus s = server.canStart(mode);
		Trace.trace(Trace.STRING_FINEST, "Ensuring Server can start: " + s.getMessage()); //$NON-NLS-1$
		if (!s.isOK())
			throw new CoreException(s);
		
		IControllableServerBehavior jbsBehavior = JBossServerBehaviorUtils.getControllableBehavior(server);
		if (LaunchCommandPreferences.isIgnoreLaunchCommand(server)) {
			Trace.trace(Trace.STRING_FINEST, "Server is marked as ignore Launch. Marking as started."); //$NON-NLS-1$
			((ControllableServerBehavior)jbsBehavior).setServerStarted();
			return false;
		}
		
		validateServerStructure(server);
		
		Trace.trace(Trace.STRING_FINEST, "Checking if similar server is already up on the same ports."); //$NON-NLS-1$
		IStatus startedStatus = isServerStarted(server);
		boolean started = startedStatus.isOK();
		if (started) {
			Trace.trace(Trace.STRING_FINEST, "A server is already started. Now handling the already started scenario."); //$NON-NLS-1$
			return handleAlreadyStartedScenario(server, startedStatus);
		}

		Trace.trace(Trace.STRING_FINEST, "A full launch will now proceed."); //$NON-NLS-1$
		return true;
	}

	
	protected boolean handleAlreadyStartedScenario(	IServer server, IStatus startedStatus) {
		Object ret = getPrompter().promptUser(UserPrompter.EVENT_CODE_SERVER_ALREADY_STARTED, server, startedStatus);
		if( ret instanceof Integer ) {
			int handlerResult = ((Integer)ret).intValue();
			if( handlerResult == UserPrompter.RETURN_CODE_SAS_CONTINUE_STARTUP) {
				return true;
			}
			if( handlerResult == UserPrompter.RETURN_CODE_SAS_CANCEL) {
				return false;
			}
		}
		Trace.trace(Trace.STRING_FINEST, "Either no handler available, or user selected continue. The server will be set to started automatically. "); //$NON-NLS-1$
		// force server to started mode
		IControllableServerBehavior jbsBehavior = JBossServerBehaviorUtils.getControllableBehavior(server);
		((ControllableServerBehavior)jbsBehavior).setServerStarted();
		return false;
	}
	
	public String[] getJavaLibraryPath(ILaunchConfiguration configuration) throws CoreException {
		return new String[] {};
	}
	
	protected void beforeVMRunner(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		if( beh != null ) {
			((ControllableServerBehavior)beh).setRunMode(mode);
			((ControllableServerBehavior)beh).setServerStarting();
		}
	}
	
	protected void afterVMRunner(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProcess[] processes = launch.getProcesses();
		final IServer server = ServerUtil.getServer(configuration);
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		if( processes != null && processes.length >= 1 && processes[0] != null ) {
			ProcessTerminatedDebugListener debug = new ProcessTerminatedDebugListener(ServerUtil.getServer(configuration), processes[0]) { 
				protected void handleEarlyTermination() {
					cancelPolling(server);
					IStatus status = new Status(IStatus.INFO,
							ASWTPToolsPlugin.PLUGIN_ID, IEventCodes.BEHAVIOR_PROCESS_TERMINATED, 
							Messages.TERMINATED, null);
					logStatus(server, status);
				}
			};
			if( beh != null ) {
				final IProcess launched = processes[0];
				beh.putSharedData(PROCESS, launched);
				beh.putSharedData(DEBUG_LISTENER, debug);
				final IServer ser = beh.getServer();
				
				
				// During some niche cases, a server may be set to stopped, 
				// but the process may not be shutting down. For example if two 
				// conflicting servers were launched using the same ports.
				// This is a cleanup thread that waits a significant amount of time 
				// that should be suitable for all genuine shutdowns, and then
				// force terminates the process. 
				IServerListener list = new IServerListener() {
					public void serverChanged(ServerEvent event) {
						final IServerListener list2 = this;
						int eventKind = event.getKind();
						if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
							// server change event
							if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
								if( ser.getServerState() == IServer.STATE_STOPPED ) {
									new Thread() {
										public void run() {
											// wait 7 seconds
											try {
												Thread.sleep(getProcessTerminationDelay());
											} catch(InterruptedException ie) {
												// do nothing
											}
											if( !launched.isTerminated()) {
												// Alert user
												Object result = getPrompter().promptUser(
														UserPrompter.EVENT_CODE_PROCESS_UNTERMINATED, ser);
												if( result == null || (result instanceof Boolean && ((Boolean)result).booleanValue())) {
													// force terminate this process
													try {
														launched.terminate();
													} catch(DebugException de) {
														ASWTPToolsPlugin.log(de);
													}
												}
											}
											ser.removeServerListener(list2);
										}
									}.start();
								}
							}
						}
					}
				};
				ser.addServerListener(list);
			}
			DebugPlugin.getDefault().addDebugEventListener(debug);
		} 
		initiatePolling(server);
	}
	
	
	protected IUserPrompter getPrompter() {
		return UserPrompter.getDefaultPrompter();
	}
	
	protected abstract void initiatePolling(IServer server);
	
	protected abstract void cancelPolling(IServer server);

	protected abstract void logStatus(IServer server, IStatus stat);
	
	/*
	 * A solution needs to be found here. 
	 * Should ideally use the poller that the server says is its poller,
	 * but some pollers such as timeout poller 
	 */
	protected abstract IStatus isServerStarted(IServer server);

	
	protected abstract void validateServerStructure(IServer server) throws CoreException;
}
