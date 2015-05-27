/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.wtp.server.launchbar;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * In charge of adding remote connections based on the creation
 * or deletion of server adapters in the workspace. 
 */
public class RemoteConnectionHandler implements IServerLifecycleListener {
	
	/**
	 * The id of our remote connection type
	 */
	private static final String REMOTE_ID = "org.jboss.tools.wtp.server.launchbar.connection";
	
	public static final String SERVER_ID = "serverId";
	
	/**
	 * A reference to the remote services manager
	 */
	private IRemoteServicesManager  manager;
	
	
	
	public RemoteConnectionHandler() {
		manager = getRemoteServicesManager();
	}
	IRemoteServicesManager getRemoteServicesManager() {
		return getService(IRemoteServicesManager.class);
	}
	public static <T> T getService(Class<T> service) {
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}
	@Override
	public void serverAdded(IServer server) {
		IRemoteConnectionType type = manager.getConnectionType(REMOTE_ID);
		try {
			IRemoteConnectionWorkingCopy wc = type.newConnection(server.getName());
			wc.setAttribute(SERVER_ID, server.getId());
			IRemoteConnection con = wc.save();
		} catch (RemoteConnectionException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void serverChanged(IServer server) {
		
		// The change event could be a name change, which would screw us up a bit
		IRemoteConnectionType type = manager.getConnectionType(REMOTE_ID);
		List<IRemoteConnection> con = type.getConnections();
		IServer[] allServers = ServerCore.getServers();
		
		// remove connections with no matching server
		for( int i = 0; i < con.size(); i++ ) {
			IRemoteConnection rc = con.get(i);
			IServer match = findServerFor(rc, allServers);
			if(  match == null || match.equals(server)) {
				try {
					type.removeConnection(rc);
				} catch(RemoteConnectionException rce) {
					IStatus status = new Status(IStatus.INFO, Activator.PLUGIN_ID, rce.getMessage(), rce);
					Activator.getDefault().getLog().log(status);
				}
			}
		}
		
		// Add any connections for new servers
		con = type.getConnections();
		allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ ) {
			if( findConnectionFor(allServers[i], con) == null) {
				serverAdded(allServers[i]);
			}
		}
	}
	
	private IRemoteConnection findConnectionFor(IServer server, List<IRemoteConnection> con) {
		for( int i = 0; i < con.size(); i++ ) {
			if( con.get(i).getName().equals(server.getName())) {
				return con.get(i);
			}
		}
		return null;
	}
	
	private IServer findServerFor(IRemoteConnection rc, IServer[] all) {
		String id = rc.getAttribute(SERVER_ID);
		return ServerCore.findServer(id);
	}
	
	@Override
	public void serverRemoved(IServer server) {
		IRemoteConnectionType type = manager.getConnectionType(REMOTE_ID);
		IRemoteConnection con = type.getConnection(server.getName());
		if( con != null ) {
			try {
				type.removeConnection(con);
			} catch (RemoteConnectionException e) {
				IStatus status = new Status(IStatus.INFO, Activator.PLUGIN_ID, e.getMessage(), e);
				Activator.getDefault().getLog().log(status);
			}
		}
	}

}
