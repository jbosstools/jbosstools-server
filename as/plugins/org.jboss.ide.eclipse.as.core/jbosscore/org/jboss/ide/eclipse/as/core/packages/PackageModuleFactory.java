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
package org.jboss.ide.eclipse.as.core.packages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ModuleFile;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PackageModuleFactory extends ProjectModuleFactoryDelegate {
	protected Map moduleDelegates = new HashMap(5);
	protected HashMap packageToModule = new HashMap(5);
	
	private static int nextArchiveId = -1;
	private static final String NEXT_ARCHIVE_KEY = "org.jboss.ide.eclipse.as.core.PackageModuleFactory.nextId";
	
	public static final String FACTORY_TYPE_ID = "org.jboss.ide.eclipse.as.core.PackageModuleFactory";
	public static final String MODULE_TYPE = "jboss.package";
	public static final String VERSION = "1.0";

	public static final String MODULE_ID_PROPERTY_KEY = "org.jboss.ide.eclipse.as.core.packages.ModuleIDPropertyKey";
	
	private static PackageModuleFactory factory;
	public static PackageModuleFactory getFactory() {
		if( factory != null ) return factory;
		
		ModuleFactory[] factories = ServerPlugin.getModuleFactories();
		for( int i = 0; i < factories.length; i++ ) {
			if( factories[i].getId().equals(PackageModuleFactory.FACTORY_TYPE_ID)) {
				Object o = factories[i].getDelegate(new NullProgressMonitor());
				if( o instanceof PackageModuleFactory ) {
					factory = (PackageModuleFactory)o;
					return factory;
				}
			}
		}
		return null;
	}

	
	
	public PackageModuleFactory() {
		super();
	}
	
	/**
	 * @param archives
	 * @return  returns whether a save has occurred
	 */
	protected boolean ensureArchivesHaveIDs(IProject project, IArchive[] archives) {
		boolean requiresSave = false;
		for( int i = 0; i < archives.length; i++ ) {
			if( getID(archives[i]) == null ) {
				requiresSave = true;
				archives[i].setProperty(MODULE_ID_PROPERTY_KEY, getID(archives[i], true));
			}
		}
		if( requiresSave ) {
			// save
			ArchivesModel.instance().saveModel(project.getLocation(), new NullProgressMonitor());
		}
		return requiresSave;
	}
	protected IModule[] createModules(IProject project) {
		try {
		if( ArchivesModelCore.getProjectPackages(project.getLocation(), null, true).length > 0 ) {
			ArrayList list = new ArrayList();
			IModule module;
			IArchive[] packages = ArchivesModelCore.getProjectPackages(project.getLocation(), new NullProgressMonitor(), true);
			boolean saved = ensureArchivesHaveIDs(project, packages);
			for( int i = 0; i < packages.length; i++ ) {
				module = createModule(getID(packages[i]), getName(packages[i]),						 
						MODULE_TYPE, VERSION, project);
				list.add(module);
				Object moduleDelegate = new PackagedModuleDelegate(packages[i]);
				moduleDelegates.put(module, moduleDelegate);
				packageToModule.put(packages[i], module);
			}
			return (IModule[]) list.toArray(new IModule[list.size()]);
		}
		} catch( Throwable t ) {
			t.printStackTrace();
		}
		return null;
	}
	
	public static String getID(IArchive pack) {
		return getID(pack, false);
	}
	protected static String getID(IArchive pack, boolean create) {
		String propVal = pack.getProperty(MODULE_ID_PROPERTY_KEY);
		if( propVal == null && create ) {
			if( nextArchiveId == -1 ) {
				nextArchiveId = 
					new InstanceScope().getNode(JBossServerCorePlugin.PLUGIN_ID).getInt(MODULE_ID_PROPERTY_KEY, 0);
			}
			nextArchiveId++;
			new InstanceScope().getNode(JBossServerCorePlugin.PLUGIN_ID).putInt(MODULE_ID_PROPERTY_KEY, nextArchiveId);
			return MODULE_ID_PROPERTY_KEY + "." + nextArchiveId;
		} else if( propVal == null ) {
			return null;
		} 
		return propVal;
	}

	public static String getProjectName(IArchiveNode node) {
		IPath projPath = node.getProjectPath();
		if( projPath == null ) return null;
		IProject[] list = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for( int i = 0; i < list.length; i++ )
			if( list[i].getLocation().equals(projPath))
				return list[i].getName();
		return null;
	}
	public static String getName(IArchive pack) {
		String projName = getProjectName(pack);
		return projName + "/" + pack.getName();
	}
	public ModuleDelegate getModuleDelegate(IModule module) {
		return (ModuleDelegate) moduleDelegates.get(module);
	}


	protected void clearCache() {
		moduleDelegates = new HashMap(5);
		packageToModule = new HashMap(5);
	}
	
	public IModule getModuleFromPackage(IArchive pack) {
		getModules(); // prime it
		return (IModule)packageToModule.get(pack);
	}
	
	public IModule[] getModulesFromProject(IProject project) {
		ArrayList mods = new ArrayList();
		IArchive[] packs = ArchivesModelCore.getProjectPackages(project.getLocation(), new NullProgressMonitor(), true);
		for( int i = 0; i < packs.length; i++ ) {
			IModule mod = getModuleFromPackage(packs[i]);
			if( mod != null ) mods.add(mod);
		}
		return (IModule[]) mods.toArray(new IModule[mods.size()]);
	}
	
	/**
	 * Returns the list of resources that the module should listen to
	 * for state changes. The paths should be project relative paths.
	 * Subclasses can override this method to provide the paths.
	 *
	 * @return a possibly empty array of paths
	 */
	protected IPath[] getListenerPaths() {
		return new IPath[] { new Path(ArchivesModel.PROJECT_PACKAGES_FILE) };
	}

	public class PackagedModuleDelegate extends ModuleDelegate {
		private IArchive pack;
		private HashMap members;
		public PackagedModuleDelegate(IArchive pack) {
			this.pack = pack;
			members = new HashMap();
		}
		public IArchive getPackage() {return pack;}
		public IModule[] getChildModules() {
			return new IModule[0];
		}
		
		public void reset() {
			members = new HashMap();
		}

		public IModuleResource[] members() throws CoreException {
			Collection c = members.values();
			return (IModuleResource[]) c.toArray(new IModuleResource[c.size()]);
		}
		
		public void fileUpdated(IPath filePath) {
			long timestamp;
			timestamp = new Date().getTime(); // now

			IPath dest = pack.getDestinationPath();
			if( dest.isPrefixOf(filePath)) {
				filePath = filePath.removeFirstSegments(dest.segmentCount());
			}
			members.put(filePath, new ModuleFile(filePath.lastSegment(), filePath, timestamp));
		}
		public void fileRemoved(IPath filePath) {
			IPath dest = pack.getDestinationPath();
			if( dest.isPrefixOf(filePath)) {
				filePath = filePath.removeFirstSegments(dest.segmentCount());
			}
			members.remove(filePath);
		}
		

		public IStatus validate() {
			return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
					0, "", null);
		}
	}
}
