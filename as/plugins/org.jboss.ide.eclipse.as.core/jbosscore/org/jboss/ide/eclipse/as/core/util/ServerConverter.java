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
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ServerBehavior;

/**
 * 
 * @author rob.stryker@jboss.com
 * @author adietish@redhat.com
 */
public class ServerConverter {

	public static JBossServer findJBossServer(String serverId) throws CoreException {
		if (serverId == null)
			return null;

		IServer s = ServerCore.findServer(serverId);
		if (s == null)
			return null;
		return getJBossServer(s);
	}

	@SuppressWarnings("unchecked")
	public static <SERVER> SERVER checkedLoadAdapter(IServer server, Class<SERVER> serverClass) throws CoreException {
		if (server == null) {
			return null;
		}
		SERVER adaptedServer = (SERVER) server.loadAdapter(serverClass, new NullProgressMonitor());
		if (adaptedServer == null) {
			throw new CoreException(
					new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
							NLS.bind(Messages.CannotSetUpImproperServer, server.getName())));

		}
		return adaptedServer;
	}
	
	public static JBossServer getJBossServer(IServer server) {
		if (server == null) {
			return null;
		}
		JBossServer jbServer = (JBossServer) server.getAdapter(JBossServer.class);
		if (jbServer == null) {
			jbServer = (JBossServer) server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		}
		return jbServer;
	}

	public static JBossServer checkedGetJBossServer(IServer server) throws CoreException {
		JBossServer jBossServer = getJBossServer(server);
		if (jBossServer == null) {
			throw new CoreException(
					new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
							NLS.bind(Messages.CannotSetUpImproperServer, server.getName())));
		}
		return jBossServer;
	}

	public static JBossServer getJBossServer(IServerWorkingCopy server) {
		if (server == null) {
			return null;
		}
		JBossServer jbServer = (JBossServer) server.getAdapter(JBossServer.class);
		if (jbServer == null) {
			jbServer = (JBossServer) server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		}
		return jbServer;
	}

	public static IDeployableServer getDeployableServer(IServer server) {
		if (server == null) {
			return null;
		}
		IDeployableServer dep = (IDeployableServer) server.getAdapter(IDeployableServer.class);
		if (dep == null) {
			dep = (IDeployableServer) server.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
		}
		return dep;
	}

	public static DeployableServerBehavior getDeployableServerBehavior(IServer server) {
		if (server == null)
			return null;
		return (DeployableServerBehavior) server.loadAdapter(
				DeployableServerBehavior.class, new NullProgressMonitor());
	}

	public static JBoss7ServerBehavior getJBoss7ServerBehavior(IServer server) {
		if (server == null)
			return null;
		return (JBoss7ServerBehavior) server.loadAdapter(
				JBoss7ServerBehavior.class, new NullProgressMonitor());
	}

	/**
	 * Return all JBossServer instances from the ServerCore
	 * 
	 * @return
	 */
	public static JBossServer[] getAllJBossServers() {
		ArrayList<JBossServer> servers = new ArrayList<JBossServer>();
		IServer[] iservers = ServerCore.getServers();
		for (int i = 0; i < iservers.length; i++) {
			if (getJBossServer(iservers[i]) != null) {
				servers.add(getJBossServer(iservers[i]));
			}
		}
		JBossServer[] ret = new JBossServer[servers.size()];
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

	@Deprecated
	public static IJBossServerRuntime getJBossRuntime(IServer server) throws CoreException {
		return RuntimeUtils.checkedGetJBossServerRuntime(server);
	}

	@Deprecated
	public static IJBossServerRuntime getJBossRuntime(IServerAttributes server) {
		return RuntimeUtils.getJBossServerRuntime(server);
	}
}
