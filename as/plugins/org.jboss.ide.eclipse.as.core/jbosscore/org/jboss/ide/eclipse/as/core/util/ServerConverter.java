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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ServerBehavior;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class ServerConverter {

	public static JBossServer findJBossServer(String serverId) throws CoreException {
		if (serverId == null)
			return null;

		IServer s = ServerCore.findServer(serverId);
		if (s == null)
			return null;
		return ServerConverter.getJBossServer(s);
	}

	public static JBossServer getJBossServer(IServer server) {
		JBossServer jbServer = (JBossServer)server.getAdapter(JBossServer.class);
		if (jbServer == null) {
			jbServer = (JBossServer) server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		}
		return jbServer;
	}
	public static JBossServer getJBossServer(IServerWorkingCopy server) {
		JBossServer jbServer = (JBossServer)server.getAdapter(JBossServer.class);
		if (jbServer == null) {
			jbServer = (JBossServer) server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		}
		return jbServer;
	}
	
	public static IDeployableServer getDeployableServer(IServer server) {
		IDeployableServer dep = (IDeployableServer)server.getAdapter(IDeployableServer.class);
		if (dep == null) {
			dep = (IDeployableServer) server.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
		}
		return dep;
	}
	
	public static DeployableServerBehavior getDeployableServerBehavior(IServer server) {
		if( server == null ) return null;
		return (DeployableServerBehavior)server.loadAdapter(
				DeployableServerBehavior.class, new NullProgressMonitor());
	}

	public static JBoss7ServerBehavior getJBoss7ServerBehavior(IServer server) {
		if( server == null ) return null;
		return (JBoss7ServerBehavior)server.loadAdapter(
				JBoss7ServerBehavior.class, new NullProgressMonitor());
	}

	/**
	 * Return all JBossServer instances from the ServerCore
	 * @return
	 */
	public static JBossServer[] getAllJBossServers() {
		ArrayList<JBossServer> servers = new ArrayList<JBossServer>();
		IServer[] iservers = ServerCore.getServers();
		for( int i = 0; i < iservers.length; i++ ) {
			if( getJBossServer(iservers[i]) != null ) {
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
		for( int i = 0; i < iservers.length; i++ ) {
			if( getJBossServer(iservers[i]) != null ) {
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
		for( int i = 0; i < iservers.length; i++ ) {
			if( getDeployableServer(iservers[i]) != null ) {
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
		for( int i = 0; i < iservers.length; i++ ) {
			if( getDeployableServer(iservers[i]) != null ) {
				servers.add(iservers[i]);
			}
		}
		IServer[] ret = new IServer[servers.size()];
		servers.toArray(ret);
		return ret;
	}
	public static IJBossServerRuntime getJBossRuntime(IServer server) {
		return getJBossRuntime((IServerAttributes)server);
	}

	public static IJBossServerRuntime getJBossRuntime(IServerAttributes server) {
		if( server == null ) return null;
		IRuntime rt = server.getRuntime();
		return (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, null);
	}
}
