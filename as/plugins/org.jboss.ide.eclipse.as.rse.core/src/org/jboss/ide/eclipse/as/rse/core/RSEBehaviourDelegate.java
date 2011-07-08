/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class RSEBehaviourDelegate extends AbstractRSEBehaviourDelegate {

	@Override
	protected void forceStop() {
		setServerStopped();
	}

	@Override
	protected String getShutdownCommand(IServer server) throws CoreException {
		String defaultCommand = ServerUtil.checkedGetBehaviorDelegate(server).getDefaultStopArguments();
		ILaunchConfiguration config = getServer().getLaunchConfiguration(false, new NullProgressMonitor());
		return RSELaunchConfigProperties.getShutdownCommand(config, defaultCommand);
	}

	public void serverIsStarting() {
		pollServer(IServerStatePoller.SERVER_UP);
	}
	
	public void serverIsStopping() {
		pollServer(IServerStatePoller.SERVER_DOWN);
	}
}
