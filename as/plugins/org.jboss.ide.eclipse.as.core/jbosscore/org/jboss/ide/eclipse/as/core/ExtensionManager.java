/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "pollers");
		for( int i = 0; i < cf.length; i++ ) {
			pollers.put(cf[i].getAttribute("id"), new ServerStatePollerType(cf[i]));
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
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "pollerFailureHandler");
		for( int i = 0; i < cf.length; i++ ) {
			try {
				pollerFailureHandlers.put(cf[i].getAttribute("id"), 
						(IPollerFailureHandler)cf[i].createExecutableExtension("class"));
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
	
	public IPollerFailureHandler getFirstPollFailureHandler(IServerStatePoller poller, String action, List requiredProperties) {
		IPollerFailureHandler[] handlers = getPollerFailureHandlers();
		for( int i = 0; i < handlers.length; i++ ) {
			if( handlers[i].accepts(poller, action, requiredProperties)) {
				return handlers[i];
			}
		}
		return null;
	}
	
	
	public IJBossServerPublisher getPublisher(IServer server, IModule[] module) {
		if( publishers == null ) 
			loadPublishers();
		Iterator i = publishers.iterator();
		PublisherWrapper wrapper;
		while(i.hasNext()) {
			wrapper = (PublisherWrapper)i.next();
			if( wrapper.publisher.accepts(server, module))
				return wrapper.publisher;
		}
		return null;
	}
	
	public IJBossServerPublisher getZippedPublisher() {
		if( publishers == null ) 
			loadPublishers();
		Iterator<PublisherWrapper> i = publishers.iterator();
		PublisherWrapper wrapper;
		while(i.hasNext()) {
			wrapper = i.next();
			if( wrapper.isZipDelegate )
				return wrapper.publisher;
		}
		return null;
	}
	
	private void loadPublishers() {
		ArrayList<PublisherWrapper> publishers = new ArrayList<PublisherWrapper>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "publishers");
		for( int i = 0; i < cf.length; i++ ) {
			try {
				Object clazz = cf[i].createExecutableExtension("class");
				String priority = cf[i].getAttribute("priority");
				String zipDelegate = cf[i].getAttribute("zipDelegate");
				int p = -1; 
				try {
					p = Integer.parseInt(priority);
				} catch( NumberFormatException nfe) {}
				publishers.add(new PublisherWrapper(p, zipDelegate, (IJBossServerPublisher)clazz));
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
		private PublisherWrapper(int priority, String zipDelegate, IJBossServerPublisher publisher) {
			this.priority = priority;
			this.publisher = publisher;
			isZipDelegate = Boolean.parseBoolean(zipDelegate);
		}
	}
}
