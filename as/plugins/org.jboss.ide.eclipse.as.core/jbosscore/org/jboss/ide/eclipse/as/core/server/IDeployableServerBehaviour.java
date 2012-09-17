package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;

public interface IDeployableServerBehaviour {
	public IServer getServer();
	public boolean changedFileRequiresModuleRestart(IModuleFile mf);
	public IModulePathFilter getPathFilter(IModule[] moduleTree);
	public IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module);
	public int getPublishType(int kind, int deltaKind, int modulePublishState);
}
