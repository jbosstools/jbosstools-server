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

import org.eclipse.wst.server.core.IServerAttributes;

/**
 * @author André Dietisheim
 */
public class LaunchCommandPreferences {

	public static boolean isIgnoreLaunchCommand(IServerAttributes server) {
		return isIgnoreLaunchCommand(server, false);
	}
	
	public static boolean isIgnoreLaunchCommand(IServerAttributes server, boolean defaultValue) {
		return checkBooleanAttribute(server, IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, defaultValue );
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
