package org.jboss.ide.eclipse.as.management.as7.deployment;

import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7DeploymentManager;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7ManagementInterface;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7ManagementService;


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
