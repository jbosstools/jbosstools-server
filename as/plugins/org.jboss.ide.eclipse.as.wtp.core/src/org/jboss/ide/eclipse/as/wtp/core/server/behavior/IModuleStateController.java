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

/**
 * A subsystem controller for handling operations dealing with module state
 * 
 * @since 3.0
 */
public interface IModuleStateController extends ISubsystemController {
	
	/**
	 * The name of the system this interface relates to
	 */
	public static final String SYSTEM_ID = "modules";
	
	
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
	 * @return The new state of the module
	 */
	public int startModule(IModule[] module, IProgressMonitor monitor) throws CoreException;

	/**
	 * Stops the given module on the server. See the specification of 
	 * {@link ServerBehaviourDelegate#stopModule(IModule[], IProgressMonitor)}
	 * 
	 * @param module the module to be stopped
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if an error occurs while trying to restart the module
	 * @return The new state of the module
	 */	
	public int stopModule(IModule[] module, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Restarts the given module on the server. See the specification of
	 * {@link ServerBehaviourDelegate#restartModule(IModule[], IProgressMonitor)}
	 * @param module the module to be stopped
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if an error occurs while trying to restart the module
	 * @return The new state of the module
	 */
	public int restartModule(IModule[] module, IProgressMonitor monitor) throws CoreException;
	
	
	/**
	 * Get the running state of the given module
	 * @param server
	 * @param module
	 * @param monitor
	 * @return IServer.STATE_XXX flag
	 */
	public int getModuleState(IModule module[], IProgressMonitor monitor);
	
	/**
	 * Check to see if the module is started on the server
	 */
	public boolean isModuleStarted(IModule module[], IProgressMonitor monitor);
	
	/**
	 * Wait until the module is started on the server
	 */
	public void waitModuleStarted( IModule module[], IProgressMonitor monitor);
	
	/**
	 * Wait until the module is started on the server with a max delay as provided
	 */
	public void waitModuleStarted(IModule module[], int maxDelay);
	
}
