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
package org.jboss.ide.eclipse.as.internal.management.eap61plus;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.IncrementalDeploymentManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;

/**
 * @author Rob Stryker
 */
public class EAP61PlusManagerService implements IJBoss7ManagerService {

	public void init() throws JBoss7ManangerException {
	}

	/**
	 * Add a deployment but do not deploy it
	 */
	public IJBoss7DeploymentResult addDeployment(IAS7ManagementDetails details, String deploymentName,
			File file, IProgressMonitor monitor) throws JBoss7ManangerException {
		EAP61PlusManager manager = new EAP61PlusManager(details);
		try {
			IJBoss7DeploymentResult result = manager.add(deploymentName, file);
			result.getStatus();
			return result;
		} finally {
			manager.dispose();
		}
	}

	/**
	 * Remove a deployment which has been undeployed
	 */
	public IJBoss7DeploymentResult removeDeployment(IAS7ManagementDetails details, String deploymentName,
			IProgressMonitor monitor) throws JBoss7ManangerException {
		EAP61PlusManager manager = new EAP61PlusManager(details);
		try {
			IJBoss7DeploymentResult result = manager.remove(deploymentName);
			result.getStatus();
			return result;
		} finally {
			manager.dispose();
		}
	}

	/**
	 * replace a deployment
	 */
	public IJBoss7DeploymentResult replaceDeployment(IAS7ManagementDetails details, String deploymentName,
			File file, IProgressMonitor monitor) throws JBoss7ManangerException {
		EAP61PlusManager manager = new EAP61PlusManager(details);
		try {
			IJBoss7DeploymentResult result = manager.replace(deploymentName, file);
			result.getStatus();
			return result;
		} finally {
			manager.dispose();
		}
	}

	
	/* 
	 * This asynch method does not really work. 
	 * They dispose the manager before the result has come through
	 */
	public IJBoss7DeploymentResult deployAsync(IAS7ManagementDetails details, String deploymentName,
			File file, boolean add, IProgressMonitor monitor) throws JBoss7ManangerException {
		EAP61PlusManager manager = new EAP61PlusManager(details);
		try {
			IJBoss7DeploymentResult result = manager.deploy(deploymentName, file, add);
			return result;
		} finally {
			manager.dispose();
		}
	}
	/* 
	 * This asynch method does not really work. 
	 * They dispose the manager before the result has come through
	 */
	public IJBoss7DeploymentResult undeployAsync(IAS7ManagementDetails details, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws JBoss7ManangerException {
		EAP61PlusManager manager = new EAP61PlusManager(details);
		try {
			IJBoss7DeploymentResult result = manager.undeploy(deploymentName, removeFile);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public IJBoss7DeploymentResult deploySync(IAS7ManagementDetails details, String deploymentName,
			File file, boolean add, IProgressMonitor monitor) throws JBoss7ManangerException {
		EAP61PlusManager manager = new EAP61PlusManager(details);
		try {
			IJBoss7DeploymentResult result = manager.deploySync(deploymentName, file, add, monitor);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public IJBoss7DeploymentResult undeploySync(IAS7ManagementDetails details, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws JBoss7ManangerException {
		EAP61PlusManager manager = new EAP61PlusManager(details);
		try {
			IJBoss7DeploymentResult result = manager.undeploySync(deploymentName, removeFile, monitor);
			return result;
		} finally {
			manager.dispose();
		}
	}

	public JBoss7DeploymentState getDeploymentState(IAS7ManagementDetails details, String deploymentName) throws JBoss7ManangerException {
		EAP61PlusManager manager = new EAP61PlusManager(details);
		try {
			JBoss7DeploymentState result = manager.getDeploymentStateSafe(deploymentName);
			return result;
		} finally {
			manager.dispose();
		}
	}
	
	public JBoss7ServerState getServerState(IAS7ManagementDetails details) throws JBoss7ManangerException {
		EAP61PlusManager manager = new EAP61PlusManager(details);
		try {
			JBoss7ServerState state = manager.getServerState();
			return state;
		} finally {
			manager.dispose();
		}
	}

	public boolean isRunning(IAS7ManagementDetails details) throws JBoss7ManangerException {
		EAP61PlusManager manager = new EAP61PlusManager(details);
		try {
			boolean ret = manager.isRunning();
			return ret;
		} finally {
			manager.dispose();
		}
	}

	public void stop(IAS7ManagementDetails details) throws JBoss7ManangerException {
		EAP61PlusManager manager = new EAP61PlusManager(details);
		try {
			manager.stopServer();
		} finally {
			manager.dispose();
		}
	}

    public String execute(IAS7ManagementDetails details, String request) throws JBoss7ManangerException {
        EAP61PlusManager manager = new EAP61PlusManager(details);
        try {
            return manager.execute(ModelNode.fromJSONString(request)).toJSONString(true);
        } finally {
            manager.dispose();
        }
    }

    @Override
	public void dispose() {
	}

	@Override
	public boolean supportsIncrementalDeployment() {
		return this instanceof IncrementalDeploymentManagerService;
	}
}
