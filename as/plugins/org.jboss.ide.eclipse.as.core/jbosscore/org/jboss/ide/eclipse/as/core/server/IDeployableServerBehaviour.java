package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.wst.server.core.model.IModuleFile;

public interface IDeployableServerBehaviour {
	public boolean changedFileRequiresModuleRestart(IModuleFile mf);
}
