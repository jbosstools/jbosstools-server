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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.AbstractServerToolsPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.v7.DeploymentMarkerUtils;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * This class is in charge of RSE zipped publishing for flexible projects.
 * It extends the functionality of the local zipped publishing class
 * by uploading the file after building it in a temporary directory
 */
public class AltMethodZippedJSTPublisher extends WTPZippedPublisher {
	/**
	 * Here we put the deployment first in a temporary remote deploy folder
	 * Then during the publishModule call, we'll also upload it to remote machine
	 */
	protected String getDeployRoot(IModule[] module, IDeployableServer ds) {
		IPath deployRoot = JBossServerCorePlugin.getServerStateLocation(ds.getServer()).
			append(IJBossToolingConstants.TEMP_REMOTE_DEPLOY).makeAbsolute();
		deployRoot.toFile().mkdirs();
		return deployRoot.toString();
	}
	
	private IModule[] module;
	private IServer server;
	private IJBossServerPublishMethod method;
	
	@Override
	public IStatus publishModule(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		try {
			this.module = module;
			this.server = server;
			this.method = method;
			
			String taskName = "Publishing " + module[0].getName();  //$NON-NLS-1$
			monitor.beginTask(taskName, 200);
			monitor.setTaskName(taskName);
			if( module.length > 1 ) {
				return null;
			}
			
			monitor.setTaskName("Publishing to remote server " + server.getName()); //$NON-NLS-1$
			
			// set up needed vars
			IDeployableServer server2 = ServerConverter.getDeployableServer(server);
			String remoteTempDeployRoot = getDeployRoot(module, ServerConverter.getDeployableServer(server));
			IPath sourcePath = PublishUtil.getDeployPath(module, remoteTempDeployRoot, server2);
			IPath destFolder = PublishUtil.getDeployPath(method, module, server2);
			String name = sourcePath.lastSegment();
			IStatus result = null;
			
			removeDeployFailedMarker(monitor);
			
			// Am I a removal? If yes, remove me, and return
			if( publishType == IJBossServerPublisher.REMOVE_PUBLISH) {
				result = removeRemoteDeployment(sourcePath, destFolder, name, monitor);
			} else if( publishType != IJBossServerPublisher.NO_PUBLISH ){
				// Locally zip it up into the remote tmp folder
				result = super.publishModule(method, server, module, publishType, delta, 
						AbstractServerToolsPublisher.getSubMon(monitor, 50));
				if( result.isOK() ) {
					result = remoteFullPublish(sourcePath, destFolder.removeLastSegments(1), name, 
							AbstractServerToolsPublisher.getSubMon(monitor, 150));
				}
			}
	
			if( result == null ) {
				result = Status.OK_STATUS;
			}
	
			return result;
		} finally {
			monitor.done();
		}
	}
	
	private IStatus remoteFullPublish(IPath sourcePath, 
			IPath destFolder, String name, IProgressMonitor monitor) {
		// Now transfer the file to RSE
		try {
			deleteRemoteResource(destFolder, name, new NullProgressMonitor());
			IModuleFile mf = new ModuleFile(sourcePath.toFile(), name, new Path("/")); //$NON-NLS-1$
			method.getCallbackHandler(destFolder, server).copyFile(mf, new Path(name),
					AbstractServerToolsPublisher.getSubMon(monitor, 150)
			);
			if( DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server)) 
				DeploymentMarkerUtils.addDeployMarker(method, ServerConverter.getDeployableServer(server), module, monitor);
		} catch(CoreException ce) {
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}

	private IStatus removeDeployFailedMarker(IProgressMonitor monitor) {
		try {
		if (DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server)) {
			return DeploymentMarkerUtils.removeDeployFailedMarkerIfExists(method, server, module, monitor);
		}
		} catch(CoreException ce) {
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}
	
	private IStatus removeRemoteDeployment( IPath sourcePath, 
			IPath destFolder, String name, IProgressMonitor monitor) throws CoreException {
		try {
			if( DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server))
				return DeploymentMarkerUtils.removeDeployedMarkerIfExists(method, server, module, monitor);
			return deleteRemoteResource(destFolder, name, monitor);
		} catch(CoreException ce) {
			return ce.getStatus();
		}
	}
	
	/**
	 * Removes the resource with the given name from the given parent folder. Either files or folders are removed.   
	 * @param sourcePath
	 * @param destFolder
	 * @param name
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	private IStatus deleteRemoteResource(IPath destFolder, String name, IProgressMonitor monitor) throws CoreException {
		// Now delete the file from RSE
		// TODO *** FIX THIS IT IS NOT LOGGING ERRORS
		IStatus[] tmp = method.getCallbackHandler(destFolder, server).deleteResource(new Path(name), monitor);
		return Status.OK_STATUS;
	}
}
