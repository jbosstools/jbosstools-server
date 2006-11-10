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
package org.jboss.ide.eclipse.as.core.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.model.ModuleModel;
import org.jboss.ide.eclipse.as.core.server.JBossServer;

public abstract class JBossModuleFactory extends ModuleFactoryDelegate {
	
	public static final String NO_LOCATION = "__NO_LOCATION__";
	
	protected HashMap pathToModule = null;
	protected HashMap moduleToDelegate = null;
	
	public JBossModuleFactory() {
		pathToModule = new HashMap();
		moduleToDelegate = new HashMap();
		initialize();
	}
	
	public abstract void initialize();
	public abstract boolean supports(String path);
	
	public boolean supports( IResource resource ) {
		return supports(getPath(resource));
	}
	
	/**
	 * Get a delegate for this module. 
	 */
	public ModuleDelegate getModuleDelegate(IModule module) {
		return (ModuleDelegate)moduleToDelegate.get(module);
	}

	/**
	 * Get a list of all modules this factory has references to.
	 */
	public IModule[] getModules() {		
		if( pathToModule == null ) {
			cacheModules();
		}
		Collection modules = pathToModule.values();

		IModule[] modules2 = new IModule[modules.size()];
		modules.toArray(modules2);
		return modules2;
	}
	
	/**
	 * Get a list of minimal modules to have on hand, 
	 * specifically those that are already deployed to a server
	 * and are expected. 
	 */
	protected void cacheModules() {
		this.pathToModule = new HashMap();
		this.moduleToDelegate = new HashMap();
		
		String[] paths = getServerModulePaths();
		for( int i = 0; i < paths.length; i++ ) {
			acceptAddition(paths[i]);
		}
	}
	
	// lifted from other class
	private String[] getServerModulePaths() {
		// Stolen from Server.class, not public
		final String MODULE_LIST = "modules";

		ArrayList paths = new ArrayList();
		IServer[] server = ServerCore.getServers();
		for( int i = 0; i < server.length; i++ ) {
			if( server[i].getClass().equals(Server.class)) {
				// Get the module ID list:
				List l = ((Server)server[i]).getAttribute(MODULE_LIST, (List) null);

				if( l == null ) return new String[0];
				
				Object[] o = l.toArray();
				// Get the module ID list:
				for( int j = 0; j < o.length; j++ ) {
					String moduleId = (String) o[j];
					String name = "<unknown>";
					int index = moduleId.indexOf("::");
					if (index > 0) {
						name = moduleId.substring(0, index);
						moduleId = moduleId.substring(index+2);
					}
					
					String moduleTypeId = null;
					String moduleTypeVersion = null;
					index = moduleId.indexOf("::");
					if (index > 0) {
						int index2 = moduleId.indexOf("::", index+1);
						moduleTypeId = moduleId.substring(index+2, index2);
						moduleTypeVersion = moduleId.substring(index2+2);
						moduleId = moduleId.substring(0, index);
					}
					
					if( moduleId.startsWith(getFactoryId() + ":")) {
						String path = moduleId.substring((getFactoryId()+":").length());
						paths.add(path);
					}
					
				}
			}
		}
		return (String[]) paths.toArray(new String[paths.size()]);
	}
	
	/**
	 * Return an associated module for this resource, 
	 * or null of none is found in this factory.
	 * @param resource
	 * @return
	 */
	public IModule getModule(String path) {
		//ASDebug.p("getModule: " + resource.getFullPath() + ", my ID is " + getId(), this);
		if(pathToModule == null) {
			cacheModules();
		}
		
		// return the module if it already exists
		//String path = getPath(resource);
		if( pathToModule.get(path) != null ) {
			return (IModule)pathToModule.get(path);
		}
		if( supports(path)) {
			acceptAddition(path);
			if( pathToModule.get(path) != null ) {
				return (IModule)pathToModule.get(path);
			}
		}
		return null;
	}
	
	public IModule getModule(IResource resource) {
		return getModule(getPath(resource));
	}
	
	/**
	 * I dont think any class uses this yet, but I thought it should be public.
	 * @return
	 */
	public String getFactoryId() {
		return getId();
	}
	
	protected abstract IModule acceptAddition(String path);
	
	/**
	 * Handle a deleted resource.
	 * For now, we're removing the module from the factory entirely.
	 * This will mean the server has a reference to a module it cannot find, but
	 * the module will still be on the server. 
	 * @param resource
	 */
	protected void acceptDeletion(String resourcePath) {
		IModule module = getModule(resourcePath);
		if( module == null ) return;
		
		Object delegate = moduleToDelegate.get(module);
		moduleToDelegate.remove(module);
		pathToModule.remove(resourcePath);
		// TODO Mark removed in a model
		ModuleModel.getDefault().markModuleChanged(module);
	}

	protected void acceptChange(String resourcePath) {
		IModule module = getModule(resourcePath);
		if( module == null ) return;
		
		Object delegate = moduleToDelegate.get(module);
		// TODO Mark change in some model 
		ModuleModel.getDefault().markModuleChanged(module);
	}
	
	
	public void resourceEvent(IResource resource, int kind) {
		switch( kind ) {
			case IResourceDelta.REMOVED:
				if( contains(getPath(resource))) 
					acceptDeletion(getPath(resource));
				break;
			case IResourceDelta.ADDED:
				acceptAddition(getPath(resource));
				break;
			case IResourceDelta.CHANGED:
				if( contains(getPath(resource))) 
					acceptChange(getPath(resource));
				break;
			default:
				break;
		}
		
	}
	
	/**
	 * Does this factory already have a reference to the resource?
	 * @param resource
	 * @return
	 */
	protected boolean contains(String resourcePath) {
		if(pathToModule == null) {
			cacheModules();
		}

		Object o = pathToModule.get(resourcePath);
		return o == null ? false : true;
	}
	
	
	/**
	 * Return the absolute location of the resource, 
	 * or a constant indicating not found
	 * @param resource
	 * @return
	 */
	public String getPath(IResource resource) {
		try {
			return resource.getLocation().toOSString();
		} catch( Exception e ) {
			return NO_LOCATION;
		}
	}
	
	
}
