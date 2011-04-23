/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.v7;

/**
 * This class may belong in the main JBoss AS core plugin,
 * in case several different plugins need to implement it
 */
public interface IJBoss7ManagementService {
	/**
	 * Get a manager which can be responsible *only* for 
	 * deployments, both synchronously and asynchronously. 
	 * @return
	 */
	public IJBoss7DeploymentManager getDeploymentManager();
	
	/**
	 * Get an interface which can handle executing any and all remote
	 * management tasks via wrappers of more raw APIs.
	 * 
	 * @return
	 */
	public IJBoss7ManagementInterface getManagementInterface();
}
