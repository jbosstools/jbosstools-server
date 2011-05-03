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
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.launch.AbstractJBossLaunchConfigType;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

public class JBossRuntimeLaunchConfigBuilder {

	private ILaunchConfigurationWorkingCopy launchConfig;
	private IJBossServerRuntime jbossRuntime;

	public JBossRuntimeLaunchConfigBuilder(ILaunchConfigurationWorkingCopy launchConfig, IJBossServerRuntime runtime) {
		this.launchConfig = launchConfig;
		this.jbossRuntime = runtime;
	}

	public JBossRuntimeLaunchConfigBuilder setVmContainer() {
		IVMInstall vmInstall = jbossRuntime.getVM();
		if (vmInstall != null) {
			setVmContainer(JavaRuntime.newJREContainerPath(vmInstall).toPortableString());
		}
		return this;
	}

	public JBossRuntimeLaunchConfigBuilder setVmContainer(String vmPath) {
		launchConfig.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, vmPath);
		return this;
	}

	public JBossRuntimeLaunchConfigBuilder setDefaultArguments() {
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				jbossRuntime.getDefaultRunArgs());
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
				jbossRuntime.getDefaultRunVMArgs());
		return this;
	}

	public JBossRuntimeLaunchConfigBuilder setClassPath(IServer server) throws CoreException {
		return setClassPath(JBoss7RuntimeClasspathUtil.getClasspath(server, jbossRuntime.getVM()));
	}

	public JBossRuntimeLaunchConfigBuilder setClassPath(List<String> entries) throws CoreException {
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER,
				JBoss7ServerBehavior.DEFAULT_CP_PROVIDER_ID);
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, entries);
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
		return this;
	}

	public JBossRuntimeLaunchConfigBuilder setMainType(String mainType) {
		launchConfig.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainType);
		return this;
	}

	public JBossRuntimeLaunchConfigBuilder setWorkingDirectory(IRuntime runtime) {
		setWorkingDirectory(runtime.getLocation().append(IJBossRuntimeResourceConstants.BIN).toString());
		return this;
	}

	public JBossRuntimeLaunchConfigBuilder setWorkingDirectory(String directory) {
		launchConfig.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, directory);
		return this;
	}

	public void setServerId(IServer server) {
		launchConfig.setAttribute(AbstractJBossLaunchConfigType.SERVER_ID, server.getId());
	}
}
