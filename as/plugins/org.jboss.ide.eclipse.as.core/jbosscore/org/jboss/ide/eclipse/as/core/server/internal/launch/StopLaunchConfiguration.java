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
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.AbstractJavaServerLaunchDelegate;

/**
 * This is a legacy class still in use for shutting down application servers 
 * less than AS-7
 */
public class StopLaunchConfiguration extends AbstractJavaServerLaunchDelegate {
	
	public StopLaunchConfiguration() {
		super();
	}
	
	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		IControllableServerBehavior jbsBehavior = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		if (LaunchCommandPreferences.isIgnoreLaunchCommand(server)) {
			((ControllableServerBehavior)jbsBehavior).setServerStopping();
			((ControllableServerBehavior)jbsBehavior).setServerStopped();
			return false;
		}
		return true;
	}

	@Override
	public void launch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		fireVMRunner(configuration, mode, launch, monitor);
	}
}
