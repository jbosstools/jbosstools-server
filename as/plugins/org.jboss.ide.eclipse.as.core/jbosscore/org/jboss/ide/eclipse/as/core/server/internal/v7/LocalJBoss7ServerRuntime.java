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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

public class LocalJBoss7ServerRuntime extends LocalJBossServerRuntime implements IJBossRuntimeConstants {
	public static final String CONFIG_FILE = "org.jboss.ide.eclipse.as.core.server.internal.v7.CONFIG_FILE"; //$NON-NLS-1$
	public static final String CONFIG_FILE_DEFAULT = "standalone.xml"; //$NON-NLS-1$
	/**
	 * @since 2.5
	 */
	public static final String BASE_DIRECTORY = "org.jboss.ide.eclipse.as.core.server.internal.v7.BASE_DIRECTORY"; //$NON-NLS-1$
	
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
	
	/**
	 * @since 2.5
	 */
	public String getConfigurationFileFullPath() {
		String configFile = getConfigurationFile();
		if( new Path(configFile).isAbsolute())
			return configFile;
		
		// If the file is not absolute, it's relative to the configuration folder
		IPath configFolder = new Path(getBaseDirectory()).append(IJBossRuntimeResourceConstants.CONFIGURATION);
		return configFolder.append(configFile).toFile().getAbsolutePath();
	}

	
	/**
	 * This method is technically internal and is not part of any interface
	 * Gets an absolute path for the server's base directory
	 * 
	 * @since 2.5
	 */
	public String getBaseDirectory() {
		String bd = getAttribute(BASE_DIRECTORY, IJBossRuntimeResourceConstants.AS7_STANDALONE);
		Path p = new Path(bd);
		if( p.isAbsolute() )
			return p.toFile().getAbsolutePath();
		return getRuntime().getLocation().append(p).toFile().getAbsolutePath();
	}
	
	/**
	 * Set a base directory, which may either be relative to the server home, 
	 * or, a file-system absolute path. Setting the value of 'null' will
	 * restore it to the default of 'standalone'
	 * 
	 * @since 2.5
	 */
	public void setBaseDirectory(String s) {
		setAttribute(BASE_DIRECTORY, s);
	}

	
	
	// Overrides of as6-and-below's notion of configuration
	@Override
	public String getConfigLocation() {
		return new Path(getBaseDirectory()).append(IJBossRuntimeResourceConstants.CONFIGURATION).toFile().getAbsolutePath();
	}
	
	@Override
	public String getJBossConfiguration() {
		return ""; //$NON-NLS-1$
	}
}
