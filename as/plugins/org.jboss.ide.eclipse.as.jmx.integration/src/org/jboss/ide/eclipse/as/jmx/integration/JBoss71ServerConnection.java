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
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IManagementPortProvider;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.jmx.integration.JMXUtil.CredentialException;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.JMXException;

public class JBoss71ServerConnection extends JBossServerConnection {
	public JBoss71ServerConnection(IServer server) {
		super(server);
	}

	public IConnectionProvider getProvider() {
		return ExtensionManager.getProvider(JBoss71ConnectionProvider.PROVIDER_ID);
	}

	private String user;
	private String pass;
	private Map<MBeanServerConnection, JMXConnector> connectionToConnector;
	protected void initializeEnvironment(IServer s, String user, String pass) throws CredentialException {
		this.user = user;
		this.pass = pass;
		this.connectionToConnector = new HashMap<MBeanServerConnection, JMXConnector>();
	}
	
	protected MBeanServerConnection createConnection(IServer s) throws Exception  {
		ServerDelegate sd = (ServerDelegate)s.loadAdapter(ServerDelegate.class, null);
		int port = -1;
		if( !(sd instanceof IManagementPortProvider))
			port = IJBossToolingConstants.AS7_MANAGEMENT_PORT_DEFAULT_PORT;
		else {
			port = ((IManagementPortProvider)sd).getManagementPort();
		}
		
		String url = "service:jmx:remoting-jmx://" + s.getHost() + ":" + port; 
		Map<String, String[]> environment = new HashMap<String, String[]>();
        environment.put(JMXConnector.CREDENTIALS, new String[]{user,pass});
        
		JMXConnector connector = null;
		try {
			connector = JMXConnectorFactory.connect(new JMXServiceURL(url), environment);
			MBeanServerConnection connection = connector.getMBeanServerConnection();
			this.connectionToConnector.put(connection, connector);
			return connection;
		} catch(IOException ioe) {
			return null;
		} catch( RuntimeException re) {
			IStatus stat = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					"Runtime Exception contacting JBoss instance. Please ensure your server is up and exposes its management ports via the -Djboss.bind.address.management=yourwebsite.com system property", re);
			throw new JMXException(stat);
		}
	}
	
	protected void cleanupConnection(IServer server, MBeanServerConnection connection) {
		super.cleanupConnection(server, connection);
		if( connectionToConnector.get(connection) != null ) {
			try {
				connectionToConnector.remove(connection);
				connectionToConnector.get(connection).close();
			} catch(Exception e) { /* Ignore */ }
		}
	}

}
