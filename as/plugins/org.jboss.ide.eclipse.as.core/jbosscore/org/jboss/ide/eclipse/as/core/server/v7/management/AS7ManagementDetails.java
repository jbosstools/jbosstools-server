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
import java.util.Properties;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IServerProvider;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7Server;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;

public class AS7ManagementDetails implements IServerProvider, IAS7ManagementDetails {
	private IServer server;
	public AS7ManagementDetails(IServer server) {
		this.server = server;
	}
	
	public String getHost() {
		return server.getHost();
	}
	
	public int getManagementPort() {
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
		IJBossServer jbs = ServerConverter.getJBossServer(server);
		boolean emptyCreds = 
				(jbs.getUsername() == null || jbs.getUsername().equals("")) //$NON-NLS-1$
				&& (jbs.getPassword() == null || jbs.getPassword().equals("")); //$NON-NLS-1$
		if( !emptyCreds ) {
			return new String[]{jbs.getUsername(), jbs.getPassword()};
		}
		
		Properties props = PollThreadUtils.requestCredentialsSynchronous(this, requiredProperties);

		if( props == null )
			return null;
		
		// Fit the returned properties in the same order as the request prompts
		String[] rets = new String[prompts.length];
		for( int i = 0; i < rets.length; i++ ) {
			rets[i] = (String)props.get(prompts[i]);
		}
		return rets;
	}
	
	public IServer getServer() {
		return server;
	}
}
