/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.management.core;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IJBoss7ManagerService {

	public static final String AS_VERSION_PROPERTY = "as.version"; //$NON-NLS-1$

	@Deprecated public static final String AS_VERSION_700 = "700"; //$NON-NLS-1$
	public static final String AS_VERSION_710_Beta = "710.beta1"; //$NON-NLS-1$
	
	// I believe this is unused
	public static final String AS_VERSION_720 = "720"; //$NON-NLS-1$
	public static final String WILDFLY_VERSION_800 = "8.0.0"; //$NON-NLS-1$
	
	public static final int MGMT_PORT = 9999;

	public void init() throws JBoss7ManangerException;
	
	/**
	 * Asynchronously deploy a file to a server
	 * @param host     The host
	 * @param port     The port
	 * @param name     The deployment's name
	 * @param file     The file to be deployed
	 * @param add      Add the deployment? True if the deployment is not 
	 * 				   already added, false if you just want to start the existing deployment
	 * @param monitor  The progress monitor
	 * 
	 * @return Not sure what to return yet
	 * @throws JBoss7ManangerException
	 */
	public IJBoss7DeploymentResult deployAsync(IAS7ManagementDetails details,
			String deploymentName, File file, boolean add, IProgressMonitor monitor) throws JBoss7ManangerException;

	/**
	 * Synchronously deploy a file to a server
	 * 
	 * @param host    The host
	 * @param port    The port
	 * @param name    The deployment's name
	 * @param file    The file to be deployed
	 * @param add     Add the deployment? True if the deployment is not 
	 * 				  already added, false if you just want to start the existing deployment
	 * @param monitor The progress monitor
	 * 
	 * @return Not sure what to return yet
	 * @throws JBoss7ManangerException
	 */
	public IJBoss7DeploymentResult deploySync(IAS7ManagementDetails details,
			String deploymentName, File file, boolean add, IProgressMonitor monitor) throws JBoss7ManangerException;

	/**
	 * Asynchronously undeploy a file to a server
	 * 
	 * @param host    The host
	 * @param port    The port
	 * @param name    The deployment's name
	 * @param file    The file to be deployed
	 * @param monitor The progress monitor
	 * 
	 * @return Not sure what to return yet
	 * @throws JBoss7ManangerException
	 */
	public IJBoss7DeploymentResult undeployAsync(IAS7ManagementDetails details,
			String deploymentName, boolean removeFile, IProgressMonitor monitor) throws JBoss7ManangerException;

	/**
	 * Synchronously undeploy a file to a server
	 * 
	 * @param host    The host
	 * @param port    The port
	 * @param name    The deployment's name
	 * @param file    The file to be deployed
	 * @param monitor The progress monitor
	 * 
	 * @return Not sure what to return yet
	 * @throws JBoss7ManangerException
	 */
	public IJBoss7DeploymentResult undeploySync(IAS7ManagementDetails details,
			String deploymentName, boolean removeFile, IProgressMonitor monitor) throws JBoss7ManangerException;

	/**
	 * Returns the state for a given deployment name on a given host and port.
	 * 
	 * @param host    the host to query
	 * @param port    the port to contact it on
	 * @param deploymentName  the name of the deployment that shall be queried
	 * 
	 * @return the state of the deployment
	 * @throws JBoss7ManangerException
	 */
	public JBoss7DeploymentState getDeploymentState(IAS7ManagementDetails details, String deploymentName) throws JBoss7ManangerException;

	/**
	 * Returns the state of the server 
	 * 
	 * @param host the server to query
	 * @param port the port to communicate on
	 * @return the state of the server
	 * 
	 * @throws JBoss7ManangerException
	 */
	public JBoss7ServerState getServerState(IAS7ManagementDetails details) throws JBoss7ManangerException;

	/**
	 * Returns <code>true</code> if the server is running, <code>false</code>
	 * otherwise.
	 * 
	 * @param host the server to to query
	 * @param port the port to communicate on
	 * @return true if it's running, false otherwise
	 * @throws JBoss7ManangerException
	 */
	public boolean isRunning(IAS7ManagementDetails details) throws JBoss7ManangerException;

	/**
	 * Stops the given server
	 * 
	 * @throws JBoss7ManangerException
	 * @throws JBoss7ManangerException 
	 */
	public void stop(IAS7ManagementDetails details) throws JBoss7ManangerException;
	
	
	/**
	 * Add a deployment but do not deploy it
	 * @param details
	 * @param deploymentName
	 * @param file
	 * @param monitor
	 * @return
	 * @throws JBoss7ManangerException
	 */
	public IJBoss7DeploymentResult addDeployment(IAS7ManagementDetails details, String deploymentName,
			File file, IProgressMonitor monitor) throws JBoss7ManangerException;
	
	
	/**
	 * Remove a deployment 
	 * @param details
	 * @param deploymentName
	 * @param monitor
	 * @return
	 * @throws JBoss7ManangerException
	 */
	public IJBoss7DeploymentResult removeDeployment(IAS7ManagementDetails details, String deploymentName,
			IProgressMonitor monitor) throws JBoss7ManangerException;

	
	/**
	 * Replace a deployment with a new version
	 * @param details
	 * @param deploymentName
	 * @param file
	 * @param monitor
	 * @return
	 * @throws JBoss7ManangerException
	 */
	public IJBoss7DeploymentResult replaceDeployment(IAS7ManagementDetails details, String deploymentName,
			File file, IProgressMonitor monitor) throws JBoss7ManangerException;
	

    /**
     * Execute a management command. Note, this method returns the "result" node
     * from the execution.
     * 
     * @param details connection details.
     * @param request a JSON request to process.
     * @return the JSON response from the server.
     * @throws JBoss7ManangerException
     */
	public String execute(IAS7ManagementDetails details, String request) throws JBoss7ManangerException;

	public void dispose();
}
