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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;

/**
 * @author André Dietisheim
 */
public class JBossLaunchConfigProperties {

	private static final String SERVER_ID = "server-id"; //$NON-NLS-1$

	public void setClasspath(List<String> entries, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(entries)) {
			launchConfig.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, entries);
		}
	}

	public boolean isClasspathSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH);
	}

	public void setUseDefaultClassPath(boolean useDefaultClassPath, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, useDefaultClassPath);
	}

	public boolean isUseDefaultClasspath(ILaunchConfiguration launchConfig) throws CoreException {
		return isUseDefaultClasspath(launchConfig, true);
	}

	public boolean isUseDefaultClasspath(ILaunchConfiguration launchConfig, boolean defaultValue) throws CoreException {
		return launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, defaultValue);
	}

	public boolean isUseDefaultClasspathSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH);
	}

	public void setMainType(String mainType, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(mainType)) {
			launchConfig.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainType);
		}
	}

	public boolean isMainTypeSet(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME);
	}

	public void setWorkingDirectory(String directory, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(directory)) {
			launchConfig.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, directory);
		}
	}

	public boolean isWorkingDirectorySet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY);
	}

	public void setServerId(String serverId, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(serverId)) {
			launchConfig.setAttribute(SERVER_ID, serverId);
		}
	}

	public String getServerId(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.getAttribute(SERVER_ID, (String) null);
	}

	public boolean isServerIdSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(SERVER_ID);
	}

	public void setHost(String host, ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if (isSet(host)) {
			String currentHost = getHost(launchConfig);
			if (currentHost == null
					|| !host.equals(currentHost)) {
				String programArguments = getProgramArguments(launchConfig);
				programArguments = ArgsUtil.setArg(programArguments,
						IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
						IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG, host);
				setProgramArguments(programArguments, launchConfig);
			}
		}
	}

	public boolean isHostSet(ILaunchConfiguration launchConfig) throws CoreException {
		String value = ArgsUtil.getValue(getProgramArguments(launchConfig),
				IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
				IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG);
		return value != null;
	}

	public void setProgramArguments(String programArguments, ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, programArguments);
	}

	public String getProgramArguments(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$;
	}

	public boolean areProgramArgumentsSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS);
	}

	public boolean isConfigSet(ILaunchConfiguration launchConfig) throws CoreException {
		String value = ArgsUtil.getValue(getProgramArguments(launchConfig),
				IJBossRuntimeConstants.STARTUP_ARG_CONFIG_SHORT,
				IJBossRuntimeConstants.STARTUP_ARG_CONFIG_LONG);
		return value != null;
	}

	public String getHost(ILaunchConfiguration launchConfig) throws CoreException {
		return ArgsUtil.getValue(getProgramArguments(launchConfig),
				IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
				IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG);
	}

	public void setConfig(String config, ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if (isSet(config)) {
			String programArguments = getProgramArguments(launchConfig);
			programArguments = ArgsUtil.setArg(programArguments,
					IJBossRuntimeConstants.STARTUP_ARG_CONFIG_SHORT,
					IJBossRuntimeConstants.STARTUP_ARG_CONFIG_LONG, config);
			setProgramArguments(programArguments, launchConfig);
		}
	}

	public void setJreContainer(String vmContainerPath, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(vmContainerPath)) {
			launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, vmContainerPath);
		}
	}

	public boolean isJreContainerSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH);
	}

	public void setServerHome(String serverHome, IJBossServerRuntime runtime,
			ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if (isSet(serverHome)) {
			String programArguments = getProgramArguments(launchConfig);
			String arguments = ArgsUtil.setArg(programArguments,
					null,
					IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_SERVER_HOME_URL,
					serverHome);
			setProgramArguments(arguments, launchConfig);
		}
	}

	public boolean isServerHomeSet(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		String value = ArgsUtil.getValue(getProgramArguments(launchConfig),
				null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_SERVER_HOME_URL);
		return value != null;
	}

	public void setClasspathProvider(String classpathProvider, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(classpathProvider)) {
			launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, classpathProvider);
		}
	}

	public boolean isClasspathProviderSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER);
	}

	public void setEndorsedDir(String endorsedDir, ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		if (isSet(endorsedDir)) {
			String vmArguments = getVMArguments(launchConfig);
			vmArguments = ArgsUtil.setArg(vmArguments,
					null, IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.ENDORSED_DIRS, endorsedDir);
			setVmArguments(vmArguments, launchConfig);
		}
	}
	
	public void setJavaLibPath(String libPath, ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		if (isSet(libPath)) {
			String vmArguments = getVMArguments(launchConfig);
			vmArguments = ArgsUtil.setArg(vmArguments,
					null, IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JAVA_LIB_PATH, 
					IJBossRuntimeConstants.QUOTE + libPath + IJBossRuntimeConstants.QUOTE);
			setVmArguments(vmArguments, launchConfig);
		}
	}


	public boolean isEndorsedDirSet(ILaunchConfiguration launchConfig) throws CoreException {
		String value = ArgsUtil.getValue(getVMArguments(launchConfig),
				null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.ENDORSED_DIRS);
		return value != null;
	}

	public void setVmArguments(String vmArguments, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(vmArguments)) {
			launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArguments);
		}
	}

	public String getVMArguments(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$
	}

	public boolean areVMArgumentsSet(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS);
	}

	public void setEnvironmentVariables(Map<String, String> environmentVariables,
			ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(environmentVariables)) {
			launchConfig.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, environmentVariables);
		}
	}

	public boolean areEnvironmentVariablesSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES);
	}

	@SuppressWarnings("unchecked")
	public List<String> getClasspath(ILaunchConfiguration launchConfig) throws CoreException {
		return (List<String>) launchConfig.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, new ArrayList<String>());
	}

	protected boolean isSet(String value) {
		return value != null && value.length() > 0;
	}

	protected boolean isSet(List<String> list) {
		return list != null;
	}

	protected boolean isSet(Map<String, String> map) {
		return map != null;
	}

}
