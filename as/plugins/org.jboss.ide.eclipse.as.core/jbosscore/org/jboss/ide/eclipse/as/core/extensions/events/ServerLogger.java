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
package org.jboss.ide.eclipse.as.core.extensions.events;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.osgi.service.prefs.BackingStoreException;

public class ServerLogger {
	private static final String RELOG_ERROR_PREF_KEY = "package org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger.RELOG_ERROR_PREF_KEY"; //$NON-NLS-1$
	private static ServerLogger instance;
	public static ServerLogger getDefault() {
		if( instance == null ) {
			instance = new ServerLogger();
		}
		return instance;
	}
	
	private HashMap<String, ServerLog> map = new HashMap<String, ServerLog>();
	private HashMap<String, ArrayList<IServerLogListener>> listeners = 
		new HashMap<String, ArrayList<IServerLogListener>>();
	
	public void addListener(IServer server, IServerLogListener listener) {
		ArrayList<IServerLogListener> list = listeners.get(server.getId());
		if( list == null ) {
			list = new ArrayList<IServerLogListener>();
			listeners.put(server.getId(), list);
		}
		list.add(listener);
	}
	
	public void removeListener(IServer server, IServerLogListener listener) {
		ArrayList<IServerLogListener> list = listeners.get(server.getId());
		if( list != null ) {
			list.remove(listener);
		}
	}
	
	public void log(IServer server, IStatus status) {
		ServerLog log = map.get(server.getId());
		if( log == null ) {
			log = new ServerLog(getServerLogFile(server));
			map.put(server.getId(), log);
		}
		log.log(status);
		if( shouldDoubleLogErrors() && status.getSeverity() == IStatus.ERROR) {
			JBossServerCorePlugin.getDefault().getLog().log(status);
		}
		
		ArrayList<IServerLogListener> list = listeners.get(server.getId());
		if( list != null ) {
			IServerLogListener[] listeners = 
				list.toArray(new IServerLogListener[list.size()]);
			for( int i = 0; i < listeners.length;i++) 
				listeners[i].logging(status, server);
		}
	}
	
	public static boolean shouldDoubleLogErrors() {
		IEclipsePreferences prefs = new InstanceScope().getNode(JBossServerCorePlugin.PLUGIN_ID);
		return prefs.getBoolean(RELOG_ERROR_PREF_KEY, true);
	}
	
	public static void setDoubleLogErrors(boolean val) {
		IEclipsePreferences prefs = new InstanceScope().getNode(JBossServerCorePlugin.PLUGIN_ID);
		prefs.putBoolean(RELOG_ERROR_PREF_KEY, val);
		try {
			prefs.flush();
		} catch( BackingStoreException bse) {
		}
	}
	
	public static File getServerLogFile(IServer server) {
		File f = JBossServerCorePlugin.getServerStateLocation(
				server).append(IJBossToolingConstants.LOG).toFile();
		if( !f.getParentFile().exists() ) 
			f.getParentFile().mkdirs();
		return f;
	}
	
	public static IServer findServerForFile(File file) {
		if( file == null )
			return null;
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ )
			if(getServerLogFile(servers[i]).equals(file))
				return servers[i];
		return null;
	}
}
