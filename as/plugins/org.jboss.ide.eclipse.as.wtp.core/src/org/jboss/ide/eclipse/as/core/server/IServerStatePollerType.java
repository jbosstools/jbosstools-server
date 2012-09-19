package org.jboss.ide.eclipse.as.core.server;


public interface IServerStatePollerType {
	public boolean supportsStartup();
	public boolean supportsShutdown();
	public String getName();
	public String getId(); 
	public String getServerTypes();
	public IServerStatePoller createPoller(); 
}
