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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;

/**
 * This is a launch configuration which serves as an entry point
 * to a controllable server. It will delegate to a relevant 
 * {@link ILaunchServerController} for all methods. 
 * 
 * This class is necessary because a server can only have
 * one specific launch configuration type declared in its extension point.
 * If behavior must change based on settings, the single entry point
 * must delegate to the currently active implementation. 
 * 
 * This class cannot keep state! It must pull all state from the 
 * launch configuration that has been passed in. 
 */
public class ControllableServerLaunchConfiguration implements
		ILaunchConfigurationDelegate2 {
	protected ILaunchServerController getController(ILaunchConfiguration configuration) throws CoreException {
		
		IControllableServerBehavior jbsBehavior = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		ILaunchServerController delegate = (ILaunchServerController)jbsBehavior.getController(IControllableServerBehavior.SYSTEM_LAUNCH);
		if( delegate == null ) {
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Unable to locate launch delegate for server")); //$NON-NLS-1$
		}
		return delegate;
	}
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		getController(configuration).launch(configuration, mode, launch, monitor);
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		ILaunchServerController del = getController(configuration);
		if( del instanceof ILaunchConfigurationDelegate2 ) {
			return ((ILaunchConfigurationDelegate2)del).getLaunch(configuration, mode);
		}
		return null;
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		ILaunchServerController del = getController(configuration);
		if( del instanceof ILaunchConfigurationDelegate2 ) {
			return ((ILaunchConfigurationDelegate2)del).buildForLaunch(configuration, mode, monitor);
		}
		return false;
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		ILaunchServerController del = getController(configuration);
		if( del instanceof ILaunchConfigurationDelegate2 ) {
			return ((ILaunchConfigurationDelegate2)del).finalLaunchCheck(configuration, mode, monitor);
		}
		return true;
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		ILaunchServerController del = getController(configuration);
		if( del instanceof ILaunchConfigurationDelegate2 ) {
			return ((ILaunchConfigurationDelegate2)del).preLaunchCheck(configuration, mode, monitor);
		}
		return true;
	}

}
