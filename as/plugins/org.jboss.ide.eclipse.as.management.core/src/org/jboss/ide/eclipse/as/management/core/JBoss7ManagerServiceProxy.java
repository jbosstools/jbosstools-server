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
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author André Dietisheim
 */
public class JBoss7ManagerServiceProxy extends ServiceTracker<IJBoss7ManagerService, IJBoss7ManagerService>
		implements IJBoss7ManagerService, IncrementalDeploymentManagerService {

	private String serviceVersion;
	public JBoss7ManagerServiceProxy(BundleContext context, String serviceVersion) throws InvalidSyntaxException {
		super(
				context,
				context.createFilter(MessageFormat
						.format("(&(objectClass={0})(as.version={1}))", IJBoss7ManagerService.class.getCanonicalName(), serviceVersion)), null); //$NON-NLS-1$
		this.serviceVersion = serviceVersion;
	}
	
	public String getServiceVersion() {
		return serviceVersion;
	}

	public void init() throws JBoss7ManangerException {
		checkedGetService().init();
	}

	public IJBoss7DeploymentResult deployAsync(IAS7ManagementDetails details, String deploymentName, 
			File file, boolean add,
			IProgressMonitor monitor) throws JBoss7ManangerException {
		return checkedGetService().deployAsync(details, deploymentName, file, add, monitor);
	}

	public IJBoss7DeploymentResult deploySync(IAS7ManagementDetails details, String deploymentName,
			File file, boolean add,
			IProgressMonitor monitor) throws JBoss7ManangerException {
		return checkedGetService().deploySync(details, deploymentName, file, add, monitor);
	}

	public IJBoss7DeploymentResult undeployAsync(IAS7ManagementDetails details, String deploymentName, boolean removeFile,
			IProgressMonitor monitor) throws JBoss7ManangerException {
		return checkedGetService().undeployAsync(details, deploymentName, removeFile, monitor);
	}

	public IJBoss7DeploymentResult undeploySync(IAS7ManagementDetails details, String deploymentName, boolean removeFile,
			IProgressMonitor monitor) throws JBoss7ManangerException {
		return checkedGetService().undeploySync(details, deploymentName, removeFile, monitor);
	}

	public JBoss7DeploymentState getDeploymentState(IAS7ManagementDetails details, String deploymentName) throws JBoss7ManangerException {
		return checkedGetService().getDeploymentState(details, deploymentName);
	}

	public JBoss7ServerState getServerState(IAS7ManagementDetails details) throws JBoss7ManangerException {
		return checkedGetService().getServerState(details);
	}

	public boolean isRunning(IAS7ManagementDetails details) throws JBoss7ManangerException {
		try {
			return checkedGetService().isRunning(details);
		} catch (Exception e) {
			return false;
		}
	}

	public void stop(IAS7ManagementDetails details) throws JBoss7ManangerException {
		checkedGetService().stop(details);
	}

    public String execute(IAS7ManagementDetails details, String request) throws JBoss7ManangerException {
        return checkedGetService().execute(details, request);
    }

    private IJBoss7ManagerService checkedGetService() throws JBoss7ManangerException {
		IJBoss7ManagerService service = getService();
		if (service == null) {
			throw new JBoss7ManangerException("Could not acquire JBoss Management service version " + serviceVersion); //$NON-NLS-1$
		}
		return service;
	}

	public void dispose() {
		IJBoss7ManagerService service = getService();
		if( service != null )
			service.dispose();
		close();
	}

	public IJBoss7DeploymentResult addDeployment(IAS7ManagementDetails details,
			String deploymentName, File file, IProgressMonitor monitor)
			throws JBoss7ManangerException {
		return checkedGetService().addDeployment(details, deploymentName, file, monitor);
	}

	public IJBoss7DeploymentResult removeDeployment(
			IAS7ManagementDetails details, String deploymentName,
			IProgressMonitor monitor) throws JBoss7ManangerException {
		return checkedGetService().removeDeployment(details, deploymentName, monitor);
	}

	public IJBoss7DeploymentResult replaceDeployment(
			IAS7ManagementDetails details, String deploymentName, File file,
			IProgressMonitor monitor) throws JBoss7ManangerException {
		return checkedGetService().replaceDeployment(details, deploymentName, file, monitor);
	}

	@Override
	public boolean supportsIncrementalDeployment() {
		return checkedGetService().supportsIncrementalDeployment();
	}

	@Override
	public IJBoss7DeploymentResult incrementalPublish(IAS7ManagementDetails details, String deploymentName,
			IncrementalManagementModel model, boolean redeploy, IProgressMonitor monitor)
			throws JBoss7ManangerException {
		IJBoss7ManagerService serv = checkedGetService();
		if( serv instanceof IncrementalDeploymentManagerService) {
			return ((IncrementalDeploymentManagerService)serv).incrementalPublish(details, deploymentName, model, redeploy, monitor);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public IJBoss7DeploymentResult deploySync(IAS7ManagementDetails details, String deploymentName, File file,
			boolean add, String[] explodePaths, IProgressMonitor monitor) throws JBoss7ManangerException {
		IJBoss7ManagerService serv = checkedGetService();
		if( serv instanceof IncrementalDeploymentManagerService) {
			return ((IncrementalDeploymentManagerService)serv).deploySync(details, deploymentName, file, add, explodePaths, monitor);
		}
		throw new UnsupportedOperationException();
	}

}
