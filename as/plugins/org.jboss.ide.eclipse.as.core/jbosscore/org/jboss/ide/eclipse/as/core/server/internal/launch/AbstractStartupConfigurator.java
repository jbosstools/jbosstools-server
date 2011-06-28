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
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setClasspath;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setClasspathProvider;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setConfig;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setEndorsedDir;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setEnvironmentVariables;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setHost;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setJreContainer;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setMainType;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setProgramArguments;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setServerId;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setUseDefaultClassPath;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setVmArguments;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.setWorkingDirectory;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public abstract class AbstractStartupConfigurator implements ILaunchConfigConfigurator {

	private static final String DEFAULT_CP_PROVIDER_ID = "org.jboss.ide.eclipse.as.core.server.internal.launch.serverClasspathProvider"; ///$NON-NLS-1$
	private static final String DEFAULTS_SET = "DEFAULTS_SET"; //$NON-NLS-1$

	private JBossServer jbossServer;
	private IJBossServerRuntime jbossRuntime;

	public AbstractStartupConfigurator(IServer server) throws CoreException {
		this.jbossServer = ServerConverter.checkedGetJBossServer(server);
		this.jbossRuntime = jbossServer.getRuntime();
	}

	@Override
	public void configure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if (!areDefaultsSet(launchConfig)) {
			doConfigure(launchConfig);
			setDefaultsSet(launchConfig);
		}
	}

	protected void doConfigure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		setProgramArguments(getDefaultProgramArguments(jbossServer, jbossRuntime), launchConfig);
		setHost(getHost(jbossServer, jbossRuntime), launchConfig);
		setConfig(getServerConfig(jbossRuntime), launchConfig);
		setServerHome(getServerHome(jbossRuntime), jbossRuntime, launchConfig);
		setVmArguments(getDefaultVMArguments(jbossRuntime), launchConfig);
		setJreContainer(getJreContainerPath(jbossRuntime), launchConfig);
		setEndorsedDir(getEndorsedDir(jbossRuntime), launchConfig);
		setMainType(getMainType(), launchConfig);
		setWorkingDirectory(getWorkingDirectory(jbossServer, jbossRuntime), launchConfig);
		setEnvironmentVariables(getEnvironmentVariables(jbossRuntime), launchConfig);
		setClasspathProvider(getClasspathProvider(), launchConfig);
		setClasspath(getClasspath(jbossServer, jbossRuntime, JBossRuntimeLaunchConfigUtils.getClasspath(launchConfig)), launchConfig);
		setUseDefaultClassPath(false, launchConfig);
		setServerId(getServerId(jbossServer), launchConfig);
		setDefaultsSet(launchConfig);
	}

	protected abstract String getEndorsedDir(IJBossServerRuntime runtime);

	protected abstract String getServerConfig(IJBossServerRuntime runtime);

	protected abstract String getServerHome(IJBossServerRuntime runtime);

	private void setServerHome(String serverHome, IJBossServerRuntime runtime,
			ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if (!isCustomConfigLocation(runtime)) {
			JBossRuntimeLaunchConfigUtils.setServerHome(serverHome, runtime, launchConfig);
		}
	}

	protected boolean isCustomConfigLocation(IJBossServerRuntime runtime) {
		return runtime.getConfigLocation().equals(IJBossRuntimeResourceConstants.SERVER);
	}

	protected String getClasspathProvider() {
		return DEFAULT_CP_PROVIDER_ID;
	}

	protected Map<String, String> getEnvironmentVariables(IJBossServerRuntime runtime) {
		return runtime.getDefaultRunEnvVars();
	}

	protected abstract String getDefaultProgramArguments(JBossServer server, IJBossServerRuntime runtime);

	protected abstract String getDefaultVMArguments(IJBossServerRuntime runtime);
	
	protected String getJreContainerPath(IJBossServerRuntime runtime) {
		IVMInstall vmInstall = runtime.getVM();
		if (vmInstall == null) {
			return null;
		}
		return JavaRuntime.newJREContainerPath(vmInstall).toPortableString();
	}

	protected abstract List<String> getClasspath(JBossServer server,  IJBossServerRuntime runtime, List<String> currentClasspath) throws CoreException;

	protected abstract String getMainType();

	protected abstract String getWorkingDirectory(JBossServer server, IJBossServerRuntime jbossRuntime)
			throws CoreException;

	protected String getServerId(JBossServer server) {
		return server.getServer().getId();
	}

	protected abstract String getHost(JBossServer server, IJBossServerRuntime runtime);

	private boolean areDefaultsSet(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		return launchConfig.hasAttribute(DEFAULTS_SET);
	}

	private void setDefaultsSet(ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(DEFAULTS_SET, true);
	}
}