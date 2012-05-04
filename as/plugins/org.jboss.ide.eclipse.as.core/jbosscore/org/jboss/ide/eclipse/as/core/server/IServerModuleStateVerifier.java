/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;

/**
 * An interface for checking the state of remote modules by 
 * actually communicating with the server to check it. 
 * 
 */
public interface IServerModuleStateVerifier {
	/**
	 * Check to see if the module is started on the server
	 */
	public boolean isModuleStarted(IServer server, IModule module[], IProgressMonitor monitor);
	
	/**
	 * Wait until the module is started on the server
	 */
	public void waitModuleStarted(IServer server, IModule module[], IProgressMonitor monitor);
	
	/**
	 * Wait until the module is started on the server with a max delay as provided
	 */
	public void waitModuleStarted(IServer server, IModule module[], int maxDelay);
	
	
}
