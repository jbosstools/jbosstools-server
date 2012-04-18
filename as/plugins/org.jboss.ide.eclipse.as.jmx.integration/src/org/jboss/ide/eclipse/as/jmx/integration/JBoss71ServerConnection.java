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

import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.server.IManagementPortProvider;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.jmx.integration.JMXUtil.CredentialException;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;

public class JBoss71ServerConnection extends JBossServerConnection {
	public JBoss71ServerConnection(IServer server) {
		super(server);
	}

	public IConnectionProvider getProvider() {
		return ExtensionManager.getProvider(JBoss71ConnectionProvider.PROVIDER_ID);
	}

	private String user;
	private String pass;
	protected void initializeEnvironment(IServer s, String user, String pass) throws CredentialException {
		this.user = user;
		this.pass = pass;
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
			return connection;
		} catch(IOException ioe) {
			if( connector != null ) {
				try {
					connector.close();
				} catch(Exception e) { /* Ignore */ }
			}
			return null;
		}
	}
}
