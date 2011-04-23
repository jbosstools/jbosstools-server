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
package org.jboss.ide.eclipse.as.management.as7.deployment;

import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7DeploymentManager;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7ManagementInterface;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7ManagementService;

/**
 * @author Rob Stryker
 */
public class JBossManagementService implements IJBoss7ManagementService {

	private IJBoss7DeploymentManager deploymentManager = null;
	private IJBoss7ManagementInterface manager = null;
	
	public IJBoss7DeploymentManager getDeploymentManager() {
		if( deploymentManager == null )
			deploymentManager = new JBossDeploymentManager();
		return deploymentManager;
	}

	public IJBoss7ManagementInterface getManagementInterface() {
		// TODO Auto-generated method stub
		return null;
	}
}
