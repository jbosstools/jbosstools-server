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
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.launch.JBossServerStartupLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.internal.launch.LocalJBossServerStartupLaunchUtil;

public class JBoss7ServerStartupLaunchConfiguration extends
		JBossServerStartupLaunchConfiguration implements
		ILaunchConfigurationDelegate {
	public void actualLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		//super.actualLaunch(configuration, mode, launch, monitor);
	}
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		//super.preLaunch(configuration, mode, launch, monitor);
		return true;
	}
	public void preLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		//super.preLaunch(configuration, mode, launch, monitor);
		try {
			JBossServerBehavior jbsBehavior = LocalJBossServerStartupLaunchUtil.getServerBehavior(configuration);
			jbsBehavior.setRunMode(mode);
			jbsBehavior.serverStarting();
		} catch( CoreException ce ) {
			// report it
		}
	}
	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		//super.postLaunch(configuration, mode, launch, monitor);
		try {
			JBossServerBehavior jbsBehavior = LocalJBossServerStartupLaunchUtil.getServerBehavior(configuration);
			jbsBehavior.setRunMode(mode);
			jbsBehavior.setServerStarted();
		} catch( CoreException ce ) {
			// report it
		}
	}
	
}
