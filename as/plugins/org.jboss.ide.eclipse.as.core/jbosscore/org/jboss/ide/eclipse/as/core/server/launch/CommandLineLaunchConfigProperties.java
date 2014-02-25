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
package org.jboss.ide.eclipse.as.core.server.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * This class is an expansion of the use of RSELaunchConfigProperties. 
 * Because of this, key constants need to be changed for the new usecase,
 * while still preserving the historical keys for rse use. 
 */
public class CommandLineLaunchConfigProperties {
	// A list of property keys
	public static class KeySet {
		protected String DEFAULT_STARTUP_COMMAND;
		protected String STARTUP_COMMAND;
		protected String DEFAULT_SHUTDOWN_COMMAND;
		protected String SHUTDOWN_COMMAND;
		protected String DETECT_STARTUP_COMMAND;
		protected String DETECT_SHUTDOWN_COMMAND;
	}
	// Keys for the new expanded use case
	public static class DefaultKeySet extends KeySet {
		public DefaultKeySet() {
			DEFAULT_STARTUP_COMMAND = "org.jboss.ide.eclipse.as.core.launch.DEFAULT_STARTUP_COMMAND"; //$NON-NLS-1$
			STARTUP_COMMAND = "org.jboss.ide.eclipse.as.core.launch.STARTUP_COMMAND";//$NON-NLS-1$
			DEFAULT_SHUTDOWN_COMMAND = "org.jboss.ide.eclipse.as.core.launch.DEFAULT_SHUTDOWN_COMMAND";//$NON-NLS-1$
			SHUTDOWN_COMMAND = "org.jboss.ide.eclipse.as.core.launch.SHUTDOWN_COMMAND";//$NON-NLS-1$
			DETECT_STARTUP_COMMAND = "org.jboss.ide.eclipse.as.core.launch.DETECT_STARTUP_COMMAND";//$NON-NLS-1$
			DETECT_SHUTDOWN_COMMAND = "org.jboss.ide.eclipse.as.core.launch.DETECT_SHUTDOWN_COMMAND";//$NON-NLS-1$
		}
	}
	
	public static KeySet DEFAULT_KEYSET = new DefaultKeySet();
	
	private KeySet keySet;
	public CommandLineLaunchConfigProperties() {
		this(DEFAULT_KEYSET);
	}
	public CommandLineLaunchConfigProperties(KeySet set) {
		this.keySet = set;
	}

	public boolean isDetectStartupCommand(ILaunchConfiguration launchConfig) throws CoreException {
		return isDetectStartupCommand(launchConfig, true);
	}

	public boolean isDetectStartupCommand(ILaunchConfiguration launchConfig, boolean defaultValue)
			throws CoreException {
		return launchConfig.getAttribute(keySet.DETECT_STARTUP_COMMAND, defaultValue);
	}

	public void setDetectStartupCommand(boolean detectStartup, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(keySet.DETECT_STARTUP_COMMAND, detectStartup);
	}

	public boolean isDetectShutdownCommand(ILaunchConfiguration launchConfig) throws CoreException {
		return isDetectShutdownCommand(launchConfig, true);
	}

	public boolean isDetectShutdownCommand(ILaunchConfiguration launchConfig, boolean defaultValue)
			throws CoreException {
		return launchConfig.getAttribute(keySet.DETECT_SHUTDOWN_COMMAND, defaultValue);
	}

	public void setDetectShutdownCommand(boolean detectShutdown, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(keySet.DETECT_SHUTDOWN_COMMAND, detectShutdown);
	}

	public void setDefaultStartupCommand(String startupCommand, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(keySet.DEFAULT_STARTUP_COMMAND, startupCommand);
	}

	public String getDefaultStartupCommand(ILaunchConfiguration launchConfig, String defaultCommand)
			throws CoreException {
		return launchConfig.getAttribute(keySet.DEFAULT_STARTUP_COMMAND, defaultCommand);
	}

	public void setStartupCommand(String startupCommand, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(keySet.STARTUP_COMMAND, startupCommand);
	}

	public String getStartupCommand(ILaunchConfiguration launchConfig) throws CoreException {
		return getStartupCommand(launchConfig, (String) null);
	}

	public String getStartupCommand(ILaunchConfiguration launchConfig, String defaultCommand)
			throws CoreException {
		return launchConfig.getAttribute(keySet.STARTUP_COMMAND, defaultCommand);
	}

	public void setShutdownCommand(String shutdownCommand, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(keySet.SHUTDOWN_COMMAND, shutdownCommand);
	}

	public String getShutdownCommand(ILaunchConfiguration launchConfig) throws CoreException {
		return getShutdownCommand(launchConfig, (String) null);
	}

	public String getShutdownCommand(ILaunchConfiguration launchConfig, String defaultCommand)
			throws CoreException {
		if (launchConfig == null) { 
			return defaultCommand;
		}
		return launchConfig.getAttribute(keySet.SHUTDOWN_COMMAND, defaultCommand);
	}

	public void setDefaultShutdownCommand(String shutdownCommand, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(keySet.DEFAULT_SHUTDOWN_COMMAND, shutdownCommand);
	}

	public String getDefaultShutdownCommand(ILaunchConfiguration launchConfig, String defaultCommand)
			throws CoreException {
		return launchConfig.getAttribute(keySet.DEFAULT_SHUTDOWN_COMMAND, defaultCommand);
	}

}
