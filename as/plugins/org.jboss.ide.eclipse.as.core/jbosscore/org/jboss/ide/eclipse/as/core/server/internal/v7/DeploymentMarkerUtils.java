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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;
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
		IPath depPath = server.getDeploymentLocation(moduleTree, true);
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
		IPath lastSegment = new Path(depPath.lastSegment() + DO_DEPLOY);
		callback.copyFile(createBlankModule(), lastSegment, monitor);
		Trace.trace(Trace.STRING_FINER, "Creating dodeploy file: " + folder.append(lastSegment)); //$NON-NLS-1$
		return Status.OK_STATUS;
	}

	public static IStatus addDoDeployMarker(IJBossServerPublishMethod method, IServer server,
			List<IPath> deployPaths, IProgressMonitor monitor) throws CoreException {
		for(Iterator<IPath> pathsIterator = deployPaths.iterator(); pathsIterator.hasNext(); ) {
			addDoDeployMarker(method, server, pathsIterator.next(), new SubProgressMonitor(monitor, 1));
		}
		return Status.OK_STATUS;
	}

	private static IModuleFile createBlankModule() throws CoreException {
		return new ModuleFile(getOrCreateBlankFile(), "", new Path("/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static File getOrCreateBlankFile() throws CoreException {
		IPath p = JBossServerCorePlugin.getDefault().getStateLocation().append("BLANK_FILE"); //$NON-NLS-1$
		if (!p.toFile().exists()) {
			try {
				OutputStream out = new FileOutputStream(p.toFile());
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
				throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, ioe.getMessage(), ioe));
			}
		}
		return p.toFile();
	}

	public static IStatus removeDeployFailedMarker(IJBossServerPublishMethod method, IServer server, IPath depPath, 
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
		IPath deployPath = deployableServer.getDeploymentLocation(module, true);
		return removeDeployedMarkerIfExists(method, server, deployPath, monitor);
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
		IPath deployPath = jbServer.getDeploymentLocation(moduleTree, true);
		return removeDeployedMarkerIfExists(method, jbServer.getServer(), deployPath, monitor);
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
	public static IStatus removeDeployedMarkerIfExists(IJBossServerPublishMethod method, IServer server, IPath depPath, 
			IProgressMonitor monitor) throws CoreException {
		Trace.trace(Trace.STRING_FINER, "Removing deployment marker file on path " + depPath); //$NON-NLS-1$

		try {
			return removeFile(DEPLOYED, server, depPath, method, monitor);
		} catch (CoreException e) {
			// Not a critical error that should abort the publish. 
			// Simply return the status object
			return e.getStatus();
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
		IPath deployPath = deployableServer.getDeploymentLocation(module, true);
		return removeFile(FAILED_DEPLOY, server, deployPath, method, monitor);
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
		String deploymentName = depPath.lastSegment();
		if (deploymentName != null) {
			IPath file = new Path(deploymentName + suffix);
			callback.deleteResource(file, monitor);
		}
		return Status.OK_STATUS;
	}

	public static String getDeployedMarker(String file) {
		return file + DEPLOYED;
	}

	public static String getFailedMarkerName(String file) {
		return file + FAILED_DEPLOY;
	}

	public static String getDoDeployMarkerName(String file) {
		return file + DO_DEPLOY;
	}
}

