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
package org.jboss.ide.eclipse.as.internal.management.as71;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.as.core.server.v7.management.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.core.server.v7.management.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7ServerState;

/**
 * @author Rob Stryker
 */
public class JBoss71ManagerService implements IJBoss7ManagerService {

	public IJBoss7DeploymentResult deployAsync(String host, int port, String deploymentName,
			File file, IProgressMonitor monitor) throws Exception {
		AS71Manager manager = new AS71Manager(host, port);
		try {
			IJBoss7DeploymentResult result = manager.deploy(deploymentName, file);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public IJBoss7DeploymentResult deploySync(String host, int port, String deploymentName,
			File file, IProgressMonitor monitor) throws Exception {
		AS71Manager manager = new AS71Manager(host, port);
		try {
			IJBoss7DeploymentResult result = manager.deploySync(deploymentName, file, monitor);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public IJBoss7DeploymentResult undeployAsync(String host, int port, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws Exception {
		AS71Manager manager = new AS71Manager(host, port);
		try {
			IJBoss7DeploymentResult result = manager.undeploy(deploymentName);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public IJBoss7DeploymentResult syncUndeploy(String host, int port, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws Exception {
		AS71Manager manager = new AS71Manager(host, port);
		try {
			IJBoss7DeploymentResult result = manager.undeploySync(deploymentName, monitor);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public JBoss7DeploymentState getDeploymentState(String host, int port, String deploymentName) throws Exception {
		AS71Manager manager = new AS71Manager(host, port);
		try {
			JBoss7DeploymentState result = manager.getDeploymentStateSafe(deploymentName);
			return result;
		} finally {
			manager.dispose();
		}
	}
	
	@Deprecated
	public JBoss7ServerState getServerState(String host) throws Exception {
		return getServerState(host, AS71Manager.MGMT_PORT);
	}

	public JBoss7ServerState getServerState(String host, int port) throws Exception {
		AS71Manager manager = new AS71Manager(host, port);
		try {
			JBoss7ServerState state = manager.getServerState();
			return state;
		} finally {
			manager.dispose();
		}
	}

	public boolean isRunning(String host, int port) throws Exception {
		AS71Manager manager = new AS71Manager(host, port);
		try {
			boolean ret = manager.isRunning();
			return ret;
		} finally {
			manager.dispose();
		}
	}

	@Deprecated
	public void stop(String host) throws Exception {
		stop(host, AS71Manager.MGMT_PORT);
	}
	
	public void stop(String host, int port) throws Exception {
		AS71Manager manager = new AS71Manager(host, port);
		try {
			manager.stopServer();
		} finally {
			manager.dispose();
		}
	}

	@Override
	public void dispose() {
	}
}
