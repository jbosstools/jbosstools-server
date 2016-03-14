/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

public class ProfileServerBehavior extends ControllableServerBehavior {

	@Override
	public ISubsystemController getController(String system) throws CoreException {
		return getController(system, null);
	}
	
	@Override
	protected ILaunchServerController getLaunchController() throws CoreException {
		return (ILaunchServerController)getController(ILaunchServerController.SYSTEM_ID, null);
	}
	
	@Override
	protected IModuleStateController getModuleStateController() throws CoreException {
		return (IModuleStateController)getController(IModuleStateController.SYSTEM_ID, null);
	}

	@Override
	protected IServerShutdownController getShutdownController() throws CoreException {
		return (IServerShutdownController)getController(IServerShutdownController.SYSTEM_ID, null);
	}
	
	@Override
	protected IPublishController getPublishController() throws CoreException {
		return (IPublishController)getController(IPublishController.SYSTEM_ID, null);
	}
	
	/**
	 * Use the profile model to find a subsystem for the given profile
	 */
	@Override
	public ISubsystemController getController(String system, ControllerEnvironment env) throws CoreException {
		// Check override props
		ISubsystemController ret = getOverrideController(system, env);
		if( ret == null ) {
			// Check profile
			String profile = ServerProfileModel.getProfile(getServer());
			ServerProfileModel.ServerProfile sp = ServerProfileModel.getDefault().getProfile(getServer().getServerType().getId(), profile);
			if( sp == null ) {
				ServerProfileModel.getDefault().logMissingProfile(getServer(), profile);
			} else {
				ret = ServerProfileModel.getDefault().getController(getServer(), profile, system, env);
			}
		}
		if( ret == null ) {
			throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 0, 
					"Unable to locate system " + system + " for server " + getServer().getName(), null)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ret;
	}
}
