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
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * @deprecated replaced by {@link DelegatingStartLaunchConfiguration}
 * 
 * @author Rob Strkyer
 */
@Deprecated
public class JBossServerStartupLaunchConfiguration extends AbstractJBossStartLaunchConfiguration {
	
	/**
	 * @deprecated replaced by {@link IStartLaunchDelegate}
	 */
	@Deprecated
	public static interface StartLaunchDelegate {
		public void actualLaunch(JBossServerStartupLaunchConfiguration launchConfig,
				ILaunchConfiguration configuration, 
				String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException;
		
		public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException;
		public void preLaunch(ILaunchConfiguration configuration, 
				String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException;
		public void postLaunch(ILaunchConfiguration configuration, String mode,
				ILaunch launch, IProgressMonitor monitor) throws CoreException;
		
	}
	
	public static interface IStartLaunchSetupParticipant {
		public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IServer server) throws CoreException;
	}
	
	private static HashMap<String, StartLaunchDelegate> launchDelegates;
	private static ArrayList<IStartLaunchSetupParticipant> setupParticipants;
	
	static {
		setupParticipants = new ArrayList<IStartLaunchSetupParticipant>();
//		setupParticipants.add(new LocalJBossStartLaunchDelegate());
		launchDelegates = new HashMap<String, StartLaunchDelegate>();
//		launchDelegates.put(LocalPublishMethod.LOCAL_PUBLISH_METHOD, new LocalJBossStartLaunchDelegate());
	}
	
	public static void addLaunchDelegateMapping(String mode, StartLaunchDelegate del) {
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

	/**
	* looks like nobody uses this method. deprecating it.
	*/
	@Deprecated
	public String[] getJavaLibraryPath(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		JBossServer jbs = ServerConverter.findJBossServer(server.getId());
		if (jbs == null) {
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(Messages.ServerNotFound, server.getId())));
		}
		IJBossServerRuntime runtime = (IJBossServerRuntime)
			jbs.getServer().getRuntime().loadAdapter(IJBossServerRuntime.class, null);
		IPath nativeFolder = runtime.getRuntime().getLocation()
				.append(IJBossRuntimeResourceConstants.BIN)
				.append(IJBossRuntimeResourceConstants.NATIVE);
		if( nativeFolder.toFile().exists() ) {
			return new String[]{nativeFolder.toOSString()};
		}
		return new String[]{};
	}
	
	protected StartLaunchDelegate getDelegate(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		DeployableServerBehavior beh = ServerConverter.getDeployableServerBehavior(server);
		IJBossServerPublishMethodType type = beh.createPublishMethod().getPublishMethodType();
		return launchDelegates.get(type.getId());
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
	
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return getDelegate(configuration).preLaunchCheck(configuration, mode, monitor);
	}

	public void preLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		getDelegate(configuration).preLaunch(configuration, mode, launch, monitor);
	}

	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		getDelegate(configuration).postLaunch(configuration, mode, launch, monitor);
	}
}
