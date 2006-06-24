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
package org.jboss.ide.eclipse.as.core.module.factory;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.jboss.ide.eclipse.as.core.model.ModuleModel;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public abstract class JBossModuleFactory extends ModuleFactoryDelegate {
	
	public static final String NO_LOCATION = "__NO_LOCATION__";
	
	protected HashMap pathToModule = null;
	protected HashMap moduleToDelegate = null;
	
	public JBossModuleFactory() {
	}
	
	public abstract void initialize();
	public abstract Object getLaunchable(JBossModuleDelegate delegate);
	public abstract boolean supports(IResource resource);
	
	/**
	 * Get a delegate for this module. 
	 */
	public ModuleDelegate getModuleDelegate(IModule module) {
		ASDebug.p("getModuleDelegate", this);
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
	 * Go through the entire workspace looking for valid modules. 
	 * Store them when they're found. 
	 */
	protected void cacheModules() {
		ASDebug.p("Caching factory ", this);
		this.pathToModule = new HashMap();
		this.moduleToDelegate = new HashMap();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for( int i = 0; i < projects.length; i++ ) {
			try {
				projects[i].accept(new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						acceptAddition(resource);
						return true;
					} 
				});
			} catch( CoreException ce ) {
			}
		}
	}
	
	
	/**
	 * Return an associated module for this resource, 
	 * or null of none is found in this factory.
	 * @param resource
	 * @return
	 */
	public IModule getModule(IResource resource) {
		//ASDebug.p("getModule: " + resource.getFullPath() + ", my ID is " + getId(), this);
		if(pathToModule == null) {
			cacheModules();
		}
		
		// return the module if it already exists
		String path = getPath(resource);
		if( pathToModule.get(path) != null ) {
			return (IModule)pathToModule.get(path);
		}
		return null;
	}
	
	/**
	 * I dont think any class uses this yet, but I thought it should be public.
	 * @return
	 */
	public String getFactoryId() {
		return getId();
	}
	
	protected abstract IModule acceptAddition(IResource resource);
	
	/**
	 * Handle a deleted resource.
	 * For now, we're removing the module from the factory entirely.
	 * This will mean the server has a reference to a module it cannot find, but
	 * the module will still be on the server. 
	 * @param resource
	 */
	protected void acceptDeletion(IResource resource) {
		IModule module = getModule(resource);
		if( module == null ) return;
		
		Object delegate = moduleToDelegate.get(module);
		if( delegate != null ) {
			((JBossModuleDelegate)delegate).clearDocuments();
		}
		moduleToDelegate.remove(module);
		pathToModule.remove(getPath(resource));
		ModuleModel.getDefault().markModuleChanged(module, IResourceDelta.REMOVED);
	}

	
	/**
	 * Respond to a changed resource. (type IResourceDelta.CHANGED)
	 * Specifically, clear stale xml document references and 
	 * alert Module Model that you need to be republished.
	 * 
	 * @param resource
	 */
	protected void acceptChange(IResource resource) {
		IModule module = getModule(resource);
		if( module == null ) return;
		
		Object delegate = moduleToDelegate.get(module);
		if( delegate != null ) {
			((JBossModuleDelegate)delegate).clearDocuments();
		}
		ModuleModel.getDefault().markModuleChanged(module, IResourceDelta.CHANGED);
	}
	
	
	/**
	 * Respond to a resource change event. 
	 * This is important to allow the factory to clear any xml documents
	 * it might have reference to within a module, since the module
	 * has had changes and the document references may no longer be accurate
	 * 
	 * @param resource
	 * @param kind
	 */
	public void resourceEvent(IResource resource, int kind) {
		switch( kind ) {
			case IResourceDelta.REMOVED:
				if( contains(resource)) 
					acceptDeletion(resource);
				break;
			case IResourceDelta.ADDED:
				acceptAddition(resource);
				break;
			case IResourceDelta.CHANGED:
				if( contains(resource)) 
					acceptChange(resource);
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
	protected boolean contains(IResource resource) {
		if(pathToModule == null) {
			cacheModules();
		}

		Object o = pathToModule.get(getPath(resource));
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
