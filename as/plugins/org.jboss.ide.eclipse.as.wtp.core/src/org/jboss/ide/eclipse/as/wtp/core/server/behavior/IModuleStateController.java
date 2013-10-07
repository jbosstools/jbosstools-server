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
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;

/**
 * A subsystem controller for handling operations dealing with module state
 * 
 * @since 3.0
 */
public interface IModuleStateController extends ISubsystemController, IServerModuleStateVerifier {
	/**
	 * Checks whether the given module on the server can be restarted.
	 * See the specification of 
	 * {@link ServerBehaviourDelegate#canRestartModule(IModule[])}
	 * 
	 * @param module the module to be started
	 */
	public boolean canRestartModule(IModule[] module);
	
	/**
	 * Starts the given module on the server. See the specification of 
	 * {@link ServerBehaviourDelegate#startModule(IModule[], IProgressMonitor)}
	 * 
	 * @param module the module to be started
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if an error occurs while trying to restart the module
	 */
	public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException;

	/**
	 * Stops the given module on the server. See the specification of 
	 * {@link ServerBehaviourDelegate#stopModule(IModule[], IProgressMonitor)}
	 * 
	 * @param module the module to be stopped
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if an error occurs while trying to restart the module
	 */
	public void stopModule(IModule[] module, IProgressMonitor monitor) throws CoreException;
	/**
	 * Restarts the given module on the server. See the specification of
	 * {@link ServerBehaviourDelegate#restartModule(IModule[], IProgressMonitor)}
	 * @param module the module to be stopped
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if an error occurs while trying to restart the module
	 */
	public void restartModule(IModule[] module, IProgressMonitor monitor) throws CoreException;
}
