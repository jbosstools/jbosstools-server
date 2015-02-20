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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;

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

	/**
	 * Returns <code>true</code> if the given server supports the marker deployment method introduced in JBoss AS7.
	 * 
	 * @param the server to query
	 * @return <code>true</code> if the server supports this deployment method
	 */
	public static boolean supportsJBoss7MarkerDeployment(IServer server) {
		return server.loadAdapter(IJBoss7Deployment.class, new NullProgressMonitor()) != null;
	}

	public static String getDeployedMarker(String file) {
		return file + DEPLOYED;
	}

	public static String getUndeployedMarker(String file) {
		return file + UNDEPLOYED;
	}

	public static String getFailedMarkerName(String file) {
		return file + FAILED_DEPLOY;
	}

	public static String getDoDeployMarkerName(String file) {
		return file + DO_DEPLOY;
	}
	
	
	/*
	 * New methods for use with IFilesystemController
	 */
	public static IStatus removedDeployFailedMarker(IPath module, IFilesystemController controller) throws CoreException {
		IPath folder = module.removeLastSegments(1);
		IPath p = folder.append(getFailedMarkerName(module.lastSegment()));
		return controller.deleteResource(p, new NullProgressMonitor());
	}

	public static IStatus removedDeployedMarker(IPath module, IFilesystemController controller) throws CoreException {
		IPath folder = module.removeLastSegments(1);
		IPath p = folder.append(getDeployedMarker(module.lastSegment()));
		return controller.deleteResource(p, new NullProgressMonitor());
	}

	public static IStatus removedUndeployedMarker(IPath module, IFilesystemController controller) throws CoreException {
		IPath folder = module.removeLastSegments(1);
		IPath p = folder.append(getUndeployedMarker(module.lastSegment()));
		return controller.deleteResource(p, new NullProgressMonitor());
	}

	public static IStatus removeDoDeployMarker(IPath module, IFilesystemController controller) throws CoreException {
		IPath folder = module.removeLastSegments(1);
		IPath p = folder.append(getDoDeployMarkerName(module.lastSegment()));
		return controller.deleteResource(p, new NullProgressMonitor());
	}
	
	public static IStatus createDoDeployMarker(IPath module, IFilesystemController controller) throws CoreException {
		IPath folder = module.removeLastSegments(1);
		IPath p = folder.append(getDoDeployMarkerName(module.lastSegment()));
		return controller.touchResource(p, new NullProgressMonitor());
	}
	

	public static boolean markerExists(IPath module, IFilesystemController controller, String markerId) throws CoreException {
		IPath folder = module.removeLastSegments(1);
		IPath p = folder.append(module.lastSegment() + markerId);
		return controller.exists(p, new NullProgressMonitor());
	}
}

