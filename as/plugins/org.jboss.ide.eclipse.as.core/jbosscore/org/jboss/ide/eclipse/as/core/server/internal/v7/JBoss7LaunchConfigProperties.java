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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;

public class JBoss7LaunchConfigProperties extends JBossLaunchConfigProperties {
	public void setModulesFolder(String mp, ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		if (isSet(mp)) {
			String progArgs = getProgramArguments(launchConfig);
			progArgs = ArgsUtil.setArg(progArgs,
					IJBossRuntimeConstants.DASH + IJBossRuntimeConstants.JB7_MP_ARG, null,  
					IJBossRuntimeConstants.QUOTE + mp + IJBossRuntimeConstants.QUOTE);
			setProgramArguments(progArgs, launchConfig);
		}
	}
	public void setConfigurationFile(String file, ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		if (isSet(file)) {
			String progArgs = getProgramArguments(launchConfig);
			progArgs = ArgsUtil.setArg(progArgs,
					null, IJBossRuntimeConstants.JB7_SERVER_CONFIG_ARG, file);
			setProgramArguments(progArgs, launchConfig);
		}
	}

	public void setBootLogFile(String blf, ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		if (isSet(blf)) {
			String vmArgs = getVMArguments(launchConfig);
			vmArgs = ArgsUtil.setArg(vmArgs, null,
					IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JB7_BOOT_LOG_ARG, blf);
			setVmArguments(vmArgs, launchConfig);
		}
	}

	public void setLoggingConfigFile(String lcf, ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		if (isSet(lcf)) {
			String vmArgs = getVMArguments(launchConfig);
			vmArgs = ArgsUtil.setArg(vmArgs, null, 
					IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JB7_LOGGING_CONFIG_FILE, lcf);
			setVmArguments(vmArgs, launchConfig);
		}
	}
	
	public void setServerHome(String serverHome, IJBossServerRuntime runtime, 
			ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if (isSet(serverHome)) {
			String vmArguments = getVMArguments(launchConfig);
			String arguments = ArgsUtil.setArg(vmArguments,
					null,
					IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_HOME_DIR,
					serverHome);
			setVmArguments(arguments, launchConfig);
		}
	}
}
