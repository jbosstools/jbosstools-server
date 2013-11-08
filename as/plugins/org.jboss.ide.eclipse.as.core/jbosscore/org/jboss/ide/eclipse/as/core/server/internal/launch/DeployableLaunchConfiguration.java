/******************************************************************************* 
* Copyright (c) 2007 Red Hat, Inc. 
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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;

/**
 * Not deprecated, still in use by the deploy-only server
 * 
 * This class represents a launch configuration which can delegate
 * to the current launch controller provided by the ControllableServerBehavior
 */
public class DeployableLaunchConfiguration implements
		ILaunchConfigurationDelegate, ILaunchConfigurationDelegate2 {
	
	public static ControllableServerBehavior getServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		ControllableServerBehavior behavior = (ControllableServerBehavior) server.getAdapter(ControllableServerBehavior.class);
		return behavior;
	}

	private ILaunchServerController controller = null;
	private ILaunchServerController getController(ILaunchConfiguration config) throws CoreException {
		if( controller == null ) {
			ControllableServerBehavior behavior = getServerBehavior(config);
			controller = (ILaunchServerController)behavior.getController(ControllableServerBehavior.SUBSYSTEM_LAUNCH);			
		}
		return controller;
	}

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if( getController(configuration) != null ) {
			getController(configuration).launch(configuration, mode, launch, monitor);
		}
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		if( getController(configuration) instanceof ILaunchConfigurationDelegate2 )
			return ((ILaunchConfigurationDelegate2)getController(configuration)).getLaunch(configuration, mode);
		return null;
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		if( getController(configuration) instanceof ILaunchConfigurationDelegate2 )
			return ((ILaunchConfigurationDelegate2)getController(configuration)).buildForLaunch(configuration, mode, monitor);
		return true;
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		if( getController(configuration) instanceof ILaunchConfigurationDelegate2 )
			return ((ILaunchConfigurationDelegate2)getController(configuration)).finalLaunchCheck(configuration, mode, monitor);
		return true;
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		if( getController(configuration) instanceof ILaunchConfigurationDelegate2 )
			return ((ILaunchConfigurationDelegate2)getController(configuration)).preLaunchCheck(configuration, mode, monitor);
		return true;
	}
}
