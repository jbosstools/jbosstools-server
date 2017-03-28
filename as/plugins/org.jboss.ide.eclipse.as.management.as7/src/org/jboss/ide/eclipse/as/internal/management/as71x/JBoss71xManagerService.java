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
package org.jboss.ide.eclipse.as.internal.management.as71x;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.IncrementalDeploymentManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.service.DelegatingDetails;
import org.jboss.ide.eclipse.as.management.core.service.DelegatingManagerService;


/**
 * This class uses the wildfly management bundle to provide the jars
 * for communication with as7.1.0 or greater, and eap6.x. 
 * 
 * It overrides the timeouts for deployment of files, since 
 * the default timeout of the wf jars does not seem to work
 * when sending a large file to as7.1 or greater.  
 * 
 */
public class JBoss71xManagerService extends DelegatingManagerService {
	public JBoss71xManagerService() {
		super();
	}
	
	@Override
	protected String getDelegateServiceId() {
		return IJBoss7ManagerService.WILDFLY_VERSION_900;
	}

	
	/*
	 * Ensure the default timeout is larger for large files based on an 
	 * average expected rate after multiple tests, with some extra room. 
	 */
	private IAS7ManagementDetails getLargeTimeoutDetails(IAS7ManagementDetails details, File file) {
		Object timeout = details.getProperty(IAS7ManagementDetails.PROPERTY_TIMEOUT);
		if( timeout == null ) {
			int l = (int)(file.length() / 2500);
			l = l < 20000 ? 20000 : l;
			details = new DelegatingDetails(details);
			((DelegatingDetails)details).overrideProperty(IAS7ManagementDetails.PROPERTY_TIMEOUT, l);
		}
		return details;
	}
	
	public IJBoss7DeploymentResult deploySync(IAS7ManagementDetails details, String deploymentName,
			File file, boolean add, IProgressMonitor monitor) throws JBoss7ManangerException {
		IAS7ManagementDetails details2 = getLargeTimeoutDetails(details, file);
		return getDelegateService().deploySync(details2, deploymentName, file, add, monitor);
	}
	@Override
	public boolean supportsIncrementalDeployment() {
		return this instanceof IncrementalDeploymentManagerService;
	}
}
