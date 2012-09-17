/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IDeployableServerBehaviour;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.internal.launch.DeployableLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * @author Rob Stryker
 */
public class DeployableServerBehavior extends ServerBehaviourDelegate 
	implements IDeployableServerBehaviour {

	public DeployableServerBehavior() {
	}
	
	public void stop(boolean force) {
		setServerStopped(); // simple enough
	}
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		workingCopy.setAttribute(DeployableLaunchConfiguration.ACTION_KEY, DeployableLaunchConfiguration.START);
	}
	
	
	private IJBossServerPublishMethod method;
	protected HashMap<String, Object> publishTaskModel;
	protected void publishStart(IProgressMonitor monitor) throws CoreException {
		if( method != null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Already publishing")); //$NON-NLS-1$
		Trace.trace(Trace.STRING_FINER, "Publish Start in DeployableServerBehavior"); //$NON-NLS-1$
		method = getOrCreatePublishMethod();
		publishTaskModel = new HashMap<String, Object>();
		method.publishStart(this, monitor);
	}

	/**
	 * Get the current publish method if one exists, or, create one and return it. 
	 * Any created publish method will be cached and stored. 
	 * 
	 * This method should NOT be called except as part of the publishing process. 
	 * Clients that need a disposable single-use publish method should call
	 * createPublishMethod() instead. 
	 * 
	 * @return
	 * @throws CoreException
	 */
	protected IJBossServerPublishMethod getOrCreatePublishMethod() throws CoreException {
		if( method == null ) {
			method = createPublishMethod();
			Trace.trace(Trace.STRING_FINER, "Creating publish method " + (method == null ? null : method.getClass().getName())); //$NON-NLS-1$
		}
		return method;
	}
	
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		Trace.trace(Trace.STRING_FINER, "PublishFinish in DeployableServerBehavior"); //$NON-NLS-1$
		
		if( method == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Not publishing")); //$NON-NLS-1$
		try {
			int result = method.publishFinish(this, monitor);
			setServerPublishState(result);
		} finally {
			publishTaskModel = null;
			method = null;
		}
	}

	protected void setPublishData(String key, Object val) {
		if( publishTaskModel != null )
			publishTaskModel.put(key, val);
	}
	
	protected Object getPublishData(String key) {
		if( publishTaskModel != null )
			return publishTaskModel.get(key);
		return null;
	}
	
	protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
		Trace.trace(Trace.STRING_FINER, "Beginning to publish module " + module[module.length-1].getName() + " in DeployableServerBehavior"); //$NON-NLS-1$ //$NON-NLS-2$

		if( method == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Not publishing")); //$NON-NLS-1$
		try { 
			int result = method.publishModule(this, kind, deltaKind, module, monitor);
			if( result != -1 ) {
				setModulePublishState(module, result);
				setModuleState(module, IServer.STATE_STARTED );
			}
		} catch(CoreException ce) {
			Trace.trace(Trace.STRING_FINER, "Error publishing module " + module[module.length-1].getName() + " in DeployableServerBehavior. " + ce.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			setModulePublishState(module, IServer.PUBLISH_STATE_FULL);
			setModuleState(module, IServer.STATE_UNKNOWN );
			throw ce;
		}
	}
	
	/**
	 * This method creates a disposable publishMethod for use in publishing modules and projects, 
	 * touching files, copying files, or any other task relevent to publish methods. 
	 * 
	 * This method will also be called once during a "publish cycle" 
	 * between publishStart and publishFinish, with the 
	 * IJBossServerPublishMethod cached for the duration. 
	 * 
	 * Other clients can use this API to have a disposable publish method to use.
	 * 
	 * @return The publish method
	 */
	public IJBossServerPublishMethod createPublishMethod() {
		IJBossServerPublishMethodType type = DeploymentPreferenceLoader.getCurrentDeploymentMethodType(getServer());
		if( type != null )
			return type.createPublishMethod();
		return new LocalPublishMethod(); // sensible default
	}
	
	public IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
		return super.getPublishedResourceDelta(module);
	}

	public int getPublishType(int kind, int deltaKind, int modulePublishState) {
		if( deltaKind == ServerBehaviourDelegate.ADDED ) 
			return IJBossServerPublisher.FULL_PUBLISH;
		else if (deltaKind == ServerBehaviourDelegate.REMOVED) {
			return IJBossServerPublisher.REMOVE_PUBLISH;
		} else if (kind == IServer.PUBLISH_FULL 
				|| modulePublishState == IServer.PUBLISH_STATE_FULL 
				|| kind == IServer.PUBLISH_CLEAN ) {
			return IJBossServerPublisher.FULL_PUBLISH;
		} else if (kind == IServer.PUBLISH_INCREMENTAL 
				|| modulePublishState == IServer.PUBLISH_STATE_INCREMENTAL 
				|| kind == IServer.PUBLISH_AUTO) {
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
					Trace.trace(Trace.STRING_FINER, "Framework has changed server state from starting to stopped. Ensuring server has stopped."); //$NON-NLS-1$
					// something's been changed by the framework and NOT by us. 
					if( serverStateVal == IServer.STATE_STARTING && event.getState() == IServer.STATE_STOPPED) {
						stop(true);
					}
				}
			} 
		} );
	}
	
	public boolean changedFileRequiresModuleRestart(IModuleFile file) {
		IDeployableServer ds = ServerConverter.getDeployableServer(getServer());
		Pattern restartFilePattern = ds.getRestartFilePattern();
		boolean result = false;
		if (restartFilePattern != null) { 
			// using find over matches to make it a substring search by default and avoid having to specify .*.class$ instead of just .class$
			result = restartFilePattern.matcher(file.getName()).find(); 
		}
		return result;
	}
	
	/**
	 * Some projects may request post-processing filtering on 
	 * the servertools list of resources. 
	 * 
	 * @since 2.3
	 */
	public IModulePathFilter getPathFilter(IModule[] moduleTree) {
		return null;
	}

}
