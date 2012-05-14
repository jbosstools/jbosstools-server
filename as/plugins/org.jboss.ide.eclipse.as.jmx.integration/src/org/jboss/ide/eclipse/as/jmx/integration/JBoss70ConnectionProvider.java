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
import java.net.MalformedURLException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionProviderListener;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.providers.DefaultConnectionProvider;
import org.jboss.tools.jmx.core.providers.DefaultConnectionWrapper;
import org.jboss.tools.jmx.core.providers.MBeanServerConnectionDescriptor;

public class JBoss70ConnectionProvider extends AbstractJBossJMXConnectionProvider{
	public static final String PROVIDER_ID = "org.jboss.ide.eclipse.as.core.extensions.jmx.JBoss70ConnectionProvider"; //$NON-NLS-1$
	
	public JBoss70ConnectionProvider() {
		super();
		JBossJMXConnectionProviderModel.getDefault().registerProvider(ServerExtendedProperties.JMX_DEFAULT_PROVIDER, this);
	}

	public String getName(IConnectionWrapper wrapper) {
		if( wrapper instanceof ExtendedDefaultConnectionWrapper) {
			MBeanServerConnectionDescriptor desc =
					((ExtendedDefaultConnectionWrapper)wrapper).getDescriptor();
				if( desc != null )
					return desc.getID();
		}
		return null;
	}

	protected boolean belongsHere(IServer server) {
		JBossExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getJBossExtendedProperties(server);
		int type = props == null ? -1 : props.getJMXProviderType();
		return type == JBossExtendedProperties.JMX_DEFAULT_PROVIDER;
	}
	
	protected IConnectionWrapper createConnection(IServer server) {
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
						Status s = null;
						if( started ) 
							s = (new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Unable to reach JBoss instance. Please ensure your server is up and exposes its management ports via the -Djboss.bind.address.management=yourwebsite.com system property."));
						else
							s = (new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Error disconnecting from this server's JMX service: " + event.getServer().getName(), ioe));
						JBossServerCorePlugin.log(s);
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
		
		public IConnectionProvider getProvider() {
			return ExtensionManager.getProvider(PROVIDER_ID);
		}
	}

	public String getId() {
		return PROVIDER_ID;
	}
}
