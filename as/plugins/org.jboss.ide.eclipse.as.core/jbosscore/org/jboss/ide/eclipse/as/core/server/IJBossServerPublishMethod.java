/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;

public interface IJBossServerPublishMethod {
//	public IJBossServerPublishMethodType getPublishMethodType();
	public void publishStart(IDeployableServerBehaviour behaviour, IProgressMonitor monitor) throws CoreException;
	public int publishFinish(IDeployableServerBehaviour behaviour, IProgressMonitor monitor) throws CoreException;

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
	public int publishModule(IDeployableServerBehaviour behaviour, int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException;

	/**
	 * Create a callback handler with the given deploy path and server.
	 * Use a default temporary folder as provided by the server's settings
	 */
	public IPublishCopyCallbackHandler getCallbackHandler(IPath deployPath, IServer server);

	/**
	 * Create a callback handler with the given deploy path and server.
	 * Use a default temporary folder as provided by the server's settings
	 */
	public IPublishCopyCallbackHandler getCallbackHandler(IPath deployPath, IPath tmpFolder, IServer server);

	
	/**
	 * Get the default root deploy folder for this server
	 * @param server
	 * @return
	 */
	public String getPublishDefaultRootFolder(IServer server);

	/**
	 * Get the default root TEMP deploy folder for this server
	 * @param server
	 * @return
	 */
	public String getPublishDefaultRootTempFolder(IServer server);
}
