/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.module.JBossModuleFactory;
import org.jboss.ide.eclipse.as.core.server.JBossServer;

public class ModuleModel implements IResourceChangeListener{
	
	private static ModuleModel singleton;
	private static ArrayList factories;
	
	public static ModuleModel getDefault() {
		if( singleton == null ) {
			singleton = new ModuleModel();
		}
		return singleton;
	}
	
	public static ModuleFactory[] getJBossModuleFactories() {
		if( factories == null ) 
			loadAcceptableFactories();
		return (ModuleFactory[]) factories.toArray(new ModuleFactory[factories.size()]);
	}
	
	private static void loadAcceptableFactories() {
		
		Comparator factoryComparator = new Comparator() {
			public int compare(Object arg0, Object arg1) {
				if( arg0 instanceof ModuleFactory  && !(arg1 instanceof ModuleFactory))
					return 1;
				
				if( arg1 instanceof ModuleFactory  && !(arg0 instanceof ModuleFactory))
					return -1;
				
				if( !(arg0 instanceof ModuleFactory) && !(arg1 instanceof ModuleFactory)) 
					return 0;
				
				int p0 = ((ModuleFactory)arg0).getOrder();
				int p1 = ((ModuleFactory)arg1).getOrder();
				
				int retval = 0;
				if( p0 == p1 ) {
					retval = 0;
				} else if( p0 > p1 ) {
					retval = -1;
				} else if( p0 < p1 ) {
					retval = 1;
				}
				return retval;
			}
		};
		factories = new ArrayList();

		
		ModuleFactory[] mfs = ServerPlugin.getModuleFactories();
		String[] jbossIds = loadJBossFactoryIDs();
		
		for( int i = 0; i < mfs.length; i++ ) {
			for( int j = 0; j < jbossIds.length; j++ ) {
				if( jbossIds[j].equals(mfs[i].getId()) ) {
					factories.add(mfs[i]);
				}
			}
		}
		
		Collections.sort(factories, factoryComparator);

		Iterator i = factories.iterator();
		ModuleFactory f = null;
		ModuleFactoryDelegate delegate = null;

		while(i.hasNext()) {
			f = (ModuleFactory)i.next();
			delegate = f.getDelegate(null);
			if( delegate instanceof JBossModuleFactory ) {
				((JBossModuleFactory)delegate).initialize();
			} else {
				factories.remove(f);
			}
		}

	}
	
	private static String[] loadJBossFactoryIDs() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "jbossModuleFactory");

		int size = cf.length;
		ArrayList list = new ArrayList();
		for (int i = 0; i < size; i++) {
			list.add(cf[i].getAttribute("id"));
			try {
			} catch (Throwable t) {
			}
		}
		String[] jbossFactories = new String[list.size()];
		list.toArray(jbossFactories);
		return jbossFactories;
	}



	
	private ModuleModel() {
		getJBossModuleFactories();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE );
	}
	

	
	/**
	 * Gets the first module found for a resource. 
	 * Checks the factories in order of their precedence.
	 * @param resource
	 * @return
	 */
	public IModule getModule(IResource resource) {
		IModule[] mods = getAllModules(resource);
		if( mods.length == 0 ) return null;
		return mods[0];
	}
	
	
	
	public IModule[] getAllModules(IResource resource) {
		ArrayList list = new ArrayList();
		Iterator i = factories.iterator();
		ModuleFactory factory = null;
		JBossModuleFactory jbFactory = null;
		while(i.hasNext()) {
			factory = (ModuleFactory)i.next();
			jbFactory = (JBossModuleFactory)factory.getDelegate(null);
			if( jbFactory.supports(resource) && jbFactory.getModule(resource) != null ) {
				list.add( jbFactory.getModule(resource) );
			}
		}
		IModule[] mods = new IModule[list.size()];
		list.toArray(mods);
		return mods;
	}
	
	
	/**
	 * Keep track of future changes in the workspace
	 * Alert all factories of the change.
	 */
	
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		try {
			final ArrayList removed = new ArrayList();
			final ArrayList added = new ArrayList();
			final ArrayList changed = new ArrayList();
			
			// First get what was added or removed
			delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					if( delta.getKind() == IResourceDelta.CHANGED) {
						changed.add(delta.getResource());
					}
					
					if( delta.getKind() == IResourceDelta.ADDED ) {
						added.add(delta.getResource());
					}

					if( delta.getKind() == IResourceDelta.REMOVED) {
						removed.add(delta.getResource());
					}
					return true;
				}
			} );
			
			JBossModuleFactory delegate;
			ModuleFactory factory;
			IResource res;
			JBossServer[] jbServers = JBossServerCore.getAllJBossServers();

			Iterator resourceIterator = removed.iterator();

			// Do removed first
			while(resourceIterator.hasNext()) {
				res = (IResource)resourceIterator.next();
				Iterator fact = factories.iterator();
				while(fact.hasNext()) {
					try {
						factory = (ModuleFactory)fact.next();
						delegate = (JBossModuleFactory)factory.getDelegate(null);
						delegate.resourceEvent(res, IResourceDelta.REMOVED);
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}
			}
			
			// Then Add
			resourceIterator = added.iterator();
			while(resourceIterator.hasNext()) {
				res = (IResource)resourceIterator.next();
				Iterator fact = factories.iterator();
				while(fact.hasNext()) {
					try {
						factory = (ModuleFactory)fact.next();
						delegate = (JBossModuleFactory)factory.getDelegate(null);
						delegate.resourceEvent(res, IResourceDelta.ADDED);
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}
			}

			
			// Now Changed 
			// Then Add
			resourceIterator = changed.iterator();
			while(resourceIterator.hasNext()) {
				res = (IResource)resourceIterator.next();
				Iterator fact = factories.iterator();
				while(fact.hasNext()) {
					try {
						factory = (ModuleFactory)fact.next();
						delegate = (JBossModuleFactory)factory.getDelegate(null);
						delegate.resourceEvent(res, IResourceDelta.CHANGED);
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}
			}

			
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	
	
	public void markModuleChanged(IModule module) {
		IServer[] servers = ServerCore.getServers();
		Server s;
		for( int i = 0; i < servers.length; i++ ) {
			if( servers[i] instanceof Server ) {
				s = (Server)servers[i];
				int stateOnServer = s.getModulePublishState(new IModule[] {module});
				if( stateOnServer != 0 ) {
					s.setModulePublishState(new IModule[] { module }, IServer.PUBLISH_STATE_UNKNOWN);
				}
			}
		}
	}

	
}