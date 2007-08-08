package org.jboss.ide.eclipse.as.core.singledeployable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.publishers.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.core.singledeployable.SingleDeployableFactory.SingleDeployableModuleDelegate;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class SingleFilePublisher implements IJBossServerPublisher {

	private IDeployableServer server;
	public SingleFilePublisher(IServer server) {
		this.server = ServerConverter.getDeployableServer(server);
	}
	
	public int getPublishState() {
		return IServer.PUBLISH_STATE_NONE;
	}

	public IStatus publishModule(int kind, int deltaKind,
			int modulePublishState, IModule module, IProgressMonitor monitor)
			throws CoreException {
		
		// COPY THE FILE HERE!!!
		SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module.loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
		IPath sourcePath = delegate.getGlobalSourcePath();
		IPath destFolder = new Path(server.getDeployDirectory());
		FileUtil.fileSafeCopy(sourcePath.toFile(), destFolder.append(sourcePath.lastSegment()).toFile());
		
		return null;
	}

	public void setDelta(IModuleResourceDelta[] delta) {
		// ignore delta
	}

}
