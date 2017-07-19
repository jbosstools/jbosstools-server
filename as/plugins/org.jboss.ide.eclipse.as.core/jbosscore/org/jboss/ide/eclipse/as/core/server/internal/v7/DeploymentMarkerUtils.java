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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
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
	
	/**
	 * @since 3.1
	 */
	public static IStatus removeDeployFailedMarker(IPath module, IFilesystemController controller) throws CoreException {
		IPath folder = module.removeLastSegments(1);
		IPath p = folder.append(getFailedMarkerName(module.lastSegment()));
		return controller.deleteResource(p, new NullProgressMonitor());
	}
	
	@Deprecated // Spelling error
	public static IStatus removedDeployFailedMarker(IPath module, IFilesystemController controller) throws CoreException {
		return removeDeployFailedMarker(module, controller);
	}

	
	@Deprecated // Spelling error
	public static IStatus removedDeployedMarker(IPath module, IFilesystemController controller) throws CoreException {
		return removeDeployedMarker(module, controller);
	}
	
	/**
	 * @since 3.1
	 */
	public static IStatus removeDeployedMarker(IPath module, IFilesystemController controller) throws CoreException {
		IPath folder = module.removeLastSegments(1);
		IPath p = folder.append(getDeployedMarker(module.lastSegment()));
		return controller.deleteResource(p, new NullProgressMonitor());
	}

	@Deprecated // spelling error
	public static IStatus removedUndeployedMarker(IPath module, IFilesystemController controller) throws CoreException {
		return removeUndeployedMarker(module, controller);
	}
	/**
	 * @since 3.1
	 */
	public static IStatus removeUndeployedMarker(IPath module, IFilesystemController controller) throws CoreException {
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

