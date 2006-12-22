/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.jboss.ide.eclipse.as.core.model.ModuleModel;
import org.jboss.ide.eclipse.as.core.model.PackagesBuildListener;
import org.jboss.ide.eclipse.as.core.runtime.server.IServerPollerTimeoutListener;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;

/**
 * 
 * @author rstryker
 *
 */
public class JBossServerCore {

	/*
	 * Static portion
	 */
	private static JBossServerCore instance;
	
	public static JBossServerCore getDefault() {
		if( instance == null ) {
			instance = new JBossServerCore();
		}
		return instance;
	}
	
	public static JBossServer getJBossServer(IServer server) {
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
	
	/**
	 * Return all JBossServer instances from the ServerCore
	 * @return
	 */
	public static JBossServer[] getAllJBossServers() {
		ArrayList servers = new ArrayList();
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
		ArrayList servers = new ArrayList();
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
		ArrayList servers = new ArrayList();
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
		ArrayList servers = new ArrayList();
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

	
	public JBossServerCore() {
		ModuleModel.getDefault();
		new PackagesBuildListener();
	}

	public static ModuleFactory[] getJBossModuleFactories() {
		return ModuleModel.getJBossModuleFactories();
	}
	
	
	private HashMap pollerListeners = null;
	public IServerPollerTimeoutListener[] getTimeoutListeners(String pollerClass) {
		if( pollerListeners == null ) 
			loadTimeoutListeners();
		ArrayList list = (ArrayList)pollerListeners.get(pollerClass);
		if( list != null ) {
			return (IServerPollerTimeoutListener[]) list.toArray(new IServerPollerTimeoutListener[list.size()]);
		}
		return new IServerPollerTimeoutListener[0];
	}
	
	protected void loadTimeoutListeners() {
		pollerListeners = new HashMap();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "pollerTimeoutListener");
		for( int i = 0; i < cf.length; i++ ) {
			try {
				String poller = cf[i].getAttribute("pollerType");
				Object listener = cf[i].createExecutableExtension("listener");
				
				ArrayList list = (ArrayList)pollerListeners.get(poller);
				if( list == null ) list = new ArrayList();
				list.add(listener);
				pollerListeners.put(poller, list);
			} catch( CoreException ce ) {
				ce.printStackTrace();
			}
		}
	}
}
