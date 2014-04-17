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

package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.ServerUtil;

/**
 * @author André Dietisheim
 */
public class LaunchCommandPreferences {

	@Deprecated
	public static boolean isIgnoreLaunchCommand(IServerAttributes server) {
		return isIgnoreLaunchCommand(server, false);
	}
	
	@Deprecated
	public static boolean isIgnoreLaunchCommand(IServerAttributes server, boolean defaultValue) {
		return checkBooleanAttribute(server, IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, defaultValue );
	}

	public static boolean isIgnoreLaunchCommand(ILaunchConfiguration launch) throws CoreException {
		return isIgnoreLaunchCommand(launch, false);
	}
	
	public static boolean isIgnoreLaunchCommand(ILaunchConfiguration launch, boolean defaultValue) throws CoreException {
		if( launch.hasAttribute(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS)) {
			return launch.getAttribute(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, defaultValue);
		}
		IServer server = ServerUtil.getServer(launch);
		return checkBooleanAttribute(server, IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, defaultValue );
	}

	public static void setIgnoreLaunchCommand(ILaunchConfigurationWorkingCopy launch, boolean val) {
		launch.setAttribute(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, val);
	}
	
	
	public static boolean listensOnAllHosts(IServerAttributes server) {
		return listensOnAllHosts(server, false);
	}
	
	public static boolean listensOnAllHosts(IServerAttributes server, boolean defaultValue) {
		return checkBooleanAttribute(server, IJBossToolingConstants.LISTEN_ALL_HOSTS, defaultValue );
	}

	public static boolean exposesManagement(IServerAttributes server) {
		return exposesManagement(server, false);
	}
	
	public static boolean exposesManagement(IServerAttributes server, boolean defaultValue) {
		return checkBooleanAttribute(server, IJBossToolingConstants.EXPOSE_MANAGEMENT_SERVICE, defaultValue );
	}

	private static boolean checkBooleanAttribute(IServerAttributes server, String key, boolean defaultValue) {
		if (server == null) {
			return false;
		}
		String ignoreCommand = server.getAttribute(key, Boolean.toString(defaultValue));
		return Boolean.valueOf(ignoreCommand);
	}

}
