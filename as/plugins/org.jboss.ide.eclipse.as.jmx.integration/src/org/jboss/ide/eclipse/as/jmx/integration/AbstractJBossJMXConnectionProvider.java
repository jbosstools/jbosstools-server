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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.tools.jmx.core.AbstractConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionCategory;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionProviderEventEmitter;
import org.jboss.tools.jmx.core.IConnectionWrapper;

public abstract class AbstractJBossJMXConnectionProvider extends AbstractConnectionProvider 
	implements IConnectionProvider, IConnectionProviderEventEmitter, IConnectionCategory {

	private HashMap<String, IConnectionWrapper> idToConnection;
	public AbstractJBossJMXConnectionProvider() {
		UnitedServerListener listener = createUnitedListener();
		UnitedServerListenerManager.getDefault().addListener(listener);
	}
	
	private UnitedServerListener createUnitedListener() {
		UnitedServerListener listener = new UnitedServerListener() {
			public boolean canHandleServer(IServer server) {
				return true;
			}

			// server state or publish event
			public void serverChanged(ServerEvent event) {
				if( belongsHere(event.getServer())) {
					init();
					boolean switchToStarted = serverSwitchesToState(event, IServer.STATE_STARTED);
					boolean switchToStopped = serverSwitchesToState(event, IServer.STATE_STOPPED);
					
					IConnectionWrapper con = idToConnection.get(event.getServer().getId());
					if( switchToStarted ) {
						if( con == null && getConnectionPersistenceBehavior() == ON_START) {
							// We don't have a connection yet, but this server is started, so lets create AND register the connection
							serverAdded(event.getServer());
						} else if( con != null ) {
							// Connection already created it. Let's just register it
							fireAdded(con);
						}
					} else if( switchToStopped ) {
						try {
							if( con.isConnected()) {
								con.disconnect();
							}
						} catch (IOException e) {
						}
						if( getConnectionPersistenceBehavior() == ON_START) {
							serverRemoved(event.getServer());
						} else {
							if( con != null ) {
								fireRemoved(con);
							}
						}
					}
				}
			}

			// Detail in server has changed (ie a property / attribute)
			public void serverChanged(IServer server) {
				if( belongsHere(server)) {
					init();
					Object o = idToConnection.get(server.getId());
					if( o == null && getConnectionPersistenceBehavior() == ON_SERVER_ADD_REMOVE) {
						// A jmx con. should have been registered earlier, but it was missed somehow
						IConnectionWrapper connection = createConnection(server);
						idToConnection.put(server.getId(), connection);
						if( connection != null && server.getServerState() == IServer.STATE_STARTED )
							fireAdded(connection);
					}
				}
			}
			
			public void serverAdded(IServer server) {
				if( belongsHere(server)) {
					init();
					IConnectionWrapper con = idToConnection.get(server.getId());
					if( con != null ) {
						serverRemoved(server);
					}
					if(getConnectionPersistenceBehavior() == ON_SERVER_ADD_REMOVE || server.getServerState() == IServer.STATE_STARTED ) {
						IConnectionWrapper connection = createConnection(server);
						idToConnection.put(server.getId(), connection);
						if( connection != null && server.getServerState() == IServer.STATE_STARTED ) {
							fireAdded(connection);
						}
					} 
				}
			}


			public void serverRemoved(IServer server) {
				if( belongsHere(server)) {
					init();
					IConnectionWrapper connection = idToConnection.get(server.getId());
					if( connection != null ) {
						idToConnection.remove(server.getId());
						fireRemoved(connection);
					}
				}
			}
			

			protected void init() {
				if( idToConnection == null ) {
					getConnections();
				}
			}
		};
		return listener;
	}
	
	
	protected static final boolean ON_SERVER_ADD_REMOVE = false;
	protected static final boolean ON_START = true;
	
	/**
	 * Should we make a new connection each time the server starts?
	 * This is an internal cache issue only. 
	 * The viewer will still only show connections for servers that are started no matter what
	 * 
	 * @return true if new connection, false if re-use old object
	 */
	protected boolean getConnectionPersistenceBehavior() {
		return ON_SERVER_ADD_REMOVE;
	}
	
	protected abstract boolean belongsHere(IServer server);
	public abstract String getId();
	protected abstract IConnectionWrapper createConnection(IServer server);
	public abstract String getName(IConnectionWrapper wrapper);


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
			if( getConnectionPersistenceBehavior() == ON_SERVER_ADD_REMOVE ) {
				for( int i = 0; i < allServers.length; i++ ) {
					if( belongsHere(allServers[i])) {
						c = createConnection(allServers[i]);
						if( c != null ) 
							idToConnection.put(allServers[i].getId(), c);
					}
				}
			}
		} 
		ArrayList<IConnectionWrapper> list = new ArrayList<IConnectionWrapper>();
		Set<String> serverIds = idToConnection.keySet();
		Iterator<String> it = serverIds.iterator();
		while(it.hasNext()) {
			String id = it.next();
			if( isServerStarted(id)) {
				list.add(idToConnection.get(id));
			}
		}
		return list.toArray(new IConnectionWrapper[list.size()]);
	}
	
	private boolean isServerStarted(String id) {
		IServer s = ServerCore.findServer(id);
		if( s != null ) {
			return s.getServerState() == IServer.STATE_STARTED;
		}
		return false;
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
	

	@Override
	public String getCategoryId() {
		return IConnectionCategory.SERVER_CATEGORY;
	}
}
