package org.jboss.ide.eclipse.as.core.publishers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory.SingleDeployableModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
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
			int modulePublishState, IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		
		IModule module2 = module[0];
		
		IStatus status = null;
		if(ServerBehaviourDelegate.REMOVED == deltaKind){
        	status = unpublish(server, module2, kind, deltaKind, modulePublishState, monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind || kind == IServer.PUBLISH_FULL || kind == IServer.PUBLISH_CLEAN ){
        	// if there's no change, do nothing. Otherwise, on change or add, re-publish
        	status = publish(server, module2, kind, deltaKind, modulePublishState, monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind && kind == IServer.PUBLISH_INCREMENTAL ){
        	status = publish(server, module2, kind, deltaKind, modulePublishState, monitor);
        }
		return status;

	}
	
	protected IStatus publish(IDeployableServer server, IModule module, int kind, int deltaKind, int modulePublishState, IProgressMonitor monitor) {
		// COPY THE FILE HERE!!!
		SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module.loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
		if( delegate != null ) {
			IPath sourcePath = delegate.getGlobalSourcePath();
			IPath destFolder = new Path(server.getDeployDirectory());
			FileUtil.fileSafeCopy(sourcePath.toFile(), destFolder.append(sourcePath.lastSegment()).toFile());
		}
		
		return null;
	}

	protected IStatus unpublish(IDeployableServer server, IModule module, int kind, int deltaKind, int modulePublishState, IProgressMonitor monitor) {
		// delete file
		SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module.loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
		if( delegate != null ) {
			IPath sourcePath = delegate.getGlobalSourcePath();
			IPath destFolder = new Path(server.getDeployDirectory());
			FileUtil.safeDelete(destFolder.append(sourcePath.lastSegment()).toFile());
		}
		
		return null;
	}
	public void setDelta(IModuleResourceDelta[] delta) {
		// ignore delta
	}

}
