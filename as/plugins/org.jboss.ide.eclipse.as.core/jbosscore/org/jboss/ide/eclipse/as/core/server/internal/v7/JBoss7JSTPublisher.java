/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.publishers.AbstractServerToolsPublisher;
import org.jboss.ide.eclipse.as.core.publishers.JSTPublisherXMLToucher;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class JBoss7JSTPublisher extends AbstractServerToolsPublisher {
	
	public IStatus publishModule(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		boolean useAS7Behavior = DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server);
		if( useAS7Behavior )
			return publishModuleToAS7(method, server, module, publishType, delta, monitor);
		else
			return super.publishModule(method, server, module, publishType, delta, monitor);
	}

	public IStatus publishModuleToAS7(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		Trace.trace(Trace.STRING_FINER, "Using AS7 publishModule logic in JBoss7JSTPublisher for module " + module[module.length-1].getName() ); //$NON-NLS-1$

		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		DeploymentMarkerUtils.removeDeployFailedMarker(method, server, ds.getDeploymentLocation(module, true), monitor);
		
		if( publishType == IJBossServerPublisher.REMOVE_PUBLISH) {
			IStatus s = DeploymentMarkerUtils.removeDeployedMarkerIfExists(method, ds, module, monitor);
			super.publishModule(method, server, module, publishType, delta, monitor);
			return s;
		} else {
			IStatus s = super.publishModule(method, server, module, publishType, delta, monitor);
			if( module.length == 1 && 
					publishType == IJBossServerPublisher.FULL_PUBLISH) {
				// Only mark a doDeploy file for the root module, but this must be delayed, 
				// becuase we don't know how many children modules will get published here (SUCK)
				doDeployRequired(method, ds, module, monitor);
			}
			return s;
		}
	}    
	
	@Override
	protected void markModuleRequiresRestart(IPath deployPath, IModule[] moduleTree,
			IJBossServerPublishMethod method, IPublishCopyCallbackHandler handler) throws CoreException {
		boolean useAS7Behavior = DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server.getServer());
		System.out.println("Mark " + deployPath + " for restart");  //$NON-NLS-1$//$NON-NLS-2$
		if( !useAS7Behavior) {
			// Simply touch the descriptor as needed
			JSTPublisherXMLToucher.getInstance().touch(deployPath, 
					moduleTree[moduleTree.length-1], handler);
		} else {
			// Mark this module as if it needs a module restart
			doDeployRequired(method, this.server, moduleTree, new NullProgressMonitor());
		}
	}

	private void doDeployRequired(IJBossServerPublishMethod method,IDeployableServer server,
			IModule[] moduleTree, IProgressMonitor monitor ) throws CoreException {
		IPath p = server.getDeploymentLocation(moduleTree, true);
		Trace.trace(Trace.STRING_FINER, "Marking path " + p + " as requiring a .dodeploy marker, but NOT creating the file yet"); //$NON-NLS-1$ //$NON-NLS-2$
		DelegatingJBoss7ServerBehavior beh = ServerConverter.getJBoss7ServerBehavior(server.getServer());
		beh.markDoDeploy(p);
	}
}
