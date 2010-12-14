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
package org.jboss.ide.eclipse.as.core.extensions.jmx;

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
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionProviderListener;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public class JBossServerConnectionProvider implements IConnectionProvider, IServerLifecycleListener {
	public static final String PROVIDER_ID = "org.jboss.ide.eclipse.as.core.extensions.jmx.JBossServerConnectionProvider"; //$NON-NLS-1$
	
	public static JBossServerConnectionProvider getProvider() {
		return (JBossServerConnectionProvider)ExtensionManager.getProvider(PROVIDER_ID);
	}
	
	public static JBossServerConnection getConnection(IServer s) {
		return (JBossServerConnection)getProvider().findConnection(s);
	}
	
	// Run this action on the server. 
	// If the connection doesn't exist, make a new one
	public static void run(IServer s, IJMXRunnable r) throws JMXException {
		JBossServerConnection c = getConnection(s);
		if( c != null )
			// JMX is not installed here
			c.run(r);
	}
	
	
	private ArrayList<IConnectionProviderListener> listeners = 
		new ArrayList<IConnectionProviderListener>();
	
	private HashMap<String, JBossServerConnection> idToConnection;
	public JBossServerConnectionProvider() {
		ServerCore.addServerLifecycleListener(this);
	}

	protected boolean belongsHere(IServer server) {
		return ServerConverter.getJBossServer(server) != null;
	}
	
	protected JBossServerConnection createConnection(IServer server) {
		return new JBossServerConnection(server); 
	}
	
	public void serverAdded(IServer server) {
		if( belongsHere(server)) {
			getConnections();
			if( !idToConnection.containsKey(server.getId())) {
				JBossServerConnection connection = createConnection(server);
				idToConnection.put(server.getId(), connection);
			}
			fireAdded(idToConnection.get(server.getId()));
		}
	}

	public void serverChanged(IServer server) {
		if( belongsHere(server)) {
			getConnections();
			if( !idToConnection.containsKey(server.getId())) {
				JBossServerConnection connection = createConnection(server);
				idToConnection.put(server.getId(), connection);
			}
			fireAdded(idToConnection.get(server.getId()));
		}
	}

	public void serverRemoved(IServer server) {
		if( belongsHere(server)) {
			JBossServerConnection connection;
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
				fireRemoved(createConnection(server));
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
			idToConnection = new HashMap<String, JBossServerConnection>();
			IServer[] allServers = ServerCore.getServers();
			JBossServerConnection c;
			for( int i = 0; i < allServers.length; i++ ) {
				if( belongsHere(allServers[i])) {
					c = createConnection(allServers[i]);
					if( c != null ) 
						idToConnection.put(allServers[i].getId(), c);
				}
			}
		} 
		ArrayList<JBossServerConnection> list = new ArrayList<JBossServerConnection>();
		list.addAll(idToConnection.values());
		return list.toArray(new JBossServerConnection[list.size()]);
	}
	
	public String getName(IConnectionWrapper wrapper) {
		if( wrapper instanceof JBossServerConnection) {
			return ((JBossServerConnection)wrapper).getName();
		}
		return null;
	}

	public String getId() {
		return PROVIDER_ID;
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
			} catch(RuntimeException re) {}
	}

	void fireChanged(IConnectionWrapper wrapper) {
		for(Iterator<IConnectionProviderListener> i = listeners.iterator(); i.hasNext();)
			try {
				i.next().connectionChanged(wrapper);
			} catch(RuntimeException re) {}
	}

	void fireRemoved(IConnectionWrapper wrapper) {
		for(Iterator<IConnectionProviderListener> i = listeners.iterator(); i.hasNext();)
			try {
				i.next().connectionRemoved(wrapper);
			} catch(RuntimeException re) {}
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
}
