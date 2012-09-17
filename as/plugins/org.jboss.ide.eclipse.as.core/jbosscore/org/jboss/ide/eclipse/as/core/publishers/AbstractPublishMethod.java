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
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IDeployableServerBehaviour;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;

public abstract class AbstractPublishMethod implements IJBossServerPublishMethod {
	public abstract String getPublishMethodId();

	public void publishStart(IDeployableServerBehaviour behaviour,
			IProgressMonitor monitor) throws CoreException {
		Trace.trace(Trace.STRING_FINER, "Publish start in " + getClass().getName()); //$NON-NLS-1$
	}

	public int publishFinish(IDeployableServerBehaviour behaviour,
			IProgressMonitor monitor) throws CoreException {
		Trace.trace(Trace.STRING_FINER, "Beginning publishFinish in " + getClass().getName()); //$NON-NLS-1$
        int result = getServerPublishState(behaviour);
		Trace.trace(Trace.STRING_FINER, "Ending publishFinish with server restart state of " + result); //$NON-NLS-1$
        return result;
	}
	
	public int getServerPublishState(IDeployableServerBehaviour behaviour) {
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

	public int publishModule(IDeployableServerBehaviour behaviour, int kind,
			int deltaKind, IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		Trace.trace(Trace.STRING_FINER, "Beginning to publish module " + module[module.length-1].getName()); //$NON-NLS-1$

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
			Trace.trace(Trace.STRING_FINER, "Publisher for module " + module[module.length-1].getName() + " is " + publisher.getClass().getName());  //$NON-NLS-1$//$NON-NLS-2$
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
					Trace.trace(Trace.STRING_FINER, "Exception publishing module: " + ce.getMessage()); //$NON-NLS-1$
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
