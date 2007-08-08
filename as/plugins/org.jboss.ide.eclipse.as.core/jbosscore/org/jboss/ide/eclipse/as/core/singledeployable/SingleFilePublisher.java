package org.jboss.ide.eclipse.as.core.singledeployable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.publishers.IJBossServerPublisher;

public class SingleFilePublisher implements IJBossServerPublisher {

	public int getPublishState() {
		return IServer.PUBLISH_STATE_NONE;
	}

	public IStatus publishModule(int kind, int deltaKind,
			int modulePublishState, IModule module, IProgressMonitor monitor)
			throws CoreException {
		
		// COPY THE FILE HERE!!!
		
		return null;
	}

	public void setDelta(IModuleResourceDelta[] delta) {
		// ignore delta
	}

}
