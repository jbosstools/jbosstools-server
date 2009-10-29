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
package org.jboss.ide.eclipse.as.ssh.server;

import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;

public class SSHPublishUtil {
	public static boolean getZipsSSHDeployments(IServer server) {
		return ((Server)server).getAttribute(ISSHDeploymentConstants.ZIP_DEPLOYMENTS_PREF, false);
	}
	public static String getDeployDir(IServer server) {
		return ((Server)server).getAttribute(ISSHDeploymentConstants.DEPLOY_DIRECTORY, "/home"); 
	}
	
	public static String getUser(IServer server) {
		return ((Server)server).getAttribute(ISSHDeploymentConstants.USERNAME, "username"); 
	}
	
	public static String getPass(IServer server) {
		return ((Server)server).getAttribute(ISSHDeploymentConstants.PASSWORD, "password"); 
	}

	public static String getHostsFile(IServer server) {
		return ((Server)server).getAttribute(ISSHDeploymentConstants.HOSTS_FILE, (String)null); 
	}
}
