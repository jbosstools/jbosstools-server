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

/**
 *
 */
public class JBossAS7ExtendedProperties extends JBossExtendedProperties {
	public JBossAS7ExtendedProperties(IAdaptable obj) {
		super(obj);
	}

	public String getNewFilesetDefaultRootFolder() {
		return "standalone/configuration"; //$NON-NLS-1$
	}
	
	public int getJMXProviderType() {
		return JMX_DEFAULT_PROVIDER;
	}

	public boolean runtimeSupportsBindingToAllInterfaces() {
		String version = getServerBeanLoader().getFullServerVersion();
		if( version.startsWith("7.0.1") || version.startsWith("7.0.0"))  //$NON-NLS-1$//$NON-NLS-2$
			return false;
		return true;
	}
	public int getMultipleDeployFolderSupport() {
		return DEPLOYMENT_SCANNER_AS7_MANAGEMENT_SUPPORT;
	}

}
