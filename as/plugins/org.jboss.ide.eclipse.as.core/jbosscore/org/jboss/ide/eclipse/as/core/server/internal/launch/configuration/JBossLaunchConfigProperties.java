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

	public static void setClasspath(List<String> entries, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(entries)) {
			launchConfig.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, entries);
		}
	}

	public static boolean isClasspathSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH);
	}

	public static void setUseDefaultClassPath(boolean useDefaultClassPath, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, useDefaultClassPath);
	}

	public static boolean isUseDefaultClasspath(ILaunchConfiguration launchConfig) throws CoreException {
		return isUseDefaultClasspath(launchConfig, true);
	}

	public static boolean isUseDefaultClasspath(ILaunchConfiguration launchConfig, boolean defaultValue) throws CoreException {
		return launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, defaultValue);
	}

	public static boolean isUseDefaultClasspathSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH);
	}

	public static void setMainType(String mainType, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(mainType)) {
			launchConfig.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainType);
		}
	}

	public static boolean isMainTypeSet(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME);
	}

	public static void setWorkingDirectory(String directory, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(directory)) {
			launchConfig.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, directory);
		}
	}

	public static boolean isWorkingDirectorySet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY);
	}

	public static void setServerId(String serverId, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(serverId)) {
			launchConfig.setAttribute(SERVER_ID, serverId);
		}
	}

	public static String getServerId(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.getAttribute(SERVER_ID, (String) null);
	}

	public static boolean isServerIdSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(SERVER_ID);
	}

	public static void setHost(String host, ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if (isSet(host)) {
			String currentHost = getHost(launchConfig);
			if (currentHost == null
					|| !host.equals(currentHost)) {
				String programArguments = getProgramArguments(launchConfig);
				programArguments = ArgsUtil.setArg(programArguments,
						IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
						IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG,
						host);
				setProgramArguments(programArguments, launchConfig);
			}
		}
	}

	public static boolean isHostSet(ILaunchConfiguration launchConfig) throws CoreException {
		String value = ArgsUtil.getValue(getProgramArguments(launchConfig),
				IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
				IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG);
		return value != null;
	}

	public static void setProgramArguments(String programArguments, ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, programArguments);
	}

	public static String getProgramArguments(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$;
	}

	public static boolean areProgramArgumentsSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS);
	}

	public static boolean isConfigSet(ILaunchConfiguration launchConfig) throws CoreException {
		String value = ArgsUtil.getValue(getProgramArguments(launchConfig),
				IJBossRuntimeConstants.STARTUP_ARG_CONFIG_SHORT,
				IJBossRuntimeConstants.STARTUP_ARG_CONFIG_LONG);
		return value != null;
	}

	public static String getHost(ILaunchConfiguration launchConfig) throws CoreException {
		return ArgsUtil.getValue(getProgramArguments(launchConfig),
				IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
				IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG);
	}

	public static void setConfig(String config, ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if (isSet(config)) {
			String programArguments = getProgramArguments(launchConfig);
			programArguments = ArgsUtil.setArg(programArguments,
					IJBossRuntimeConstants.STARTUP_ARG_CONFIG_SHORT,
					IJBossRuntimeConstants.STARTUP_ARG_CONFIG_LONG, config);
			setProgramArguments(programArguments, launchConfig);
		}
	}

	public static void setJreContainer(String vmContainerPath, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(vmContainerPath)) {
			launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, vmContainerPath);
		}
	}

	public static boolean isJreContainerSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH);
	}

	public static void setServerHome(String serverHome, IJBossServerRuntime runtime,
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

	public static boolean isServerHomeSet(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		String value = ArgsUtil.getValue(getProgramArguments(launchConfig),
				null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_SERVER_HOME_URL);
		return value != null;
	}

	public static void setClasspathProvider(String classpathProvider, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(classpathProvider)) {
			launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, classpathProvider);
		}
	}

	public static boolean isClasspathProviderSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER);
	}

	public static void setEndorsedDir(String endorsedDir, ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		if (isSet(endorsedDir)) {
			String vmArguments = getVMArguments(launchConfig);
			vmArguments = ArgsUtil.setArg(vmArguments,
					null, IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.ENDORSED_DIRS, endorsedDir);
			setVmArguments(vmArguments, launchConfig);
		}
	}

	public static boolean isEndorsedDirSet(ILaunchConfiguration launchConfig) throws CoreException {
		String value = ArgsUtil.getValue(getVMArguments(launchConfig),
				null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.ENDORSED_DIRS);
		return value != null;
	}

	public static void setVmArguments(String vmArguments, ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(vmArguments)) {
			launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArguments);
		}
	}

	public static String getVMArguments(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$
	}

	public static boolean areVMArgumentsSet(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		return launchConfig.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS);
	}

	public static void setEnvironmentVariables(Map<String, String> environmentVariables,
			ILaunchConfigurationWorkingCopy launchConfig) {
		if (isSet(environmentVariables)) {
			launchConfig.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, environmentVariables);
		}
	}

	public static boolean areEnvironmentVariablesSet(ILaunchConfiguration launchConfig) throws CoreException {
		return launchConfig.hasAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES);
	}

	@SuppressWarnings("unchecked")
	public static List<String> getClasspath(ILaunchConfiguration launchConfig) throws CoreException {
		return (List<String>) launchConfig.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, new ArrayList<String>());
	}

	private static boolean isSet(String value) {
		return value != null && value.length() > 0;
	}

	private static boolean isSet(List<String> list) {
		return list != null;
	}

	private static boolean isSet(Map<String, String> map) {
		return map != null;
	}

}
