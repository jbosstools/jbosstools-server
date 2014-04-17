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
package org.jboss.ide.eclipse.as.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.management.MBeanServerConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.jboss.ide.eclipse.as.core.server.IProvideCredentials;
import org.jboss.ide.eclipse.as.core.server.IServerProvider;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;

/**
 * Manages the extensions for this plugin
 * @author rob.stryker@jboss.com
 */
public class ExtensionManager {
	
	/** Singleton instance of the manager */
	private static ExtensionManager instance;
	
	/** Singleton getter */
	public static ExtensionManager getDefault() {
		if( instance == null ) 
			instance = new ExtensionManager();
		return instance;
	}
	
	/** The map of pollerID -> PollerObject */
	private HashMap<String, ServerStatePollerType> pollers;
	
	/** The map of pollerID -> PollerObject */
	private HashMap<String, IProvideCredentials> credentialProviders;

	/** The method used to load / instantiate the pollers */
	public void loadPollers() {
		pollers = new HashMap<String, ServerStatePollerType>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "pollers"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			String id = cf[i].getAttribute("id"); //$NON-NLS-1$
			ServerStatePollerType sspt = new ServerStatePollerType(cf[i]);
			if( pollers.get(id) != null) {
				// This is an actual error, not just an annoyance
				IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
						"Two pollers with conflicting id's have been declared and will clobber each other. The existing poller will be kept.\n " //$NON-NLS-1$
						+ pollers.get(id).toString() + "\n" + sspt.toString()); //$NON-NLS-1$
				JBossServerCorePlugin.getDefault().getLog().log(s);
			} else {
				pollers.put(id, sspt); 
			}
		}
	}
	
	/**
	 * Get a poller with the specified ID
	 * @param id the id
	 * @return the poller
	 */
	public IServerStatePollerType getPollerType(String id) {
		if( pollers == null ) 
			loadPollers();
		return pollers.get(id);
	}
	
	/**
	 * Get only the pollers that can poll for startups for this server type and mode
	 * @param serverType the server type to be polled
	 * @param mode The mode this server is in (local, rse, etc)
	 * @return
	 */
	public IServerStatePollerType[] getStartupPollers(IServerType serverType, String mode) {
		if( pollers == null ) 
			loadPollers();
		ArrayList<ServerStatePollerType> list = new ArrayList<ServerStatePollerType>();
		Iterator<ServerStatePollerType> i = pollers.values().iterator();
		ServerStatePollerType type;
		while(i.hasNext()) {
			type = i.next();
			if( type.supportsStartup() && pollerSupportsServerType(type, serverType)) {
				if( mode == null || pollerSupportsServerMode(type, mode))
					list.add(type);
			}
		}
		return list.toArray(new ServerStatePollerType[list.size()]);
	}
	
	/**
	 * Get only the pollers that can poll for startups for this server type
	 * @param serverType the server type to be polled
	 * @return
	 */
	public IServerStatePollerType[] getStartupPollers(IServerType serverType) {
		return getShutdownPollers(serverType, null);
	}
	
	/**
	 * Get only the pollers that can poll for shutdowns for this server type and mode
	 * @param serverType the server type to be polled
	 * @param mode The mode this server is in (local, rse, etc)
	 * @return
	 */
	public IServerStatePollerType[] getShutdownPollers(IServerType serverType, String mode) {
		if( pollers == null ) 
			loadPollers();
		ArrayList<ServerStatePollerType> list = new ArrayList<ServerStatePollerType>();
		Iterator<ServerStatePollerType> i = pollers.values().iterator();
		ServerStatePollerType type;
		while(i.hasNext()) {
			type = i.next();
			if( type.supportsShutdown()  && pollerSupportsServerType(type, serverType)) {
				if( mode == null || pollerSupportsServerMode(type, mode))
					list.add(type);
			}
		}
		return list.toArray(new ServerStatePollerType[list.size()]);
	}
	
	/**
	 * Get only the pollers that can poll for shutdowns for this server type
	 * @param serverType the server type to be polled
	 * @return
	 */
	public IServerStatePollerType[] getShutdownPollers(IServerType serverType) {
		return getShutdownPollers(serverType, null);
	}
	

	protected boolean pollerSupportsServerType(IServerStatePollerType type, IServerType serverType) {
		return commaSeparatedContains(type.getServerTypes(), serverType.getId());
	}

	protected boolean pollerSupportsServerMode(IServerStatePollerType type, String mode) {
		return commaSeparatedContains(type.getServerModes(), mode);
	}
	
	private boolean commaSeparatedContains(String cs, String val) {
		if(cs == null || cs.equals("")) //$NON-NLS-1$
			return true;
		String[] arr = cs.split(","); //$NON-NLS-1$
		for( int i = 0; i < arr.length; i++ ) {
			if( arr[i].trim().equals(val)) {
				return true;
			}
		}
		return false;
	}

	/** The method used to load / instantiate the failure handlers */
	public void loadCredentialProviders() {
		credentialProviders = new HashMap<String, IProvideCredentials>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "pollerFailureHandler"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			try {
				credentialProviders.put(cf[i].getAttribute("id"),  //$NON-NLS-1$
						(IProvideCredentials)cf[i].createExecutableExtension("class")); //$NON-NLS-1$
			} catch( CoreException e ) {
				JBossServerCorePlugin.log(e.getStatus());
			}
		}
	}

	public IProvideCredentials[] getCredentialProviders() {
		if( credentialProviders == null ) 
			loadCredentialProviders();
		Collection<IProvideCredentials> c = credentialProviders.values();
		return c.toArray(new IProvideCredentials[c.size()]);
	}
	
	public IProvideCredentials getFirstCredentialProvider(IServerProvider serverProvider, List<String> requiredProperties) {
		IProvideCredentials[] handlers = getCredentialProviders();
		for( int i = 0; i < handlers.length; i++ ) {
			if( handlers[i].accepts(serverProvider, requiredProperties)) {
				return handlers[i];
			}
		}
		return null;
	}
	
	
	// API extension
	public static interface IServerJMXRunnable {
		public void run(MBeanServerConnection connection) throws Exception;
	}
	
	public static interface IServerJMXRunner {
		public void run(IServer server, IServerJMXRunnable runnable) throws CoreException;
		public void beginTransaction(IServer server, Object lock);
		public void endTransaction(IServer server, Object lock);
	}
	
	private IServerJMXRunner jmxRunner = null;
	private Object JMX_RUNNER_NOT_FOUND = null;
	public IServerJMXRunner getJMXRunner() {
		if( jmxRunner != null )
			return this.jmxRunner;
		if( JMX_RUNNER_NOT_FOUND != null)
			return null;
		
		// find runner
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "jmxRunner"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			try {
				Object o = cf[i].createExecutableExtension("class"); //$NON-NLS-1$
				if( o != null && (o instanceof IServerJMXRunner))
					return ((IServerJMXRunner)o);
			} catch(CoreException e) {
				JBossServerCorePlugin.log(e.getStatus());
			}
		}
		JMX_RUNNER_NOT_FOUND = new Object();
		return null;
	}
}
