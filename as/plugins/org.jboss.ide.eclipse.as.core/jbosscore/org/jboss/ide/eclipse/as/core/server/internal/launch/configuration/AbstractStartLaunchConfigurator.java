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

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.JavaUtils;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.LaunchConfiguratorWithOverrides;

/**
 * @author André Dietisheim
 */
public abstract class AbstractStartLaunchConfigurator extends LaunchConfiguratorWithOverrides {

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

	protected JBossServer getJBossServer() {
		return (JBossServer)server.loadAdapter(JBossServer.class, new NullProgressMonitor());
	}
	protected IJBossServerRuntime getJBossRuntime() throws CoreException {
		return RuntimeUtils.checkedGetJBossServerRuntime(runtime);
	}
	
	@Override
	protected void doConfigure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		JBossServer jbossServer = getJBossServer();
		IJBossServerRuntime jbossRuntime = getJBossRuntime();
		getProperties().setProgramArguments(getDefaultProgramArguments(), launchConfig);
		getProperties().setHost(getHost(jbossServer, jbossRuntime), launchConfig);
		getProperties().setConfig(getServerConfig(jbossRuntime), launchConfig);
		getProperties().setServerHome(getServerHome(jbossRuntime), jbossRuntime, launchConfig);
		getProperties().setServerFlag(getSupportsServerFlag(jbossRuntime), jbossRuntime, launchConfig);
		getProperties().setVmArguments(getDefaultVMArguments(jbossRuntime), launchConfig);
		getProperties().setJreContainer(getJreContainerPath(jbossRuntime), launchConfig);
		getProperties().setEndorsedDir(getEndorsedDir(jbossRuntime), launchConfig);
		getProperties().setMainType(getMainType(), launchConfig);
		getProperties().setWorkingDirectory(getWorkingDirectory(), launchConfig);
		getProperties().setEnvironmentVariables(getEnvironmentVariables(), launchConfig);
		getProperties().setClasspathProvider(getClasspathProvider(), launchConfig);
		getProperties().setClasspath(getClasspath(getProperties().getClasspath(launchConfig)), launchConfig);
		getProperties().setUseDefaultClassPath(isUseDefaultClasspath(), launchConfig);
		getProperties().setServerId(getServerId(server), launchConfig);
	}

	@Override
	protected void doOverrides(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		JBossServer jbossServer = getJBossServer();
		IJBossServerRuntime jbossRuntime = getJBossRuntime();
		
		getProperties().setHost(getHost(jbossServer, jbossRuntime), launchConfig);
		getProperties().setConfig(getServerConfig(jbossRuntime), launchConfig);
		getProperties().setServerHome(getServerHome(jbossRuntime), jbossRuntime, launchConfig);
		getProperties().setServerFlag(getSupportsServerFlag(jbossRuntime), jbossRuntime, launchConfig);
		getProperties().setJreContainer(getJreContainerPath(jbossRuntime), launchConfig);
		getProperties().setEndorsedDir(getEndorsedDir(jbossRuntime), launchConfig);
		getProperties().setJavaLibPath(getJavaLibraryPath(jbossRuntime), launchConfig);
		getProperties().setWorkingDirectory(getWorkingDirectory(), launchConfig);
		getProperties().setClasspathProvider(getClasspathProvider(), launchConfig);
		getProperties().setClasspath(getClasspath(getProperties().getClasspath(launchConfig)), launchConfig);
		getProperties().setUseDefaultClassPath(isUseDefaultClasspath(), launchConfig);
		getProperties().setServerId(getServerId(server), launchConfig);
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
	
	protected abstract String getDefaultProgramArguments() throws CoreException;

	protected abstract String getMainType();

	protected abstract String getWorkingDirectory()
			throws CoreException;
	protected abstract List<String> getClasspath(List<String> currentClasspath) throws CoreException;

	protected boolean isUseDefaultClasspath() {
		return false;
	}

	protected String getClasspathProvider() {
		return DEFAULT_CP_PROVIDER_ID;
	}
	
	protected JBossExtendedProperties getExtendedProperties() {
		return (JBossExtendedProperties)getJBossServer().getServer().getAdapter(JBossExtendedProperties.class);
	}

	protected Map<String, String> getEnvironmentVariables() {
		return getExtendedProperties().getDefaultLaunchArguments().getDefaultRunEnvVars();
	}

	protected String getDefaultVMArguments(IJBossServerRuntime runtime) {
		return getExtendedProperties().getDefaultLaunchArguments().getStartDefaultVMArgs();
	}
	
	
	protected String getJreContainerPath(IJBossServerRuntime runtime) {
		IVMInstall vmInstall = runtime.getVM();
		if (vmInstall == null) {
			return null;
		}
		return JavaRuntime.newJREContainerPath(vmInstall).toPortableString();
	}

	protected abstract String getHost(JBossServer server, IJBossServerRuntime runtime);
}