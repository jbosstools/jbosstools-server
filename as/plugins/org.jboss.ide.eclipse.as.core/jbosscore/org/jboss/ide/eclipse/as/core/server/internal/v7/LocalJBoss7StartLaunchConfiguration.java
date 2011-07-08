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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossBehaviorDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.IStartLaunchDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.IStartLaunchSetupParticipant;
import org.jboss.ide.eclipse.as.core.server.internal.launch.LocalJBossStartLaunchDelegate;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;

/**
 * @deprecated replaced by {@link LocalJBoss7StartLaunchDelegate}
 * 
 * @author Rob Stryker
 */
public class LocalJBoss7StartLaunchConfiguration extends LocalJBossStartLaunchDelegate 
	implements IStartLaunchDelegate, IStartLaunchSetupParticipant {

	public String[] getJavaLibraryPath(ILaunchConfiguration configuration) throws CoreException {
		return new String[] {};
	}

	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		DelegatingServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
		if( LaunchCommandPreferences.isIgnoreLaunchCommand(jbsBehavior.getServer())) {
			jbsBehavior.setServerStarting();
			jbsBehavior.setServerStarted();
			return false;
		}
		// TODO: use the configured poller
		boolean started = WebPortPoller.onePing(jbsBehavior.getServer());
		if( started ) {
			jbsBehavior.setServerStarting();
			jbsBehavior.setServerStarted();
			return false;
		}
		return true;
	}

	public void preLaunch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		try {
			DelegatingServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
			jbsBehavior.setRunMode(mode);
			jbsBehavior.setServerStarting();
		} catch (CoreException ce) {
			// report it
		}
	}

	public void postLaunch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		try {
			DelegatingJBoss7ServerBehavior behavior = JBossServerBehaviorUtils.getJBoss7ServerBehavior(configuration);
			IProcess[] processes = launch.getProcesses();
			if (processes != null && processes.length >= 1) {
				behavior.setProcess(processes[0]);
				((LocalJBossBehaviorDelegate) (behavior.getDelegate())).setProcess(processes[0]);
			}
			behavior.setRunMode(mode);
		} catch (CoreException ce) {
			// report it
		}
	}
}
