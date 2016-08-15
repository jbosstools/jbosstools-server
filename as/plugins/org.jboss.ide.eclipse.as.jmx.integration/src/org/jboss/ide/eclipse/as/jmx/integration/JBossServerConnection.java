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
import java.net.NoRouteToHostException;
import java.util.HashMap;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.naming.InitialContext;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.jmx.integration.JMXUtil.CredentialException;
import org.jboss.ide.eclipse.as.jmx.integration.jvmmonitor.JBossActiveJvm;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionProviderListener;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;
import org.jboss.tools.jmx.core.tree.ErrorRoot;
import org.jboss.tools.jmx.core.tree.NodeUtils;
import org.jboss.tools.jmx.core.tree.Root;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.jvmmonitor.core.IJvmFacade;
import org.jboss.tools.jmx.jvmmonitor.core.JvmCoreException;

public class JBossServerConnection implements IConnectionWrapper, IServerListener, 
			IConnectionProviderListener, IJvmFacade, IAdaptable {
	private IServer server;
	private Root root;
	private boolean isConnected;
	private boolean isLoading;
	
	private MBeanServerConnection activeConnection;
	private JBossActiveJvm customJvm;
	
	public JBossServerConnection(IServer server) {
		this.server = server;
		this.isConnected = false;
		this.isLoading = false;
		connectViaJmxIfRequired(server); // prime the state
		((AbstractJBossJMXConnectionProvider)getProvider()).addListener(this);
		server.addServerListener(this);
	}
	
	public void connect() throws IOException {
		// re-connect
		connectToStartedServer();
	}

	public void disconnect() throws IOException {
		// close
		if( activeConnection != null ) {
			cleanupConnection(server, activeConnection);
		}
		if( customJvm != null ) {
			customJvm.disconnect();
			customJvm = null;
		}
		activeConnection = null;
		root = null;
		isConnected = false;
		((AbstractJBossJMXConnectionProvider)getProvider()).fireChanged(JBossServerConnection.this);
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
	
	// Potential api upstream in jmx ?
	public void run(IJMXRunnable runnable, HashMap<String, String> prefs) throws JMXException {
		run(runnable, prefs, false);
	}
	
	public void run(IJMXRunnable runnable, HashMap<String, String> prefs, boolean saveActiveConnection) throws JMXException {
		boolean force = prefs.get("force") == null ? false : Boolean.parseBoolean(prefs.get("force"));
		if( force || server.getServerState() == IServer.STATE_STARTED) {
			IJBossServer jbs = ServerConverter.getJBossServer(server);
			String storedOrDefaultUser = jbs.getUsername();
			String storedOrDefaultPass = jbs.getPassword();
			String rawStoredUser = ((JBossServer)jbs).getRawUsername();
			String rawStoredPassword = ((JBossServer)jbs).getRawPassword();
			
			// If these are passed in directly, then use them
			String user = prefs.get("user");
			String pass = prefs.get("pass");
			
			if( user == null || pass == null ) {
				// Nothing passed in directly, so lets use the stored raw values
				user = rawStoredUser;
				pass = rawStoredPassword;
			}
			if( shouldUseDefaultCredentials() && (user == null || pass == null)) {
				user = (user == null ? storedOrDefaultUser : user);
				pass = (pass == null ? storedOrDefaultPass : pass);
			}
			run(server, runnable, user, pass, saveActiveConnection);
		}
	}
	
	
	/**
	 * Some workspaces some previous versions may include server adapters with
	 * null values for username and password. In some of these situations, providing 
	 * default credentials will allow authorization to jmx operations. 
	 * 
	 * Subclasses for server versions that should not use default credentials in the event 
	 * of no credentials being passed in should override this method and return false. 
	 * 
	 * @return boolean whether to use default credentials as provided by the server implementation
	 */
	protected boolean shouldUseDefaultCredentials() {
		return true;
	}
	
	protected void run(IServer s, IJMXRunnable r, String user, String pass) throws JMXException {
		run(s,r,user,pass,false);
	}
	
	protected void run(IServer s, IJMXRunnable r, String user, String pass, boolean saveActiveConnection) throws JMXException {

		// Mark the event
		getProvider2().getClassloaderRepository().addConcerned(s, r);
		
		// Set the classloader
		ClassLoader currentLoader = Thread.currentThread()
				.getContextClassLoader();
		ClassLoader newLoader = getProvider2().getClassloaderRepository().getClassLoader(s);
		
		if( newLoader == null ) {
			getProvider2().getClassloaderRepository().removeConcerned(s, r);
			throw new JMXException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					"No JMX Client jars were found to communicate with server " + s.getName() + ". Assigning a local runtime to your server will allow client jars to be found."));

		}
		
		
		Thread.currentThread().setContextClassLoader(newLoader);
		MBeanServerConnection connection = null;
		try {
			// IF we have a primary active connection, use it instead of a new one 
			if( activeConnection != null ) {
				connection = activeConnection;
			} else {
				initializeEnvironment(s, user, pass);
				connection = createConnection(s);
			}
			if( connection != null ) {
				r.run(connection);
			}
			// save this for the next use
			if( saveActiveConnection && activeConnection == null && connection != null ) {
				activeConnection = connection;
			}
		} catch(JMXException jmxe) {
			// rethrow
			throw jmxe;
		} catch( Exception e ) {
			if( e.getCause() != null && e.getCause() instanceof NoRouteToHostException) {
				throw new JMXException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
						"Error connecting to remote JMX. Please ensure your server is properly configured for JMX access. A firewall may be blocking the request. You may wish to review your application server's security guide for information on ports used.", e));
			}
			// wrap all others
			throw new JMXException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					"Error connecting to remote JMX. Please ensure your server is properly configured for JMX access.", e));
		} finally {
			// Only close it if it's not our primary active connection
			if( connection != activeConnection ) 
				cleanupConnection(s, connection);
			getProvider2().getClassloaderRepository().removeConcerned(s, r);
			Thread.currentThread().setContextClassLoader(currentLoader);
		}
	}

	protected MBeanServerConnection createConnection(IServer s) throws Exception {
		Properties p = JMXUtil.getDefaultProperties(s);
		InitialContext ic = new InitialContext(p);
		Object obj = null;
		try {
			obj = ic.lookup(IJBossRuntimeConstants.RMIAdaptor);
		} finally {
			ic.close();
		}
		if (obj != null && obj instanceof MBeanServerConnection) {
			((MBeanServerConnection)obj).getDomains();
			return (MBeanServerConnection)obj;
		}
		return null;
	}
	
	protected void cleanupConnection(IServer server, MBeanServerConnection connection) {
		// Do nothing, provide subclasses ability 
	}
	
	protected void initializeEnvironment(IServer s, String user, String pass) throws CredentialException {
		JMXUtil.setCredentials(s,user,pass);
	}
	
	public String getName() {
		return server.getName();
	}

	
	/* **************
	 *  If there's a change in the server state, then set my connection
	 *  state properly.   If there's been a change then fire to the listeners
	 */

	public void serverChanged(ServerEvent event) {
		int eventKind = event.getKind();
		if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
			// server change event
			if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
				// server state has changed. If it's changed to started, let's connect to jmx
				connectViaJmxIfRequired(event.getServer());
			}
		}
	}
	
	protected void connectViaJmxIfRequired(IServer server) {
		if( shouldConnect(server)) {
			// DO NOT automatically connect. Let the user do it themselves. 
			//launchConnectionJob(server);
		} else {
			try {
				disconnect();
			} catch(IOException ioe) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
						"Unable to cleanly disconnect jmx connection", ioe));
			}
		}
	}
	
	/**
	 * Should we connect to this server now? 
	 * 
	 * @param server
	 * @return
	 */
	protected boolean shouldConnect(IServer server) {
		IDeployableServer jbs = ServerConverter.getDeployableServer(server);
		boolean supportsJMX = jbs != null && jbs.hasJMXProvider();
		boolean started = server.getServerState() == IServer.STATE_STARTED;
		
		// We require a runtime to launch jmx, because we load those client jars onto the classpath. 
		boolean hasRuntime = server.getRuntime() != null;
		if( started && supportsJMX && hasRuntime ) {
			return true;
		}
		return false;
	}
	
	protected void launchConnectionJob(final IServer server) {
		new Job("Connecting to " + server.getName() + " via JMX") { //$NON-NLS-1$ //$NON-NLS-2$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if( server.getServerState() == IServer.STATE_STARTED)
					// Since this job runs late, it's possible server already stopped by user
					connectToStartedServer();
				return Status.OK_STATUS;
			} 
		}.schedule(5000);
	}

	protected void connectToStartedServer() {
		try {
			IJMXRunnable run = new IJMXRunnable() {
				public void run(MBeanServerConnection connection)
						throws Exception {
					// Do nothing, just see if the connection worked
				} 
			};
			
			HashMap<String, String> map = new HashMap<String,String>();
			map.put("force", new Boolean(true).toString());
			run(run, map, true); // save this nw connection as the active one

			
			if( !isConnected ) {
				isConnected = true;
				((AbstractJBossJMXConnectionProvider)getProvider()).fireChanged(JBossServerConnection.this);
			}
		} catch( Exception jmxe ) {
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error connecting to jmx for server "+server.getName(), jmxe);
			ServerLogger.getDefault().log(server, status);	
			// I thought i was connected but I'm not. 
			if( isConnected ) {
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
		return server.getServerState() == IServer.STATE_STARTED && server.getRuntime() != null;
	}

	@Override
	public IActiveJvm getActiveJvm() {
		if( server.getServerState() == IServer.STATE_STARTED && isConnected ) {
			if( customJvm == null ) {
				IActiveJvm active = JBossJVMFacadeUtility.findJvmForServer(server);
				try {
					if( active != null ) {
						customJvm = new JBossActiveJvm(this, active);
					} else {
						throw new JvmCoreException(IStatus.ERROR, "Cannot find matching local process for server " + server.getName(), null);
					}
				} catch(JvmCoreException jvmce) {
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, jvmce.getMessage(), jvmce));
				}
			}
			return customJvm;
		}
		return null;
	}
	

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		ITabbedPropertySheetPageContributor contributor = new ITabbedPropertySheetPageContributor() {
			public String getContributorId() {
				return "org.jboss.tools.jmx.jvmmonitor.ui.JvmExplorer";
			}
		};
		if (adapter == IPropertySheetPage.class) {
			return new TabbedPropertySheetPage(contributor);
		} else if (adapter == ITabbedPropertySheetPageContributor.class) {
			return contributor;
		}
		return null;
	}
	
	public MBeanServerConnection getActiveConnection() {
		// get an active connection if it exists
		return activeConnection;
	}
	
}