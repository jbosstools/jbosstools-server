/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;

public class DeployableLaunchConfiguration implements
		ILaunchConfigurationDelegate {

	public static final String ACTION_KEY = "org.jboss.ide.eclipse.as.core.server.stripped.DeployableLaunchConfiguration.Action";
	public static final String START = "_START_";
	public static final String STOP = "_STOP_";

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
