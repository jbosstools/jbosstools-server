package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 * This class is interim API and may change drastically 
 * as development on the application server continues. 
 * I expect credentials to be required eventually,
 * and the API will need to adjust to handle them. 
 * 
 */
public interface IJBoss7DeploymentManager {
	/**
	 * Asynchronously deploy a file to a server
	 * 
	 * @param host The host
	 * @param port The port
	 * @param name The deployment's name
	 * @param file The file to be deployed
	 * @param monitor The progress monitor
	 * 
	 * @return Not sure what to return yet
	 * @throws Exception 
	 */
	public IJBoss7DeploymentResult deployAsync(String host, int port, 
					String deploymentName, File file, IProgressMonitor monitor) throws Exception;

	/**
	 * Synchronously deploy a file to a server
	 * 
	 * @param host The host
	 * @param port The port
	 * @param name The deployment's name
	 * @param file The file to be deployed
	 * @param monitor The progress monitor
	 * 
	 * @return Not sure what to return yet
	 * @throws Exception 
	 */
	public IJBoss7DeploymentResult deploySync(String host, int port, 
			String deploymentName, File file, IProgressMonitor monitor) throws Exception;
	
	
	/**
	 * Asynchronously undeploy a file to a server
	 * 
	 * @param host The host
	 * @param port The port
	 * @param name The deployment's name
	 * @param file The file to be deployed
	 * @param monitor The progress monitor
	 * 
	 * @return Not sure what to return yet
	 * @throws Exception 
	 */
	public IJBoss7DeploymentResult undeployAsync(String host, int port,
			String deploymentName, boolean removeFile, IProgressMonitor monitor) throws Exception;
	

	/**
	 * Synchronously undeploy a file to a server
	 * 
	 * @param host The host
	 * @param port The port
	 * @param name The deployment's name
	 * @param file The file to be deployed
	 * @param monitor The progress monitor
	 * 
	 * @return Not sure what to return yet
	 * @throws Exception 
	 */
	public IJBoss7DeploymentResult syncUndeploy(String host, int port,
			String deploymentName, boolean removeFile, IProgressMonitor monitor) throws Exception;
	
	/**
	 * Returns the state for a given deployment name on a given host and port.
	 * 
	 * @param host the host to query
	 * @param port the port to contact it on
	 * @param deploymentName the name of the deployment that shall be queried
	 * 
	 * @return the state of the deployment
	 * @throws Exception
	 */
	public JBoss7DeploymentState getDeploymentState(String host, int port, String deploymentName) throws Exception;

}
