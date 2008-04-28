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
package org.jboss.ide.eclipse.as.core.util;

import java.util.ArrayList;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class ServerConverter {
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
		return (DeployableServerBehavior)server.loadAdapter(
				DeployableServerBehavior.class, new NullProgressMonitor());
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

}
