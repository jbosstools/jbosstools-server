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
package org.jboss.ide.eclipse.as.core.server.v7.management;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IJBoss7ManagerService {

	public static final String AS_VERSION_PROPERTY = "as.version"; //$NON-NLS-1$

	public static final String AS_VERSION_700 = "700"; //$NON-NLS-1$
	public static final String AS_VERSION_710_Beta = "710.beta1"; //$NON-NLS-1$
	public static final int MGMT_PORT = 9999;

	public void init() throws Exception;
	
	/**
	 * Asynchronously deploy a file to a server
	 * @param host     The host
	 * @param port     The port
	 * @param name     The deployment's name
	 * @param file     The file to be deployed
	 * @param monitor  The progress monitor
	 * 
	 * @return Not sure what to return yet
	 * @throws Exception
	 */
	public IJBoss7DeploymentResult deployAsync(AS7ManagementDetails details,
			String deploymentName, File file, IProgressMonitor monitor) throws Exception;

	/**
	 * Synchronously deploy a file to a server
	 * 
	 * @param host    The host
	 * @param port    The port
	 * @param name    The deployment's name
	 * @param file    The file to be deployed
	 * @param monitor The progress monitor
	 * 
	 * @return Not sure what to return yet
	 * @throws Exception
	 */
	public IJBoss7DeploymentResult deploySync(AS7ManagementDetails details,
			String deploymentName, File file, IProgressMonitor monitor) throws Exception;

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
	 * @throws Exception
	 */
	public IJBoss7DeploymentResult undeployAsync(AS7ManagementDetails details,
			String deploymentName, boolean removeFile, IProgressMonitor monitor) throws Exception;

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
	 * @throws Exception
	 */
	public IJBoss7DeploymentResult syncUndeploy(AS7ManagementDetails details,
			String deploymentName, boolean removeFile, IProgressMonitor monitor) throws Exception;

	/**
	 * Returns the state for a given deployment name on a given host and port.
	 * 
	 * @param host    the host to query
	 * @param port    the port to contact it on
	 * @param deploymentName  the name of the deployment that shall be queried
	 * 
	 * @return the state of the deployment
	 * @throws Exception
	 */
	public JBoss7DeploymentState getDeploymentState(AS7ManagementDetails details, String deploymentName) throws Exception;

	/**
	 * Returns the state of the server 
	 * 
	 * @param host the server to query
	 * @param port the port to communicate on
	 * @return the state of the server
	 * 
	 * @throws Exception
	 */
	public JBoss7ServerState getServerState(AS7ManagementDetails details) throws Exception;

	/**
	 * Returns <code>true</code> if the server is running, <code>false</code>
	 * otherwise.
	 * 
	 * @param host the server to to query
	 * @param port the port to communicate on
	 * @return true if it's running, false otherwise
	 * @throws Exception
	 */
	public boolean isRunning(AS7ManagementDetails details) throws Exception;

	/**
	 * Stops the given server
	 * 
	 * @throws JBoss7ManangerException
	 * @throws Exception 
	 */
	public void stop(AS7ManagementDetails details) throws Exception;

	public void dispose();
}
