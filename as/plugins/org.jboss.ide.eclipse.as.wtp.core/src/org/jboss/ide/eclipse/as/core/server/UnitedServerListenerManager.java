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
package org.jboss.ide.eclipse.as.core.server;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

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
	IServerLifecycleListener, IServerListener, IPublishListener, IRuntimeLifecycleListener {
	protected static UnitedServerListenerManager instance;
	private boolean isInitialized = false;
	
	public static synchronized UnitedServerListenerManager getDefault() {
		if( instance == null )
			instance = new UnitedServerListenerManager();
		return instance;
	}
	
	protected ArrayList<UnitedServerListener> list;
	private UnitedServerListenerManager() {
		list = new ArrayList<UnitedServerListener>();
		ServerCore.addServerLifecycleListener(UnitedServerListenerManager.this);
		ServerCore.addRuntimeLifecycleListener(UnitedServerListenerManager.this);
		IServer[] allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ ) {
			// Workaround line for eclipse bug 437351
			allServers[i].removeServerListener(new UnitedServerListener());
			// Workaround line for eclipse bug 437351
			allServers[i].removePublishListener(new UnitedServerListener());
			
			allServers[i].addServerListener(UnitedServerListenerManager.this);
			allServers[i].addPublishListener(UnitedServerListenerManager.this);
		}
	}
	
	public synchronized UnitedServerListener[] getListeners() {
		return (UnitedServerListener[]) list.toArray(new UnitedServerListener[list.size()]);
	}
	
	public static boolean isJBossServer(IServer server) {
		return ServerConverter.getJBossServer(server) != null;
	}


	public synchronized void addListener(UnitedServerListener listener) {
		if( !list.contains(listener)) {
			list.add(listener);
			// united listener manager is already initialized, so init this new listener individually
			if( isInitialized )
				initializeListener(listener);
		}
	}

	private synchronized void initializeCurrentListeners() {
		Iterator<UnitedServerListener> it = list.iterator();
		while(it.hasNext()) {
			initializeListener(it.next());
		}
		isInitialized = true;
	}

	
	private synchronized void initializeListener(UnitedServerListener listener) {
		IServer[] allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ ) {
			listener.init(allServers[i]);
		}
	}
	public synchronized void removeListener(UnitedServerListener listener) {
		list.remove(listener);
		IServer[] allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ ) {
			listener.cleanUp(allServers[i]);
		}
	}

	public void serverAdded(IServer server) {
		server.addServerListener(this);
		server.addPublishListener(this);
		UnitedServerListener[] listeners = getListeners();
		for( int i = 0; i < listeners.length; i++) {
			if( listeners[i].canHandleServer(server))
				listeners[i].serverAdded(server);
		}
	}
	public void serverChanged(IServer server) {
		UnitedServerListener[] listeners = getListeners();
		for( int i = 0; i < listeners.length; i++) {
			if( listeners[i].canHandleServer(server))
				listeners[i].serverChanged(server);
		}
	}
	public void serverRemoved(IServer server) {
		server.removeServerListener(this);
		server.removePublishListener(this);
		UnitedServerListener[] listeners = getListeners();
		for( int i = 0; i < listeners.length; i++) {
			if( listeners[i].canHandleServer(server))
				listeners[i].serverRemoved(server);
		}
	}
	
	public void serverChanged(ServerEvent event) {
		IServer server = event.getServer();
		UnitedServerListener[] listeners = getListeners();
		for( int i = 0; i < listeners.length; i++) {
			if( listeners[i].canHandleServer(server))
				listeners[i].serverChanged(event);
		}
	}

	public void publishStarted(IServer server) {
		UnitedServerListener[] listeners = getListeners();
		for( int i = 0; i < listeners.length; i++) {
			if( listeners[i].canHandleServer(server))
				listeners[i].publishStarted(server);
		}
	}

	public void publishFinished(IServer server, IStatus status) {
		UnitedServerListener[] listeners = getListeners();
		for( int i = 0; i < listeners.length; i++) {
			if( listeners[i].canHandleServer(server))
				listeners[i].publishFinished(server, status);
		}
	}

	public void runtimeAdded(IRuntime runtime) {
		UnitedServerListener[] listeners = getListeners();
		for( int i = 0; i < listeners.length; i++) {
			if( listeners[i].canHandleRuntime(runtime))
				listeners[i].runtimeAdded(runtime);
		}
	}
	public void runtimeChanged(IRuntime runtime) {
		UnitedServerListener[] listeners = getListeners();
		for( int i = 0; i < listeners.length; i++) {
			if( listeners[i].canHandleRuntime(runtime))
				listeners[i].runtimeChanged(runtime);
		}
	}
	public void runtimeRemoved(IRuntime runtime) {
		UnitedServerListener[] listeners = getListeners();
		for( int i = 0; i < listeners.length; i++) {
			if( listeners[i].canHandleRuntime(runtime))
				listeners[i].runtimeRemoved(runtime);
		}
	}
}
