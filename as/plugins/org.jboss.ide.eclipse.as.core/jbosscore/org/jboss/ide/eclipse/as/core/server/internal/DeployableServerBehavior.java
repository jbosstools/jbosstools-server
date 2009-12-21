/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.launch.DeployableLaunchConfiguration;

public class DeployableServerBehavior extends ServerBehaviourDelegate {

	public DeployableServerBehavior() {
	}

	public void stop(boolean force) {
		setServerStopped(); // simple enough
	}
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		workingCopy.setAttribute(DeployableLaunchConfiguration.ACTION_KEY, DeployableLaunchConfiguration.START);
	}
	
	
	protected IJBossServerPublishMethod method;
	protected void publishStart(IProgressMonitor monitor) throws CoreException {
		if( method != null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Already publishing")); //$NON-NLS-1$
		method = createPublishMethod();
		method.publishStart(this, monitor);
	}

	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		if( method == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Not publishing")); //$NON-NLS-1$
		int result = method.publishFinish(this, monitor);
		setServerPublishState(result);
		method = null;
	}

	protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
		if( method == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Not publishing")); //$NON-NLS-1$
		int result = method.publishModule(this, kind, deltaKind, module, monitor);
		setModulePublishState(module, result);
	}
	
	/**
	 * This should only be called once per overall publish. 
	 * publishStart() should call this, cache the method, and use it 
	 * until after publishFinish() is called. 
	 * 
	 * @return
	 */
	protected IJBossServerPublishMethod createPublishMethod() {
		return new LocalPublishMethod(); // TODO FIX THIS
	}
	
	public IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
		return super.getPublishedResourceDelta(module);
	}

	public int getPublishType(int kind, int deltaKind, int modulePublishState) {
		if (ServerBehaviourDelegate.REMOVED == deltaKind) {
			return IJBossServerPublisher.REMOVE_PUBLISH;
		} else if (kind == IServer.PUBLISH_FULL || modulePublishState == IServer.PUBLISH_STATE_FULL ||  kind == IServer.PUBLISH_CLEAN ) {
			return IJBossServerPublisher.FULL_PUBLISH;
		} else if (kind == IServer.PUBLISH_INCREMENTAL || modulePublishState == IServer.PUBLISH_STATE_INCREMENTAL || kind == IServer.PUBLISH_AUTO) {
			if( ServerBehaviourDelegate.CHANGED == deltaKind ) 
				return IJBossServerPublisher.INCREMENTAL_PUBLISH;
		} 
		return IJBossServerPublisher.NO_PUBLISH;
	}
	
	// Expose 
	public List<IModule[]> getRemovedModules() {
		final List<IModule[]> moduleList = getAllModules();
		int size = moduleList.size();
		super.addRemovedModules(moduleList, null);
		for( int i = 0; i < size; i++ ) 
			moduleList.remove(0);
		return moduleList;
	}

	public boolean hasBeenPublished(IModule[] module) {
		return super.hasBeenPublished(module);
	}

	
	
	/*
	 * Change the state of the server
	 * Also, cache the state we think we're setting it to.
	 * 
	 * Much of this can be changed once eclipse bug 231956 is fixed
	 */
	protected int serverStateVal;
	protected int getServerStateVal() {
		return serverStateVal;
	}
	public void setServerStarted() {
		serverStateVal = IServer.STATE_STARTED;
		setServerState(IServer.STATE_STARTED);
	}
	
	public void setServerStarting() {
		serverStateVal = IServer.STATE_STARTING;
		setServerState(IServer.STATE_STARTING);
	}
	
	public void setServerStopped() {
		serverStateVal = IServer.STATE_STOPPED;
		setServerState(IServer.STATE_STOPPED);
	}
	
	public void setServerStopping() {
		serverStateVal = IServer.STATE_STOPPING;
		setServerState(IServer.STATE_STOPPING);
	}
	protected void initialize(IProgressMonitor monitor) {
		serverStateVal =  getServer().getServerState();
		getServer().addServerListener(new IServerListener() {
			public void serverChanged(ServerEvent event) {
				if( event.getState() != serverStateVal ) {
					// something's been changed by the framework and NOT by us. 
					if( serverStateVal == IServer.STATE_STARTING && event.getState() == IServer.STATE_STOPPED) {
						stop(true);
					}
				}
			} 
		} );
	}
}
