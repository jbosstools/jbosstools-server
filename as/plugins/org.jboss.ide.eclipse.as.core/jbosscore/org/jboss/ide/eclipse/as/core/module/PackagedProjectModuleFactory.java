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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.model.ModuleModel;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PackagedProjectModuleFactory extends ProjectModuleFactoryDelegate implements IResourceChangeListener {
	protected Map moduleDelegates = new HashMap(5);
	protected HashMap projectsToModule = new HashMap(5);
	
	public static final String FACTORY_TYPE_ID = "org.jboss.ide.eclipse.as.core.PackagedModuleFactory";
	public static final String MODULE_TYPE = "jboss.packaged.project";
	public static final String VERSION = "1.0";

	public PackagedProjectModuleFactory() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE );
	}
	
	protected IModule[] createModules(IProject project) {
		if( PackagesCore.projectHasPackages(project) ) {
			IModule module = createModule(project.getName(), 
					project.getName(), MODULE_TYPE, VERSION, project);
			Object moduleDelegate = new PackagedModuleDelegate();
			moduleDelegates.put(module, moduleDelegate);
			projectsToModule.put(project, module);
			return new IModule[] {module};
		}
		
		return null;
	}

	public ModuleDelegate getModuleDelegate(IModule module) {
		return (ModuleDelegate) moduleDelegates.get(module);
	}


	protected void clearCache() {
		moduleDelegates = new HashMap(5);
		projectsToModule = new HashMap(5);
	}
	
	public IModule getModuleFromProject(IProject project) {
		getModules(); // prime it
		return (IModule)projectsToModule.get(project);
	}
	
	/**
	 * Returns the list of resources that the module should listen to
	 * for state changes. The paths should be project relative paths.
	 * Subclasses can override this method to provide the paths.
	 *
	 * @return a possibly empty array of paths
	 */
	protected IPath[] getListenerPaths() {
		return null;
	}

	public class PackagedModuleDelegate extends ModuleDelegate {

		public IModule[] getChildModules() {
			return new IModule[0];
		}

		public IModuleResource[] members() throws CoreException {
			return new IModuleResource[0];
		}

		public IStatus validate() {
			return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
					0, "", null);
		}
	}

	public void resourceChanged(IResourceChangeEvent event) {
		IResource res;
		IResourceDelta delta = event.getDelta();
		IResourceDelta[] children = delta.getAffectedChildren();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i].getResource() instanceof IProject ) {
				res = children[i].getResource();
				IModule mod = getModuleFromProject((IProject)res);
				if( mod != null )
					ModuleModel.getDefault().markModuleChanged(mod);
			}
		}
	}
}
