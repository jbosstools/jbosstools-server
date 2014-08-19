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
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;

public class NoOpLaunchController extends AbstractSubsystemController implements
		ILaunchServerController {

	public NoOpLaunchController() {
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
		IControllableServerBehavior beh = getServerBehavior(configuration);
		if( beh != null ) {
			((ControllableServerBehavior)beh).setServerStarting();
			((ControllableServerBehavior)beh).setServerStarted();
			((ControllableServerBehavior)getControllableBehavior()).setRunMode(mode);
		} else {
			// TODO throw error
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
