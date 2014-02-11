package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishControllerDelegate;
import org.jboss.tools.as.core.server.controllable.util.PublishControllerUtility;

/**
 * This class is here as a fall-back in case publishers not declared in astools
 * get forgotten. This ensures that at the very least the legacy publishers
 * will continue to work for them as much as possible. 
 * This class will be removed once it is no longer required. 
 */
public class LegacyPublisherWrapperController extends AbstractSubsystemController implements IPublishControllerDelegate {
	private IJBossServerPublisher legacyPublisher;
	public void setLegacyPublisher(IJBossServerPublisher pub) {
		this.legacyPublisher = pub;
	}
	
	@Override
	public int publishModule(int kind, int deltaKind, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		legacyPublisher.publishModule(createPublishMethod(), getServer(), module, 
				PublishControllerUtility.getPublishType(getServer(), module, kind, deltaKind),
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
}
