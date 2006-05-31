package org.jboss.ide.eclipse.as.core.server;

import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;

public interface IServerLogListener {
	public void logChanged(ServerProcessModelEntity ent);
}
