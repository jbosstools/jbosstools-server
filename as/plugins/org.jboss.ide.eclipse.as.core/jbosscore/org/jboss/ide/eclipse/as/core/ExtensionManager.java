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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.IPollerFailureHandler;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
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
	private HashMap<String, IPollerFailureHandler> pollerFailureHandlers;

	private ArrayList<PublisherWrapper> publishers;
	
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
	public ServerStatePollerType getPollerType(String id) {
		if( pollers == null ) 
			loadPollers();
		return pollers.get(id);
	}
	
	/** Get only the pollers that can poll for startups */
	public ServerStatePollerType[] getStartupPollers() {
		if( pollers == null ) 
			loadPollers();
		ArrayList<ServerStatePollerType> list = new ArrayList<ServerStatePollerType>();
		Iterator<ServerStatePollerType> i = pollers.values().iterator();
		ServerStatePollerType type;
		while(i.hasNext()) {
			type = i.next();
			if( type.supportsStartup())
				list.add(type);
		}
		return list.toArray(new ServerStatePollerType[list.size()]);
	}
	
	/** Get only the pollers that can poll for shutdowns */
	public ServerStatePollerType[] getShutdownPollers() {
		if( pollers == null ) 
			loadPollers();
		ArrayList<ServerStatePollerType> list = new ArrayList<ServerStatePollerType>();
		Iterator<ServerStatePollerType> i = pollers.values().iterator();
		ServerStatePollerType type;
		while(i.hasNext()) {
			type = i.next();
			if( type.supportsShutdown() )
				list.add(type);
		}
		return list.toArray(new ServerStatePollerType[list.size()]);
	}
	

	/** The method used to load / instantiate the failure handlers */
	public void loadFailureHandler() {
		pollerFailureHandlers = new HashMap<String, IPollerFailureHandler>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "pollerFailureHandler"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			try {
				pollerFailureHandlers.put(cf[i].getAttribute("id"),  //$NON-NLS-1$
						(IPollerFailureHandler)cf[i].createExecutableExtension("class")); //$NON-NLS-1$
			} catch( CoreException e ) {
				// TODO ERROR LOG
			} catch( ClassCastException cce ) {
				// TODO ERROR LOG
			}
		}
	}

	public IPollerFailureHandler[] getPollerFailureHandlers() {
		if( pollerFailureHandlers == null ) 
			loadFailureHandler();
		Collection<IPollerFailureHandler> c = pollerFailureHandlers.values();
		return c.toArray(new IPollerFailureHandler[c.size()]);
	}
	
	public IPollerFailureHandler getFirstPollFailureHandler(IServerStatePoller poller, String action, List<String> requiredProperties) {
		IPollerFailureHandler[] handlers = getPollerFailureHandlers();
		for( int i = 0; i < handlers.length; i++ ) {
			if( handlers[i].accepts(poller, action, requiredProperties)) {
				return handlers[i];
			}
		}
		return null;
	}
	
	
	public IJBossServerPublisher getPublisher(IServer server, IModule[] module, String deployMethod) {
		if( publishers == null ) 
			loadPublishers();
		Iterator<PublisherWrapper> i = publishers.iterator();
		PublisherWrapper wrapper;
		while(i.hasNext()) {
			wrapper = i.next();
			if( wrapper.publisher.accepts(deployMethod, server, module))
				return wrapper.getNewInstance();
		}
		return null;
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
				} catch( NumberFormatException nfe) {}
				publishers.add(new PublisherWrapper(p, zipDelegate, (IJBossServerPublisher)clazz, cf[i]));
			} catch( CoreException e ) {
			} catch( ClassCastException cce ) {
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
			}
			return publisher;
		}
	}
}
