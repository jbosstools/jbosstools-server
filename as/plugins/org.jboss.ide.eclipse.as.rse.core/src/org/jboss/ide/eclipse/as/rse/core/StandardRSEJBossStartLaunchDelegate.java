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
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IPollResultListener;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.launch.AbstractJBossStartLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;

/**
 * This is a launch configuration delegate for use with rse jboss servers. 
 * It will launch the remote commands, update server state, and store
 * the PID for the remote process
 * 
 * We also kick off the polling mechanism from here as part of the launch
 */
public class StandardRSEJBossStartLaunchDelegate extends
		AbstractJBossStartLaunchConfiguration {
	protected static final String DELIMETER = ":";
	protected static final String ECHO_KEY_DISCOVER_PID = "JBTOOLS_SERVER_START_CMD";


	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		final IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		boolean dontLaunch = LaunchCommandPreferences.isIgnoreLaunchCommand(server);
		if (dontLaunch || isStarted(server)) {
			((ControllableServerBehavior)beh).setServerStarted();
			return false;
		}
		String currentHost = server.getAttribute(RSEUtils.RSE_SERVER_HOST, (String)null);
		if( currentHost == null || RSEFrameworkUtils.findHost(currentHost) == null ) {
			throw new CoreException(new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, 
					"Host \"" + currentHost + "\" not found. Host may have been deleted or RSE model may not be completely loaded"));
		}
		
		return true;
	}
	
	@Override
	protected void beforeVMRunner(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		if( beh != null ) {
			((ControllableServerBehavior)beh).setRunMode(mode);
			((ControllableServerBehavior)beh).setServerStarting();
		}
	}
	

	@Override
	protected void actualLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Pull the already-generated command from the launch config and run it
		IServer server = ServerUtil.getServer(configuration);
		String command = RSELaunchConfigProperties.getStartupCommand(configuration);
		executeRemoteCommand(command, server);
	}
	
	@Override
	protected void afterVMRunner(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Initiate Polling!
		pollServer(ServerUtil.getServer(configuration), IServerStatePoller.SERVER_UP);
	}
	
	/*
	 * Below is code for kicking off a poll thread
	 */
	protected boolean isStarted(IServer server) {
		return PollThreadUtils.isServerStarted(server).isOK();
	}
	
	protected void pollServer(IServer server, final boolean expectedState) {
		// IF shutting down a process started OUTSIDE of eclipse, force use the web poller, 
		// since there's no process watch for shutdowns
		IServerStatePoller poller = PollThreadUtils.getPoller(expectedState, server);
		pollServer(server, expectedState, poller);
	}

	protected void pollServer(IServer server, boolean expectedState, IServerStatePoller poller) {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(server);
		PollThread pollThread = (PollThread)beh.getSharedData(IDeployableServerBehaviorProperties.POLL_THREAD);
		pollThread = PollThreadUtils.pollServer(expectedState, poller, pollThread, onPollingFinished(server), server);
		beh.putSharedData(IDeployableServerBehaviorProperties.POLL_THREAD, pollThread);
	}
	

	protected IPollResultListener onPollingFinished(final IServer server) {
		return new IPollResultListener() {

			@Override
			public void stateNotAsserted(boolean expectedState, boolean currentState) {
				server.stop(true);
			}

			@Override
			public void stateAsserted(boolean expectedState, boolean currentState) {
				IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(server);
				if (currentState == IServerStatePoller.SERVER_UP) {
					((ControllableServerBehavior)beh).setServerStarted();
				} else {
					((ControllableServerBehavior)beh).setServerStopped();
				}
			}
		};
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
				for (int i = 0; i < out.length; i++) {
					if( out[i].toString().startsWith(ECHO_KEY_DISCOVER_PID)) {
						// pid found
						int lastColon = out[i].toString().lastIndexOf(DELIMETER);
						String pid = out[i].toString().substring(lastColon+1);
						behavior.putSharedData(IDeployableServerBehaviorProperties.PROCESS_ID, pid);
					}
				}
			}
		};
		shell.addOutputListener(listener);
	}
	
}
