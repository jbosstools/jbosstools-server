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
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

public class LocalJBoss7ServerRuntime extends LocalJBossServerRuntime implements IJBossRuntimeConstants {
	public static final String CONFIG_FILE = "org.jboss.ide.eclipse.as.core.server.internal.v7.CONFIG_FILE"; //$NON-NLS-1$
	public static final String CONFIG_FILE_DEFAULT = "standalone.xml"; //$NON-NLS-1$
	
	@Override
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		// clear as6 default property
		setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, ""); //$NON-NLS-1$
	}

	@Override
	public IStatus validate() {
		return Status.OK_STATUS;
	}

	@Override
	public String getDefaultRunArgs() {
		return getDefaultRunArgs(getRuntime().getLocation());
	} 

	@Override
	public String getDefaultRunArgs(IPath serverHome) {
		return getExtendedProperties().getDefaultLaunchArguments().getStartDefaultProgramArgs();
	}
		
	@Override
	public String getDefaultRunVMArgs(IPath serverHome) {
		return getExtendedProperties().getDefaultLaunchArguments().getStartDefaultVMArgs();
	}

	@Override
	public String getDefaultRunVMArgs() {
		IPath loc = getRuntime().getLocation();
		return getDefaultRunVMArgs(loc);
	}
	
	public String getConfigurationFile() {
		return getAttribute(CONFIG_FILE, CONFIG_FILE_DEFAULT);
	}
	public void setConfigurationFile(String file) {
		setAttribute(CONFIG_FILE, file);
	}
	
	// Overrides of as6-and-below's notion of configuration
	@Override
	public String getConfigLocation() {
		 IPath config = getRuntime().getLocation().append(IJBossRuntimeResourceConstants.AS7_STANDALONE)
			.append(IJBossRuntimeResourceConstants.CONFIGURATION);
		 return config.toFile().getAbsolutePath();
	}
	
	@Override
	public String getJBossConfiguration() {
		return ""; //$NON-NLS-1$
	}
}
