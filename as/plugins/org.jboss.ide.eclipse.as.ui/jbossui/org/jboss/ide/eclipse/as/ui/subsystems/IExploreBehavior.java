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
package org.jboss.ide.eclipse.as.ui.subsystems;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;

/**
 * This class represents a way to explore a given server/module
 * combination. This is necessary because local and remote servers
 * require different dialogs or perhaps even initialization 
 * of connections. 
 */
public interface IExploreBehavior extends ISubsystemController {
	
	public static final String SYSTEM_ID = "exploreBehavior"; //$NON-NLS-1$
	
	/**
	 * Can this behavior properly explore the given server / module?
	 * @param server
	 * @param module
	 * @return true if yes, false if no
	 */
	public boolean canExplore(IServer server, IModule[] module);
	
	
	/**
	 * Opens some type of file-system browsing UI 
	 * suitable for the given server / module / server-mode 
	 * combination. 
	 * 
	 * @param server
	 * @param module
	 */
	public void openExplorer(IServer server, IModule[] module);
}
