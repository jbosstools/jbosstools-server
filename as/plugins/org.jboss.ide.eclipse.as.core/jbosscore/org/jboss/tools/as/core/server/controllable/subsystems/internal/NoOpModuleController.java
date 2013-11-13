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
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IModuleStateController;

/**
 * A controller that is unable to control modules state
 */
public class NoOpModuleController extends AbstractSubsystemController implements IModuleStateController {


	@Override
	public int getModuleState(IModule[] module, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isModuleStarted(IModule[] module, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void waitModuleStarted(IModule[] module, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void waitModuleStarted(IModule[] module, int maxDelay) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canRestartModule(IModule[] module) {
		return false;
	}

	@Override
	public int startModule(IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		// This impl is unable to stop or start a module		
		return getServer().getModuleState(module);
	}

	@Override
	public int stopModule(IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		// This impl is unable to stop or start a module		
		return getServer().getModuleState(module);
	}

	@Override
	public int restartModule(IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		// This impl is unable to stop or start a module		
		return getServer().getModuleState(module);
	}

}
