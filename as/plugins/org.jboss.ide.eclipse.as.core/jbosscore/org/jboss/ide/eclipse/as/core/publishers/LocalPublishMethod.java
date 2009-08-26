package org.jboss.ide.eclipse.as.core.publishers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;

public class LocalPublishMethod implements IJBossServerPublishMethod {

	public boolean accepts(String methodType) {
		return "local".equals(methodType); //$NON-NLS-1$
	}

	public void publishStart(DeployableServerBehavior behaviour,
			IProgressMonitor monitor) throws CoreException {
	}

	public int publishFinish(DeployableServerBehavior behaviour,
			IProgressMonitor monitor) throws CoreException {
        IModule[] modules = behaviour.getServer().getModules();
        boolean allpublished= true;
        for (int i = 0; i < modules.length; i++) {
        	if(behaviour.getServer().getModulePublishState(new IModule[]{modules[i]})!=IServer.PUBLISH_STATE_NONE)
                allpublished=false;
        }
        return allpublished ? IServer.PUBLISH_STATE_NONE : IServer.PUBLISH_STATE_INCREMENTAL;
	}
	
	public int getServerPublishState(DeployableServerBehavior behaviour) {
        IModule[] modules = behaviour.getServer().getModules();
        boolean allpublished= true;
        for (int i = 0; i < modules.length; i++) {
        	if(behaviour.getServer().getModulePublishState(new IModule[]{modules[i]})!=IServer.PUBLISH_STATE_NONE)
                allpublished=false;
        }
        if(allpublished)
        	return IServer.PUBLISH_STATE_NONE;
        return IServer.PUBLISH_STATE_INCREMENTAL;
	}

	public int publishModule(DeployableServerBehavior behaviour, int kind,
			int deltaKind, IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		// kind = [incremental, full, auto, clean] = [1,2,3,4]
		// delta = [no_change, added, changed, removed] = [0,1,2,3]
		if( module.length == 0 ) return IServer.PUBLISH_STATE_NONE;
		int modulePublishState = behaviour.getServer().getModulePublishState(module);
		int publishType = behaviour.getPublishType(kind, deltaKind, modulePublishState);
		IJBossServerPublisher publisher;
		
		// Let the publisher decide what to do
		if( module.length > 0 ) {
			publisher = ExtensionManager.getDefault().getPublisher(behaviour.getServer(), module, "local"); //$NON-NLS-1$
			IModuleResourceDelta[] deltas = new IModuleResourceDelta[]{};
			if( deltaKind != ServerBehaviourDelegate.REMOVED)
				deltas = behaviour.getPublishedResourceDelta(module);
			if( publisher != null ) {
				IStatus result = publisher.publishModule(behaviour.getServer(), module, 
						publishType, deltas, monitor);
				if( result != null )
			        ServerLogger.getDefault().log(behaviour.getServer(), result);
				return publisher.getPublishState();
			}
			return IServer.PUBLISH_STATE_INCREMENTAL;
		}
		return IServer.PUBLISH_STATE_NONE;
	}

}
