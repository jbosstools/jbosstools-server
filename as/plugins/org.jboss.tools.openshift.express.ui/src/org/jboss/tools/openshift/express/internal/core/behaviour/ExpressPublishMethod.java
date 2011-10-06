package org.jboss.tools.openshift.express.internal.core.behaviour;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;

public class ExpressPublishMethod implements IJBossServerPublishMethod {

	public ExpressPublishMethod() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void publishStart(DeployableServerBehavior behaviour,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public int publishFinish(DeployableServerBehavior behaviour,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int publishModule(DeployableServerBehavior behaviour, int kind,
			int deltaKind, IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IPublishCopyCallbackHandler getCallbackHandler(IPath path,
			IServer server) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPublishDefaultRootFolder(IServer server) {
		// TODO Auto-generated method stub
		return null;
	}

}
