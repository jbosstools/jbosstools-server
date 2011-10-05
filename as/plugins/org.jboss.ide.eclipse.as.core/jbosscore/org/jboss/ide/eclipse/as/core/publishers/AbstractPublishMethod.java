/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
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
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;

public abstract class AbstractPublishMethod implements IJBossServerPublishMethod {
	public abstract String getPublishMethodId();

//	@Deprecated
//	public IJBossServerPublishMethodType getPublishMethodType() {
//		return ExtensionManager.getDefault().getPublishMethod(getPublishMethodId());
//	}
	
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
		
		// Let the publisher decide what to do
		if( module.length > 0 ) {
			IJBossServerPublisher publisher = ExtensionManager.getDefault().getPublisher(behaviour.getServer(), module, getPublishMethodId());
			IModuleResourceDelta[] deltas = new IModuleResourceDelta[]{};
			if( deltaKind != ServerBehaviourDelegate.REMOVED)
				deltas = behaviour.getPublishedResourceDelta(module);
			if( publisher != null ) {
				try {
					IStatus result = publisher.publishModule(
							this, 
							behaviour.getServer(), module, 
							publishType, deltas, monitor);
					if( result != null )
				        ServerLogger.getDefault().log(behaviour.getServer(), result);
				} catch( CoreException ce) {
					// Let the user know
			        ServerLogger.getDefault().log(behaviour.getServer(), ce.getStatus());
			        throw ce;
				}
				return publisher.getPublishState();
			}
			return IServer.PUBLISH_STATE_INCREMENTAL;
		}
		return IServer.PUBLISH_STATE_NONE;
	}
}
