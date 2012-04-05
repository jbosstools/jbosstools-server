/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;

public class ServerExtendedProperties {
	protected IServerAttributes server;
	protected IRuntime runtime;
	public ServerExtendedProperties(IAdaptable adaptable) {
		if( adaptable instanceof IServerAttributes) {
			this.server = (IServer)adaptable;
			this.runtime = server.getRuntime();
		} else if( adaptable instanceof IRuntime){
			this.runtime = (IRuntime)adaptable;
		}
	}

	public String getNewFilesetDefaultRootFolder() {
		return "servers/${jboss_config}"; //$NON-NLS-1$
	}
	
	public static final int JMX_NULL_PROVIDER = -1;
	public static final int JMX_DEFAULT_PROVIDER = 0;
	public static final int JMX_OVER_JNDI_PROVIDER = 1;
	
	/**
	 * Server types that have JMX_OVER_AS_MANAGEMENT_PORT as their jmx type
	 * are expected to implement IManagementPortProvider, so the jmx
	 * knows what port to check. Any servers that use JMX_OVER_AS_MANAGEMENT_PORT
	 * but do not implement the interface will have a default port of 9999 used.
	 */
	public static final int JMX_OVER_AS_MANAGEMENT_PORT_PROVIDER = 2;
	public int getJMXProviderType() {
		return JMX_NULL_PROVIDER;
	}
	
	public boolean hasWelcomePage() {
		return false;
	}
	
	public String getWelcomePageUrl() {
		return null;
	}
	
	public static final int DEPLOYMENT_SCANNER_NO_SUPPORT = 1;
	public static final int DEPLOYMENT_SCANNER_JMX_SUPPORT = 2;
	public static final int DEPLOYMENT_SCANNER_AS7_MANAGEMENT_SUPPORT = 3;
	
	public int getMultipleDeployFolderSupport() {
		return DEPLOYMENT_SCANNER_NO_SUPPORT;
	}
	
	public IStatus verifyServerStructure() {
		return Status.OK_STATUS;
	}
	
	public boolean canVerifyRemoteModuleState() {
		return false;
	}
	
	public IServerModuleStateVerifier getModuleStateVerifier() {
		return null;
	}
}
