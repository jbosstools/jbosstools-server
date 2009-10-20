package org.jboss.ide.eclipse.as.ssh.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SSHServerBehaviourDelegate extends DeployableServerBehavior {

	public SSHServerBehaviourDelegate() {
		super();
	}

	@Override
	public void stop(boolean force) {
		setServerState(IServer.STATE_STOPPED);
	}
	
	protected IJBossServerPublishMethod createPublishMethod() {
		return new SSHPublishMethod(); // TODO FIX THIS in superclass
	}
	
	public class SSHPublishMethod extends LocalPublishMethod {
		public static final String SSH_PUBLISH_METHOD = "ssh";  //$NON-NLS-1$
		
		@Override
		public String getPublishMethodId() {
			return SSH_PUBLISH_METHOD;
		}
		
		private Session session;
		public Session getSession() {
			return session;
		}

		@Override
		public void publishStart(DeployableServerBehavior behaviour,
				IProgressMonitor monitor) throws CoreException {
			
			try {
				ServerUserInfo info = new ServerUserInfo(getServer());
				JSch jsch = new JSch();
				session = jsch.getSession(info.getUser(), behaviour.getServer().getHost(), 22);
				jsch.setKnownHosts(info.getHostsFile());
				session.setUserInfo(info);
				session.connect();
			} catch( JSchException jsche) {
				// TODO handle
			}
		}
		
		@Override
		public int publishFinish(DeployableServerBehavior behaviour,
				IProgressMonitor monitor) throws CoreException {
			int ret = super.publishFinish(behaviour, monitor);
			if( session != null )
				session.disconnect();
			session = null;
			return ret;
		}
	}
	
	
	public static class ServerUserInfo implements UserInfo {
		private String user;
		private String password;
		private String hostsFile;
		public ServerUserInfo(IServer server) {
			user = SSHPublishUtil.getUser(server);
			password = SSHPublishUtil.getPass(server);
			hostsFile = SSHPublishUtil.getHostsFile(server);
		}
		public String getPassword() {
			return password;
		}

		public String getUser() {
			return user;
		}
		
		public String getHostsFile() {
			return hostsFile;
		}
		
		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return true;
		}

		public boolean promptPassword(String message) {
			return true;
		}

		public void showMessage(String message) {
			// TODO eh?
		}

		public boolean promptYesNo(String message) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
}
