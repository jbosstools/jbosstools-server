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

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;

public class RSEBehaviourDelegate extends AbstractRSEBehaviourDelegate {

	@Override
	protected void forceStop() {
		setServerStopped();
	}

	@Override
	protected IStatus gracefullStop() {
		try {
			String shutdownCommand = getShutdownCommand(getServer());
			ServerShellModel model = RSEHostShellModel.getInstance().getModel(getServer());
			model.executeRemoteCommand("/", shutdownCommand, new String[]{}, new NullProgressMonitor(), 10000, true);
			IHostShell shell = model.getStartupShell();
			if( RSEUtils.isActive(shell)) {
				shell.writeToShell("exit");
			}
			return Status.OK_STATUS;
		} catch(CoreException ce) {
			ServerLogger.getDefault().log(getServer(), ce.getStatus());
			return new Status(
					IStatus.ERROR, RSECorePlugin.PLUGIN_ID,
					MessageFormat.format("Could not stop server {0}", getServer().getName()), 
					ce);
		}
	}
	
	private String getShutdownCommand(IServer server) throws CoreException {
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
