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
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * 
 * @author André Dietisheim
 * @author Rob Stryker
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

	/**
	 * Adds a marker to the given deployment artifact (in form of a module tree) that
	 * instructs the server to deploy it. (.dodeploy)
	 * 
	 * @param method the method to use to add the marker
	 * @param server the server that the marker shall be added to
	 * @param moduleTree the deployment(-tree)
	 * @param monitor the monitor to use to give progress feedback
	 * @return the result of the marker addition operation
	 * @throws CoreException
	 */
	public static IStatus addDoDeployMarker(IJBossServerPublishMethod method, IDeployableServer server,
			IModule[] moduleTree, IProgressMonitor monitor) throws CoreException {
		IPath depPath = PublishUtil.getDeployPath(method, moduleTree, server);
		return addDoDeployMarker(method, server.getServer(), depPath, monitor);
	}

	/**
	 * Adds a marker to the given deployment (in form of a path) that
	 * instructs the server to deploy the given artifact. (.dodeploy)
	 * 
	 * @param method the method to use to add the marker
	 * @param server the server that the marker shall be added to
	 * @param depPath the path of the deployment artifact
	 * @param monitor the monitor to use to give progress feedback
	 * @return the result of the marker addition operation
	 * @throws CoreException
	 */
	public static IStatus addDoDeployMarker(IJBossServerPublishMethod method, IServer server,
			IPath depPath, IProgressMonitor monitor) throws CoreException {
		IPath folder = depPath.removeLastSegments(1);
		IPublishCopyCallbackHandler callback = method.getCallbackHandler(folder, server);
		callback.copyFile(createBlankModule(), new Path(depPath.lastSegment() + DO_DEPLOY), monitor);
		return Status.OK_STATUS;
	}

	private static IModuleFile createBlankModule() {
		return new ModuleFile(getOrCreateBlankFile(), "", new Path("/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static File getOrCreateBlankFile() {
		IPath p = JBossServerCorePlugin.getDefault().getStateLocation().append("BLANK_FILE"); //$NON-NLS-1$
		if (!p.toFile().exists()) {
			try {
				OutputStream out = new FileOutputStream(p.toFile());
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
				// TODO: implement error handling
			}
		}
		return p.toFile();
	}

	public static IStatus removeDeployFailedMarker(IServer server, IPath depPath, IJBossServerPublishMethod method,
			IProgressMonitor monitor) throws CoreException {
		return removeFile(FAILED_DEPLOY, server, depPath, method, monitor);
	}

	/**
	 * Removes the marker that indicates that the given deployment (in form of a module tree) 
	 * was deployed (.deployed). Does nothing if the marker does not exist.
	 * 
	 * @param method the method to use to manipulate the files on the server
	 * @param server the server to manipulate
	 * @param moduleTree the deployment (in form of a module tree)
	 * @param monitor the monitor to use when giving progress feedback
	 * @return the result of the removal operation
	 * @throws CoreException
	 */
	public static IStatus removeDeployedMarkerIfExists(IJBossServerPublishMethod method, IServer server,
			IModule[] module, IProgressMonitor monitor)	throws CoreException {
		IDeployableServer deployableServer = ServerConverter.getDeployableServer(server);
		IPath deployPath = PublishUtil.getDeployPath(method, module, deployableServer);
		return removeDeployedMarkerIfExists(server, deployPath, method, monitor);
	}

	/**
	 * Removes the marker that indicates that the given deployment (in form of a module tree) 
	 * was deployed (.deployed). Does nothing if the marker does not exist.
	 * 
	 * @param method the method to use to manipulate the files on the server
	 * @param jbServer the server to manipulate
	 * @param moduleTree the deployment (in form of a module tree)
	 * @param monitor the monitor to use when giving progress feedback
	 * @return the result of the removal operation
	 * @throws CoreException
	 */
	public static IStatus removeDeployedMarkerIfExists(IJBossServerPublishMethod method, IDeployableServer jbServer,
			IModule[] moduleTree, IProgressMonitor monitor)
			throws CoreException {
		IPath deployPath = PublishUtil.getDeployPath(method, moduleTree, jbServer);
		return removeDeployedMarkerIfExists(jbServer.getServer(), deployPath, method, monitor);
	}

	/**
	 * Removes the marker that indicates that the given deployment (in form of a artifact path) 
	 * was deployed (.deployed). Does nothing if the marker does not exist.
	 * 
	 * @param server the server to remove the marker from
	 * @param method the method to use to manipulate the marker on the server
	 * @param depPath the path of the artifact to remove the marker of
	 * @param monitor the monitor to use when giving progress feedback
	 * @return the result of the removal operation
	 * @throws CoreException
	 */
	public static IStatus removeDeployedMarkerIfExists(IServer server, IPath depPath, IJBossServerPublishMethod method,
			IProgressMonitor monitor) throws CoreException {
		try {
			return removeFile(DEPLOYED, server, depPath, method, monitor);
		} catch (Exception e) {
			return Status.OK_STATUS;
		}
	}

	/**
	 * Removes the marker that indicates that the given deployment (in form of a module tree) 
	 * could not be deployed (.failed). Does nothing if the marker does not exist.
	 * 
	 * @param method the method to use to manipulate the files on the server
	 * @param server the server to manipulate
	 * @param moduleTree the deployment (in form of a module tree)
	 * @param monitor the monitor to use when giving progress feedback
	 * @return the result of the removal operation
	 * @throws CoreException
	 */
	public static IStatus removeDeployFailedMarkerIfExists(IJBossServerPublishMethod method, IServer server,
			IModule[] module, IProgressMonitor monitor) throws CoreException {
		IDeployableServer deployableServer = ServerConverter.getDeployableServer(server);
		return removeDeployedMarkerIfExists(method, deployableServer, module, monitor);
	}

	/**
	 * Removes the marker that indicates that the given deployment (in form of a module tree) 
	 * could not be deployed (.failed). Does nothing if the marker does not exist.
	 * 
	 * @param method the method to use to manipulate the files on the server
	 * @param jbServer the server to manipulate
	 * @param moduleTree the deployment (in form of a module tree)
	 * @param monitor the monitor to use when giving progress feedback
	 * @return the result of the removal operation
	 * @throws CoreException
	 */
	public static IStatus removeDeployFailedMarkerIfExists(IJBossServerPublishMethod method,
			IDeployableServer jbServer, IModule[] module, IProgressMonitor monitor) throws CoreException {
		IPath depPath = PublishUtil.getDeployPath(method, module, jbServer);
		return removeDeployedMarkerIfExists(jbServer.getServer(), depPath, method, monitor);
	}

	/**
	 * Returns <code>true</code> if the given server supports the marker deployment method introduced in JBoss AS7.
	 * 
	 * @param the server to query
	 * @return <code>true</code> if the server supports this deployment method
	 */
	public static boolean supportsJBoss7MarkerDeployment(IServer server) {
		return server.loadAdapter(IJBoss7Deployment.class, new NullProgressMonitor()) != null;
	}

	private static IStatus removeFile(String suffix, IServer server, IPath depPath, IJBossServerPublishMethod method,
			IProgressMonitor monitor) throws CoreException {
		IPath folder = depPath.removeLastSegments(1);
		IPublishCopyCallbackHandler callback = method.getCallbackHandler(folder, server);
		IPath file = new Path(depPath.lastSegment() + suffix);
		callback.deleteResource(file, monitor);
		return Status.OK_STATUS;
	}

}
