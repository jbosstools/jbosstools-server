/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.management.core.service;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.management.core.AS7ManagementActivator;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;


/**
 * This class represents a management service
 * that delegates to some other service... for
 * example, a service of version 8.0.0 delegating to 
 * service 9.0.0 since the 9.0.0 jars work for the 8.0.0 case
 */
public abstract class DelegatingManagerService implements IJBoss7ManagerService {
	protected boolean hasLoggedError = false;
	
	public DelegatingManagerService() {
	}

	protected abstract String getDelegateServiceId();
	
	protected IJBoss7ManagerService getDelegateService() {
		IJBoss7ManagerService service = JBoss7ManagerUtil.getManagerService(getDelegateServiceId());
		return service;
	}
	
	public void init() throws JBoss7ManangerException {
		checkDelegate();
		getDelegateService().init();
	}
	
	/**
	 * Add a deployment but do not deploy it
	 */
	public IJBoss7DeploymentResult addDeployment(IAS7ManagementDetails details, String deploymentName,
			File file, IProgressMonitor monitor) throws JBoss7ManangerException {
		checkDelegate();
		return getDelegateService().addDeployment(details, deploymentName, file, monitor);
	}

	/**
	 * Remove a deployment which has been undeployed
	 */
	public IJBoss7DeploymentResult removeDeployment(IAS7ManagementDetails details, String deploymentName,
			IProgressMonitor monitor) throws JBoss7ManangerException {
		checkDelegate();
		return getDelegateService().removeDeployment(details, deploymentName, monitor);
	}

	/**
	 * replace a deployment
	 */
	public IJBoss7DeploymentResult replaceDeployment(IAS7ManagementDetails details, String deploymentName,
			File file, IProgressMonitor monitor) throws JBoss7ManangerException {
		checkDelegate();
		return getDelegateService().replaceDeployment(details, deploymentName, file, monitor);
	}

	
	/* 
	 * This asynch method does not really work. 
	 * They dispose the manager before the result has come through
	 */
	public IJBoss7DeploymentResult deployAsync(IAS7ManagementDetails details, String deploymentName,
			File file, boolean add, IProgressMonitor monitor) throws JBoss7ManangerException {
		checkDelegate();
		return getDelegateService().deployAsync(details, deploymentName, file, add, monitor);
	}
	/* 
	 * This asynch method does not really work. 
	 * They dispose the manager before the result has come through
	 */
	public IJBoss7DeploymentResult undeployAsync(IAS7ManagementDetails details, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws JBoss7ManangerException {
		checkDelegate();
		return getDelegateService().undeployAsync(details, deploymentName, removeFile, monitor);
	}
	
	public IJBoss7DeploymentResult deploySync(IAS7ManagementDetails details, String deploymentName,
			File file, boolean add, IProgressMonitor monitor) throws JBoss7ManangerException {
		checkDelegate();
		return getDelegateService().deploySync(details, deploymentName, file, add, monitor);
	}

	public IJBoss7DeploymentResult undeploySync(IAS7ManagementDetails details, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws JBoss7ManangerException {
		checkDelegate();
		return getDelegateService().undeploySync(details, deploymentName, removeFile, monitor);
	}

	public JBoss7DeploymentState getDeploymentState(IAS7ManagementDetails details, String deploymentName) throws JBoss7ManangerException {
		checkDelegate();
		return getDelegateService().getDeploymentState(details, deploymentName);
	}
	
	public JBoss7ServerState getServerState(IAS7ManagementDetails details) throws JBoss7ManangerException {
		checkDelegate();
		return getDelegateService().getServerState(details);
	}

	public boolean isRunning(IAS7ManagementDetails details) throws JBoss7ManangerException {
		return getDelegateService().isRunning(details);
	}

	public void stop(IAS7ManagementDetails details) throws JBoss7ManangerException {
		checkDelegate();
		getDelegateService().stop(details);
	}

    public String execute(IAS7ManagementDetails details, String request) throws JBoss7ManangerException {
		checkDelegate();
    	return getDelegateService().execute(details, request);
    }

	public void dispose() {
		checkDelegate();
		getDelegateService().dispose();
	}
	
	protected void checkDelegate() throws JBoss7ManangerException {
		IJBoss7ManagerService del = getDelegateService();
		if( del == null ) {
			JBoss7ManangerException ex = new JBoss7ManangerException("Unable to locate delegate management service with as.version=" + getDelegateServiceId());
			if( !hasLoggedError) {
				AS7ManagementActivator.getDefault().getLog().log(new Status(IStatus.ERROR, AS7ManagementActivator.PLUGIN_ID, ex.getMessage(), ex));
				hasLoggedError = true;
			}
			throw ex;
		}
	}
}
