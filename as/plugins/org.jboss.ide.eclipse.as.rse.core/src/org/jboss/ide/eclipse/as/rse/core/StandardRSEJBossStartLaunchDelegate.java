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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.rse.core.subsystems.IServerLauncher;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.ServerHomeValidationUtility;
import org.jboss.ide.eclipse.as.wtp.core.debug.RemoteDebugUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;

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
		boolean attachDebugger = server.getAttribute(RSEJbossLaunchConstants.ATTACH_DEBUGGER, true);

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
		executeRemoteCommand(command, server);
	}
	
	@Override
	protected void afterVMRunner(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Initiate Polling!
		IServer s = ServerUtil.getServer(configuration);
		if( "debug".equals(mode)) {
			// add a listener which will run the 
			IServerListener listener = createAttachDebuggerListener();
			s.addServerListener(listener);
		}
		pollServer(s, IServerStatePoller.SERVER_UP);
	}
	
	private IServerListener createAttachDebuggerListener() {
		return new IServerListener() {
			boolean serverSwitchesToState(ServerEvent event, int state) {
				int eventKind = event.getKind();
				if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
					// server change event
					if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
						if( event.getServer().getServerState() == state ) {
							return true;
						}
					}
				}
				return false;
			}
			public void serverChanged(ServerEvent event) {
				if( serverSwitchesToState(event, IServer.STATE_STARTED)) {
					event.getServer().removeServerListener(this);
					IServer s = event.getServer();
					int debugPort = getDebugPort(s);
					try {
						attachRemoteDebugger(event.getServer(), debugPort, new NullProgressMonitor());
					} catch(CoreException ce) {
						RSECorePlugin.pluginLog().logError(ce);
					}
				} else if( serverSwitchesToState(event, IServer.STATE_STOPPED)) {
					event.getServer().removeServerListener(this);
				}
			}
		};
	}
	
	protected int getDebugPort(IServer server) {
		String debugPort = server.getAttribute(RSEJbossLaunchConstants.DEBUG_PORT, Integer.toString(RSEJbossLaunchConstants.DEFAULT_DEBUG_PORT));
		int port = -1;
		try {
			port = Integer.parseInt(debugPort);
		} catch(NumberFormatException nfe) {
			port = RSEJbossLaunchConstants.DEFAULT_DEBUG_PORT;
		}
		return port;
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
	
	protected boolean setServerAlreadyStarted(ILaunchConfiguration configuration, String mode, 
			IControllableServerBehavior beh) throws CoreException {
		boolean ret = super.setServerAlreadyStarted(configuration, mode, beh);
		connectDebugger(configuration, mode, beh);
		return ret;
	}
	
	protected void connectDebugger(ILaunchConfiguration configuration, String mode, 
			IControllableServerBehavior beh) throws CoreException {
		attachRemoteDebugger(beh.getServer(), getDebugPort(beh.getServer()), new NullProgressMonitor());
	}
	

	private ILaunch attachRemoteDebugger(IServer server, int localDebugPort, IProgressMonitor monitor) 
			throws CoreException {
		monitor.subTask("Attaching remote debugger");
		ILaunch ret = null;
		RemoteDebugUtils debugUtils = RemoteDebugUtils.get();
		ILaunchConfiguration debuggerLaunchConfig = debugUtils.getRemoteDebuggerLaunchConfiguration(server);
		ILaunchConfigurationWorkingCopy workingCopy;
		if (debuggerLaunchConfig == null) {
			workingCopy = debugUtils.createRemoteDebuggerLaunchConfiguration(server);
		} else {
			if (debugUtils.isRunning(debuggerLaunchConfig, localDebugPort)) {
				return null;
			}
			workingCopy = debuggerLaunchConfig.getWorkingCopy();
		}
				
		debugUtils.setupRemoteDebuggerLaunchConfiguration(workingCopy, null, localDebugPort);
		debuggerLaunchConfig = workingCopy.doSave();
		boolean launched = false;
		try {
			ret = debuggerLaunchConfig.launch("debug", new NullProgressMonitor());
			launched = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (!launched){
			throw toCoreException("Unable to start a remote debugger to localhost:"+localDebugPort);
		}
		
	    monitor.worked(10);
	    return ret;
	}
	private CoreException toCoreException(String msg, Exception e) {
		return new CoreException(StatusFactory.errorStatus(RSECorePlugin.PLUGIN_ID, msg, e));
	}
	
	private CoreException toCoreException(String msg) {
		return toCoreException(msg, null);
	}
}
