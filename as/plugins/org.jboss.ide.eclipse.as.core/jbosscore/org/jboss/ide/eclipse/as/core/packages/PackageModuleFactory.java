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

import java.io.File;
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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ModuleFile;
import org.eclipse.wst.server.core.internal.ModuleFolder;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeVisitor;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.osgi.service.prefs.BackingStoreException;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PackageModuleFactory extends ModuleFactoryDelegate {
	
	private static int nextArchiveId = -1;
	private static final String NEXT_ARCHIVE_KEY = "org.jboss.ide.eclipse.as.core.PackageModuleFactory.nextId";
	
	public static final String FACTORY_TYPE_ID = "org.jboss.ide.eclipse.as.core.PackageModuleFactory";
	public static final String MODULE_TYPE = "jboss.package";
	public static final String VERSION = "1.0";

	public static final String MODULE_ID_PROPERTY_KEY = "org.jboss.ide.eclipse.as.core.packages.ModuleIDPropertyKey";
	
	// the factory delegate and the factory respectively
	private static PackageModuleFactory factDelegate;
	private static ModuleFactory factory;

	public static PackageModuleFactory getFactory() {
		if( factDelegate == null ) {
			// find them
			ModuleFactory[] factories = ServerPlugin.getModuleFactories();
			
			search:
			for( int i = 0; i < factories.length; i++ ) {
				if( factories[i].getId().equals(PackageModuleFactory.FACTORY_TYPE_ID)) {
					Object o = factories[i].getDelegate(new NullProgressMonitor());
					if( o instanceof PackageModuleFactory ) {
						factory = factories[i];
						factDelegate = (PackageModuleFactory)o;
						break search;
					}
				}
			}
		}
		return factDelegate;
	}

	public static String getID(IArchive pack) {
		return getID(pack, false);
	}
	protected static String getID(IArchive pack, boolean create) {
		String propVal = pack.getProperty(MODULE_ID_PROPERTY_KEY);
		if( propVal == null && create ) {
			if( nextArchiveId == -1 ) {
				nextArchiveId = 
					new InstanceScope().getNode(JBossServerCorePlugin.PLUGIN_ID).getInt(NEXT_ARCHIVE_KEY, 0);
			}
			nextArchiveId++;
			IEclipsePreferences prefs = new InstanceScope().getNode(JBossServerCorePlugin.PLUGIN_ID);
			prefs.putInt(NEXT_ARCHIVE_KEY, nextArchiveId);
			try {
				prefs.flush();
			} catch( BackingStoreException bse ) {
			}
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

	
	
	/*
	 * The beginning of the class. Finally!
	 */
	
	
	
	
	
	protected HashMap moduleDelegates = new HashMap(5);
	protected HashMap packageToModule = new HashMap(5);	
	protected HashMap projectToPackages = new HashMap(5);
	public PackageModuleFactory() {
		super();
	}

	/**
	 * Set a property so that each module that's here in the factory
	 * has a unique ID other than it's name (which is not unique)
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
	
	
	
	/**
	 * Create and return the modules for a given IProject
	 * @param project The project which has the modules
	 * @return The modules that the project has
	 */
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
				projectToPackages.put(project, packages);
				return (IModule[]) list.toArray(new IModule[list.size()]);
			}
		} catch( Throwable t ) {
			t.printStackTrace();
		}
		return new IModule[]{};
	}

	public ModuleDelegate getModuleDelegate(IModule module) {
		return (ModuleDelegate) moduleDelegates.get(module);
	}

	public IModule getModuleFromPackage(IArchive pack) {
		return (IModule)packageToModule.get(pack);
	}

	private static boolean initiated = false;
	public IModule[] getModules() {
		if( !initiated ) 
			init();
		Collection c = packageToModule.values();
		return (IModule[]) c.toArray(new IModule[c.size()]);
	}
	
	protected void init() {
		IProject[] projects2 = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		int size = projects2.length;
		for (int i = 0; i < size; i++) {
			if (projects2[i].isAccessible()) {
				createModules(projects2[i]);
			}
		}
	}
		
	
	public void refreshProject(IPath projectLoc) {
		IProject proj = null;
		IProject[] projects2 = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		int size = projects2.length;
		for (int i = 0; i < size; i++) {
			if( projects2[i].getLocation().equals(projectLoc))
				proj = projects2[i];
		}
		if( proj != null ) {
			IArchive[] archives = (IArchive[])projectToPackages.get(proj);
			IModule mod;
			projectToPackages.remove(proj);
			for( int i = 0; i < archives.length; i++ ) {
				mod = (IModule)packageToModule.get(archives[i]);
				packageToModule.remove(archives[i]);
				moduleDelegates.remove(mod);
			}
			createModules(proj);
			clearModuleCache();
		}
	}
	
	protected static interface IExtendedModuleResource {
		public IPath getPath();
		public IArchiveNode getNode();
		public IPath getDeepDestination();
	}

	public class PackagedModuleDelegate extends ModuleDelegate {
		private IArchive pack;
		private HashMap members;
		private IArchiveNodeVisitor initVisitor;
		public PackagedModuleDelegate(IArchive pack) {
			this.pack = pack;
			initVisitor = createInitVisitor();
		}
		private IArchiveNodeVisitor createInitVisitor() {
			return new IArchiveNodeVisitor() {
				public boolean visit(IArchiveNode node) {
					if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE ) {
						IPath rel = ((IArchive)node).getRootArchiveRelativePath();
						members.put(rel, new ArchiveFolderModuleFolder(((IArchive)node).getName(), rel, node));
					} else if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FOLDER) {
						IPath rel = ((IArchiveFolder)node).getRootArchiveRelativePath();
						members.put(rel, new ArchiveFolderModuleFolder(((IArchiveFolder)node).getName(), rel, node));
					} else if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FILESET) {
						IArchiveFileSet fs = (IArchiveFileSet)node;
						IPath[] paths = fs.findMatchingPaths();
						File tmp = null;
						IPath archiveRelative;
						for( int i = 0; i < paths.length; i++ ) {
							archiveRelative = fs.getRootArchiveRelativePath(paths[i]);
							tmp = paths[i].toFile();
							ExtendedModuleFile emf = new ExtendedModuleFile(tmp.getName(), archiveRelative, tmp.lastModified(), paths[i], fs);							
							members.put(archiveRelative, emf);
							IPath tmp2 = archiveRelative.removeFirstSegments(fs.getRootArchiveRelativePath().segmentCount()).removeLastSegments(1);
							if( tmp2.segmentCount() > 0 ) 
								addFoldersFor(fs, tmp2);
						}
					}
					return true;
				} 
			};
		}
		protected void addFoldersFor(IArchiveFileSet fs, IPath fsRelative) {
			IPath fsBase = fs.getRootArchiveRelativePath();
			IPath folderPath = null;
			IPath tmpRelative;
			for( int i = 0; i < fsRelative.segmentCount(); i++ ) {
				tmpRelative = fsRelative.removeLastSegments(i);
				folderPath = fsBase.append(tmpRelative);
				if( !members.containsKey(folderPath))
					members.put(folderPath, new FilesetModuleFolder(folderPath.lastSegment(), folderPath, fs, fs.getGlobalSourcePath().append(tmpRelative)));
			}
		}
		public IArchive getPackage() {
			return pack;
		}
		public IModule[] getChildModules() {
			return new IModule[0];
		}
		
		public void reset() {
			members = null;
		}

		protected void init() {
			members = new HashMap();
			pack.accept(initVisitor);
		}

		public IModuleResource[] members() throws CoreException {
			init();
			Collection c = members.values();
			return (IModuleResource[]) c.toArray(new IModuleResource[c.size()]);
		}
		
		public void fileUpdated(IPath changedFile, IArchiveFileSet fs) {
			IPath archiveRelative = fs.getRootArchiveRelativePath(changedFile);
			long stamp = new Date().getTime();
			ExtendedModuleFile emf = new ExtendedModuleFile(changedFile.lastSegment(), archiveRelative, stamp, changedFile, fs);
			members.put(archiveRelative, emf);
		}
		public void fileRemoved(IPath removedFile, IArchiveFileSet fs) {
			IPath archiveRelative = fs.getRootArchiveRelativePath(removedFile);
			members.remove(archiveRelative);
		}
				
		
		public class ExtendedModuleFile extends ModuleFile implements IExtendedModuleResource {
			private IPath srcPath;
			private IArchiveFileSet node;
			public ExtendedModuleFile(String name, IPath relativePath, long stamp, 
					IPath srcPath, IArchiveFileSet fs) {
				super(name, relativePath, stamp);
				this.srcPath = srcPath;
				this.node = fs;
				System.out.println("adding emfile " + srcPath);
			}
			public IPath getPath() { return srcPath; }
			public IArchiveNode getNode() { return node; }
			public IPath getDeepDestination() {
				return node.getRootArchive().getDestinationPath().append(node.getRootArchiveRelativePath(path));
			}
		}

		public class FilesetModuleFolder extends ModuleFolder implements IExtendedModuleResource {

			private IArchiveFileSet node;
			private IPath srcPath;
			public FilesetModuleFolder(String name, IPath path, IArchiveFileSet fs, IPath srcPath) {
				super(name, path);
				this.node = fs;
				this.srcPath = srcPath;
			}
			public IPath getPath() { return srcPath; }
			public IArchiveNode getNode() { return node; }
			public IPath getDeepDestination() {
				return node.getRootArchive().getDestinationPath().append(node.getRootArchiveRelativePath(path));
			}
		}
		
		public class ArchiveFolderModuleFolder extends ModuleFolder implements IExtendedModuleResource {
			private IArchiveNode node;
			private IPath srcPath;
			public ArchiveFolderModuleFolder(String name, IPath path, IArchiveNode node) {
				super(name, path);
				this.node = node;
				this.srcPath = null;
			}
			public IPath getPath() { return srcPath; }
			public IArchiveNode getNode() { return node; }
			public IPath getDeepDestination() {
				return node.getRootArchive().getDestinationPath().append(node.getRootArchiveRelativePath());
			}
		}
		
		public IPath getSourceFile(IModuleResource mf) {
			if( mf instanceof IExtendedModuleResource ) {
				return ((IExtendedModuleResource)mf).getPath();
			}
			return null;
		}
		
		public IPath getConcreteDestFile(IModuleResource mr) {
			if( mr instanceof IExtendedModuleResource ) {
				IExtendedModuleResource emf =(IExtendedModuleResource)mr;
				return ModelUtil.getBaseDestinationFile(emf.getNode(), emf.getPath());
			}
			return null;
		}

		public IPath getDeepDestFile(IModuleResource mf) {
			if( mf instanceof IExtendedModuleResource ) {
				return ((IExtendedModuleResource)mf).getDeepDestination();
			}
			return null;
		}

		public IStatus validate() {
			return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
					0, "", null);
		}
	}

}
