/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.wst.server.ui.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class ServerManager {
	private static ServerManager instance;
	
	public static synchronized ServerManager getInstance() {
		if(instance == null) {
			instance = new ServerManager();
		}
		return instance;
	}
	
	private List<ServerManagerListener> listeners = new ArrayList<ServerManagerListener>();
	private IServerListener serverListener;
	protected IServer[] servers = new IServer[0];

	IServer selected = null;

	public ServerManager() {
		serverListener = new ServerListenerImpl();
		load();
	}
	
	void load() {
		servers = (IServer[])ServerCore.getServers().clone();
		loadSelectedServer();
		ServerResourceListenerImpl listener = new ServerResourceListenerImpl();
		ServerCore.addRuntimeLifecycleListener(listener);
		ServerCore.addServerLifecycleListener(listener);
	}
	
	private void loadSelectedServer() {
		if(servers == null || servers.length == 0) return;
		String ds = getDefaultWebServer();
		IServer s = getServer(ds);
		if(s == null && servers.length > 0) {
			s = servers[0];
			setDefaultWebServer(servers[0].getId());
		}
		setSelectedServerInternal(s);
	}
	
	public IServer[] getServers() {
		return servers;
	}
	
	public IServer getServer(String serverId) {
		for (int i = 0; i < servers.length; i++) {
			if(servers[i].getId().equals(serverId)) {
				return servers[i];
			}
		}
		return null;
	}
	
	public void addListener(ServerManagerListener listener) {
		if(!listeners.contains(listener)) listeners.add(listener);
	}
	
	public void removeListener(ServerManagerListener listener) {
		listeners.remove(listener);
	}
	
	void fire() {
		ServerManagerListener[] ls = (ServerManagerListener[])listeners.toArray(new ServerManagerListener[0]);
		for (int i = 0; i < ls.length; i++) {
			ls[i].serverManagerChanged();
		}
	}
	
	public void setSelectedServer(String id) {
		IServer server = getServer(id);
		if(server == selected) return;
		setSelectedServerInternal(server);
		setDefaultWebServer(id);
		fire();
	}
	
	private void setSelectedServerInternal(IServer server) {
		if(selected == server) return;
		if(selected != null) selected.removeServerListener(serverListener);
		selected = server;
		if(selected != null) selected.addServerListener(serverListener);
	}
	
	public String getSelectedServerId() {
		String result = getDefaultWebServer();
		return result == null ? "" : result;
	}

	public IServer getSelectedServer() {
		return selected;
	}

	class ServerResourceListenerImpl implements IRuntimeLifecycleListener, IServerLifecycleListener {
		public void serverAdded(IServer server) {
			IServer[] ss = new IServer[servers.length + 1];
			System.arraycopy(servers, 0, ss, 0, servers.length);
			ss[servers.length] = server;
			servers = ss;
			loadSelectedServer();
			fire();
		}
		public void serverChanged(IServer server) {
			fire();
		}
		public void serverRemoved(IServer server) {
			List<IServer> l = new ArrayList<IServer>();
			for (int i = 0; i < servers.length; i++) {
				if(servers[i] != server) l.add(servers[i]);
			}
			if(l.size() == servers.length) return;
			servers = l.toArray(new IServer[0]);
			loadSelectedServer();
			fire();
		}
		public void runtimeAdded(IRuntime runtime) {
			fire();
		}
		public void runtimeChanged(IRuntime runtime) {
			fire();
		}
		public void runtimeRemoved(IRuntime runtime) {
			fire();
		}
	}
	
	class ServerListenerImpl implements IServerListener {
		public void serverChanged(ServerEvent arg0) {
			fire();
		}
	}
	
	static String DEFAULT_WEB_SERVER = JBossServerUIPlugin.PLUGIN_ID + ".defaultWebServer";
	
	static String getDefaultWebServer() {
		String result = getInstancePreference(DEFAULT_WEB_SERVER);
		return result;
	}
	
	static void setDefaultWebServer(String value) {
		getInstancePreferences().put(DEFAULT_WEB_SERVER, value);
	}

	static IEclipsePreferences getInstancePreferences() {
		return new InstanceScope().getNode(JBossServerUIPlugin.PLUGIN_ID);
	}

	static IEclipsePreferences getDefaultPreferences() {
		return new DefaultScope().getNode(JBossServerUIPlugin.PLUGIN_ID);
	}

	static String getInstancePreference(String key) {
		IEclipsePreferences p = getInstancePreferences();
		String value = p == null ? null : p.get(key, null);
		return value != null ? value : getDefaultPreference(key);
	}

	static String getDefaultPreference(String key) {
		IEclipsePreferences p = getDefaultPreferences();
		return p == null ? null : p.get(key, null);
	}

}
