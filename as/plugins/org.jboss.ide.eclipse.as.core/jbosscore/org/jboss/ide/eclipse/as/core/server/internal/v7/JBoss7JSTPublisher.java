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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.AbstractJSTPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class JBoss7JSTPublisher extends AbstractJSTPublisher {
	// Same as super class but just a *bit* different
	public boolean accepts(String method, IServer server, IModule[] module) {
		return super.accepts(method, server, module) && 
			JBoss7Server.supportsJBoss7MarkerDeployment(server);
	}
	
	public static final String DEPLOYED = ".deployed"; //$NON-NLS-1$
	public static final String FAILED_DEPLOY = ".failed";//$NON-NLS-1$
	public static final String DO_DEPLOY = ".dodeploy";//$NON-NLS-1$
	public static final String DEPLOYING = ".isdeploying";//$NON-NLS-1$
	public static final String UNDEPLOYING = ".isundeploying";//$NON-NLS-1$
	public static final String UNDEPLOYED = ".undeployed";//$NON-NLS-1$
	public static final String SKIP_DEPLOY = ".skipdeploy";//$NON-NLS-1$
	public static final String PENDING = ".pending";//$NON-NLS-1$

	public IStatus publishModule(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		//IJBoss7Manager service = JBoss7ManagerUtil.findManagementService(server);
		
		// jboss-7 specific
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		if( publishType == IJBossServerPublisher.REMOVE_PUBLISH) {
			if( JBoss7Server.supportsJBoss7MarkerDeployment(server) )
				JBoss7JSTPublisher.removeDeployedMarkerFile(method, ds, module, monitor);
			else 
				super.publishModule(method, server, module, publishType, delta, monitor); 
		} else {
			IStatus s = super.publishModule(method, server, module, publishType, delta, monitor);
			if( JBoss7Server.supportsJBoss7MarkerDeployment(server) ) {
				if( module.length == 1 && 
						publishType == IJBossServerPublisher.FULL_PUBLISH || 
						publishType == IJBossServerPublisher.INCREMENTAL_PUBLISH) {
					// Only mark a doDeploy file for the root module, but this must be delayed, 
					// becuase we don't know how many children modules will get published here (SUCK)
					JBoss7JSTPublisher.markDeployed(method, ds, module, monitor);
				}
			}
			return s;
		}
		return Status.OK_STATUS;
	}
    
	
	public static final String MARK_DO_DEPLOY = "org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher.markUndeploy"; //$NON-NLS-1$
//	public static final String MARK_UNDEPLOY = "org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher.markUndeploy"; //$NON-NLS-1$

	public static void markDeployed(IJBossServerPublishMethod method,IDeployableServer server,
			IModule[] moduleTree, IProgressMonitor monitor ) throws CoreException {
		IPath p = PublishUtil.getDeployPath(method, moduleTree, server);
		DeployableServerBehavior beh = ServerConverter.getDeployableServerBehavior(server.getServer());
		Object o = beh.getPublishData(MARK_DO_DEPLOY);
		if( o == null || !(o instanceof ArrayList<?>)) {
			o = new ArrayList<IPath>();
			beh.setPublishData(MARK_DO_DEPLOY, o);
		}
		ArrayList<IPath> list = (ArrayList<IPath>)o;
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
	
	public static IStatus addDoDeployMarkerFile(IJBossServerPublishMethod method,IDeployableServer server,
			IModule[] moduleTree, IProgressMonitor monitor ) throws CoreException {
		IPath depPath = PublishUtil.getDeployPath(method, moduleTree, server);
		return addDoDeployMarkerFile(method, server.getServer(), depPath, monitor);
	}

	public static IStatus addDoDeployMarkerFile(IJBossServerPublishMethod method,IServer server,
			IPath depPath, IProgressMonitor monitor ) throws CoreException {
		IPath folder = depPath.removeLastSegments(1);
		IPublishCopyCallbackHandler callback = method.getCallbackHandler(folder, server);
		callback.copyFile(createBlankModuleFile(), new Path(depPath.lastSegment() + DO_DEPLOY), monitor);
		return Status.OK_STATUS;
	}

	
	public static IStatus removeDeployedMarkerFile(
			IJBossServerPublishMethod method,
			IDeployableServer jbServer, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		IPath depPath = PublishUtil.getDeployPath(method, module, jbServer);
		return removeDeployedMarkerFile(jbServer.getServer(), depPath, method, monitor);
	}
	public static IStatus removeDeployedMarkerFile(
			IServer server, IPath depPath,
			IJBossServerPublishMethod method,
			IProgressMonitor monitor) throws CoreException {
		IPath folder = depPath.removeLastSegments(1);
		IPublishCopyCallbackHandler callback = method.getCallbackHandler(folder, server);
		IPath deployed = new Path(depPath.lastSegment()+DEPLOYED);
		callback.deleteResource(deployed, monitor);
		return Status.OK_STATUS;
	}

	
	public static IModuleFile createBlankModuleFile() {
		return new ModuleFile(getBlankFile(), "", new Path("/")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	protected static File getBlankFile() {
		IPath p = JBossServerCorePlugin.getDefault().getStateLocation().append("BLANK_FILE"); //$NON-NLS-1$
		if( !p.toFile().exists()) {
			try {
				OutputStream out = new FileOutputStream(p.toFile());
				if (out != null) {
					out.close();
				}
			} catch(IOException ioe) {}
		}
		return p.toFile();
	}
}
