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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7Server;

public class AS7ManagementDetails {
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
		return new String[0];
	}
}
