package org.jboss.ide.eclipse.as.core.server;

public interface IJBossServer extends IDeployableServer {
	public String getHost();
	public String getUsername();
	public String getPassword();
	public int getJNDIPort();
	public int getJBossWebPort();
	public IJBossServerRuntime getRuntime();
}
