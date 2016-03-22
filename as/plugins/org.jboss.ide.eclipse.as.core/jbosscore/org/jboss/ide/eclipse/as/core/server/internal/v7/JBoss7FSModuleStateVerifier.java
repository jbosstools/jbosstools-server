/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IModuleStateController;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;

/**
 * This class is in charge of verifying and changing state of modules
 * using only the filesystem, without any expectation that management
 * operations are available. 
 */
public class JBoss7FSModuleStateVerifier extends AbstractJBoss7ModuleStateVerifier implements IModuleStateController, IServerModuleStateVerifier {
	/*
	 * The deployment options gives us access to things like
	 * where the deployment root dir for a server should be,
	 * or whether the server prefers zipped settings
	 */
	private IDeploymentOptionsController deploymentOptions;
	
	/*
	 * The deploy path controller helps us to discover
	 * a module's root deployment directory
	 */
	private IModuleDeployPathController deployPathController;
	
	/*
	 * A filesystem controller gives us access to 
	 * a way to transfer individual files
	 */
	private IFilesystemController filesystemController;
	
	
	/**
	 * get the filesystem controller for transfering files
	 */
	protected IFilesystemController getFilesystemController() throws CoreException {
		if( filesystemController == null ) {
			filesystemController = (IFilesystemController)findDependencyFromBehavior(IFilesystemController.SYSTEM_ID);
		}
		return filesystemController;
	}

	private IPath getModuleDeployRoot(IModule[] module) throws CoreException {
		// Find dependency will throw a CoreException if an object is not found, rather than return null
		IDeploymentOptionsController opts = getDeploymentOptions();
		IModuleDeployPathController depPath = getDeployPathController();
		return new RemotePath(depPath.getDeployDirectory(module).toOSString(), 
				opts.getPathSeparatorCharacter());
	}
	
	/**
	 * Get the system for deployment options such as zipped or not
	 * We must pass in a custom environment here. 
	 */
	protected IDeploymentOptionsController getDeploymentOptions() throws CoreException {
		if( deploymentOptions == null ) {
			deploymentOptions = (IDeploymentOptionsController)findDependencyFromBehavior(IDeploymentOptionsController.SYSTEM_ID);
		}
		return deploymentOptions;
	}
	
	/**
	 * get the system for deploy path for a given module
	 */
	protected IModuleDeployPathController getDeployPathController() throws CoreException {
		if( deployPathController == null ) {
			deployPathController = (IModuleDeployPathController)findDependencyFromBehavior(IModuleDeployPathController.SYSTEM_ID);
		}
		return deployPathController;
	}
	
	protected int getRootModuleState(IServer server, IModule root,
			String deploymentName, IProgressMonitor monitor) throws Exception {
		IFilesystemController c = getFilesystemController();
		IPath d = getModuleDeployRoot(new IModule[]{root});
		if( DeploymentMarkerUtils.markerExists(d, c, DeploymentMarkerUtils.DEPLOYED)) {
			return IServer.STATE_STARTED;
		}
		if( DeploymentMarkerUtils.markerExists(d, c, DeploymentMarkerUtils.DEPLOYING)) {
			return IServer.STATE_STARTING;
		}
		if( DeploymentMarkerUtils.markerExists(d, c, DeploymentMarkerUtils.FAILED_DEPLOY)) {
			return IServer.STATE_STOPPED;
		}
		if( DeploymentMarkerUtils.markerExists(d, c, DeploymentMarkerUtils.PENDING)) {
			return IServer.STATE_STARTING;
		}
		if( DeploymentMarkerUtils.markerExists(d, c, DeploymentMarkerUtils.SKIP_DEPLOY)) {
			return IServer.STATE_STOPPED;
		}
		if( DeploymentMarkerUtils.markerExists(d, c, DeploymentMarkerUtils.UNDEPLOYED)) {
			return IServer.STATE_STOPPED;
		}
		if( DeploymentMarkerUtils.markerExists(d, c, DeploymentMarkerUtils.UNDEPLOYING)) {
			return IServer.STATE_STOPPING;
		}
		return IServer.STATE_UNKNOWN;
	}
	
	public int changeModuleStateTo(IModule[] module, int state, IProgressMonitor monitor) throws CoreException {
		IPath archiveDestination = getModuleDeployRoot(new IModule[]{module[0]});
		if(state == IServer.STATE_STARTED ) {
			// Remove undeployed marker, add dodeploy marker
			DeploymentMarkerUtils.removedUndeployedMarker(archiveDestination, getFilesystemController());
			DeploymentMarkerUtils.createDoDeployMarker(archiveDestination, getFilesystemController());
		} else if (state == IServer.STATE_STOPPED) {
			// remove deployed marker
			DeploymentMarkerUtils.removedDeployedMarker(archiveDestination, getFilesystemController());
		} else {
			throw new IllegalArgumentException("Only states IServer.STATE_STARTED and IServer.STATE_STOPPED are supported"); //$NON-NLS-1$
		}
		return state;
	}
	
}
