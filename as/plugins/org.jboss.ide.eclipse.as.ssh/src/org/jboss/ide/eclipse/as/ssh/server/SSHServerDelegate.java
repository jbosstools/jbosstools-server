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

import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;

public class SSHServerDelegate extends DeployableServer {

	public SSHServerDelegate() {
		super();
	}

	public IJBossServerRuntime getRuntime() {
		return null;
	}

	public String getUsername() {
		return getAttribute(ISSHDeploymentConstants.USERNAME, (String)null);
	}
	public String getPassword() {
		return getAttribute(ISSHDeploymentConstants.PASSWORD, (String)null);
	}
	public String getHostsFile() {
		return getAttribute(ISSHDeploymentConstants.HOSTS_FILE, (String)null);
	}
	public String getDeployFolder() {
		return getAttribute(ISSHDeploymentConstants.DEPLOY_DIRECTORY, (String)null);
	}
	
	public void setUsername(String user) {
		setAttribute(ISSHDeploymentConstants.USERNAME, user);
	}
	public void setPassword(String pass) {
		setAttribute(ISSHDeploymentConstants.PASSWORD, pass);
	}
	public void setHostsFile(String file) {
		setAttribute(ISSHDeploymentConstants.HOSTS_FILE, file);
	}
	public void setDeployFolder(String folder) {
		setAttribute(ISSHDeploymentConstants.DEPLOY_DIRECTORY, folder);
	}
}
