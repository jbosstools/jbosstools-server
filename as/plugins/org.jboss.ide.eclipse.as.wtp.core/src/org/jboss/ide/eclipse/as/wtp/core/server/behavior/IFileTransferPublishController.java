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

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IServer;

/**
 * A subsystem controller handling publish operations 
 * which is capable of using a standard
 * publisher logic of traversing module delta trees and performing
 * incremental deployments over standard file-copy or file-transfer calls.  
 * 
 * @since 3.0
 */
public interface IFileTransferPublishController extends IPublishController {
	/**
	 * Create a callback handler with the given deploy path and server.
	 * Use a default temporary folder as provided by the server's settings
	 */
	public IFilesystemController getFilesystemController(IPath deployPath, IServer server);

	/**
	 * Create a callback handler with the given deploy path and server.
	 * Use a default temporary folder as provided by the server's settings
	 */
	public IFilesystemController getFilesystemController(IPath deployPath, IPath tmpFolder, IServer server);

	
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
