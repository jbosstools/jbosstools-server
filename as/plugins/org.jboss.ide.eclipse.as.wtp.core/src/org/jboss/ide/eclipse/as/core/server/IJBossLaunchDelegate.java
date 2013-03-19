/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.wst.server.core.IServer;

public interface IJBossLaunchDelegate {
	public void actualLaunch(LaunchConfigurationDelegate launchConfig, ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException;
	
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) 
			throws CoreException;

	public void preLaunch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) 
			throws CoreException;

	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException;
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IServer server) 
			throws CoreException;

}
