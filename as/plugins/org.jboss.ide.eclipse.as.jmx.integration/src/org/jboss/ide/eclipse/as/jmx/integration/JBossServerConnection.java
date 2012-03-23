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
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.jmx.integration.JMXUtil.CredentialException;
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
		((AbstractJBossJMXConnectionProvider)getProvider()).addListener(this);
		server.addServerListener(this);
	}
	
	public void connect() throws IOException {
		// Not supported
	}

	public void disconnect() throws IOException {
		// Not supported
	}

	public IConnectionProvider getProvider() {
		return ExtensionManager.getProvider(JBoss3To6ConnectionProvider.PROVIDER_ID);
	}
	
	protected AbstractJBossJMXConnectionProvider getProvider2() {
		return (AbstractJBossJMXConnectionProvider)getProvider();
	}

	public Root getRoot() {
		return root;
	}
	
	public void loadRoot(IProgressMonitor monitor) {
		if( isConnected() && !isLoading) {
			isLoading = true;
			// saferunner just adds itself as a concern and then removes, after each call.
			// This will ensure the classloader does not need to make multiple loads
			getProvider2().getClassloaderRepository().addConcerned(server, this);
			try {
				if( root == null ) {
					root = NodeUtils.createObjectNameTree(this, monitor);
				}
			} catch( CoreException ce ) {
				IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, ce.getMessage(), ce);
				JBossServerCorePlugin.getDefault().getLog().log(status);
				root = new ErrorRoot();
			} finally {
				getProvider2().getClassloaderRepository().removeConcerned(server, this);
				isLoading = false;
			}
		}
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void run(IJMXRunnable runnable) throws JMXException {
		run(runnable, new HashMap<String, String>());
	}
	
	protected void run(IJMXRunnable runnable, boolean force) throws JMXException {
		HashMap<String, String> map = new HashMap<String,String>();
		map.put("force", "true");
		run(runnable, map);
	}
	
	// Potential api upstream in jmx ?
	public void run(IJMXRunnable runnable, HashMap<String, String> prefs) throws JMXException {
		boolean force = prefs.get("force") == null ? false : Boolean.parseBoolean(prefs.get("force"));
		if( force || server.getServerState() == IServer.STATE_STARTED) {
			String defaultUser = ServerConverter.getJBossServer(server).getUsername();
			String defaultPass = ServerConverter.getJBossServer(server).getPassword();
			String user = prefs.get("user") == null ? defaultUser : prefs.get("user");
			String pass = prefs.get("pass") == null ? defaultPass : prefs.get("pass");
			run(server, runnable, user, pass);
		}
	}
	
	public void run(IServer s, IJMXRunnable r, String user, String pass) throws JMXException {
		// Mark the event
		getProvider2().getClassloaderRepository().addConcerned(s, r);
		
		// Set the classloader
		ClassLoader currentLoader = Thread.currentThread()
				.getContextClassLoader();
		ClassLoader newLoader = getProvider2().getClassloaderRepository().getClassLoader(s);
		Thread.currentThread().setContextClassLoader(newLoader);
		try {
			initializeEnvironment(s, user, pass);
			MBeanServerConnection connection = createConnection(s);
			if( connection != null ) {
				r.run(connection);
			}
		} catch( Exception e ) {  
			throw new JMXException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e));
		} finally {
			getProvider2().getClassloaderRepository().removeConcerned(s, r);
			Thread.currentThread().setContextClassLoader(currentLoader);
		}
	}

	protected MBeanServerConnection createConnection(IServer s) throws Exception {
		Properties p = JMXUtil.getDefaultProperties(s);
		InitialContext ic = new InitialContext(p);
		Object obj = ic.lookup(IJBossRuntimeConstants.RMIAdaptor);
		ic.close();
		if (obj instanceof MBeanServerConnection) {
			return (MBeanServerConnection)obj;
		}
		return null;
	}
	
	protected void initializeEnvironment(IServer s, String user, String pass) throws CredentialException {
		JMXUtil.setCredentials(s,user,pass);
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
				new Job("Connecting to " + event.getServer().getName() + " via JMX") { //$NON-NLS-1$ //$NON-NLS-2$
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						checkState();
						return Status.OK_STATUS;
					} 
				}.schedule();
			}
		}
	}
	
	protected void checkState() {
		IDeployableServer jbs = ServerConverter.getDeployableServer(server);
		if( server.getServerState() == IServer.STATE_STARTED && jbs != null && jbs.hasJMXProvider()) {
			try {
				run(new IJMXRunnable() {
					public void run(MBeanServerConnection connection)
							throws Exception {
						// Do nothing, just see if the connection worked
					} 
				}, true);
				if( !isConnected ) {
					isConnected = true;
					((AbstractJBossJMXConnectionProvider)getProvider()).fireChanged(JBossServerConnection.this);
				}
			} catch( Exception jmxe ) {
				// I thought i was connected but I'm not. 
				if( isConnected ) {
					isConnected = false;
					((AbstractJBossJMXConnectionProvider)getProvider()).fireChanged(JBossServerConnection.this);
				}
			}
		} else {
			root = null;
			if( isConnected ) {
				// server is not in STATE_STARTED, but thinks its connected
				isConnected = false;
				((AbstractJBossJMXConnectionProvider)getProvider()).fireChanged(JBossServerConnection.this);
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
		if( connection == this )
			server.removeServerListener(this);
	}

	public boolean canControl() {
		return false;
	}
}