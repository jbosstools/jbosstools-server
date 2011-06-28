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
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * @author André Dietisheim
 */
public class RSELaunchConfigUtils {

	public static final String RSE_STARTUP_COMMAND = "org.jboss.ide.eclipse.as.rse.core.RSELaunchDelegate.STARTUP_COMMAND";
	public static final String RSE_SHUTDOWN_COMMAND = "org.jboss.ide.eclipse.as.rse.core.RSELaunchDelegate.SHUTDOWN_COMMAND";
	public static final String DETECT_STARTUP_COMMAND = "org.jboss.ide.eclipse.as.rse.core.RSELaunchDelegate.DETECT_STARTUP_COMMAND";
	public static final String DETECT_SHUTDOWN_COMMAND = "org.jboss.ide.eclipse.as.rse.core.RSELaunchDelegate.DETECT_SHUTDOWN_COMMAND";

	public static boolean isDetectStartupCommand(ILaunchConfiguration launchConfig) throws CoreException {
		return isDetectStartupCommand(launchConfig, true);
	}

	public static boolean isDetectStartupCommand(ILaunchConfiguration launchConfig, boolean defaultValue)
			throws CoreException {
		return launchConfig.getAttribute(DETECT_STARTUP_COMMAND, defaultValue);
	}

	public static void setDetectStartupCommand(boolean detectStartup, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(DETECT_STARTUP_COMMAND, detectStartup);
	}

	public static boolean isDetectShutdownCommand(ILaunchConfiguration launchConfig) throws CoreException {
		return isDetectShutdownCommand(launchConfig, true);
	}

	public static boolean isDetectShutdownCommand(ILaunchConfiguration launchConfig, boolean defaultValue)
			throws CoreException {
		return launchConfig.getAttribute(DETECT_SHUTDOWN_COMMAND, defaultValue);
	}

	public static void setDetectShutdownCommand(boolean detectShutdown, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(DETECT_SHUTDOWN_COMMAND, detectShutdown);
	}

	public static void setStartupCommand(String startupCommand, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(RSE_STARTUP_COMMAND, startupCommand);
	}

	public static String getStartupCommand(ILaunchConfiguration launchConfig) throws CoreException {
		return getStartupCommand(launchConfig, (String) null);
	}

	public static String getStartupCommand(ILaunchConfiguration launchConfig, String defaultCommand)
			throws CoreException {
		return launchConfig.getAttribute(RSE_STARTUP_COMMAND, defaultCommand);
	}

	public static void setShutdownCommand(String shutdownCommand, ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(RSE_SHUTDOWN_COMMAND, shutdownCommand);
	}

	public static String getShutdownCommand(ILaunchConfiguration launchConfig) throws CoreException {
		return getShutdownCommand(launchConfig, (String) null);
	}

	public static String getShutdownCommand(ILaunchConfiguration launchConfig, String defaultCommand)
			throws CoreException {
		return launchConfig.getAttribute(RSE_SHUTDOWN_COMMAND, defaultCommand);
	}
}
