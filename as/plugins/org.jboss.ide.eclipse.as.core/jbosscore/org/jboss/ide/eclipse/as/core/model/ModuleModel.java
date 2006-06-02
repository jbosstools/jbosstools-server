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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleFactory;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ASDebug;


/**
 * This class is the model behind the sole artifact adaptor.
 *  
 * This class / model is responsible for the following:
 * 	- A sorted list of our factories (from extension points)
 *  - Querying those factories as to whether they contain some module
 *  - Listening in for resource changes and alerting the factories
 *         that they need to adjust accordingly.
 *  - Ultimately keeping track of resource changes that may be 
 *         important to consider during the publish operation. 
 *         
 *         
 * @author rstryker
 *
 */
public class ModuleModel implements IResourceChangeListener{
	
	private static ModuleModel singleton;
	private ArrayList factories;
	private ModuleDeltaModel deltaModel;
	
	public static ModuleModel getDefault() {
		if( singleton == null ) {
			singleton = new ModuleModel();
		}


		return singleton;
	}
	
	private ModuleModel() {
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
				
				//ASDebug.p("arg0 has value " + p0 + " and arg1 has value " + p1, this);
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
		loadAcceptableFactories();
		Collections.sort(factories, factoryComparator);
		
		deltaModel = new ModuleDeltaModel();
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE );
		init();
	}
	

	private void loadAcceptableFactories() {
		ModuleFactory[] factories = ServerPlugin.getModuleFactories();
		String[] jbossIds = loadJBossFactoryIDs();
		
		for( int i = 0; i < factories.length; i++ ) {
			for( int j = 0; j < jbossIds.length; j++ ) {
				if( jbossIds[j].equals(factories[i].getId()) ) {
					ASDebug.p("Adding factory: " + factories[i].getId(), this);
					this.factories.add(factories[i]);
				}
			}
		}
		
		
	}
	
	
	private String[] loadJBossFactoryIDs() {
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "jbossModuleFactory");

		
		int size = cf.length;
		ArrayList list = new ArrayList();
		for (int i = 0; i < size; i++) {
			list.add(cf[i].getAttribute("id"));
			try {
			} catch (Throwable t) {
				ASDebug.p("Exception", this);
			}
		}
		String[] jbossFactories = new String[list.size()];
		list.toArray(jbossFactories);
		return jbossFactories;
	}
	
	
	/**
	 * At the end of this method, all JBossModuleFactories should 
	 * be created, and have a basic initialization done.
	 *
	 */
	private void init() {
		Iterator i = this.factories.iterator();
		ModuleFactory f = null;
		ModuleFactoryDelegate delegate = null;

		while(i.hasNext()) {
			f = (ModuleFactory)i.next();
			delegate = f.getDelegate(null);
			if( delegate instanceof JBossModuleFactory ) {
				((JBossModuleFactory)delegate).initialize();
			} else {
				this.factories.remove(f);
			}
		}
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

	
	
	public void markModuleChanged(IModule module, int resourceDelta) {
		if( resourceDelta == IResourceDelta.ADDED) {
			getDeltaModel().setModuleState(module, IModuleResourceDelta.ADDED);
		} else if( resourceDelta == IResourceDelta.REMOVED ) {
			getDeltaModel().setModuleState(module, IModuleResourceDelta.REMOVED);
		} else if( resourceDelta == IResourceDelta.CHANGED ) {
			getDeltaModel().setModuleState(module, IModuleResourceDelta.CHANGED);
		}
	}

	public ModuleDeltaModel getDeltaModel() {
		return deltaModel;
	}
	
	public class ModuleDeltaModel  {
		/**
		 * Maps a module id to it's most recent delta, which includes date
		 */
		private HashMap moduleToDelta;
		
		/**
		 * Maps a server+module id to the last delta that 
		 * was marked as received by the server.
		 */
		private HashMap servermodToLastDelta;
		
		public ModuleDeltaModel() {
			moduleToDelta = new HashMap();
			servermodToLastDelta = new HashMap();
		}
		
		/**
		 * The resource associated with this module has been either
		 * changed, added, or removed. This regards the underlying
		 * jar, war, ear (etc) file, and a resourceChangeEvent that
		 * has affected it. 
		 * 
		 * @param module
		 * @param resourceDeltaKind
		 */
		public void setModuleState(IModule module, int resourceDeltaKind) {
			moduleToDelta.put(module.getId(), new ModuleDelta(resourceDeltaKind));
		}
		
		
		/**
		 * Set the current delta as the last one seen by this server.
		 * @param module
		 * @param serverID
		 */
		public void setDeltaSeen(IModule module, String serverID) {
			String key = serverID + "::" + module.getId();
			IModuleResourceDelta[] delta = getDelta(module);
			if( delta != null ) {
				servermodToLastDelta.put(key, delta);
			}
		}
		
		
		/**
		 * Get the newest delta for the given module, or null if none available
		 * @param module
		 * @return
		 */
		public IModuleResourceDelta[] getDelta(IModule module) {
			Object o = moduleToDelta.get(module.getId());
			if( o == null ) {
				return new IModuleResourceDelta[0];
			}
			
			return new IModuleResourceDelta[] {(IModuleResourceDelta)o};			
		}
		
		/**
		 * Get the newest deltas for each of the modules provided
		 * @param modules
		 * @return
		 */
		public IModuleResourceDelta[] getDeltas(IModule[] modules) {
			ArrayList list = new ArrayList();
			IModuleResourceDelta[] delta;
			for( int i = 0; i < modules.length; i++ ) {
				delta = getDelta(modules[i]);
				if( delta != null ) 
					list.addAll(Arrays.asList(delta));
			}
			ModuleDelta[] deltas = new ModuleDelta[list.size()];
			list.toArray(deltas);
			return deltas;
		}
		
		public IModuleResourceDelta[] getRecentDeltas(IModule[] modules, IServer server) {
			String id = server.getId();
			ArrayList deltaList = new ArrayList();
			deltaList.addAll(Arrays.asList(getDeltas(modules)));
			
			
			// We have a list of all of the deltas for the module list.
			// now eliminate any that aren't new.
			String key;
			Object o;
			IModuleResourceDelta[] ds;
			for( int i = 0; i < modules.length; i++ ) {
				key = id + "::" + modules[i].getId();
				o = servermodToLastDelta.get(key);
				if( o != null ) {
					ds = (IModuleResourceDelta[])o;
					deltaList.removeAll(Arrays.asList(ds));
					ASDebug.p("Removing a delta that's already been seen.", this);
				}
			}
			
			IModuleResourceDelta[] deltas = new IModuleResourceDelta[deltaList.size()];
			deltaList.toArray(deltas);
			return deltas;
		}
		
	}
	
	public class ModuleDelta implements IModuleResourceDelta {
		/**
		 * Kind is a value from IModuleResourceDelta
		 */
		private int kind;
		
		/**
		 * Date is the last time this module was changed.
		 * Servers can use this to figure out if it needs to be
		 * published again or not. 
		 */
		private long date;
		public ModuleDelta(int kind) {
			this.kind = kind;
			this.date = new Date().getTime();
		}

		public IModuleResourceDelta[] getAffectedChildren() {
			return new IModuleResourceDelta[0];
		}

		public int getKind() {
			return kind;
		}

		public IPath getModuleRelativePath() {
			return null;
		}

		public IModuleResource getModuleResource() {
			return null;
		}
		
	}
	

}
