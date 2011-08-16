package org.jboss.ide.eclipse.as.egit.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;

public class EgitPublisher implements IJBossServerPublisher {

	@Override
	public boolean accepts(String method, IServer server, IModule[] module) {
		return false;
	}

	@Override
	public int getPublishState() {
		return 0;
	}

	@Override
	public IStatus publishModule(IJBossServerPublishMethod method, IServer server, IModule[] module, int publishType,
			IModuleResourceDelta[] delta, IProgressMonitor monitor) throws CoreException {
		return null;
	}

}
