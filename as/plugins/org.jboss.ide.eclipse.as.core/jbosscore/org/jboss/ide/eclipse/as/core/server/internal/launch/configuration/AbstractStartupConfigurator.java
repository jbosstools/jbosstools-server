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
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

/**
 * @author André Dietisheim
 */
public abstract class AbstractStartupConfigurator extends AbstractLaunchConfigurator {

	private static final String DEFAULT_CP_PROVIDER_ID = "org.jboss.ide.eclipse.as.core.server.internal.launch.serverClasspathProvider"; ///$NON-NLS-1$

	public AbstractStartupConfigurator(IServer server) throws CoreException {
		super(server);
	}

	@Override
	protected void doConfigure(ILaunchConfigurationWorkingCopy launchConfig, JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException {
		JBossLaunchConfigProperties.setProgramArguments(getDefaultProgramArguments(jbossServer, jbossRuntime), launchConfig);
		JBossLaunchConfigProperties.setHost(getHost(jbossServer, jbossRuntime), launchConfig);
		JBossLaunchConfigProperties.setConfig(getServerConfig(jbossRuntime), launchConfig);
		JBossLaunchConfigProperties.setServerHome(getServerHome(jbossRuntime), jbossRuntime, launchConfig);
		JBossLaunchConfigProperties.setVmArguments(getDefaultVMArguments(jbossRuntime), launchConfig);
		JBossLaunchConfigProperties.setJreContainer(getJreContainerPath(jbossRuntime), launchConfig);
		JBossLaunchConfigProperties.setEndorsedDir(getEndorsedDir(jbossRuntime), launchConfig);
		JBossLaunchConfigProperties.setMainType(getMainType(), launchConfig);
		JBossLaunchConfigProperties.setWorkingDirectory(getWorkingDirectory(jbossServer, jbossRuntime), launchConfig);
		JBossLaunchConfigProperties.setEnvironmentVariables(getEnvironmentVariables(jbossRuntime), launchConfig);
		JBossLaunchConfigProperties.setClasspathProvider(getClasspathProvider(), launchConfig);
		JBossLaunchConfigProperties.setClasspath(getClasspath(jbossServer, jbossRuntime, JBossLaunchConfigProperties.getClasspath(launchConfig)), launchConfig);
		JBossLaunchConfigProperties.setUseDefaultClassPath(isUseDefaultClasspath(), launchConfig);
		JBossLaunchConfigProperties.setServerId(getServerId(jbossServer), launchConfig);
	}


	protected abstract String getEndorsedDir(IJBossServerRuntime runtime);

	protected abstract String getServerConfig(IJBossServerRuntime runtime);

	protected abstract String getServerHome(IJBossServerRuntime runtime);

	private void setServerHome(String serverHome, IJBossServerRuntime runtime,
			ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if (!isCustomConfigLocation(runtime)) {
			JBossLaunchConfigProperties.setServerHome(serverHome, runtime, launchConfig);
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