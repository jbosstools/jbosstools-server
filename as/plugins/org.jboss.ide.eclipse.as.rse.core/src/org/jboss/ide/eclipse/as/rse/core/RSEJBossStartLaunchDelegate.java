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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;

public class RSEJBossStartLaunchDelegate extends AbstractRSELaunchDelegate {

	/*
	 * Check if we should launch this. 
	 * 	 @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#preLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.core.runtime.IProgressMonitor)

	 */
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
		IDelegatingServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
		String currentHost = jbsBehavior.getServer().getAttribute(RSEUtils.RSE_SERVER_HOST, (String)null);
		if( currentHost == null || RSEFrameworkUtils.findHost(currentHost) == null ) {
			throw new CoreException(new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, 
					"Host \"" + currentHost + "\" not found. Host may have been deleted or RSE model may not be completely loaded"));
		}
		
		return true;
	}

	protected boolean isStarted(IServer server) {
		return PollThreadUtils.isServerStarted(server).isOK();
	}
	
	@Override
	public void preLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		final IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		((ControllableServerBehavior)beh).setServerStarting();
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
