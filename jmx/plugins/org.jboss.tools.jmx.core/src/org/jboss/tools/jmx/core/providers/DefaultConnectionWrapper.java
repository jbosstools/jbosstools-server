/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.core.providers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXActivator;
import org.jboss.tools.jmx.core.JMXCoreMessages;
import org.jboss.tools.jmx.core.JMXException;
import org.jboss.tools.jmx.core.tree.NodeUtils;
import org.jboss.tools.jmx.core.tree.Root;

public class DefaultConnectionWrapper implements IConnectionWrapper {
	private JMXConnector connector;
	private MBeanServerConnection connection;
	private Root root;
	private boolean isLoading;
	private boolean isConnected;
	private Map<String, String[]> environment;

	private MBeanServerConnectionDescriptor descriptor;
	
	public DefaultConnectionWrapper(MBeanServerConnectionDescriptor descriptor) throws MalformedURLException {
		this.descriptor = descriptor;
		this.isConnected = false;
		this.isLoading = false;
        String username = descriptor.getUserName();
        environment = new HashMap<String, String[]>();
        if (username != null && username.length() > 0) {
            String[] credentials = new String[] { username, descriptor.getPassword() };
            environment.put(JMXConnector.CREDENTIALS, credentials);
        }
	}

	public MBeanServerConnectionDescriptor getDescriptor() {
		return descriptor;
	}

	public IConnectionProvider getProvider() {
		return ExtensionManager.getProvider(DefaultConnectionProvider.PROVIDER_ID);
	}

	public MBeanServerConnection getConnection() {
		return connection;
	}

	public boolean canControl() {
		return true;
	}

	public synchronized void connect() throws IOException {
		// try to connect
        connector = JMXConnectorFactory.connect(new JMXServiceURL(descriptor.getURL()), environment);
        connection = connector.getMBeanServerConnection();
		isConnected = true;
		((DefaultConnectionProvider)getProvider()).fireChanged(this);
	}
	
	public synchronized void disconnect() throws IOException {
		// close
		root = null;
		isConnected = false;
		try {
			connector.close();
		} finally {
			((DefaultConnectionProvider)getProvider()).fireChanged(this);
		}
        connector = null;
        connection = null;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public Root getRoot() {
		return root;
	}

	public void loadRoot(IProgressMonitor monitor) throws CoreException {
		if( isConnected && root == null && !isLoading) {
			try {
				isLoading = true;
				root = NodeUtils.createObjectNameTree(this, monitor);
			} finally {
				isLoading = false;
			}
		}
	}
	
	public void run(IJMXRunnable runnable) throws JMXException {
		try {
			runnable.run(connection);
		} catch( Exception e ) {
			IStatus s = new Status(IStatus.ERROR, JMXActivator.PLUGIN_ID, JMXCoreMessages.DefaultConnection_ErrorRunningJMXCode, e);
			throw new JMXException(s);
		}
	}

	public void run(IJMXRunnable runnable, HashMap<String, String> prefs)
			throws JMXException {
		run(runnable);
	}
}
