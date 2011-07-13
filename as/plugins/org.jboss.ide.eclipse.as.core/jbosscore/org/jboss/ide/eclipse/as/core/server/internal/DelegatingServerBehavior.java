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
package org.jboss.ide.eclipse.as.core.server.internal;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.publishers.AbstractServerToolsPublisher;
import org.jboss.ide.eclipse.as.core.publishers.JSTPublisherXMLToucher;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.internal.launch.DelegatingStartLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

/**
 * 
 * @author Rob Stryker
 *
 */
public class DelegatingServerBehavior extends DeployableServerBehavior {
	
	private static HashMap<String, Class> delegateClassMap;
	static {
		delegateClassMap = new HashMap<String, Class>();
		delegateClassMap.put(LocalPublishMethod.LOCAL_PUBLISH_METHOD, LocalJBossBehaviorDelegate.class);
	}
	public static void addDelegateMapping(String s, Class c) {
		delegateClassMap.put(s, c);
	}
	
	public DelegatingServerBehavior() {
		super();
	}

	private IJBossBehaviourDelegate delegate;
	private String lastModeId;
	public IJBossBehaviourDelegate getDelegate() {
		IJBossServerPublishMethodType type = DeploymentPreferenceLoader.getCurrentDeploymentMethodType(getServer());
		String id = type == null ? LocalPublishMethod.LOCAL_PUBLISH_METHOD : type.getId();
		if( id.equals(lastModeId) && delegate != null && delegate.getBehaviourTypeId().equals(id))
			return delegate;
		
		Class c = getDelegateMap().get(id);
		if( c == null )
			c = getDelegateMap().get(LocalPublishMethod.LOCAL_PUBLISH_METHOD);
		
		try {
			IJBossBehaviourDelegate o = (IJBossBehaviourDelegate)c.newInstance();
			o.setActualBehaviour(this);
			lastModeId = id;
			delegate = o;
		} catch( InstantiationException ie) {
		} catch( IllegalAccessException iae) {
		}
		return delegate;
	}
	
	protected HashMap<String, Class> getDelegateMap() {
		return delegateClassMap;
	}
	
	public void stop(boolean force) {
		if( LaunchCommandPreferences.isIgnoreLaunchCommand(getServer())) {
			super.setServerStopped();
			return;
		}
		getDelegate().stop(force);
	}
	
	/*
	 * This shouldn't be done in the delegate. 
	 * The launch config class directly should do it and allow all modes 
	 * to participate? 
	 */
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		DelegatingStartLaunchConfiguration.setupLaunchConfiguration(workingCopy, getServer());
	}

	public void setRunMode(String mode) {
		setMode(mode);
	}
	
	@Override
	public void setServerStarting() {
		super.setServerStarting();
		getDelegate().serverIsStarting();
	}
	
	@Override
	public void setServerStopping() {
		super.setServerStopping();
		getDelegate().serverIsStopping();
	}
	
	protected void publishStart(final IProgressMonitor monitor) throws CoreException {
		super.publishStart(monitor);
		getDelegate().publishStart(monitor);
	}
	
	protected void publishFinish(final IProgressMonitor monitor) throws CoreException {
		getDelegate().publishFinish(monitor);
		super.publishFinish(monitor);
	}

	@Deprecated
	protected boolean shouldSuspendScanner() {
		if( getServer().getServerState() != IServer.STATE_STARTED)
			return false;
		return true;
	}
	
	// Can start / stop / restart etc
	@Override
	public IStatus canStart(String launchMode) {
		return canChangeState(launchMode);
	}

	@Override
	public IStatus canRestart(String launchMode) {
		return canChangeState(launchMode);
	}

	@Override
	public IStatus canStop() {
		return canChangeState(null);
	}

	public IStatus canStop(String launchMode) {
		return canChangeState(launchMode);
	}

	protected IStatus canChangeState(String launchMode) {
		return getDelegate().canChangeState(launchMode);
	}
	
	public boolean canRestartModule(IModule[] module){
		if( module.length == 1 ) 
			return true;
		return false;
	}
	
	public void restartModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		IDeployableServer ds = ServerConverter.getDeployableServer(getServer());
		if( ds == null ) 
			return;

		IJBossServerPublishMethod method = getOrCreatePublishMethod();
		IPath depPath = PublishUtil.getDeployPath(method, module, ds);
		if( ServerModelUtilities.isBinaryModule(module[module.length-1]) || ds.zipsWTPDeployments()) {
			// touch the file
			getOrCreatePublishMethod().getCallbackHandler(depPath.removeLastSegments(1), getServer()).touchResource(new Path(depPath.lastSegment()));
		} else {
			// touch the descriptor
			IPublishCopyCallbackHandler callback = method.getCallbackHandler(AbstractServerToolsPublisher.getRootPath(depPath).append(depPath), getServer());
			JSTPublisherXMLToucher.getInstance().touch(depPath, module[0], callback);
		}
	}

	@Override
	public void dispose() {
		getDelegate().dispose();
	}
}
