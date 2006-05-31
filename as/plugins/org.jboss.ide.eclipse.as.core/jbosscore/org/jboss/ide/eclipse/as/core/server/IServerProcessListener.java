package org.jboss.ide.eclipse.as.core.server;

public interface IServerProcessListener {
	public static final String PROCESS_ADDED = "__PROCESS_ADDED__";
	public static final String PROCESS_REMOVED = "__PROCESS_REMOVED__";
	
	
	public void ServerProcessEventFired(ServerProcessEvent event);
}
