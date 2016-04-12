/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.core.server.controllable.systems;

import org.eclipse.wst.server.core.IModule;

/**
 * An extension to the existing deployment options controller 
 * to allow customization of zip on a per-module basis. 
 * 
 */
public interface IDeploymentZipOptionsController extends IDeploymentOptionsController {
	/**
	 * Get whether or not this server prefers zipped deployments 
	 * for this module tree path. 
	 * 
	 * @return
	 */
	public boolean shouldZipDeployment(IModule[] module);
	
	/**
	 * Set whether to zip the given deployment
	 * @param module
	 * @param value A boolean representing true or false, 
	 *  			or null to clear the setting and use server default
	 */
	public void setShouldZipDeployment(IModule[] module, Boolean value);
	
}
