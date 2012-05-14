/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.jmx.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionProviderListener;
import org.jboss.tools.jmx.core.IConnectionWrapper;

public abstract class AbstractJBossJMXConnectionProvider implements IConnectionProvider, IServerLifecycleListener  {

	private ArrayList<IConnectionProviderListener> listeners = 
		new ArrayList<IConnectionProviderListener>();
	
	private HashMap<String, IConnectionWrapper> idToConnection;
	public AbstractJBossJMXConnectionProvider() {
		ServerCore.addServerLifecycleListener(this);
	}
	
	protected abstract boolean belongsHere(IServer server);
	public abstract String getId();
	protected abstract IConnectionWrapper createConnection(IServer server);
	public abstract String getName(IConnectionWrapper wrapper);

	public void serverAdded(IServer server) {
		if( belongsHere(server)) {
			getConnections();
			if( !idToConnection.containsKey(server.getId())) {
				IConnectionWrapper connection = createConnection(server);
				idToConnection.put(server.getId(), connection);
				if( connection != null )
					fireAdded(idToConnection.get(server.getId()));
			}
		}
	}

	public void serverChanged(IServer server) {
		if( belongsHere(server)) {
			getConnections();
			Object o = idToConnection.get(server.getId());
			if( o == null ) {
				IConnectionWrapper connection = createConnection(server);
				idToConnection.put(server.getId(), connection);
				if( connection != null )
					fireAdded(idToConnection.get(server.getId()));
			}
		}
	}

	public void serverRemoved(IServer server) {
		if( belongsHere(server)) {
			IConnectionWrapper connection;
			if( idToConnection != null ) {
				connection = idToConnection.get(server.getId());
				if( connection != null ) {
					idToConnection.remove(server.getId());
					fireRemoved(connection);
				}
			} else {
				// hasn't been initialized yet
				getConnections();
				
				// but now its missing from the collection, so make one up
				IConnectionWrapper dummy = createConnection(server);
				
				// Make sure we don't fire a removal for a connection that doesn't exist
				if( dummy != null )
					fireRemoved(dummy);
			}
		}
	}

	public IConnectionWrapper findConnection(IServer s) {
		getConnections();
		return idToConnection.get(s.getId());
	}
	
	public IConnectionWrapper[] getConnections() {
		// do it all on demand right now
		if( idToConnection == null ) {
			// load them all
			idToConnection = new HashMap<String, IConnectionWrapper>();
			IServer[] allServers = ServerCore.getServers();
			IConnectionWrapper c;
			for( int i = 0; i < allServers.length; i++ ) {
				if( belongsHere(allServers[i])) {
					c = createConnection(allServers[i]);
					if( c != null ) 
						idToConnection.put(allServers[i].getId(), c);
				}
			}
		} 
		ArrayList<IConnectionWrapper> list = new ArrayList<IConnectionWrapper>();
		list.addAll(idToConnection.values());
		return list.toArray(new IConnectionWrapper[list.size()]);
	}
	
	public void addListener(IConnectionProviderListener listener) {
		if( !listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeListener(IConnectionProviderListener listener) {
		listeners.remove(listener);
	}
	
	void fireAdded(IConnectionWrapper wrapper) {
		for(Iterator<IConnectionProviderListener> i = listeners.iterator(); i.hasNext();)
			try {
				i.next().connectionAdded(wrapper);
			} catch(RuntimeException re) {
				// Intentionally ignore. This is just to protect against a bad implementer blowing away the stack
			}
	}

	void fireChanged(IConnectionWrapper wrapper) {
		for(Iterator<IConnectionProviderListener> i = listeners.iterator(); i.hasNext();)
			try {
				i.next().connectionChanged(wrapper);
			} catch(RuntimeException re) {
				// Intentionally ignore. This is just to protect against a bad implementer blowing away the stack
			}
	}

	void fireRemoved(IConnectionWrapper wrapper) {
		for(Iterator<IConnectionProviderListener> i = listeners.iterator(); i.hasNext();)
			try {
				i.next().connectionRemoved(wrapper);
			} catch(RuntimeException re) {
				// Intentionally ignore. This is just to protect against a bad implementer blowing away the stack
			}
	}

	public boolean canCreate() {
		return false;
	}

	@SuppressWarnings(value={"unchecked"})
	public IConnectionWrapper createConnection(Map map) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, Messages.NotSupported, null));
	}

	public void addConnection(IConnectionWrapper connection) {
		// Not Supported
	}
	public void removeConnection(IConnectionWrapper connection) {
		// Not Supported
	}
	public boolean canDelete(IConnectionWrapper wrapper) {
		return false;
	}
	public void connectionChanged(IConnectionWrapper connection) {
		// do nothing
	}
	
	public boolean hasClassloaderRepository() {
		return false;
	}
	
	public JMXClassLoaderRepository getClassloaderRepository() {
		return null;
	}
}
