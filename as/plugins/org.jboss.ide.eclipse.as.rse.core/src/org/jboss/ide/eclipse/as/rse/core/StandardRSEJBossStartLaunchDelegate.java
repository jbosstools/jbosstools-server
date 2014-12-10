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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;

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
		String serverHome = RSEUtils.getRSEHomeDir(beh.getServer());
		IPath remoteHome = new RemotePath(serverHome, RSEUtils.getRemoteSystemSeparatorCharacter(beh.getServer()));
		IFilesystemController fs = (IFilesystemController)beh.getController(IFilesystemController.SYSTEM_ID);
		if( !fs.exists(remoteHome, monitor)) {
			throw new CoreException(new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "The remote server's home directory does not exist: " + serverHome));
		}
		super.beforeVMRunner(configuration, mode, launch, monitor);
	}
	
	@Override
	protected void actualLaunch(ILaunchConfiguration configuration,
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
	
	@Override
	protected void afterVMRunner(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Initiate Polling!
		pollServer(ServerUtil.getServer(configuration), IServerStatePoller.SERVER_UP);
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
}
