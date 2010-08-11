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

import java.io.IOException;

import javax.management.MBeanServerConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionProviderListener;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;
import org.jboss.tools.jmx.core.tree.ErrorRoot;
import org.jboss.tools.jmx.core.tree.NodeUtils;
import org.jboss.tools.jmx.core.tree.Root;

public class JBossServerConnection implements IConnectionWrapper, IServerListener, IConnectionProviderListener {
	private IServer server;
	private Root root;
	private boolean isConnected;
	private boolean isLoading;
	public JBossServerConnection(IServer server) {
		this.server = server;
		this.isConnected = false;
		this.isLoading = false;
		checkState(); // prime the state
		((JBossServerConnectionProvider)getProvider()).addListener(this);
		server.addServerListener(this);
	}
	
	public void connect() throws IOException {
		// Not supported
	}

	public void disconnect() throws IOException {
		// Not supported
	}

	public IConnectionProvider getProvider() {
		return ExtensionManager.getProvider(JBossServerConnectionProvider.PROVIDER_ID);
	}

	public Root getRoot() {
		return root;
	}
	
	public void loadRoot(IProgressMonitor monitor) {
		if( isConnected() && !isLoading) {
			isLoading = true;
			// saferunner just adds itself as a concern and then removes, after each call.
			// This will ensure the classloader does not need to make multiple loads
			JMXClassLoaderRepository.getDefault().addConcerned(server, this);
			try {
				if( root == null ) {
					root = NodeUtils.createObjectNameTree(this, monitor);
				}
			} catch( CoreException ce ) {
				IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, ce.getMessage(), ce);
				JBossServerCorePlugin.getDefault().getLog().log(status);
				root = new ErrorRoot();
			} finally {
				JMXClassLoaderRepository.getDefault().removeConcerned(server, this);
				isLoading = false;
			}
		}
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void run(IJMXRunnable runnable) throws JMXException {
		// do nothing if the server is down.
		if( server.getServerState() != IServer.STATE_STARTED ) 
			return;
		JMXSafeRunner.run(server, runnable);
	}
	
	public String getName() {
		return server.getName();
	}

	
	/* **************
	 *  If there's a change in teh server state, then set my connection
	 *  state properly.   If there's been a change then fire to teh listeners
	 */

	public void serverChanged(ServerEvent event) {
		int eventKind = event.getKind();
		if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
			// server change event
			if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
				checkState();
			}
		}
	}
	
	protected void checkState() {
		if( server.getServerState() == IServer.STATE_STARTED ) {
			try {
				JMXSafeRunner.run(server, new IJMXRunnable() {
					public void run(MBeanServerConnection connection)
							throws Exception {
						// Do nothing, just see if the connection worked
					} 
				});
				if( !isConnected ) {
					isConnected = true;
					((JBossServerConnectionProvider)getProvider()).fireChanged(JBossServerConnection.this);
				}
			} catch( Exception jmxe ) {
				// I thought i was connected but I'm not. 
				if( isConnected ) {
					isConnected = false;
					((JBossServerConnectionProvider)getProvider()).fireChanged(JBossServerConnection.this);
				}
			}
		} else {
			root = null;
			if( isConnected ) {
				// server is not in STATE_STARTED, but thinks its connected
				isConnected = false;
				((JBossServerConnectionProvider)getProvider()).fireChanged(JBossServerConnection.this);
			}
		}
	}

	
	
	/* *************
	 * The following three methods are just here so that this class
	 * is removed as a listener to the server if it is removed
	 */
	
	public void connectionAdded(IConnectionWrapper connection) {
		// ignore
	}

	public void connectionChanged(IConnectionWrapper connection) {
		// ignore
	}

	public void connectionRemoved(IConnectionWrapper connection) {
		server.removeServerListener(this);
	}

	public boolean canControl() {
		return false;
	}
}