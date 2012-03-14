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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

/**
 *
 */
public class JBossAS7ExtendedProperties extends JBossExtendedProperties {
	public JBossAS7ExtendedProperties(IAdaptable obj) {
		super(obj);
	}

	public String getNewFilesetDefaultRootFolder() {
		return IJBossRuntimeResourceConstants.AS7_STANDALONE + "/" + IJBossRuntimeResourceConstants.CONFIGURATION; //$NON-NLS-1$
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

	public String getVerifyStructureErrorMessage() throws CoreException {
		if( server.getRuntime() == null ) 
			return NLS.bind(Messages.ServerMissingRuntime, server.getName());
		if( !server.getRuntime().getLocation().toFile().exists())
			return NLS.bind(Messages.RuntimeFolderDoesNotExist, server.getRuntime().getLocation().toOSString());
		IRuntime rt = server.getRuntime();
		LocalJBoss7ServerRuntime rt2 = (LocalJBoss7ServerRuntime)rt.loadAdapter(LocalJBoss7ServerRuntime.class, null);
		String cfile = rt2.getConfigurationFile();
		IPath cFilePath = rt.getLocation().append(IJBossRuntimeResourceConstants.AS7_STANDALONE)
				.append(IJBossRuntimeResourceConstants.CONFIGURATION).append(cfile);
		if( !cFilePath.toFile().exists())
			return NLS.bind(Messages.JBossAS7ConfigurationFileDoesNotExist, cFilePath.toOSString());
		return null;
	}

}
