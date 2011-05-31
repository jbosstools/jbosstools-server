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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;

/**
 * 
 * @author André Dietisheim
 *
 */
public class DeploymentMarkerUtils {

	public static final String DEPLOYED = ".deployed"; //$NON-NLS-1$
	public static final String FAILED_DEPLOY = ".failed";//$NON-NLS-1$
	public static final String DO_DEPLOY = ".dodeploy";//$NON-NLS-1$
	public static final String DEPLOYING = ".isdeploying";//$NON-NLS-1$
	public static final String UNDEPLOYING = ".isundeploying";//$NON-NLS-1$
	public static final String UNDEPLOYED = ".undeployed";//$NON-NLS-1$
	public static final String SKIP_DEPLOY = ".skipdeploy";//$NON-NLS-1$
	public static final String PENDING = ".pending";//$NON-NLS-1$

	public static IStatus addDeployMarker(IJBossServerPublishMethod method,IDeployableServer server,
			IModule[] moduleTree, IProgressMonitor monitor ) throws CoreException {
		IPath depPath = PublishUtil.getDeployPath(method, moduleTree, server);
		return addDeployMarker(method, server.getServer(), depPath, monitor);
	}

	public static IStatus addDeployMarker(IJBossServerPublishMethod method,IServer server,
			IPath depPath, IProgressMonitor monitor ) throws CoreException {
		IPath folder = depPath.removeLastSegments(1);
		IPublishCopyCallbackHandler callback = method.getCallbackHandler(folder, server);
		callback.copyFile(createBlankModule(), new Path(depPath.lastSegment() + DO_DEPLOY), monitor);
		return Status.OK_STATUS;
	}
	
	private static IModuleFile createBlankModule() {
		return new ModuleFile(getBlankFile(), "", new Path("/")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private static File getBlankFile() {
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
	
	public static IStatus removeDeployFailedMarker(
			IServer server, IPath depPath,
			IJBossServerPublishMethod method,
			IProgressMonitor monitor) throws CoreException {
		return removeFile(FAILED_DEPLOY, server, depPath, method, monitor);
	}

	private static IStatus removeFile(String suffix,IServer server, IPath depPath,IJBossServerPublishMethod method,
	IProgressMonitor monitor) throws CoreException {
		IPath folder = depPath.removeLastSegments(1);
		IPublishCopyCallbackHandler callback = method.getCallbackHandler(folder, server);
		IPath file = new Path(depPath.lastSegment()+suffix);
		callback.deleteResource(file, monitor);
		return Status.OK_STATUS;
	}
	
	public static IStatus removeDeployedMarkerIfExists(IJBossServerPublishMethod method, IDeployableServer jbServer, IModule[] module, IProgressMonitor monitor) 
			throws CoreException {
		return removeDeployedMarkerIfExists(
				jbServer.getServer(), PublishUtil.getDeployPath(method, module, jbServer), method, monitor);
	}

	public static IStatus removeDeployFailedMarkerIfExists(IJBossServerPublishMethod method, IDeployableServer jbServer, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		IPath depPath = PublishUtil.getDeployPath(method, module, jbServer);
		return removeDeployedMarkerIfExists(jbServer.getServer(), depPath, method, monitor);
	}

	public static IStatus removeDeployedMarkerIfExists(IServer server, IPath depPath, IJBossServerPublishMethod method, IProgressMonitor monitor) 
			throws CoreException {
		try {
			return removeFile(DEPLOYED, server, depPath, method, monitor);
		} catch(Exception e) {
			return Status.OK_STATUS;
		}
	}
	
	public static boolean supportsJBoss7MarkerDeployment(IServer server) {
		boolean retval = (server.loadAdapter(IJBoss7Deployment.class, new NullProgressMonitor()) != null);
		return retval;
	}


}
