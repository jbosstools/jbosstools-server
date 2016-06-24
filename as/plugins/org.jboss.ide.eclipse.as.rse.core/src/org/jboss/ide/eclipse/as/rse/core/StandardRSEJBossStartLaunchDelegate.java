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
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.ServerHomeValidationUtility;
import org.jboss.ide.eclipse.as.wtp.core.debug.AttachDebuggerServerListener;
import org.jboss.ide.eclipse.as.wtp.core.debug.RemoteDebugUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;

/**
 * This is a launch configuration delegate for use with rse jboss servers. 
 * It will launch the remote commands, update server state, and store
 * the PID for the remote process
 * 
 * We also kick off the polling mechanism from here as part of the launch
 */
public class StandardRSEJBossStartLaunchDelegate extends
	StandardRSEStartLaunchDelegate {
	protected static final String DELIMETER = ":";
	protected static final String ECHO_KEY_DISCOVER_PID = "JBTOOLS_SERVER_START_CMD";
	
	@Override
	protected void beforeVMRunner(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Verify the remote server home exists
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		new ServerHomeValidationUtility().validateServerHome(beh.getServer(), true);
		super.beforeVMRunner(configuration, mode, launch, monitor);
	}
	
	@Override
	protected void actualLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		RSELaunchConfigProperties propertyUtil = new RSELaunchConfigProperties();
		boolean detectStartupCommand = propertyUtil.isDetectStartupCommand(configuration, true);
		IServer server = ServerUtil.getServer(configuration);
		boolean attachDebugger = server.getAttribute(RemoteDebugUtils.ATTACH_DEBUGGER, true);

		if("debug".equals(mode) && detectStartupCommand && attachDebugger) { 
			// Only manipulate the args and attach debugger if they're 
			// using automatic detection of cmd AND have selected to attach the debugger
			launchDebug(configuration, mode, launch, monitor);
		} else {
			launchRun(configuration, mode, launch, monitor);
		}
	}
	
	protected void launchRun(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Pull the already-generated command from the launch config and run it
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		IServer server = ServerUtil.getServer(configuration);
		String command = new RSELaunchConfigProperties().getStartupCommand(configuration);
		if( command.trim().length() == 0 ) {
			if( beh != null ) {
				((ControllableServerBehavior)beh).setServerStopped();
			}
			throw new CoreException(new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Unable to start server: command to run is empty", null));
		}
		addDummyProcess(server, launch, command, "Launching " + server.getName());
		executeRemoteCommand(command, server);
	}

	protected void launchDebug(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Pull the already-generated command from the launch config and run it
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		IServer server = ServerUtil.getServer(configuration);
		String command = new RSELaunchConfigProperties().getDebugStartupCommand(configuration);
		if( command.trim().length() == 0 ) {
			if( beh != null ) {
				((ControllableServerBehavior)beh).setServerStopped();
			}
			throw new CoreException(new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Unable to start server: command to run is empty", null));
		}
		addDummyProcess(server, launch, command, "Debugging " + server.getName());
		executeRemoteCommand(command, server);
	}
	
	@Override
	protected void externallyManagedPollForStarted(IServer server, IControllableServerBehavior beh, String mode, ILaunch launch) {
		((ControllableServerBehavior)beh).setServerStarting();
		attachDebugListenerAndLaunchPoller(server, mode, null);
	}
	
	@Override
	protected void afterVMRunner(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IServer s = ServerUtil.getServer(configuration);
		attachDebugListenerAndLaunchPoller(s, mode, launch);
	}
	
	private void attachDebugListenerAndLaunchPoller(IServer s, String mode, ILaunch launch) {
		boolean attachDebugger = s.getAttribute(RemoteDebugUtils.ATTACH_DEBUGGER, true);
		if( "debug".equals(mode) && attachDebugger) {
			// add a listener which will run the debugger once server is started
			AttachDebuggerServerListener listener = createAttachDebuggerListener(launch);
			s.addServerListener(listener);
		}
		pollServer(s, IServerStatePoller.SERVER_UP);
	}
	
	private AttachDebuggerServerListener createAttachDebuggerListener(final ILaunch rseLaunch) {
		boolean register = (rseLaunch == null);
		return new AttachDebuggerServerListener(register) {
			protected void serverStarted(ServerEvent event) {
				super.serverStarted(event);
				ILaunch debuggerLaunch = getLaunch();
				if( debuggerLaunch != null && !shouldRegisterDebuggerLaunch()) {
					// If we're not registering the debugger launch as its own standalone entity, 
					// We're gonna register all debug targets and processes
					// from the debugger launch, with our rse launch instead. 
					
					IProcess[] processes = debuggerLaunch.getProcesses();
					for( int i = 0; i < processes.length; i++ ) {
						rseLaunch.addProcess(processes[i]);
					}
					
					IDebugTarget[] delegateTargets = debuggerLaunch.getDebugTargets();
					for( int i = 0; i < delegateTargets.length; i++ ) {
						rseLaunch.addDebugTarget(delegateTargets[i]);
					}
				}
			}
		};
	}
	
	
	/*
	 * Synchronous check to see if server is currently up. 
	 */
	@Override
	protected boolean isStarted(IServer server) {
		return PollThreadUtils.isServerStarted(server).isOK();
	}
	
	/*
	 * Kick off a poll thread
	 */
	@Override
	protected void pollServer(IServer server, final boolean expectedState) {
		PollThreadUtils.pollServer(server, expectedState);
	}
	
	protected void setServerAlreadyStarted(ILaunchConfiguration configuration, String mode, 
			IControllableServerBehavior beh, IServer server, ILaunch launch, String command) throws CoreException {
		
		addDummyProcess(server, launch, command, "Launching " + server.getName());
		boolean attachDebugger = server.getAttribute(RemoteDebugUtils.ATTACH_DEBUGGER, true);
		if( "debug".equals(mode) && attachDebugger) {
			// add a listener which will run the debugger once server is started
			AttachDebuggerServerListener listener = createAttachDebuggerListener(launch);
			server.addServerListener(listener);
		}

		((ControllableServerBehavior)beh).setServerStarted();
	}
}
