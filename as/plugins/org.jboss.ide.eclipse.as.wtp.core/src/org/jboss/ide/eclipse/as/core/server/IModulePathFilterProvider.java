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
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;

/**
 * This interface represents an object capable of providing a module path filter
 * for various server/module combinations.  
 * @since 3.0
 */
public interface IModulePathFilterProvider {
	/**
	 * Acquire a path filter for the given server/module combination
	 * 
	 * @param server
	 * @param module
	 * @return
	 */
	public IModulePathFilter getFilter(IServer server, IModule[] module);
}
