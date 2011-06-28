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

public class LaunchCommandPreferences {

	public static boolean ignoreLaunchCommand(IServerAttributes server) {
		String ignoreCommand = server.getAttribute(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, (String) null);
		return Boolean.valueOf(ignoreCommand);
	}
	
}
