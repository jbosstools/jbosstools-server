package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;

public interface IDeployableServerBehaviour {
	public IServer getServer();
	public boolean changedFileRequiresModuleRestart(IModuleFile mf);
	public IModulePathFilter getPathFilter(IModule[] moduleTree);
}
