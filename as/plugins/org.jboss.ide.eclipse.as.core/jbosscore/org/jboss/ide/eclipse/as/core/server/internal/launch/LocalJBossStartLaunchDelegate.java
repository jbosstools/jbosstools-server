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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IJBossLaunchDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossBehaviorDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.LocalJBossStartLaunchConfigurator;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;

/**
 * @author Rob Stryker
 * @author André Dietisheim
 * @deprecated
 */
public class LocalJBossStartLaunchDelegate extends AbstractJBossStartLaunchConfiguration implements IJBossLaunchDelegate {

	public void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy, IServer server) throws CoreException {
		new LocalJBossStartLaunchConfigurator(server).configure(workingCopy);
	}

	/*
	 * Actual instance methods
	 */
	public void actualLaunch(
			LaunchConfigurationDelegate launchConfig,
			ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		actualLaunch(configuration, mode, launch, monitor);
	}

	public void preLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		super.preLaunchCheck(configuration, mode, monitor);
		IDelegatingServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
		if( jbsBehavior != null ) {
			((DelegatingServerBehavior)jbsBehavior).setRunMode(mode);
			((DelegatingServerBehavior)jbsBehavior).setServerStarting();
		}
	}

	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		super.postLaunch(configuration, mode, launch, monitor);
		IProcess[] processes = launch.getProcesses();
		IDelegatingServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
		if( jbsBehavior != null )
			((LocalJBossBehaviorDelegate) (jbsBehavior.getDelegate())).setProcess(processes[0]);
	}
}
