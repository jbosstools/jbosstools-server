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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

/**
 * A subsystem controller to handle publishing
 * 
 * @since 3.0
 */
public interface IPublishController extends ISubsystemController {
	/**
	 * A delegate for ServerBehaviourDelegate.canPublish()
	 * {@link ServerBehaviourDelegate}
	 * @return
	 */
	public IStatus canPublish();
	
	
	/**
	 * A delegate for ServerBehaviourDelegate.canPublishModule(module)
	 * 
	 * @param module
	 * @return
	 */
	public boolean canPublishModule(IModule[] module);

	
	
	/**
	 * Start the publish operation for the given server behavior. 
	 * This may include setting up remote connections, stopping
	 * deployment scanners, or any other BEFORE tasks.  
	 * 
	 * 
	 * @param behaviour
	 * @param monitor
	 * @throws CoreException
	 */
	public void publishStart(ControllableServerBehavior behaviour, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Finish the publish operation for the given server behavior. 
	 * This may include closing remote connections, re-starting
	 * deployment scanners, or any other AFTER tasks.  
	 * 
	 * 
	 * @param behaviour
	 * @param monitor
	 * @throws CoreException
	 */
	public int publishFinish(ControllableServerBehavior behaviour, IProgressMonitor monitor) throws CoreException;

	/**
	 * Publish this module
	 * @param behaviour The behaviour associated with this server
	 * @param kind The kind of publish, as a constant from IServer
	 * @param deltaKind The kind of delta, as a constant from ServerBehaviourDelegate
	 * @param module  The module to be published
	 * @param monitor The progress monitor
	 * @return An IServer.STATE_XXX constant, or -1 if the behaviour should not change server state
	 * @throws CoreException
	 */
	public int publishModule(ControllableServerBehavior behaviour, int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException;

	/**
	 * A delegate for ServerBehaviourDelegate.publisherver(int kind, IProgressMonitor monitor)
	 * Begin the publish of the server
	 * {@link ServerBehaviourDelegate}
	 * 
	 * 
	 * @param kind
	 * @param monitor
	 * @throws CoreException
	 */
	public void publishServer(int kind, IProgressMonitor monitor) throws CoreException;
	
}
