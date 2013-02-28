/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
 ******************************************************************************/
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;

public class RSEJBoss7StartLaunchDelegate extends RSEJBossStartLaunchDelegate {
	@Override
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IServer server)
			throws CoreException {
		new RSEJBoss7LaunchConfigurator(server).configure(workingCopy);
	}
}
