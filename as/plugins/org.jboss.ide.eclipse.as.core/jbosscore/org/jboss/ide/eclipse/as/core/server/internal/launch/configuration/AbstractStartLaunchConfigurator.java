/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.launch.configuration;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.j2ee.project.facet.JavaUtilityProjectCreationDataModelProvider;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.JavaUtils;

/**
 * @author André Dietisheim
 */
public abstract class AbstractStartLaunchConfigurator extends AbstractLaunchConfigurator {

	private static final String DEFAULT_CP_PROVIDER_ID = "org.jboss.ide.eclipse.as.core.server.internal.launch.serverClasspathProvider"; ///$NON-NLS-1$

	public AbstractStartLaunchConfigurator(IServer server) throws CoreException {
		super(server);
	}

	private JBossLaunchConfigProperties properties = null;
	protected JBossLaunchConfigProperties getProperties() {
		if( properties == null )
			properties = createProperties();
		return properties;
	}
	
	protected JBossLaunchConfigProperties createProperties() {
		return new JBossLaunchConfigProperties();
	}
	
	@Override
	protected void doConfigure(ILaunchConfigurationWorkingCopy launchConfig, JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException {
		getProperties().setProgramArguments(getDefaultProgramArguments(jbossServer, jbossRuntime), launchConfig);
		getProperties().setHost(getHost(jbossServer, jbossRuntime), launchConfig);
		getProperties().setConfig(getServerConfig(jbossRuntime), launchConfig);
		getProperties().setServerHome(getServerHome(jbossRuntime), jbossRuntime, launchConfig);
		getProperties().setServerFlag(getSupportsServerFlag(jbossRuntime), jbossRuntime, launchConfig);
		getProperties().setVmArguments(getDefaultVMArguments(jbossRuntime), launchConfig);
		getProperties().setJreContainer(getJreContainerPath(jbossRuntime), launchConfig);
		getProperties().setEndorsedDir(getEndorsedDir(jbossRuntime), launchConfig);
		getProperties().setMainType(getMainType(), launchConfig);
		getProperties().setWorkingDirectory(getWorkingDirectory(jbossServer, jbossRuntime), launchConfig);
		getProperties().setEnvironmentVariables(getEnvironmentVariables(jbossRuntime), launchConfig);
		getProperties().setClasspathProvider(getClasspathProvider(), launchConfig);
		getProperties().setClasspath(getClasspath(jbossServer, jbossRuntime, getProperties().getClasspath(launchConfig)), launchConfig);
		getProperties().setUseDefaultClassPath(isUseDefaultClasspath(), launchConfig);
		getProperties().setServerId(getServerId(jbossServer), launchConfig);
	}

	@Override
	protected void doOverrides(ILaunchConfigurationWorkingCopy launchConfig, JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException {
		getProperties().setHost(getHost(jbossServer, jbossRuntime), launchConfig);
		getProperties().setConfig(getServerConfig(jbossRuntime), launchConfig);
		getProperties().setServerHome(getServerHome(jbossRuntime), jbossRuntime, launchConfig);
		getProperties().setServerFlag(getSupportsServerFlag(jbossRuntime), jbossRuntime, launchConfig);
		getProperties().setJreContainer(getJreContainerPath(jbossRuntime), launchConfig);
		getProperties().setEndorsedDir(getEndorsedDir(jbossRuntime), launchConfig);
		getProperties().setJavaLibPath(getJavaLibraryPath(jbossRuntime), launchConfig);
		getProperties().setWorkingDirectory(getWorkingDirectory(jbossServer, jbossRuntime), launchConfig);
		getProperties().setClasspathProvider(getClasspathProvider(), launchConfig);
		getProperties().setClasspath(getClasspath(jbossServer, jbossRuntime, getProperties().getClasspath(launchConfig)), launchConfig);
		getProperties().setUseDefaultClassPath(isUseDefaultClasspath(), launchConfig);
		getProperties().setServerId(getServerId(jbossServer), launchConfig);
	}	

	protected boolean getSupportsServerFlag(IJBossServerRuntime runtime) {
		IVMInstall install = runtime.getVM();
		return JavaUtils.supportsServerMode(install);
	}
	
	protected abstract String getEndorsedDir(IJBossServerRuntime runtime);

	protected abstract String getJavaLibraryPath(IJBossServerRuntime runtime);

	protected abstract String getServerConfig(IJBossServerRuntime runtime);

	protected abstract String getServerHome(IJBossServerRuntime runtime);

	protected boolean isCustomConfigLocation(IJBossServerRuntime runtime) {
		return runtime.getConfigLocation().equals(IJBossRuntimeResourceConstants.SERVER);
	}

	protected String getClasspathProvider() {
		return DEFAULT_CP_PROVIDER_ID;
	}

	protected Map<String, String> getEnvironmentVariables(IJBossServerRuntime runtime) {
		return runtime.getDefaultRunEnvVars();
	}

	protected abstract String getDefaultVMArguments(IJBossServerRuntime runtime);
	
	
	protected String getJreContainerPath(IJBossServerRuntime runtime) {
		IVMInstall vmInstall = runtime.getVM();
		if (vmInstall == null) {
			return null;
		}
		return JavaRuntime.newJREContainerPath(vmInstall).toPortableString();
	}

	protected abstract String getHost(JBossServer server, IJBossServerRuntime runtime);
}