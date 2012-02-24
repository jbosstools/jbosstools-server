/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionProviderListener;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;
import org.jboss.tools.jmx.core.providers.DefaultConnectionWrapper;
import org.jboss.tools.jmx.core.providers.MBeanServerConnectionDescriptor;

public class JBossServerConnectionProvider implements IConnectionProvider, IServerLifecycleListener {
	public static final String PROVIDER_ID = "org.jboss.ide.eclipse.as.core.extensions.jmx.JBossServerConnectionProvider"; //$NON-NLS-1$
	
	public static JBossServerConnectionProvider getProvider() {
		return (JBossServerConnectionProvider)ExtensionManager.getProvider(PROVIDER_ID);
	}
	
	public static IConnectionWrapper getConnection(IServer s) {
		return getProvider().findConnection(s);
	}
	
	// Run this action on the server. 
	// If the connection doesn't exist, make a new one
	public static void run(IServer s, IJMXRunnable r) throws JMXException {
		IConnectionWrapper c = getConnection(s);
		if( c != null )
			// JMX is not installed here
			c.run(r);
	}
	
	
	private ArrayList<IConnectionProviderListener> listeners = 
		new ArrayList<IConnectionProviderListener>();
	
	private HashMap<String, IConnectionWrapper> idToConnection;
	public JBossServerConnectionProvider() {
		ServerCore.addServerLifecycleListener(this);
	}

	protected boolean belongsHere(IServer server) {
		return ServerConverter.getJBossServer(server) != null;
	}
	
	protected boolean requiresDefaultProvider(IServer server) {
		if(server.getServerType().getId().equals(IJBossToolingConstants.SERVER_AS_70))
				return true;
		return false;
	}
	
	protected IConnectionWrapper createConnection(IServer server) {
		if( !requiresDefaultProvider(server))
			return new JBossServerConnection(server);
		return createDefaultServerConnection(server);
	}
	
	protected IConnectionWrapper createDefaultServerConnection(IServer server) {
		// This situation is not even fully supported and requires revisiting
		String SIMPLE_PREFIX = "service:jmx:rmi:///jndi/rmi://"; //$NON-NLS-1$  constants are in jmx.ui feh
		String SIMPLE_SUFFIX = "/jmxrmi"; //$NON-NLS-1$
		String host = server.getHost();
		String port = "1090"; // TODO fix hard code
		String url = SIMPLE_PREFIX + host + ":" + port + SIMPLE_SUFFIX; //$NON-NLS-1$ 

		MBeanServerConnectionDescriptor desc = new
				MBeanServerConnectionDescriptor(server.getName(), url, "", "");
		try {
			return new ExtendedDefaultConnectionWrapper(desc, server);
		} catch( MalformedURLException murle) {
			// TODO log  
			return null;
		}
	}
	
	private class ExtendedDefaultConnectionWrapper extends DefaultConnectionWrapper 
	 	implements IServerListener, IConnectionProviderListener {
		private IServer server;
		public ExtendedDefaultConnectionWrapper(
				MBeanServerConnectionDescriptor descriptor, IServer server)
				throws MalformedURLException {
			super(descriptor);
			this.server = server;
			server.addServerListener(this);
		}
		public void serverChanged(ServerEvent event) {
			int eventKind = event.getKind();
			if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
				// server change event
				if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
					boolean started = event.getServer().getServerState() == IServer.STATE_STARTED;
					try {
						if( started )
							connect();
						else
							disconnect();
					} catch( IOException ioe) {
						if( started ) 
							JBossServerCorePlugin.log(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Error connecting to this server's JMX service: " + event.getServer().getName(), ioe));
						else
							JBossServerCorePlugin.log(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Error disconnecting from this server's JMX service: " + event.getServer().getName(), ioe));
					}
				}
			}
		}
		public void connectionAdded(IConnectionWrapper connection) {
		}
		public void connectionRemoved(IConnectionWrapper connection) {
			if( connection == this )
				server.removeServerListener(this);
		}
		public void connectionChanged(IConnectionWrapper connection) {
		}
	}
	
	public void serverAdded(IServer server) {
		if( belongsHere(server)) {
			getConnections();
			if( !idToConnection.containsKey(server.getId())) {
				IConnectionWrapper connection = createConnection(server);
				idToConnection.put(server.getId(), connection);
			}
			fireAdded(idToConnection.get(server.getId()));
		}
	}

	public void serverChanged(IServer server) {
		if( belongsHere(server)) {
			getConnections();
			if( !idToConnection.containsKey(server.getId())) {
				IConnectionWrapper connection = createConnection(server);
				idToConnection.put(server.getId(), connection);
			}
			fireAdded(idToConnection.get(server.getId()));
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
}
