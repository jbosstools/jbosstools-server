/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.internal.v7.DeploymentMarkerUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.LocalFilesystemController;

public class WildFlyFileSystemPublishController extends StandardFileSystemPublishController {
	protected int removeModule(IModule[] module, IPath remote, IProgressMonitor monitor) throws CoreException {
		boolean undeploySucceeded = undeployModuleAndWait(module, remote, 45000, monitor);
		if( !undeploySucceeded ) {
			ServerLogger.getDefault().log(getServer(), new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind("Safely halting deployment {0} on server {1} before publish has failed.", module[0].getName(), getServer().getName()))); //$NON-NLS-1$
		}
		return super.removeModule(module, remote, monitor);
	}
	protected IStatus[] executeFullPublish(IModule[] module, IPath archiveDestination, IModulePathFilter filter, IProgressMonitor monitor) 
			throws CoreException {
		boolean undeploySucceeded = undeployModuleAndWait(module, archiveDestination, 45000, monitor);
		if( !undeploySucceeded ) {
			ServerLogger.getDefault().log(getServer(), new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind("Safely halting deployment {0} on server {1} before publish has failed.", module[0].getName(), getServer().getName()))); //$NON-NLS-1$
		}
		return super.executeFullPublish(module, archiveDestination, filter, monitor);
	}
	protected boolean undeployModuleAndWait(IModule[] module, IPath remote, int maxWait, IProgressMonitor monitor) throws CoreException {
		if( module.length > 1) 
			return true;
		
		// Because this behavior requires undeploying first, and having a successful undeployment, 
		// we want to minimize when we do it to only include the windows usecase of local deployment
		// Theoretically it could still happen if using linux to publish to a remote windows??  
		// No idea how to test that. 
		if( supportsJBoss7Markers() && 
				Platform.getOS().equals(Platform.OS_WIN32) &&
				getServer().getServerState() == IServer.STATE_STARTED &&
				getFilesystemController() instanceof LocalFilesystemController ) {
			
			
			if( DeploymentMarkerUtils.markerExists(remote, getFilesystemController(), DeploymentMarkerUtils.DEPLOYED)) {
				DeploymentMarkerUtils.removeDeployedMarker(remote, getFilesystemController());
				long timeout = System.currentTimeMillis() + maxWait;
				boolean done = false;
				while( !done && System.currentTimeMillis() < timeout) {
					// Now wait
					done = DeploymentMarkerUtils.markerExists(remote, getFilesystemController(), DeploymentMarkerUtils.UNDEPLOYED);
					if( !done ) {
						try {
							Thread.sleep(500);
						} catch(InterruptedException ie) {}
					}
				}
				return done;
			}
		}
		return true;
	}
	
}
