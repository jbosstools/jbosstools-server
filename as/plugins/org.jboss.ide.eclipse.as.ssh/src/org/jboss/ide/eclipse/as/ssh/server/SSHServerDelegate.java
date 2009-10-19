package org.jboss.ide.eclipse.as.ssh.server;

import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;

public class SSHServerDelegate extends DeployableServer {

	public SSHServerDelegate() {
		// TODO Auto-generated constructor stub
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
}
