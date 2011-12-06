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
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;

public interface IJBossServerPublishMethod {
//	public IJBossServerPublishMethodType getPublishMethodType();
	public void publishStart(DeployableServerBehavior behaviour, IProgressMonitor monitor) throws CoreException;
	public int publishFinish(DeployableServerBehavior behaviour, IProgressMonitor monitor) throws CoreException;

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
	public int publishModule(DeployableServerBehavior behaviour, int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException;

	// Methods moved from publishers into the publish method
	public IPublishCopyCallbackHandler getCallbackHandler(IPath path, IServer server);
	public String getPublishDefaultRootFolder(IServer server);
}
