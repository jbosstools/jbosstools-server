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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.launch.CommandLineLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.server.launch.LocalCommandLineRunner;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;

public class CommandLineLaunchController extends AbstractSubsystemController implements
		ILaunchServerController {

	public CommandLineLaunchController() {
		// TODO Auto-generated constructor stub
	}


	/**
	 * Get access to the ControllableServerBehavior
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static IControllableServerBehavior getServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		IControllableServerBehavior behavior = (IControllableServerBehavior) server.getAdapter(IControllableServerBehavior.class);
		return behavior;
	}

	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		boolean isSkipLaunch = LaunchCommandPreferences.isIgnoreLaunchCommand(server, true); 
		IControllableServerBehavior beh = getServerBehavior(configuration);
		String command = new CommandLineLaunchConfigProperties().getStartupCommand(configuration);
		boolean emptyCommand = (command == null || command.trim().length() == 0);
		if (beh != null && (isSkipLaunch || emptyCommand)) {
			((ControllableServerBehavior)beh).setServerStarting();
			((ControllableServerBehavior)beh).setServerStarted();
			return;
		}
		
		((ControllableServerBehavior)beh).setServerStarting();
		
		
		// Run the command
		runCommand(command, launch, beh, monitor);
		
		// For now we just have to assume its started, but should investigate polling being launched
		// Deploy-only server has no polling... mostly because it has no ports declared
		((ControllableServerBehavior)beh).setServerStarted();
	}
	
	
	/**
	 * Run the launch and put whatever information you need to into the server behavior's shared data
	 * @param command
	 * @param launch
	 * @param beh
	 * @param monitor
	 * @throws CoreException
	 */
	protected void runCommand(String command, ILaunch launch, IControllableServerBehavior beh, IProgressMonitor monitor) throws CoreException {
		IProcess p = new LocalCommandLineRunner().launchCommand(command, launch, monitor);
		if (beh != null ) {
			beh.putSharedData(IDeployableServerBehaviorProperties.PROCESS, p);
		}
	}
	

	
	@Override
	public IStatus canStart(String launchMode) {
		return Status.OK_STATUS;
	}

	@Override
	public void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy,
			IProgressMonitor monitor) throws CoreException {
		// Do Nothing
	}

}
