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

import java.io.File;

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
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.publishers.AbstractServerToolsPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.server.internal.v7.DeploymentMarkerUtils;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * This class is in charge of RSE zipped publishing for flexible projects.
 * It extends the functionality of the local zipped publishing class
 * by uploading the file after building it in a temporary directory
 */
public class AltMethodZippedJSTPublisher implements IJBossServerPublisher {
	
	private int moduleState = IServer.PUBLISH_STATE_NONE;
	private boolean requiresTransfer = true;
	
	public boolean accepts(String method, IServer server, IModule[] module) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		return ds != null && (module == null || ds.zipsWTPDeployments());
	}
	
	public int getPublishState() {
		return moduleState;
	}
	public boolean requiresTransfer() {
		return requiresTransfer;
	}
	
	protected IStatus localTempPublishModule(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		// Build all parts together at once. 
		// When a call for [ear, childWar] comes in, ignore it. 
		IStatus status = Status.OK_STATUS;
		
		if( module.length > 1 ) 
			return null;
		
		if( DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server)) {
			requiresTransfer = false;
			status = handleJBoss7Deployment(method, server, module, publishType, delta, monitor);
		} else {		
			Trace.trace(Trace.STRING_FINER, "Using as<=6 publishModule logic in WTPZippedPublisher for module " + module[module.length-1].getName() ); //$NON-NLS-1$
			requiresTransfer = true;
			IDeployableServer ds = ServerConverter.getDeployableServer(server);
			String deployRoot = getDeployRoot(module, ds); 
			LocalZippedPublisherUtil util = new LocalZippedPublisherUtil();
			status = util.publishModule(server, deployRoot, module, publishType, delta, monitor);
			Trace.trace(Trace.STRING_FINER, "Zipping complete for module " + module[module.length-1].getName() ); //$NON-NLS-1$			monitor.done();
		}
		return status;
	}
	
	public IStatus handleJBoss7Deployment(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		String deployRoot = getDeployRoot(module, ds);
		if( publishType == IJBossServerPublisher.REMOVE_PUBLISH) {
			Trace.trace(Trace.STRING_FINER, "Removing .dodeploy marker in WTPZippedPublisher to undeploy module " + module[module.length-1].getName() ); //$NON-NLS-1$
			DeploymentMarkerUtils.removeDeployedMarkerIfExists(method, ds, module, monitor);
		} else {
			Trace.trace(Trace.STRING_FINER, "Zipping module in WTPZippedPublisher for module " + module[module.length-1].getName() ); //$NON-NLS-1$
			LocalZippedPublisherUtil util = new LocalZippedPublisherUtil();
			IStatus s = util.publishModule(server, deployRoot, module, publishType, delta, monitor);
			IPath outPath = util.getOutputFilePath();
			if( util.hasBeenChanged()) {
				Trace.trace(Trace.STRING_FINER, "Output zip changed. Copying file to destination. WTPZippedPublisher for module " + module[module.length-1].getName() ); //$NON-NLS-1$

				// Copy out file
				IPath depPath = ds.getDeploymentLocation(module, true);
				IPath folder = depPath.removeLastSegments(1);
				IPublishCopyCallbackHandler callback = method.getCallbackHandler(folder, server);
				IModuleFile mf = new ModuleFile(outPath.toFile(), "", new Path("/")); //$NON-NLS-1$ //$NON-NLS-2$
				callback.copyFile(mf, new Path(depPath.lastSegment()), monitor);

				// Add marker
				DeploymentMarkerUtils.addDoDeployMarker(method, ds, module, new NullProgressMonitor());
			}
			monitor.done();
			return s;
		}
		monitor.done();
		return Status.OK_STATUS;
	}
	
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
	
	public IStatus publishModule(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		
		this.module = module;
		this.server = server;
		this.method = method;
		
		String taskName = "Publishing " + module[0].getName();  //$NON-NLS-1$
		monitor.beginTask(taskName, 200);
		monitor.setTaskName(taskName);
		if( module.length > 1 ) {
			// Skip any module of length > 1, since a zip does it 
			// all in one attempt.
			return null;
		}
		
		monitor.setTaskName("Publishing to remote server " + server.getName()); //$NON-NLS-1$
		
		try {
			
			// set up needed vars
			IDeployableServer server2 = ServerConverter.getDeployableServer(server);
			String remoteTempDeployRoot = getDeployRoot(module, ServerConverter.getDeployableServer(server));
			IPath sourcePath = PublishUtil.getModuleNestedDeployPath(module, remoteTempDeployRoot, server2);
			IPath destination = server2.getDeploymentLocation(module, true);
			String name = sourcePath.lastSegment();
			IStatus result = null;
			
			if( DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server) )
				DeploymentMarkerUtils.removeDeployFailedMarker(method, server, destination, monitor);
			
			if(publishType == IJBossServerPublisher.REMOVE_PUBLISH) {
				result = removeRemoteDeployment(sourcePath, destination.removeLastSegments(1), name, monitor);
			} else if( publishType != IJBossServerPublisher.NO_PUBLISH 
					|| new LocalZippedPublisherUtil().anyChangesRecurse(server, module)){

				/* 
				 * remove prior exploded deployment (prior deployfailed marker, deployed folder etc.)
				 * and redeploy
				 */
				if (isDeployedExploded(destination)) {
					removeRemoteDeployment(sourcePath, destination.removeLastSegments(1), name, monitor);
				}
				
				result = handleLocalZipAndRemotePublish(method, server, module, publishType, delta, AbstractServerToolsPublisher.getSubMon(monitor, 50));
			} 
	
			if( result == null ) {
				result = Status.OK_STATUS;
			}
	
			return result;
		} finally {
			monitor.done();
		}
	}
	
	protected IStatus handleLocalZipAndRemotePublish(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		IDeployableServer server2 = ServerConverter.getDeployableServer(server);
		String remoteTempDeployRoot = getDeployRoot(module, ServerConverter.getDeployableServer(server));
		IPath sourcePath = PublishUtil.getModuleNestedDeployPath(module, remoteTempDeployRoot, server2);
		IPath destination = server2.getDeploymentLocation(module, true);
		String name = sourcePath.lastSegment();
		
		// Locally zip it up into the remote tmp folder
		IStatus result = localTempPublishModule(method, server, module, publishType, delta, monitor);
		if( result.isOK() && requiresTransfer()) { // It seems the superclass already transfers it upstream for as7 :|
			result = remoteFullPublish(sourcePath, destination.removeLastSegments(1), name, 
					AbstractServerToolsPublisher.getSubMon(monitor, 150));
		}
		return result;
	}

	private boolean isDeployedExploded(IPath destination) {
		File file = destination.toFile();
		return file != null 
				&& file.exists() 
				&& file.isDirectory();
	}
	
	protected IStatus remoteFullPublish(IPath sourcePath, 
			IPath destFolder, String name, IProgressMonitor monitor) {
		// Now transfer the file to RSE
		try {
			IModuleFile mf = new ModuleFile(sourcePath.toFile(), name, new Path("/")); //$NON-NLS-1$
			method.getCallbackHandler(destFolder, server).copyFile(mf, new Path(name),
					AbstractServerToolsPublisher.getSubMon(monitor, 150)
			);
			if( DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server)) 
				DeploymentMarkerUtils.addDoDeployMarker(method, ServerConverter.getDeployableServer(server), module, monitor);
		} catch(CoreException ce) {
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}

	private IStatus removeRemoteDeployment( IPath sourcePath, 
			IPath destFolder, String name, IProgressMonitor monitor) throws CoreException {
		try {
			if( DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server)) {
				DeploymentMarkerUtils.removeDeployedMarkerIfExists(method, server, module, monitor);
				DeploymentMarkerUtils.removeDeployFailedMarkerIfExists(method, server, module, monitor);
			}
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
