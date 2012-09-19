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
package org.jboss.ide.eclipse.as.core.util;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IDeployableServerBehaviour;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.Messages;

/**
 * 
 * @author rob.stryker@jboss.com
 * @author adietish@redhat.com
 */
public class ServerConverter {

	public static IServer findServer(String serverId) {
		if (serverId == null)
			return null;
		return ServerCore.findServer(serverId);
	}
	
	public static IJBossServer findJBossServer(String serverId) throws CoreException {
		if (serverId == null)
			return null;

		IServer s = ServerCore.findServer(serverId);
		if (s == null)
			return null;
		return getJBossServer(s);
	}

	public static IJBossServer checkedFindJBossServer(String serverId) throws CoreException {
		IJBossServer server = findJBossServer(serverId);
		if (server == null) {
			throw new CoreException(
					new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,
							NLS.bind(Messages.CouldNotFindServer, serverId)));			
		}
		return server;
	}

	public static <SERVER> SERVER checkedGetJBossServer(IServer server, Class<SERVER> serverClass) throws CoreException {
		return checkedConvertServer(server, serverClass);
	}
	
	public static <SERVER> SERVER checkedConvertServer(IServerAttributes server, Class<SERVER> serverClass) throws CoreException {
		SERVER adaptedServer = convertServer(server, serverClass);
		if (adaptedServer == null) {
			throw new CoreException(
					new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,
							NLS.bind(Messages.CannotSetUpImproperServer, server.getName())));

		}
		return adaptedServer;
	}

	@SuppressWarnings("unchecked")
	public static <SERVER> SERVER convertServer(IServerAttributes server, Class<SERVER> serverClass) {
		if (server == null) {
			return null;
		}
		SERVER adaptedServer = (SERVER) server.loadAdapter(serverClass, new NullProgressMonitor());
		return adaptedServer;
	}
	
	public static IJBossServer getJBossServer(IServer server) {
		return convertServer(server, IJBossServer.class);
	}

	public static IJBossServer checkedGetJBossServer(IServer server) throws CoreException {
		return checkedConvertServer(server, IJBossServer.class);
	}

	public static IJBossServer getJBossServer(IServerWorkingCopy server) {
		return convertServer(server, IJBossServer.class);
	}

	public static IDeployableServer getDeployableServer(IServerAttributes server) {
		return convertServer(server, IDeployableServer.class);
	}

	public static IDeployableServerBehaviour getDeployableServerBehavior(IServer server) {
		return convertServer(server, IDeployableServerBehaviour.class);
	}

	public static IDelegatingServerBehavior getJBossServerBehavior(IServer server) {
		return convertServer(server, IDelegatingServerBehavior.class);
	}

	/**
	 * Return all JBossServer instances from the ServerCore
	 * 
	 * @return
	 */
	public static IJBossServer[] getAllJBossServers() {
		ArrayList<IJBossServer> servers = new ArrayList<IJBossServer>();
		IServer[] iservers = ServerCore.getServers();
		for (int i = 0; i < iservers.length; i++) {
			if (getJBossServer(iservers[i]) != null) {
				servers.add(getJBossServer(iservers[i]));
			}
		}
		IJBossServer[] ret = new IJBossServer[servers.size()];
		servers.toArray(ret);
		return ret;
	}

	public static IServer[] getJBossServersAsIServers() {
		ArrayList<IServer> servers = new ArrayList<IServer>();
		IServer[] iservers = ServerCore.getServers();
		for (int i = 0; i < iservers.length; i++) {
			if (getJBossServer(iservers[i]) != null) {
				servers.add(iservers[i]);
			}
		}
		IServer[] ret = new IServer[servers.size()];
		servers.toArray(ret);
		return ret;
	}

	public static IDeployableServer[] getAllDeployableServers() {
		ArrayList<IDeployableServer> servers = new ArrayList<IDeployableServer>();
		IServer[] iservers = ServerCore.getServers();
		for (int i = 0; i < iservers.length; i++) {
			if (getDeployableServer(iservers[i]) != null) {
				servers.add(getDeployableServer(iservers[i]));
			}
		}
		IDeployableServer[] ret = new IDeployableServer[servers.size()];
		servers.toArray(ret);
		return ret;
	}

	public static IServer[] getDeployableServersAsIServers() {
		ArrayList<IServer> servers = new ArrayList<IServer>();
		IServer[] iservers = ServerCore.getServers();
		for (int i = 0; i < iservers.length; i++) {
			if (getDeployableServer(iservers[i]) != null) {
				servers.add(iservers[i]);
			}
		}
		IServer[] ret = new IServer[servers.size()];
		servers.toArray(ret);
		return ret;
	}
}
