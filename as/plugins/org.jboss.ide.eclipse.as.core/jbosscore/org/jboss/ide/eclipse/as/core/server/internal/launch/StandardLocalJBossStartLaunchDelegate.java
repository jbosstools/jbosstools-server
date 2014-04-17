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
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.UserPrompter;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;

/**
 * This is a launch configuration delegate for use with local jboss servers. 
 * It will launch the configuration, update server state, and 
 * register a process termination listener. 
 * 
 * We also kick off the polling mechanism from here as part of the launch
 */
public class StandardLocalJBossStartLaunchDelegate extends
		AbstractJBossStartLaunchConfiguration {

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
		IServer server = ServerUtil.getServer(configuration);
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		if( processes != null && processes.length >= 1 && processes[0] != null ) {
			ProcessTerminatedDebugListener debug = new ProcessTerminatedDebugListener(ServerUtil.getServer(configuration), processes[0]);
			if( beh != null ) {
				final IProcess launched = processes[0];
				beh.putSharedData(IDeployableServerBehaviorProperties.PROCESS, launched);
				beh.putSharedData(IDeployableServerBehaviorProperties.DEBUG_LISTENER, debug);
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
												Object result = JBossServerCorePlugin.getDefault().getPrompter().promptUser(
														UserPrompter.EVENT_CODE_PROCESS_UNTERMINATED, ser);
												if( result == null || (result instanceof Boolean && ((Boolean)result).booleanValue())) {
													// force terminate this process
													try {
														launched.terminate();
													} catch(DebugException de) {
														JBossServerCorePlugin.log(de);
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
		
		// Initiate Polling!
		PollThreadUtils.pollServer(server, IServerStatePoller.SERVER_UP);
	}
	
	
	
	private static final String PROCESS_TERMINATION_DELAY_PREF_KEY = "org.jboss.ide.eclipse.as.core.server.launch.PROCESS_TERMINATION_DELAY_PREF_KEY"; //$NON-NLS-1$
	/**
	 * Get the fail-safe delay for ensuring process termination after a server shutdown
	 * @return
	 */
	protected int getProcessTerminationDelay() {
		return Platform.getPreferencesService().getInt(JBossServerCorePlugin.PLUGIN_ID, 
				PROCESS_TERMINATION_DELAY_PREF_KEY, 10000, null);
	}
}
