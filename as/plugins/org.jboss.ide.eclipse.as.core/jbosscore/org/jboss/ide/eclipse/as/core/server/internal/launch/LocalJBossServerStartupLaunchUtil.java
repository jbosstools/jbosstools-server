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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.StandardClasspathProvider;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractLocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossBehaviorDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.JBossServerStartupLaunchConfiguration.IStartLaunchSetupParticipant;
import org.jboss.ide.eclipse.as.core.server.internal.launch.JBossServerStartupLaunchConfiguration.StartLaunchDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.LocalJBossStartupConfigurator;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;

/**
 * @deprecated please use {@link LocalJBossStartLaunchDelegate}
 * 
 * @author Rob Stryker
 * @author André Dietisheim
 */
@Deprecated
public class LocalJBossServerStartupLaunchUtil implements StartLaunchDelegate, IStartLaunchSetupParticipant {

	public static final String DEFAULTS_SET = "jboss.defaults.been.set"; //$NON-NLS-1$
	static final String START_MAIN_TYPE = IJBossRuntimeConstants.START_MAIN_TYPE;

	public void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy, IServer server) throws CoreException {
		new LocalJBossStartupConfigurator(server).configure(workingCopy);
	}

	public static class JBossServerDefaultClasspathProvider extends StandardClasspathProvider {
		public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration)
				throws CoreException {
			boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
					true);
			if (useDefault) {
				return defaultEntries(configuration);
			}
			return super.computeUnresolvedClasspath(configuration);
		}

		protected IRuntimeClasspathEntry[] defaultEntries(ILaunchConfiguration config) {
			try {
				String server = JBossLaunchConfigProperties.getServerId(config);
				IServer s = ServerCore.findServer(server);
				AbstractLocalJBossServerRuntime ibjsrt = (AbstractLocalJBossServerRuntime)
						s.getRuntime().loadAdapter(AbstractLocalJBossServerRuntime.class, new NullProgressMonitor());
				JBossServer jbs = (JBossServer) s.loadAdapter(JBossServer.class, new NullProgressMonitor());
				IVMInstall install = ibjsrt.getVM();
				ArrayList<IRuntimeClasspathEntry> list = new ArrayList<IRuntimeClasspathEntry>();
				LaunchConfigUtils.addJREEntry(install, list);
				list.add(LaunchConfigUtils.getRunJarRuntimeCPEntry(s));
				return (IRuntimeClasspathEntry[]) list
						.toArray(new IRuntimeClasspathEntry[list.size()]);
			} catch (CoreException ce) {
				// ignore
			}

			try {
				return super.computeUnresolvedClasspath(config);
			} catch (CoreException ce) {
				// ignore
			}
			return new IRuntimeClasspathEntry[] {};
		}
	}

	/*
	 * Actual instance methods
	 */
	public void actualLaunch(
			JBossServerStartupLaunchConfiguration launchConfig,
			ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		launchConfig.superActualLaunch(configuration, mode, launch, monitor);
	}

	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		DelegatingServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
		if (!jbsBehavior.canStart(mode).isOK())
			throw new CoreException(jbsBehavior.canStart(mode));
		if (LaunchCommandPreferences.isIgnoreLaunchCommand(jbsBehavior.getServer())) {
			jbsBehavior.setServerStarting();
			jbsBehavior.setServerStarted();
			return false;
		}
		boolean started = WebPortPoller.onePing(jbsBehavior.getServer());
		if (started) {
			jbsBehavior.setServerStarting();
			jbsBehavior.setServerStarted();
			return false;
		}

		return true;
	}

	public void preLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			DelegatingServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
			jbsBehavior.setRunMode(mode);
			jbsBehavior.serverStarting();
		} catch (CoreException ce) {
			// report it
		}
	}

	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			IProcess[] processes = launch.getProcesses();
			DelegatingServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
			((LocalJBossBehaviorDelegate) (jbsBehavior.getDelegate())).setProcess(processes[0]);
		} catch (CoreException ce) {
			// report
		}
	}

}
