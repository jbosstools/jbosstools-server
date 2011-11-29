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
package org.jboss.ide.eclipse.as.internal.management.as7;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.server.v7.management.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.core.server.v7.management.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7ServerState;
import org.jboss.ide.eclipse.as.internal.management.as7.AS7Manager;

/**
 * @author Rob Stryker
 */
public class JBoss7ManagerService implements IJBoss7ManagerService {
	public void init() throws Exception {
	}
	
	public IJBoss7DeploymentResult deployAsync(AS7ManagementDetails details, String deploymentName,
			File file, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(details);
		try {
			IJBoss7DeploymentResult result = manager.deploy(deploymentName, file);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public IJBoss7DeploymentResult deploySync(AS7ManagementDetails details, String deploymentName,
			File file, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(details);
		try {
			IJBoss7DeploymentResult result = manager.deploySync(deploymentName, file, monitor);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public IJBoss7DeploymentResult undeployAsync(AS7ManagementDetails details, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(details);
		try {
			IJBoss7DeploymentResult result = manager.undeploy(deploymentName);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public IJBoss7DeploymentResult syncUndeploy(AS7ManagementDetails details, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(details);
		try {
			IJBoss7DeploymentResult result = manager.undeploySync(deploymentName, monitor);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public JBoss7DeploymentState getDeploymentState(AS7ManagementDetails details, String deploymentName) throws Exception {
		AS7Manager manager = new AS7Manager(details);
		try {
			JBoss7DeploymentState result = manager.getDeploymentStateSafe(deploymentName);
			return result;
		} finally {
			manager.dispose();
		}
	}
	
	public JBoss7ServerState getServerState(AS7ManagementDetails details) throws Exception {
		AS7Manager manager = new AS7Manager(details);
		try {
			JBoss7ServerState state = manager.getServerState();
			return state;
		} finally {
			manager.dispose();
		}
	}

	public boolean isRunning(AS7ManagementDetails details) throws Exception {
		AS7Manager manager = new AS7Manager(details);
		try {
			boolean ret = manager.isRunning();
			return ret;
		} finally {
			manager.dispose();
		}
	}

	public void stop(AS7ManagementDetails details) throws Exception {
		AS7Manager manager = new AS7Manager(details);
		try {
			manager.stopServer();
		} finally {
			manager.dispose();
		}
	}

	public void dispose() {
	}

}
