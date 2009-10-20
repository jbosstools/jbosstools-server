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
