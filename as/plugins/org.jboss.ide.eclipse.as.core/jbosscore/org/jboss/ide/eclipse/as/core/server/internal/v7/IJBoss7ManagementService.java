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
