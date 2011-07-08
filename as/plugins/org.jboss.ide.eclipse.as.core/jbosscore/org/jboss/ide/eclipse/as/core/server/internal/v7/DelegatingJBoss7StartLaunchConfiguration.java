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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.launch.DelegatingStartLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.internal.launch.IStartLaunchDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.IStartLaunchSetupParticipant;
import org.jboss.ide.eclipse.as.core.server.internal.launch.LocalJBossStartLaunchDelegate;

/**
 * @author Rob Stryker
 */
public class DelegatingJBoss7StartLaunchConfiguration extends DelegatingStartLaunchConfiguration {

	private static HashMap<String, IStartLaunchDelegate> launchDelegates;
	private static ArrayList<IStartLaunchSetupParticipant> setupParticipants;

	static {
		setupParticipants = new ArrayList<IStartLaunchSetupParticipant>();
		setupParticipants.add(new LocalJBossStartLaunchDelegate());
		launchDelegates = new HashMap<String, IStartLaunchDelegate>();
		launchDelegates.put(LocalPublishMethod.LOCAL_PUBLISH_METHOD, new LocalJBoss7StartLaunchDelegate());
	}
	
	public static void addLaunchDelegateMapping(String mode, IStartLaunchDelegate del) {
		launchDelegates.put(mode, del);
	}

	public static void addSetupLaunchParticipant(IStartLaunchSetupParticipant participant) {
		setupParticipants.add(participant);
	}

	// Allow all participants to set some defaults for their own details
	// Participants should be careful not to change shared launch keys / values 
	// unless their operation mode (local / rse / etc) is in use
	public static void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IServer server) throws CoreException {
		for( Iterator<IStartLaunchSetupParticipant> i = setupParticipants.iterator(); i.hasNext(); ) {
			i.next().setupLaunchConfiguration(workingCopy, server);
		}
	}	

	protected IStartLaunchDelegate getDelegate(ILaunchConfiguration configuration) throws CoreException {
// TODO: choose delegate upon setting (server editor)
//		IServer server = ServerUtil.getServer(configuration);
//		DeployableServerBehavior beh = ServerConverter.getDeployableServerBehavior(server);
//		IJBossServerPublishMethodType type = beh.createPublishMethod().getPublishMethodType();
//		return launchDelegates.get(type.getId());

// always return local launch delegate until all parts were implemented
		return new LocalJBoss7StartLaunchDelegate();
		
	}
	
	public void actualLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		getDelegate(configuration).actualLaunch(this, configuration, mode, launch, monitor);
	}
	
	@Deprecated
	public void superActualLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		super.actualLaunch(configuration, mode, launch, monitor);
	}	
	/*
	 * Ensures that the working directory and classpath are 100% accurate.
	 * Merges proper required params into args and vm args
	 */	
	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return getDelegate(configuration).preLaunchCheck(configuration, mode, monitor);
	}

	@Override
	public void preLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		getDelegate(configuration).preLaunch(configuration, mode, launch, monitor);
	}

	@Override
	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		getDelegate(configuration).postLaunch(configuration, mode, launch, monitor);
	}
}
