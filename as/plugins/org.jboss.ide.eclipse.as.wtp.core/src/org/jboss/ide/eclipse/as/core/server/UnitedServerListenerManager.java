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

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

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
	
	static class SingletonHolder {
		  static UnitedServerListenerManager instance = new UnitedServerListenerManager();    
	}
	
	public static UnitedServerListenerManager getDefault() {
		return SingletonHolder.instance;
	}
	
	protected CopyOnWriteArrayList<UnitedServerListener> list;
	private boolean delegatesInitialized = false;
	
	
	private UnitedServerListenerManager() {
		list = new CopyOnWriteArrayList<UnitedServerListener>();
		
		/* 
		 
		   Initialize in a new thread, to ensure fast exit of constructor, and
		   that any bundle-loading caused by calls into WTP are all executed in a different thread. 
		   
		   Even Calls in this same thread accessing UnitedServerListenerManager.getDefault() can lead to bugs.
		   The lock on the class object is still in effect, so if  execution flows back into 
		   getDefault() during the constructor, it will instantiate a second UnitedServerListenerManager 
		   object rather than just one. This can possibly cause other bugs, such as missing listeners.
		    
		*/ 
		new Thread() {
			public void run() {
				initializeManager();
			}
		}.start();
	}
	
	private void initializeManager() {
		// Make unsynchronized calls into WTP to register this class as a listener
		// If any bundles get loaded because of these calls, even if in new threads, 
		// execution can still flow into this class's synchronized methods
		IServer[] allServers = ServerCore.getServers();
		ServerCore.addServerLifecycleListener(UnitedServerListenerManager.this);
		ServerCore.addRuntimeLifecycleListener(UnitedServerListenerManager.this);
		for( int i = 0; i < allServers.length; i++ ) {
			protectAddManagerAsListeners(allServers[i]);
		}
		initializeCurrentListeners();
	}
	
	/*
	 * Should not be public
	 */
	@Deprecated
	public synchronized UnitedServerListener[] getListeners() {
		return (UnitedServerListener[]) list.toArray(new UnitedServerListener[list.size()]);
	}
	
	public static boolean isJBossServer(IServer server) {
		return ServerConverter.getJBossServer(server) != null;
	}


	private synchronized boolean isDelegatesInitialized() {
		return delegatesInitialized;
	}
	
	private synchronized void setDelegatesInitialized(boolean val) {
		delegatesInitialized = val;
	}
	public void addListener(UnitedServerListener listener) {
		boolean requiresInit = false;
		synchronized(this) {
			if( !list.contains(listener)) {
				requiresInit = isDelegatesInitialized();
				list.add(listener);
			}
		}
		// united listener manager is already initialized, so init this new listener individually
		if( requiresInit ) {
			initializeListener(listener);
		}
	}

	private void initializeCurrentListeners() {
		Iterator<UnitedServerListener> it = null;
		// Get the current listeners and set initialized to true in one guaranteed block
		synchronized(this) {
			it = list.iterator();
			// Set isInitialized to true now instead of later. 
			// If somehow any init calls load bundles or add new listeners, 
			// the addListener method will need to initialize the new listeners
			setDelegatesInitialized(true);
		}
		while( it.hasNext() ) {
			initializeListener(it.next());
		}
	}

	
	private void initializeListener(UnitedServerListener listener) {
		IServer[] allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ ) {
			listener.init(allServers[i]);
		}
	}
	public void removeListener(UnitedServerListener listener) {
		list.remove(listener);
		IServer[] allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ ) {
			listener.cleanUp(allServers[i]);
		}
	}
	
	
	private synchronized void protectAddManagerAsListeners(IServer server) {
		server.removeServerListener(this);
		server.removePublishListener(this);
		server.addServerListener(this);
		server.addPublishListener(this);
	}
	
	
	/*
	 * Below are all methods for the various WTP server listener interfaces,
	 * which we forward directly to all listeners registered in this class's list
	 * 
	 * They are all unsynchronized to ensure minimal impact or chance for deadlock, 
	 * since any listener could theoretically cause bundles to be loaded
	 * or new listeners to be added.  
	 */

	public void serverAdded(IServer server) {
		protectAddManagerAsListeners(server);
		for (UnitedServerListener working : list) {
			if( working.canHandleServer(server))
				working.serverAdded(server);
		}
	}
	public void serverChanged(IServer server) {
		for (UnitedServerListener working : list) {
			if( working.canHandleServer(server))
				working.serverChanged(server);
		}
	}
	public void serverRemoved(IServer server) {
		server.removeServerListener(this);
		server.removePublishListener(this);
		for (UnitedServerListener working : list) {
			if( working.canHandleServer(server))
				working.serverRemoved(server);
		}
	}
	
	public void serverChanged(ServerEvent event) {
		IServer server = event.getServer();
		for (UnitedServerListener working : list) {
			if( working.canHandleServer(server))
				working.serverChanged(event);
		}
	}

	public void publishStarted(IServer server) {
		for (UnitedServerListener working : list) {
			if( working.canHandleServer(server))
				working.publishStarted(server);
		}
	}

	public void publishFinished(IServer server, IStatus status) {
		for (UnitedServerListener working : list) {
			if( working.canHandleServer(server))
				working.publishFinished(server, status);
		}
	}

	public void runtimeAdded(IRuntime runtime) {
		for (UnitedServerListener working : list) {
			if( working.canHandleRuntime(runtime))
				working.runtimeAdded(runtime);
		}
	}
	public void runtimeChanged(IRuntime runtime) {
		for (UnitedServerListener working : list) {
			if( working.canHandleRuntime(runtime))
				working.runtimeChanged(runtime);
		}
	}
	public void runtimeRemoved(IRuntime runtime) {
		for (UnitedServerListener working : list) {
			if( working.canHandleRuntime(runtime))
				working.runtimeRemoved(runtime);
		}
	}
}
