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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;

public class JBoss7LaunchConfigProperties extends JBossLaunchConfigProperties {
	public void setModulesFolder(String mp, ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		if (isSet(mp)) {
			String progArgs = getProgramArguments(launchConfig);
			// This is a hack, but other users may wish to add multiple modules folders
			if( !progArgs.contains(mp)) {
				progArgs = ArgsUtil.setArg(progArgs,
						IJBossRuntimeConstants.DASH + IJBossRuntimeConstants.JB7_MP_ARG, null,  
						IJBossRuntimeConstants.QUOTE + mp + IJBossRuntimeConstants.QUOTE);
				setProgramArguments(progArgs, launchConfig);
			}
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

	/**
	 * @since 2.5
	 */
	public void setBaseDirectory(String file, ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		if (isSet(file)) {
			String progArgs = getProgramArguments(launchConfig);
			progArgs = ArgsUtil.setArg(progArgs,
					null, IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_SERVER_BASE_DIR, file);
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
	
	private boolean supportsBindingFlag(ILaunchConfigurationWorkingCopy launchConfig ) throws CoreException {
		ServerBeanLoader loader = getBeanLoader(launchConfig);
		if( loader.getServerBean().getType().equals(JBossServerType.AS7)){
			String v = loader.getFullServerVersion();
			if( "7.0.2".compareTo(v) >= 0 ) { //$NON-NLS-1$
				// this is jboss 7.0 or 7.1
				return false;
			}
		}
		return true;
	}
	
	public void setHost(String host, ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if( !supportsBindingFlag(launchConfig)) 
			return;
		
		if (isSet(host)) {
			String currentHost = getHost(launchConfig);
			if (currentHost == null
					|| !host.equals(currentHost)) {
				String programArguments = getProgramArguments(launchConfig);
				programArguments = ArgsUtil.setArg(programArguments,
						IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
						null, host);
				setProgramArguments(programArguments, launchConfig);
			}
		}
	}

	public void setExposedManagement(String host, ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if( isSet(host)) {
			String vmArguments = getVMArguments(launchConfig);
			String arguments = ArgsUtil.setArg(vmArguments,
					null,
					IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JB7_EXPOSE_MANAGEMENT,
					host);
			setVmArguments(arguments, launchConfig);
		}
	}

}
