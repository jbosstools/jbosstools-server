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
package org.jboss.tools.as.core.server.controllable.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllerEnvironment;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerDetailsController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;

/**
 * This is the behavior class for a deploy-only server
 */
public class DeployableServerBehavior extends ControllableServerBehavior {
	
	public ISubsystemController getController(String system) throws CoreException {
		if( IServerDetailsController.SYSTEM_ID.equals(system))
			return getDetailsController();
		return super.getController(system);
	}
	
	protected IServerDetailsController getDetailsController() throws CoreException {
		String existingMode = getServer().getAttribute(IDeployableServer.SERVER_MODE, "local"); //$NON-NLS-1$
		ControllerEnvironment env = new ControllerEnvironment();
		env.addRequiredProperty(IServerDetailsController.SYSTEM_ID, "target", existingMode); //$NON-NLS-1$
		return (IServerDetailsController)getController(IServerDetailsController.SYSTEM_ID, env);
	}
	

	protected ILaunchServerController getLaunchController() throws CoreException {
		String existingMode = getServer().getAttribute(IDeployableServer.SERVER_MODE, "local"); //$NON-NLS-1$
		ControllerEnvironment env = new ControllerEnvironment();
		env.addRequiredProperty(ILaunchServerController.SYSTEM_ID, "target", existingMode); //$NON-NLS-1$
		return (ILaunchServerController)getController(SYSTEM_LAUNCH, env);
	}
	
	
	protected IPublishController getPublishController() throws CoreException {
		Object o = getSharedData(SYSTEM_PUBLISH);
		if( o != null ) {
			return (IPublishController)o;
		}
		String existingMode = getServer().getAttribute(IDeployableServer.SERVER_MODE, "local"); //$NON-NLS-1$
		ControllerEnvironment env = new ControllerEnvironment();
		env.addRequiredProperty(IDeploymentOptionsController.SYSTEM_ID, "target", existingMode); //$NON-NLS-1$ 
		env.addRequiredProperty(IFilesystemController.SYSTEM_ID, "target", existingMode); //$NON-NLS-1$ 
		return (IPublishController)getController(SYSTEM_PUBLISH, env);
	}
	

	@Override
	protected void publishStart(IProgressMonitor monitor) throws CoreException {
		IPublishController controller = getPublishController();
		putSharedData(SYSTEM_PUBLISH, controller);
		controller.publishStart(monitor);
	}
	
	@Override
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		IPublishController controller = getPublishController();
		try {
			controller.publishFinish(monitor);
		} finally {
			putSharedData(SYSTEM_PUBLISH, null);
		}
	}
}
