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
package org.jboss.ide.eclipse.as.management.as7.deployment;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.as.core.server.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7DeploymentState;

/**
 * @author Rob Stryker
 */
public class JBoss7ManagerService implements IJBoss7ManagerService {

	public IJBoss7DeploymentResult deployAsync(String host, int port, String deploymentName,
			File file, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(host, port);
		return manager.deploy(deploymentName, file);
	}

	public IJBoss7DeploymentResult deploySync(String host, int port, String deploymentName,
			File file, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(host, port);
		return manager.deploySync(deploymentName, file, monitor);
	}

	public IJBoss7DeploymentResult undeployAsync(String host, int port, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(host, port);
		return manager.undeploy(deploymentName);
	}

	public IJBoss7DeploymentResult syncUndeploy(String host, int port, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(host, port);
		return manager.undeploySync(deploymentName, monitor);
	}

	public JBoss7DeploymentState getDeploymentState(String host, int port, String deploymentName) throws Exception {
		AS7Manager manager = new AS7Manager(host, port);
		return manager.getDeploymentState(deploymentName);
	}
	
	public void stop(String host) throws Exception {
		new AS7Manager(host).stopServer();
	}
	
	public void stop(String host, int port) throws Exception {
		new AS7Manager(host, port).stopServer();
	}

	@Override
	public void dispose() {
	}
}
