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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.launch.AbstractJBossLaunchConfigType;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;

public class JBoss7RuntimeLaunchConfigurator {

	private static final String DEFAULTS_SET = "DEFAULTS_SET"; //$NON-NLS-1$
	private ILaunchConfigurationWorkingCopy launchConfig;

	public JBoss7RuntimeLaunchConfigurator(ILaunchConfigurationWorkingCopy launchConfig) {
		this.launchConfig = launchConfig;
	}

	public void apply(IServer server) throws CoreException {
		if (!areDefaultsSet()) {
			IRuntime runtime = server.getRuntime();
			IJBossServerRuntime jbossRuntime = RuntimeUtils.getJBossServerRuntime(server);
			setVmContainer(jbossRuntime)
					.setClassPath(server, jbossRuntime)
					.setDefaultArguments(jbossRuntime)
					.setMainType(IJBossRuntimeConstants.START7_MAIN_TYPE)
					.setWorkingDirectory(runtime)
					.setServerId(server)
					.setDefaultsSet();
		}
	}

	private JBoss7RuntimeLaunchConfigurator setVmContainer(IJBossServerRuntime jbossRuntime) {
		IVMInstall vmInstall = jbossRuntime.getVM();
		if (vmInstall != null) {
			setVmContainer(JavaRuntime.newJREContainerPath(vmInstall).toPortableString());
		}
		return this;
	}

	private JBoss7RuntimeLaunchConfigurator setVmContainer(String vmPath) {
		launchConfig.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, vmPath);
		return this;
	}

	private JBoss7RuntimeLaunchConfigurator setDefaultArguments(IJBossServerRuntime jbossRuntime) {
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				jbossRuntime.getDefaultRunArgs());
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
				jbossRuntime.getDefaultRunVMArgs());
		return this;
	}

	private JBoss7RuntimeLaunchConfigurator setClassPath(IServer server, IJBossServerRuntime jbossRuntime)
			throws CoreException {
		return setClassPath(getClasspath(server, jbossRuntime.getVM()));
	}

	private List<String> getClasspath(IServer server, IVMInstall vmInstall) throws CoreException {
		List<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		classpath.add(LaunchConfigUtils.getModulesClasspathEntry(server));
		LaunchConfigUtils.addJREEntry(vmInstall, classpath);
		List<String> runtimeClassPaths = LaunchConfigUtils.toStrings(classpath);
		return runtimeClassPaths;
	}

	private JBoss7RuntimeLaunchConfigurator setClassPath(List<String> entries) throws CoreException {
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER,
				JBoss7ServerBehavior.DEFAULT_CP_PROVIDER_ID);
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, entries);
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
		return this;
	}

	private JBoss7RuntimeLaunchConfigurator setMainType(String mainType) {
		launchConfig.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainType);
		return this;
	}

	private JBoss7RuntimeLaunchConfigurator setWorkingDirectory(IRuntime runtime) {
		setWorkingDirectory(runtime.getLocation().append(IJBossRuntimeResourceConstants.BIN).toString());
		return this;
	}

	private JBoss7RuntimeLaunchConfigurator setWorkingDirectory(String directory) {
		launchConfig.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, directory);
		return this;
	}

	private JBoss7RuntimeLaunchConfigurator setServerId(IServer server) {
		launchConfig.setAttribute(AbstractJBossLaunchConfigType.SERVER_ID, server.getId());
		return this;
	}

	private boolean areDefaultsSet() throws CoreException {
		return launchConfig.hasAttribute(DEFAULTS_SET);
	}

	private void setDefaultsSet() {
		launchConfig.setAttribute(DEFAULTS_SET, true);
	}
}