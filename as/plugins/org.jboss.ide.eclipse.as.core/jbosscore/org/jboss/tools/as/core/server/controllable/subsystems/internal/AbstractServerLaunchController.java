/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
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
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;

public abstract class AbstractServerLaunchController extends AbstractSubsystemController implements
		ILaunchServerController {

	public AbstractServerLaunchController() {
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


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
		ILaunch launch, IProgressMonitor monitor) throws CoreException {

		IServer server = LaunchConfigUtils.checkedGetServer(configuration);
		if( LaunchCommandPreferences.isIgnoreLaunchCommand(server)) {
			return;
		}
		beforeFinalLaunch(configuration, mode, launch, monitor);
		launchImpl(configuration, mode, launch, monitor);
		afterLaunch(configuration, mode, launch, monitor);
	}

	/**
	 * This is the place to do all pre-launch details that are required. 
	 * Specifically, the server should be set to 'starting' here, 
	 * the server state / structure should be verified, 
	 * and any other details should be checked before executing the final launch
	 * 
	 * @param configuration
	 * @param mode
	 * @param launch
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract void beforeFinalLaunch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) 
			throws CoreException;
	
	
	/**
	 * This is the launch implementation. Either use a delegate like JavaLaunchDelegate
	 * to do the work, or implement it yourself. 
	 * 
	 * @param configuration
	 * @param mode
	 * @param launch
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract void launchImpl(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException;
	

	/**
	 * After the launch has run and the process has been spawned, this is 
	 * where you can do post-launch processing, such as storing the IProcess objects
	 * in the {@link ControllableServerBehavior}
	 * 
	 * 
	 * @param configuration
	 * @param mode
	 * @param launch
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract void afterLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException;

	
	/**
	 * Can the server start?
	 * {@link ServerBehaviourDelegate#canStart(String)}
	 */
	@Override
	public IStatus canStart(String launchMode) {
		return Status.OK_STATUS;
	}

	/**
	 * Set up the launch configuration, overriding important 
	 * flags with new data from whatever models you have
	 * 
	 * {@link ServerBehaviourDelegate#setupLaunchConfiguration(ILaunchConfigurationWorkingCopy, IProgressMonitor)}
	 */
	@Override
	public void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy,
			IProgressMonitor monitor) throws CoreException {
		// Subclasses can override
	}

}
