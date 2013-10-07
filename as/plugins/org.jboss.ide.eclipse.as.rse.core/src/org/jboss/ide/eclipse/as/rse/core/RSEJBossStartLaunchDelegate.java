/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IJBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IJBossLaunchDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;

public class RSEJBossStartLaunchDelegate implements IJBossLaunchDelegate {

	public static final String DELIMETER = ":";
	public static final String ECHO_KEY_DISCOVER_PID = "JBTOOLS_SERVER_START_CMD";
	protected void executeRemoteCommand(String command, IDelegatingServerBehavior behavior)
			throws CoreException {
		try {
			ServerShellModel model = RSEHostShellModel.getInstance().getModel(behavior.getServer());
			IHostShell shell = model.createStartupShell("/", command, new String[] {}, new NullProgressMonitor());
			addShellOutputListener(shell, behavior);
			String getPidCommand = "echo \"" + ECHO_KEY_DISCOVER_PID + DELIMETER + behavior.getServer().getId() + DELIMETER + "\"$!";
			shell.writeToShell(getPidCommand);
		} catch (SystemMessageException sme) {
			// could not connect to remote system
			((DelegatingServerBehavior)behavior).setServerStopped(); 
			throw new CoreException(new Status(IStatus.ERROR,
					org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID,
					MessageFormat.format("Could not execute command on remote server {0}. Please ensure the server is reachable.", behavior.getServer().getName()), sme));
		}
	}
	
	private void addShellOutputListener(final IHostShell shell, 
			final IDelegatingServerBehavior behavior) {
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
						IJBossBehaviourDelegate del = behavior.getDelegate();
						if( del instanceof AbstractRSEBehaviourDelegate) {
							((AbstractRSEBehaviourDelegate)del).setPid(pid);
						}
					}
				}
			}
		};
		shell.addOutputListener(listener);
	}

	
	
	/*
	 * Check if we should launch this. 
	 * 	 @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#preLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.core.runtime.IProgressMonitor)

	 */
	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		final IDelegatingServerBehavior beh = JBossServerBehaviorUtils.getServerBehavior(configuration);
		boolean dontLaunch = LaunchCommandPreferences.isIgnoreLaunchCommand(beh.getServer());
		if (dontLaunch || isStarted(beh)) {
			((DelegatingServerBehavior)beh).setServerStarted();
			return false;
		}
		IDelegatingServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
		String currentHost = jbsBehavior.getServer().getAttribute(RSEUtils.RSE_SERVER_HOST, (String)null);
		if( currentHost == null || RSEUtils.findHost(currentHost) == null ) {
			throw new CoreException(new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, 
					"Host \"" + currentHost + "\" not found. Host may have been deleted or RSE model may not be completely loaded"));
		}
		
		return true;
	}

	protected boolean isStarted(IDelegatingServerBehavior beh) {
		return PollThreadUtils.isServerStarted(beh).isOK();
	}
	
	@Override
	public void preLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IDelegatingServerBehavior beh = JBossServerBehaviorUtils.getServerBehavior(configuration);
		((DelegatingServerBehavior)beh).setServerStarting();
	}

	@Override
	public void actualLaunch(
			LaunchConfigurationDelegate launchConfig,
			ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		// Pull the already-generated command from the launch config and run it
		IDelegatingServerBehavior beh = JBossServerBehaviorUtils.getServerBehavior(configuration);
		String command = RSELaunchConfigProperties.getStartupCommand(configuration);
		executeRemoteCommand(command, beh);
	}
	

	@Override
	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// cleanup / cache any processes or refs we want. Here, we dont have anything to do.
	}

	/**
	 * The launch configurator is what actually sets up the startup and shutdown
	 * commands. This is all done before launch, whenever the servertools api asks
	 * us to 'set up' the launch configuration. 
	 * 
	 * Setup is done here, and during the launch, we simply access the stored field. 
	 */
	@Override
	public void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy, IServer server)
			throws CoreException {
		new RSELaunchConfigurator(server).configure(workingCopy);
	}


}
