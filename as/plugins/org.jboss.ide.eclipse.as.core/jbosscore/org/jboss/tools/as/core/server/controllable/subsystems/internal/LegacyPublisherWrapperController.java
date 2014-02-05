package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;

/**
 * This class is here as a fall-back in case publishers not declared in astools
 * get forgotten. This ensures that at the very least the legacy publishers
 * will continue to work for them as much as possible. 
 * 
 * @author rob
 *
 */
public class LegacyPublisherWrapperController extends AbstractSubsystemController implements IPublishController {
	private IJBossServerPublisher legacyPublisher;
	public void setLegacyPublisher(IJBossServerPublisher pub) {
		this.legacyPublisher = pub;
	}
	
	@Override
	public int publishModule(int kind, int deltaKind, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		legacyPublisher.publishModule(createPublishMethod(), getServer(), module, getPublishType(kind, deltaKind, module),
				getPublishedResourceDelta(module), monitor);
		return legacyPublisher.getPublishState();
	}
	
	public IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
		return ((Server)getServer()).getPublishedResourceDelta(module);
	}
	
	/*
	 * This will most likely need to change once we delete the classes. 
	 * The interface, though deprecated, will need to be re-implemented for legacy case, and so must stay around
	 */
	public IJBossServerPublishMethod createPublishMethod() {
		IJBossServerPublishMethodType type = DeploymentPreferenceLoader.getCurrentDeploymentMethodType(getServer());
		if( type != null )
			return type.createPublishMethod();
		return new LocalPublishMethod(); // sensible default
	}

	
	public int getPublishType(int kind, int deltaKind, IModule[] module) {
		int modulePublishState = getServer().getModulePublishState(module);
		
		if( deltaKind == ServerBehaviourDelegate.ADDED ) 
			return IJBossServerPublisher.FULL_PUBLISH;
		else if (deltaKind == ServerBehaviourDelegate.REMOVED) {
			return IJBossServerPublisher.REMOVE_PUBLISH;
		} else if (kind == IServer.PUBLISH_FULL 
				|| modulePublishState == IServer.PUBLISH_STATE_FULL 
				|| kind == IServer.PUBLISH_CLEAN ) {
			return IJBossServerPublisher.FULL_PUBLISH;
		} else if (kind == IServer.PUBLISH_INCREMENTAL 
				|| modulePublishState == IServer.PUBLISH_STATE_INCREMENTAL 
				|| kind == IServer.PUBLISH_AUTO) {
			if( ServerBehaviourDelegate.CHANGED == deltaKind ) 
				return IJBossServerPublisher.INCREMENTAL_PUBLISH;
		} 
		return IJBossServerPublisher.NO_PUBLISH;
	}
	

	
	@Override
	public IStatus canPublish() {
		return Status.OK_STATUS;
	}

	@Override
	public boolean canPublishModule(IModule[] module) {
		String existingMode = getServer().getAttribute(IDeployableServer.SERVER_MODE, "local"); //$NON-NLS-1$
		return legacyPublisher.accepts(existingMode, getServer(), module);
	}

	@Override
	public void publishStart(IProgressMonitor monitor) throws CoreException {
		// Remain empty, unused		
	}

	@Override
	public void publishFinish(IProgressMonitor monitor) throws CoreException {
		// Remain empty, unused
	}


	@Override
	public void publishServer(int kind, IProgressMonitor monitor)
			throws CoreException {
		// Remain empty, unused
	}	
}
