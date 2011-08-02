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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7Server;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class RSEJBoss7BehaviourDelegate extends RSEBehaviourDelegate {

	private IJBoss7ManagerService service;
	
	@Override
	protected IStatus gracefullStop() {
		IServer server = getServer();
		try {
			JBoss7Server jbossServer = ServerConverter.checkedGetJBossServer(server, JBoss7Server.class);
			getService().stop(jbossServer.getHost(), jbossServer.getManagementPort());
			return Status.OK_STATUS;
		} catch (Exception e) {
			return new Status(
					IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					MessageFormat.format(Messages.JBoss7ServerBehavior_could_not_stop, server.getName()), e);
		}
	}
	
	public void dispose() {
		try {
			JBoss7ManagerUtil.dispose(getService());
		} catch(Exception e) {
			// ignore
		}
	}
	
	protected IJBoss7ManagerService getService() throws Exception {
		if (service == null) {
			this.service = JBoss7ManagerUtil.getService(getServer());
		}
		return service;
	}
}
