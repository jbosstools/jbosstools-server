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
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.StandardClasspathProvider;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractLocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossBehaviorDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.DelegatingStartLaunchConfiguration.IStartLaunchSetupParticipant;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.LocalJBossStartupConfigurator;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;

/**
 * @author Rob Stryker
 * @author André Dietisheim
 */
public class LocalJBossStartLaunchDelegate extends AbstractJBossLaunchConfigType implements IStartLaunchDelegate, IStartLaunchSetupParticipant {

	public void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy, IServer server) throws CoreException {
		new LocalJBossStartupConfigurator(server).configure(workingCopy);
	}

	/*
	 * Actual instance methods
	 */
	public void actualLaunch(
			DelegatingStartLaunchConfiguration launchConfig,
			ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		actualLaunch(configuration, mode, launch, monitor);
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
			jbsBehavior.setServerStarting();
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
	
	public static class JBossServerDefaultClasspathProvider extends StandardClasspathProvider {
		public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration)
				throws CoreException {
			if (JBossLaunchConfigProperties.isUseDefaultClasspath(configuration)) {
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
}
