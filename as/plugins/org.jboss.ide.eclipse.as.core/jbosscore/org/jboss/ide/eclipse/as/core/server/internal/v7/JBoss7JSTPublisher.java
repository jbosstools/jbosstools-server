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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.publishers.AbstractServerToolsPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class JBoss7JSTPublisher extends AbstractServerToolsPublisher {

	// Same as super class but just a *bit* different
	public boolean accepts(String method, IServer server, IModule[] module) {
		return super.accepts(method, server, module);
		// && JBoss7Server.supportsJBoss7MarkerDeployment(server);
	}
	
	public IStatus publishModuleToAS7(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);

		DeploymentMarkerUtils.removeDeployFailedMarker(method, server, PublishUtil.getDeployPath(method, module, ds), monitor);
		
		if( publishType == IJBossServerPublisher.REMOVE_PUBLISH) {
			DeploymentMarkerUtils.removeDeployedMarkerIfExists(method, ds, module, monitor);
		} else {
			IStatus s = super.publishModule(method, server, module, publishType, delta, monitor);
			if( module.length == 1 && 
					publishType == IJBossServerPublisher.FULL_PUBLISH || 
					publishType == IJBossServerPublisher.INCREMENTAL_PUBLISH) {
				// Only mark a doDeploy file for the root module, but this must be delayed, 
				// becuase we don't know how many children modules will get published here (SUCK)
				JBoss7JSTPublisher.markDeployed(method, ds, module, monitor);
			}
			return s;
		}
		return Status.OK_STATUS;
	}
	
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
    
	
	public static final String MARK_DO_DEPLOY = "org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher.markUndeploy"; //$NON-NLS-1$
//	public static final String MARK_UNDEPLOY = "org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher.markUndeploy"; //$NON-NLS-1$

	public static void markDeployed(IJBossServerPublishMethod method,IDeployableServer server,
			IModule[] moduleTree, IProgressMonitor monitor ) throws CoreException {
		IPath p = PublishUtil.getDeployPath(method, moduleTree, server);
		DeployableServerBehavior beh = ServerConverter.getDeployableServerBehavior(server.getServer());
		Object o = beh.getPublishData(MARK_DO_DEPLOY);
		if(!(o instanceof List<?>)) {
			o = new ArrayList<IPath>();
			beh.setPublishData(MARK_DO_DEPLOY, o);
		}
		List<IPath> list = (List<IPath>)o;
		if( !list.contains(p))
			list.add(p);
	}
	
//	public static void markUndeployed(
//			IJBossServerPublishMethod method,
//			IDeployableServer server, IModule[] moduleTree,
//			IProgressMonitor monitor) throws CoreException {
//		IPath p = PublishUtil.getDeployPath(method, moduleTree, server);
//		DeployableServerBehavior beh = ServerConverter.getDeployableServerBehavior(server.getServer());
//		Object o = beh.getPublishData(MARK_UNDEPLOY);
//		if( o == null || !(o instanceof ArrayList<?>)) {
//			o = new ArrayList<IPath>();
//			beh.setPublishData(MARK_UNDEPLOY, o);
//		}
//		ArrayList<IPath> list = (ArrayList<IPath>)o;
//		if( !list.contains(p))
//			list.add(p);
//	}
}
