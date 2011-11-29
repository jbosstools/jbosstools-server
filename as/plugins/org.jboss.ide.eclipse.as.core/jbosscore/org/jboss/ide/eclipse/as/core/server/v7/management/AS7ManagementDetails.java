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
package org.jboss.ide.eclipse.as.core.server.v7.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.server.INeedCredentials;
import org.jboss.ide.eclipse.as.core.server.IProvideCredentials;
import org.jboss.ide.eclipse.as.core.server.IServerProvider;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7Server;

public class AS7ManagementDetails implements IServerProvider {
	private IServer server;
	private String hardHost = null;
	private int hardPort = -1;
	public AS7ManagementDetails(IServer server) {
		this.server = server;
	}
	
	public AS7ManagementDetails(String host, int port) {
		this.hardHost = host;
		this.hardPort = port;
	}
	
	public String getHost() {
		return hardHost == null ? server.getHost() : hardHost;
	}
	
	public int getManagementPort() {
		if( hardPort != -1 )
			return hardPort;
		
		JBoss7Server jbossServer = (JBoss7Server) server.loadAdapter(JBoss7Server.class, new NullProgressMonitor());
		if( jbossServer != null )
			return jbossServer.getManagementPort();
		return IJBoss7ManagerService.MGMT_PORT;
	}
	
	public String getManagementUsername() {
		return null;
	}
	
	public String getManagementPassword() {
		return null;
	}
	
	public String[] handleCallbacks(String[] prompts) throws UnsupportedOperationException {
		ArrayList<String> requiredProperties = new ArrayList<String>();
		requiredProperties.addAll(Arrays.asList(prompts));
		IProvideCredentials handler = ExtensionManager
				.getDefault().getFirstCredentialProvider(this, requiredProperties);
		if( handler == null )
			throw new UnsupportedOperationException();
		final Properties[] returnedProps = new Properties[1];
		final Boolean[] gotProps = new Boolean[1];
		returnedProps[0] = null;
		gotProps[0] = false;
		
		INeedCredentials needs = new INeedCredentials() {
			public IServer getServer() {
				return AS7ManagementDetails.this.getServer();
			}
			public List<String> getRequiredProperties() {
				// ignore
				return null;
			}
			public void provideCredentials(Properties credentials) {
				returnedProps[0] = credentials;
				gotProps[0] = true;
			}
		};
		handler.handle(needs, requiredProperties);

		while( !gotProps[0]) {
			try {
				Thread.sleep(100);
			} catch(InterruptedException ie) {
				// ignore
			}
		}
			
		if( returnedProps[0] == null )
			return new String[0];
		
		String[] rets = new String[prompts.length];
		for( int i = 0; i < rets.length; i++ ) {
			rets[i] = (String)returnedProps[0].get(prompts[i]);
		}
		return rets;
	}
	public IServer getServer() {
		return server;
	}
}
