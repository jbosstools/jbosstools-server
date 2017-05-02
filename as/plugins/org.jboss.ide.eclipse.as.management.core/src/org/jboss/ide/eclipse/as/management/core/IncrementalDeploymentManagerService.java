package org.jboss.ide.eclipse.as.management.core;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IncrementalDeploymentManagerService extends IJBoss7ManagerService {

	/**
	 * A synchronous API to send an incremental deployment request to the server. 
	 * 
	 * @param details
	 * @param deploymentName
	 * @param model
	 * @param redeploy
	 * @param monitor
	 * @return
	 * @throws JBoss7ManangerException
	 */
	public IJBoss7DeploymentResult incrementalPublish(
			IAS7ManagementDetails details, String deploymentName, 
			IncrementalManagementModel model,
			boolean redeploy, IProgressMonitor monitor) throws JBoss7ManangerException;
	
	
	/**
	 * An addition to deploySync to make sure the archives are deployed exploded. 
	 * @param details
	 * @param deploymentName
	 * @param file
	 * @param add
	 * @param explodePaths
	 * @param monitor
	 * @return
	 * @throws JBoss7ManangerException
	 */
	public IJBoss7DeploymentResult deploySync(IAS7ManagementDetails details, String deploymentName,
			File file, boolean add, String[] explodePaths, IProgressMonitor monitor) throws JBoss7ManangerException;
	
}
