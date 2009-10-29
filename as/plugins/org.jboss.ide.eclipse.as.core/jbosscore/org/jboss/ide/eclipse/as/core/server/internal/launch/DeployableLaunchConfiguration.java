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
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;

/**
 * @deprecated
 */
public class DeployableLaunchConfiguration implements
		ILaunchConfigurationDelegate {

	public static final String ACTION_KEY = "org.jboss.ide.eclipse.as.core.server.stripped.DeployableLaunchConfiguration.Action"; //$NON-NLS-1$
	public static final String START = "_START_"; //$NON-NLS-1$
	public static final String STOP = "_STOP_"; //$NON-NLS-1$

	public static DeployableServerBehavior getServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		DeployableServerBehavior jbossServerBehavior = (DeployableServerBehavior) server.getAdapter(DeployableServerBehavior.class);
		return jbossServerBehavior;
	}

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		String action = configuration.getAttribute(ACTION_KEY, START);
		DeployableServerBehavior behavior = getServerBehavior(configuration);
		if( START.equals(action)) behavior.setServerStarted();
		if( STOP.equals(action)) behavior.setServerStopped();
	}
}
