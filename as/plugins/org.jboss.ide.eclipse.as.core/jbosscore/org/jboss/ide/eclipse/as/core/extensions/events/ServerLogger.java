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
package org.jboss.ide.eclipse.as.core.extensions.events;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;

public class ServerLogger implements IJBossServerConstants {
	public static final int MAJOR_TYPE_MASK = 0x11111111 << 24;
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
		
		ArrayList<IServerLogListener> list = listeners.get(server.getId());
		if( list != null ) {
			IServerLogListener[] listeners = 
				list.toArray(new IServerLogListener[list.size()]);
			for( int i = 0; i < listeners.length;i++) 
				listeners[i].logging(status, server);
		}
	}
	
	public static File getServerLogFile(IServer server) {
		File f = server == null ? PLUGIN_LOCATION.toFile() : 
			PLUGIN_LOCATION.append(server.getId()).append(LOG).toFile();
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
