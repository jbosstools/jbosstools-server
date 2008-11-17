package org.jboss.ide.eclipse.as.core.extensions.events;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;

public interface IServerLogListener {
	public void logging(IStatus status, IServer server);
}
