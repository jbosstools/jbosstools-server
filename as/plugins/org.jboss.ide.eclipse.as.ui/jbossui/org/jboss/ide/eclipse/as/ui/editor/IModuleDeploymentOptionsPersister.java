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
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;

/**
 * @since 2.5
 */
public interface IModuleDeploymentOptionsPersister {
	/**
	 * Get the current working copy of a server
	 * @return
	 */
	public IServerWorkingCopy getServer();
	
	/**
	 * Save the deployment attribute changes to the working copy. 
	 * The typical implementation of this method does it via an editor command
	 * so that it can be undone. If this composite is instantiated in 
	 * any other place, a proper IModuleDeploymentOptionsPartner will just persist them in the working copy directly
	 * @param p
	 * @param keys
	 * @param vals
	 * @param cmdName
	 */
	public void firePropertyChangeCommand(DeploymentModulePrefs p, String[] keys, String[] vals, String cmdName);
}