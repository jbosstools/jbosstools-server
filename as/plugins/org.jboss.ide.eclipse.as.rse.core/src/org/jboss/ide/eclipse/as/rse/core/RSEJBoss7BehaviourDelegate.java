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
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7Server;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class RSEJBoss7BehaviourDelegate extends AbstractRSEBehaviourDelegate {

	private IJBoss7ManagerService service;

	private RSEJBoss7BehaviourDelegate() throws Exception {
		this.service = JBoss7ManagerUtil.getService(getServer());
	}

	@Override
	protected String getShutdownCommand(IServer server) throws CoreException {
		String defaultCommand = ServerUtil.checkedGetBehaviorDelegate(server).getDefaultStopArguments();
		ILaunchConfiguration config = getServer().getLaunchConfiguration(false, new NullProgressMonitor());
		return RSELaunchConfigProperties.getShutdownCommand(config, defaultCommand);
	}
	
	@Override
	public void serverIsStarting() {
		pollServer(IServerStatePoller.SERVER_UP);
	}
	
	@Override
	public void serverIsStopping() {
		pollServer(IServerStatePoller.SERVER_DOWN);
	}

	@Override
	protected IStatus gracefullStop() {
		IServer server = getServer();
		try {
			JBoss7Server jbossServer = ServerConverter.checkedGetJBossServer(server, JBoss7Server.class);
			service.stop(jbossServer.getHost(), jbossServer.getManagementPort());
			return Status.OK_STATUS;
		} catch (Exception e) {
			return new Status(
					IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					MessageFormat.format(Messages.JBoss7ServerBehavior_could_not_stop, server.getName()), e);
		}
	}
	
	public void dispose() {
		JBoss7ManagerUtil.dispose(service);
	}
}
