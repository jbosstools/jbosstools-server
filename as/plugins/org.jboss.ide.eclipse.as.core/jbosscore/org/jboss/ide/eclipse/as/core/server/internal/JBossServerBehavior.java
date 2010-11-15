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

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.internal.launch.JBossServerStartupLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;

/**
 * 
 * @author Rob Stryker
 *
 */
public class JBossServerBehavior extends DeployableServerBehavior {
	
	public static interface JBossBehaviourDelegate {
		public String getBehaviourTypeId();
		public void setActualBehaviour(JBossServerBehavior actualBehaviour);
		public void stop(boolean force);
		public void publishStart(final IProgressMonitor monitor) throws CoreException;
		public void publishFinish(final IProgressMonitor monitor) throws CoreException;
		public void serverStarting();
		public void serverStopping();
		public IStatus canChangeState(String launchMode);
	}
	
	public static HashMap<String, Class> delegateClassMap;
	static {
		delegateClassMap = new HashMap<String, Class>();
		delegateClassMap.put(LocalPublishMethod.LOCAL_PUBLISH_METHOD, LocalJBossBehaviorDelegate.class);
	}
	public static void addDelegateMapping(String s, Class c) {
		delegateClassMap.put(s, c);
	}
	
	public JBossServerBehavior() {
		super();
	}

	private JBossBehaviourDelegate delegate;
	private String lastModeId;
	public JBossBehaviourDelegate getDelegate() {
		IJBossServerPublishMethodType type = DeploymentPreferenceLoader.getCurrentDeploymentMethodType(getServer());
		String id = type == null ? LocalPublishMethod.LOCAL_PUBLISH_METHOD : type.getId();
		if( id.equals(lastModeId) && delegate != null && delegate.getBehaviourTypeId().equals(id))
			return delegate;
		
		Class c = delegateClassMap.get(id);
		if( c == null )
			c = delegateClassMap.get(LocalPublishMethod.LOCAL_PUBLISH_METHOD);
		
		try {
			JBossBehaviourDelegate o = (JBossBehaviourDelegate)c.newInstance();
			o.setActualBehaviour(this);
			lastModeId = id;
			delegate = o;
		} catch( InstantiationException ie) {
		} catch( IllegalAccessException iae) {
		}
		return delegate;
	}
	
	public void stop(boolean force) {
		getDelegate().stop(force);
	}
	
	/*
	 * This shouldn't be done in the delegate. 
	 * The launch config class directly should do it and allow all modes 
	 * to participate? 
	 */
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		JBossServerStartupLaunchConfiguration.setupLaunchConfiguration(workingCopy, getServer());
	}

	public void setRunMode(String mode) {
		setMode(mode);
	}
	
	public void serverStarting() {
		setServerStarting();
		getDelegate().serverStarting();
	}
	
	public void serverStopping() {
		setServerStopping();
		getDelegate().serverStopping();
	}
	
	protected void publishStart(final IProgressMonitor monitor) throws CoreException {
		super.publishStart(monitor);
		getDelegate().publishStart(monitor);
	}
	
	protected void publishFinish(final IProgressMonitor monitor) throws CoreException {
		getDelegate().publishFinish(monitor);
		super.publishFinish(monitor);
	}

	// Can start / stop / restart etc
	public IStatus canStart(String launchMode) {
		return canChangeState(launchMode);
	}
	public IStatus canRestart(String launchMode) {
		return canChangeState(launchMode);
	}
	public IStatus canStop() {
		return canChangeState(null);
	}
	public IStatus canStop(String launchMode) {
		return canChangeState(launchMode);
	}
	protected IStatus canChangeState(String launchMode) {
		return getDelegate().canChangeState(launchMode);
	}
}
