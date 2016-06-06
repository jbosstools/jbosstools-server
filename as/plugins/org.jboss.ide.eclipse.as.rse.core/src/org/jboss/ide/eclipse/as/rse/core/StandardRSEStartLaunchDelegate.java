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

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.launch.CommandLineLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;

/**
 * This is a launch configuration delegate for use with rse servers. 
 * It will launch the remote commands, update server state, and store
 * the PID for the remote process
 * 
 * We have API for initiating polling, but since this class is used for minimal
 * servers (such as deploy-only) polling is not guaranteed to be present, so 
 * subclasses should override the polling methods 
 */
public class StandardRSEStartLaunchDelegate extends
	AbstractJavaLaunchConfigurationDelegate {
	protected static final String DELIMETER = ":";
	protected static final String ECHO_KEY_DISCOVER_PID = "JBTOOLS_SERVER_START_CMD";
	protected static final String ECHO_KEY_PID_TERMD = "JBTOOLS_SERVER_LAUNCH_TERMINATED_CMD";


	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if( LaunchCommandPreferences.isIgnoreLaunchCommand(configuration)) {
			return;
		}
		beforeVMRunner(configuration, mode, launch, monitor);
		actualLaunch(configuration, mode, launch, monitor);
		afterVMRunner(configuration, mode, launch, monitor);
	}
	
	
	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		final IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		boolean dontLaunch = LaunchCommandPreferences.isIgnoreLaunchCommand(configuration);
		((ControllableServerBehavior)beh).setRunMode(mode);
		if (isStarted(server)) {
			return setServerAlreadyStarted(configuration, mode, beh);
		} else if( dontLaunch ) {
			return externallyManagedPollForStarted(server, (ControllableServerBehavior)beh, mode);
		}
		
		String currentHost = server.getAttribute(RSEUtils.RSE_SERVER_HOST, (String)null);
		if( currentHost == null || RSEFrameworkUtils.findHost(currentHost) == null ) {
			throw new CoreException(new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, 
					"Host \"" + currentHost + "\" not found. Host may have been deleted or RSE model may not be completely loaded"));
		}
		return true;
	}
	
	protected boolean externallyManagedPollForStarted(IServer server, ControllableServerBehavior beh, String mode) {
		((ControllableServerBehavior)beh).setServerStarting();
		pollServer(server,  IServerStatePoller.SERVER_UP);
		return false;
	}
	
	protected boolean setServerAlreadyStarted(ILaunchConfiguration configuration, String mode, 
			IControllableServerBehavior beh) throws CoreException {
		((ControllableServerBehavior)beh).setServerStarted();
		return false;
	}
	
	protected void beforeVMRunner(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		if( beh != null ) {
			((ControllableServerBehavior)beh).setServerStarting();
			((ControllableServerBehavior)beh).setRunMode(mode);
		}
	}
	

	protected void actualLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Pull the already-generated command from the launch config and run it
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		IServer server = ServerUtil.getServer(configuration);
		String command = new CommandLineLaunchConfigProperties().getStartupCommand(configuration);
		if( command.trim().length() == 0 ) {
			if( beh != null ) {
				((ControllableServerBehavior)beh).setServerStopped();
			}
			throw new CoreException(new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Unable to start server: command to run is empty", null));
		}
		executeRemoteCommand(command, server);
	}
	
	protected void afterVMRunner(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		pollServer(ServerUtil.getServer(configuration),  IServerStatePoller.SERVER_UP);
	}
	
	/*
	 * Below is code for kicking off a poll thread
	 */
	protected boolean isStarted(IServer server) {
		return false; // We have no way to determine this
	}
	
	protected void pollServer(IServer server, final boolean expectedState) {
		// We don't have pollers here, so we just set server to started
		// Subclasses can override
		final IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(server);
		if( beh != null ) {
			((ControllableServerBehavior)beh).setServerStarted();
		}
	}

	
	/*
	 * The following is for executing commands on the remote system
	 */
	protected void executeRemoteCommand(String command, IServer server)
			throws CoreException {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(server);
		try {
			ServerShellModel model = RSEHostShellModel.getInstance().getModel(server);
			IHostShell shell = model.createStartupShell("/", command, new String[] {}, new NullProgressMonitor());
			addShellOutputListener(shell, beh);
			String getPidCommand = "echo \"" + ECHO_KEY_DISCOVER_PID + DELIMETER + server.getId() + DELIMETER + "\"$!";
			shell.writeToShell(getPidCommand);
		} catch (SystemMessageException sme) {
			// could not connect to remote system
			((ControllableServerBehavior)beh).setServerStopped(); 
			throw new CoreException(new Status(IStatus.ERROR,
					org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID,
					MessageFormat.format("Could not execute command on remote server {0}. Please ensure the server is reachable.", server.getName()), sme));
		}
	}
	
	private void addShellOutputListener(final IHostShell shell, 
			final IControllableServerBehavior behavior) {
		if( shell == null ) 
			return; // No listener needed for a null shell. 
		IHostShellOutputListener listener = null;
		listener = new IHostShellOutputListener() {
			public void shellOutputChanged(IHostShellChangeEvent event) {
				IHostOutput[] out = event.getLines();
				String s = null;
				for (int i = 0; i < out.length; i++) {
					s = out[i].toString();
					if( s.contains(ECHO_KEY_DISCOVER_PID)) {
						// pid found. Let's save it,
						int lastColon = s.lastIndexOf(DELIMETER);
						String pid = s.substring(lastColon+1);
						// make sure it's an integer
						if(pid.trim().matches("-?\\d+")) {
							behavior.putSharedData(IDeployableServerBehaviorProperties.PROCESS_ID, pid);
							
							// Let's send another command to wait for the pid to terminate
							// Then we can ensure that the server is marked as 'stopped' if the server
							// terminates on its own somehow. 
							IServer ser = behavior.getServer();
							ServerShellModel model = RSEHostShellModel.getInstance().getModel(ser);
							IHostShell startupShell = model.getStartupShell();
							String waitTerminated = "wait " + pid + "; echo \"" + ECHO_KEY_PID_TERMD + DELIMETER + ser.getId() + DELIMETER + "\" $?";
							startupShell.writeToShell(waitTerminated);
						}
					}
					if( s.startsWith(ECHO_KEY_PID_TERMD)) {
						((ControllableServerBehavior)behavior).setServerStopped();
					}
				}
			}
		};
		shell.addOutputListener(listener);
	}
}
