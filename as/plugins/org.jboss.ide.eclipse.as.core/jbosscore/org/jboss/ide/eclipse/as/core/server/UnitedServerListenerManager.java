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
package org.jboss.ide.eclipse.as.core.server;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;

/**
 * The UnitedServerListenerManager keeps an array of
 * UnitedServerListeners. The manager registers itself as 
 * a listener for all server operations and passes
 * all requests to every UnitedServerListener that's been
 * added to the model.
 * 
 * @author Rob Stryker 
 *
 */
public class UnitedServerListenerManager implements 
	IServerLifecycleListener, IServerListener, IPublishListener {
	protected static UnitedServerListenerManager instance;
	public static UnitedServerListenerManager getDefault() {
		if( instance == null )
			instance = new UnitedServerListenerManager();
		return instance;
	}
	
	protected ArrayList<UnitedServerListener> list;
	protected UnitedServerListenerManager() {
		list = new ArrayList<UnitedServerListener>();
		ServerCore.addServerLifecycleListener(this);
		IServer[] allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ ) {
			allServers[i].addServerListener(this);
			allServers[i].addPublishListener(this);
		}
	}
	
	
	public void addListener(UnitedServerListener listener) {
		if( !list.contains(listener)) {
			list.add(listener);
			IServer[] allServers = ServerCore.getServers();
			for( int i = 0; i < allServers.length; i++ )
				listener.init(allServers[i]);
		}
	}
	public void removeListener(UnitedServerListener listener) {
		list.remove(listener);
		IServer[] allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ )
			listener.cleanUp(allServers[i]);
	}

	public void serverAdded(IServer server) {
		server.addServerListener(this);
		server.addPublishListener(this);
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) {
			i.next().serverAdded(server);
		}
	}
	public void serverChanged(IServer server) {
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) {
			i.next().serverChanged(server);
		}
	}
	public void serverRemoved(IServer server) {
		server.removeServerListener(this);
		server.removePublishListener(this);
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) {
			i.next().serverRemoved(server);
		}
	}
	
	public void serverChanged(ServerEvent event) {
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) {
			i.next().serverChanged(event);
		}
	}

	public void publishStarted(IServer server) {
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) 
			i.next().publishStarted(server);
	}

	public void publishFinished(IServer server, IStatus status) {
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) 
			i.next().publishFinished(server, status);
	}
	
}
