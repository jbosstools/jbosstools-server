package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;

public class UnitedServerListener {

	public void init(IServer server) {}
	public void serverAdded(IServer server) {}
	public void serverChanged(IServer server) {}
	public void serverRemoved(IServer server) {}
	public void serverChanged(ServerEvent event) {}
	public void publishStarted(IServer server){}
	public void publishFinished(IServer server, IStatus status){}
	public void cleanUp(IServer server) {}
}
