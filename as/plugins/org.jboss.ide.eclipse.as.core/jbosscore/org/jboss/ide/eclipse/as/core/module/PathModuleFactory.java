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
package org.jboss.ide.eclipse.as.core.module;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

/**
 *
 * @author rob.stryker@jboss.com
 */
public abstract class PathModuleFactory extends JBossModuleFactory {
	
	public static JBossModuleFactory getDefaultInstance(String type) {
		ModuleFactory[] factories = ServerPlugin.getModuleFactories(); // just make sure they're loaded
		for( int i = 0; i < factories.length; i++ ) {
			if( factories[i].getId().equals(type)) {
				return (ArchiveModuleFactory)factories[i].getDelegate(new NullProgressMonitor());
			}
		}
		return null;
	}
	
	public void initialize() {
		cacheModules();
	}

	protected IModule acceptAddition(String path) {
		if( !supports(path)) 
			return null;

		// otherwise create the module
		String name = new Path(path).lastSegment();
		IModule module = createModule(path, name, 
				getModuleType(path), getModuleVersion(path), null);
		
		
		PathModuleDelegate delegate = new PathModuleDelegate();
		delegate.initialize(module);
		delegate.setResourcePath(path);
		delegate.setFactory(this);
		
		// and insert it
		pathToModule.put(path, module);
		moduleToDelegate.put(module, delegate);
		
		// ensure the factory clears its cache
		clearModuleCache();
		
		return module;	
	}
	
	public abstract String getModuleType(String path);
	public abstract String getModuleVersion(String path);
	

	public boolean supports(String path) {
		return false;
	}
	
	
	public class PathModuleDelegate extends JBossModuleDelegate {
		public IModule[] getChildModules() {
			return null;
		}

		public void initialize() {
		}

		public IStatus validate() {
			return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
					0, "valid", null);
		}

		public IModuleResource[] members() throws CoreException {
			return new IModuleResource[0];
		}
	}

}
