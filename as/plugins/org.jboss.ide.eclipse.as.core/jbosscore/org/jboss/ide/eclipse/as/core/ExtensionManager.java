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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.management.MBeanServerConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.IProvideCredentials;
import org.jboss.ide.eclipse.as.core.server.IServerAlreadyStartedHandler;
import org.jboss.ide.eclipse.as.core.server.IServerProvider;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
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
			pollers.put(cf[i].getAttribute("id"), new ServerStatePollerType(cf[i])); //$NON-NLS-1$
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
	
	/** Get only the pollers that can poll for startups */
	public IServerStatePollerType[] getStartupPollers(IServerType serverType) {
		if( pollers == null ) 
			loadPollers();
		ArrayList<ServerStatePollerType> list = new ArrayList<ServerStatePollerType>();
		Iterator<ServerStatePollerType> i = pollers.values().iterator();
		ServerStatePollerType type;
		while(i.hasNext()) {
			type = i.next();
			if( type.supportsStartup() && pollerSupportsServerType(type, serverType))
				list.add(type);
		}
		return list.toArray(new ServerStatePollerType[list.size()]);
	}
	
	/** Get only the pollers that can poll for shutdowns */
	public IServerStatePollerType[] getShutdownPollers(IServerType serverType) {
		if( pollers == null ) 
			loadPollers();
		ArrayList<ServerStatePollerType> list = new ArrayList<ServerStatePollerType>();
		Iterator<ServerStatePollerType> i = pollers.values().iterator();
		ServerStatePollerType type;
		while(i.hasNext()) {
			type = i.next();
			if( type.supportsShutdown()  && pollerSupportsServerType(type, serverType))
				list.add(type);
		}
		return list.toArray(new ServerStatePollerType[list.size()]);
	}
	

	protected boolean pollerSupportsServerType(IServerStatePollerType type, IServerType serverType) {
		String sTypes = type.getServerTypes();
		if(sTypes == null || sTypes.equals("")) //$NON-NLS-1$
			return true;
		String[] allTypes = sTypes.split(","); //$NON-NLS-1$
		for( int i = 0; i < allTypes.length; i++ ) {
			if( allTypes[i].trim().equals(serverType.getId())) {
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
	
	private ArrayList<PublisherWrapper> publishers;	
	public IJBossServerPublisher getPublisher(IServer server, IModule[] module, String deployMethod) {
		if( publishers == null ) 
			loadPublishers();
		Iterator<PublisherWrapper> i = publishers.iterator();
		PublisherWrapper wrapper;
		while(i.hasNext()) {
			wrapper = i.next();
			IJBossServerPublisher publisher = wrapper.publisher;
			if( publisher.accepts(deployMethod, server, module))
				return wrapper.getNewInstance();
		}
		return null;
	}

	private void loadPublishers() {
		ArrayList<PublisherWrapper> publishers = new ArrayList<PublisherWrapper>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "publishers"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			try {
				Object clazz = cf[i].createExecutableExtension("class"); //$NON-NLS-1$
				String priority = cf[i].getAttribute("priority"); //$NON-NLS-1$
				String zipDelegate = cf[i].getAttribute("zipDelegate"); //$NON-NLS-1$
				int p = -1; 
				try {
					p = Integer.parseInt(priority);
				} catch( NumberFormatException nfe) {
					// Should never ever happen since these are our extensions
					JBossServerCorePlugin.log(new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, 
							"Publisher id " + cf[i].getAttribute("class") + " has non-integer priority: " + priority));   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				}
				publishers.add(new PublisherWrapper(p, zipDelegate, (IJBossServerPublisher)clazz, cf[i]));
			} catch( CoreException e ) {
				IStatus status = new MultiStatus(
						JBossServerCorePlugin.PLUGIN_ID, IStatus.ERROR,
						new IStatus[] { e.getStatus() },
						Messages.ExtensionManager_could_not_load_publishers, e);
				JBossServerCorePlugin.getDefault().getLog().log(status);
			}
		}
		this.publishers = publishers;
		Comparator<PublisherWrapper> comparator = new Comparator<PublisherWrapper>() {
			public int compare(PublisherWrapper o1, PublisherWrapper o2) {
				return o2.priority - o1.priority;
			} 
		};
		Collections.sort(this.publishers, comparator);
	}
	
	private class PublisherWrapper {
		private int priority;
		private IJBossServerPublisher publisher;
		private boolean isZipDelegate = false;
		private IConfigurationElement element;
		private PublisherWrapper(int priority, String zipDelegate, IJBossServerPublisher publisher, IConfigurationElement element) {
			this.priority = priority;
			this.publisher = publisher;
			isZipDelegate = Boolean.parseBoolean(zipDelegate);
			this.element = element;
		}
		private IJBossServerPublisher getNewInstance() {
			try {
				Object clazz = element.createExecutableExtension("class"); //$NON-NLS-1$
				return (IJBossServerPublisher)clazz;
			} catch( CoreException ce ) {
				JBossServerCorePlugin.log(ce.getStatus());
			}
			return publisher;
		}
		public String toString() {
			return element.getAttribute("class"); //$NON-NLS-1$
		}
	}
	
	public IJBossServerPublisher[] getZippedPublishers() {
		if( publishers == null ) 
			loadPublishers();
		ArrayList<IJBossServerPublisher> list = new ArrayList<IJBossServerPublisher>();
		Iterator<PublisherWrapper> i = publishers.iterator();
		PublisherWrapper wrapper;
		while(i.hasNext()) {
			wrapper = i.next();
			if( wrapper.isZipDelegate )
				list.add( wrapper.getNewInstance() );
		}
		return list.toArray(new IJBossServerPublisher[list.size()]);
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
	
	
	// TODO Replace with extension point or cleaner API
	// Should have an array of possible handlers
	// Should ask each handler if they 'accept' this handler, etc
	private IServerAlreadyStartedHandler defaultAlreadyStartedHandler;
	public IServerAlreadyStartedHandler getAlreadyStartedHandler(IServer server) {
		return defaultAlreadyStartedHandler;
	}
	public void setAlreadyStartedHandler(IServerAlreadyStartedHandler handler) {
		defaultAlreadyStartedHandler = handler;
	}
}
