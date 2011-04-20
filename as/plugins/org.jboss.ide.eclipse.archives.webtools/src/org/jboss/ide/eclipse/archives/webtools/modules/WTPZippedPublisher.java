/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
 ******************************************************************************/ 
package org.jboss.ide.eclipse.archives.webtools.modules;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7Server;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class WTPZippedPublisher implements IJBossServerPublisher {
	private int moduleState = IServer.PUBLISH_STATE_NONE;
	
	public boolean accepts(String method, IServer server, IModule[] module) {
		if( (publishMethodSpecific() && !method.equals(getTargetedPublishMethodId())))
			return false;
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		IModule lastMod = (module == null || module.length == 0 ) ? null : module[module.length -1];
		if( getPublishMethod().equals(method) && lastMod == null)
			return true;
		return getPublishMethod().equals(method) 
			&& ModuleCoreNature.isFlexibleProject(lastMod.getProject())
			&& ds != null && ds.zipsWTPDeployments();
	}
	
	protected boolean publishMethodSpecific() {
		return true;
	}
	
	protected String getTargetedPublishMethodId() {
		return getPublishMethod();
	}
	
	protected String getPublishMethod() {
		return LocalPublishMethod.LOCAL_PUBLISH_METHOD;
	}

	public int getPublishState() {
		return moduleState;
	}
	
	protected String getDeployRoot(IModule[] module, IDeployableServer ds) {
		return PublishUtil.getDeployRootFolder(
				module, ds, ds.getDeployFolder(), 
				IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC);
	}
	
	public IStatus publishModule(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		// Build all parts together at once. 
		// When a call for [ear, childWar] comes in, ignore it. 
		if( module.length > 1 ) 
			return null;
	
		if( JBoss7Server.supportsJBoss7MarkerDeployment(server))
			return handleJBoss7Deployment(method, server, module, publishType, delta, monitor);
		
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		String deployRoot = getDeployRoot(module, ds); 
		LocalZippedPublisherUtil util = new LocalZippedPublisherUtil();
		IStatus s = util.publishModule(server, deployRoot, module, publishType, delta, monitor);
		monitor.done();
		return s;
	}
	
	public IStatus handleJBoss7Deployment(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		String deployRoot = getDeployRoot(module, ds);
		if( publishType == IJBossServerPublisher.REMOVE_PUBLISH) {
			JBoss7JSTPublisher.removeDeployedMarkerFile(method, ds, module, monitor);
		} else {
			LocalZippedPublisherUtil util = new LocalZippedPublisherUtil();
			IStatus s = util.publishModule(server, deployRoot, module, publishType, delta, monitor);
			if( util.hasBeenChanged()) {
				JBoss7JSTPublisher.addDoDeployMarkerFile(method, ds, module, new NullProgressMonitor());
			}
			monitor.done();
			return s;
		}
		monitor.done();
		return Status.OK_STATUS;
	}
}
