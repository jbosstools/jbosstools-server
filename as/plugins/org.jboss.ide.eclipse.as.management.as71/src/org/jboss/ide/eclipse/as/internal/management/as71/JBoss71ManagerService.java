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
import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;

/**
 * @author Rob Stryker
 */
public class JBoss71ManagerService implements IJBoss7ManagerService {

	public void init() throws Exception {
	}

	public IJBoss7DeploymentResult deployAsync(IAS7ManagementDetails details, String deploymentName,
			File file, IProgressMonitor monitor) throws Exception {
		AS71Manager manager = new AS71Manager(details);
		try {
			IJBoss7DeploymentResult result = manager.deploy(deploymentName, file);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public IJBoss7DeploymentResult deploySync(IAS7ManagementDetails details, String deploymentName,
			File file, IProgressMonitor monitor) throws Exception {
		AS71Manager manager = new AS71Manager(details);
		try {
			IJBoss7DeploymentResult result = manager.deploySync(deploymentName, file, monitor);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public IJBoss7DeploymentResult undeployAsync(IAS7ManagementDetails details, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws Exception {
		AS71Manager manager = new AS71Manager(details);
		try {
			IJBoss7DeploymentResult result = manager.undeploy(deploymentName);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public IJBoss7DeploymentResult syncUndeploy(IAS7ManagementDetails details, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws Exception {
		AS71Manager manager = new AS71Manager(details);
		try {
			IJBoss7DeploymentResult result = manager.undeploySync(deploymentName, monitor);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public JBoss7DeploymentState getDeploymentState(IAS7ManagementDetails details, String deploymentName) throws Exception {
		AS71Manager manager = new AS71Manager(details);
		try {
			JBoss7DeploymentState result = manager.getDeploymentStateSafe(deploymentName);
			return result;
		} finally {
			manager.dispose();
		}
	}
	
	public JBoss7ServerState getServerState(IAS7ManagementDetails details) throws Exception {
		AS71Manager manager = new AS71Manager(details);
		try {
			JBoss7ServerState state = manager.getServerState();
			return state;
		} finally {
			manager.dispose();
		}
	}

	public boolean isRunning(IAS7ManagementDetails details) throws Exception {
		AS71Manager manager = new AS71Manager(details);
		try {
			boolean ret = manager.isRunning();
			return ret;
		} finally {
			manager.dispose();
		}
	}

	public void stop(IAS7ManagementDetails details) throws Exception {
		AS71Manager manager = new AS71Manager(details);
		try {
			manager.stopServer();
		} finally {
			manager.dispose();
		}
	}

    public String execute(IAS7ManagementDetails details, String request) throws Exception {
        AS71Manager manager = new AS71Manager(details);
        try {
            return manager.execute(ModelNode.fromJSONString(request)).toJSONString(true);
        } finally {
            manager.dispose();
        }
    }

    @Override
	public void dispose() {
	}

}
