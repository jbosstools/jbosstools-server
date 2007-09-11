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
package org.jboss.ide.eclipse.as.core.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ModuleFile;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
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

	/**
	 * Find the module factory wrapper WTP provides for us
	 * @return
	 */
	public static PackageModuleFactory getFactory() {
		if( factDelegate == null ) {
			ModuleFactory[] factories = ServerPlugin.getModuleFactories();
			
			for( int i = 0; i < factories.length; i++ ) {
				if( factories[i].getId().equals(PackageModuleFactory.FACTORY_TYPE_ID)) {
					Object o = factories[i].getDelegate(new NullProgressMonitor());
					if( o instanceof PackageModuleFactory ) {
						factory = factories[i];
						factDelegate = (PackageModuleFactory)o;
						return factDelegate;
					}
				}
			}
		}
		return factDelegate;
	}

	/**
	 * Get the module ID of this IArchive if it exists. 
	 * Do not create a new one otherwise
	 * @param pack
	 * @return
	 */
	public static String getID(IArchive pack) {
		return getID(pack, false);
	}
	
	/**
	 * Get the module ID of this IArchive if it exists.
	 * If create is true, and the ID is not set, set a default ID.
	 * @param pack
	 * @param create
	 * @return
	 */
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
			return "" + nextArchiveId;
		} else if( propVal == null ) {
			return null;
		} 
		return propVal;
	}
	
	/**
	 * Return the name of the project in the workspace
	 * @param node
	 * @return
	 */
	public static String getProjectName(IArchiveNode node) {
		IPath projPath = node.getProjectPath();
		if( projPath == null ) return null;
		IProject[] list = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for( int i = 0; i < list.length; i++ )
			if( list[i].getLocation().equals(projPath))
				return list[i].getName();
		return null;
	}
	
	/**
	 * Get the visible name of this module
	 * @param pack
	 * @return
	 */
	public static String getName(IArchive pack) {
		return getProjectName(pack) + "/" + pack.getName();
	}

	
	
	protected IModuleContributor[] moduleContributors;
	public PackageModuleFactory() {
		super();
		moduleContributors = new IModuleContributor[] { ArchivesModelModuleContributor.getInstance() };
		
	}

	protected IModule createModule2(IArchive pack, IProject project) {
		return createModule(getID(pack), getName(pack), MODULE_TYPE, VERSION, project);
	}
	
	
	public IModule[] getModules() {
		ArrayList list = new ArrayList();
		for( int i = 0; i < moduleContributors.length; i++ ) {
			list.addAll(Arrays.asList(moduleContributors[i].getModules()));
		}
		return (IModule[]) list.toArray(new IModule[list.size()]);
	}
	
	public ModuleDelegate getModuleDelegate(IModule module) {
		for( int i = 0; i < moduleContributors.length; i++ ) {
			if( moduleContributors[i].containsModule(module)) 
				return moduleContributors[i].getModuleDelegate(module);
		}
		return null;
	}
	
	public static interface IModuleContributor {
		public IModule[] getModules();
		public boolean containsModule(IModule module);
		public PackagedModuleDelegate getModuleDelegate(IModule module);
	}
	
	public static interface IExtendedModuleResource extends IModuleResource {
		public IPath getSourcePath();
		public IArchiveNode getNode();
		public IPath getDeepDestination();
		public IPath getConcreteDestFile();
	}

	public static class DelegateInitVisitor implements IArchiveNodeVisitor {

		private IArchive pack;
		private HashMap members;  // node -> imoduleresource
		private HashMap pathToNode; // path -> node
		public DelegateInitVisitor(IArchive pack) {
			this.pack = pack;
			reset();
		}
		public void reset() {
			members = new HashMap();
			pathToNode = new HashMap();
		}
		public boolean visit(IArchiveNode node) {
			int type = node.getNodeType();
			if( type == IArchiveNode.TYPE_ARCHIVE && ((IArchive)node).isTopLevel()) {
				IPath rel = ((IArchive)node).getRootArchiveRelativePath();
				members.put(node, new ArchiveContainerResource(((IArchive)node).getName(), node, rel));
				pathToNode.put(rel, node);
			} else if( type == IArchiveNode.TYPE_ARCHIVE || type == IArchiveNode.TYPE_ARCHIVE_FOLDER) {
				String name = type == IArchiveNode.TYPE_ARCHIVE ? ((IArchive)node).getName() : ((IArchiveFolder)node).getName();
				// if we're any other archive or a folder, create us and add to parent
				IArchiveNode parent = node.getParent();
				ArchiveContainerResource parentAsResource = (ArchiveContainerResource)members.get(parent);
				IPath rel = node.getRootArchiveRelativePath();
				members.put(node, new ArchiveContainerResource(name, node, rel));					
				pathToNode.put(rel, node);
				parentAsResource.addChild((IModuleResource)members.get(node));
			} else if( type == IArchiveNode.TYPE_ARCHIVE_FILESET ) {
				ArchiveContainerResource parentAsResource = (ArchiveContainerResource)members.get(node.getParent());
				parentAsResource.addFilesetAsChild((IArchiveFileSet)node);
			}

			return true;
		}
		
		public IModuleResource getRootResource() {
			return (IModuleResource)members.get(pack);
		}
		
		public IModuleResource getResourceForNode(IArchiveNode node) {
			return (IModuleResource)members.get(node);
		}
	}
	
	public static class ArchiveContainerResource implements IModuleFolder, IExtendedModuleResource {

		protected IPath moduleRelativePath;
		protected IArchiveNode node;
		protected String name;
		private HashMap members;
				
		// represents source folder on disk. only used if node is fileset
		private IPath folderGlobalPath = null;
		public ArchiveContainerResource(String name,IArchiveNode node,IPath moduleRelativePath ) {
			this.name = name;
			this.node = node;
			this.moduleRelativePath = moduleRelativePath;
			members = new HashMap();
			if( node instanceof IArchiveFileSet) {
				IPath tmp = moduleRelativePath.removeFirstSegments(node.getParent().getRootArchiveRelativePath().segmentCount());
				folderGlobalPath = ((IArchiveFileSet)node).getGlobalSourcePath().append(tmp);
			}
		}
		
		public int hashCode() {
			return name.hashCode() * 37 + moduleRelativePath.hashCode();
		}

		public boolean equals(Object other) {
			if (other instanceof IModuleFolder) {
				IModuleFolder resource = (IModuleFolder) other;
				return resource.getModuleRelativePath().equals(getModuleRelativePath());
			}
			return false;
		}

		public void addChild(IModuleResource resource) {
			members.put(resource.getModuleRelativePath(), resource);
		}
		
		public void removeChild(IPath moduleRelativePath) {
			members.remove(moduleRelativePath);
		}
		public IModuleResource getChild(IPath path) {
			return (IModuleResource)members.get(path);
		}
		
		public void addFilesetAsChild(IArchiveFileSet fs) {
			IPath[] paths = fs.findMatchingPaths(); // file-system based source paths
			IPath globalSource = fs.getGlobalSourcePath();
			for( int i = 0; i < paths.length; i++ ) {
				addFilesetPathAsChild(fs, globalSource, paths[i]);
			}
		}
		
		public void addFilesetPathAsChild(IArchiveFileSet fs, IPath globalSource, IPath path) {
			IPath archiveRelative = fs.getRootArchiveRelativePath(path);
			IPath fsRelative = path.removeFirstSegments(globalSource.segmentCount());
			ArchiveContainerResource parent = find(fs, globalSource, fsRelative.removeLastSegments(1), true);
			ExtendedModuleFile emf = new ExtendedModuleFile(archiveRelative.lastSegment(), archiveRelative, path.toFile().lastModified(), path, fs);
			parent.addChild(emf);
		}
		
		public void removeFilesetPathAsChild(IArchiveFileSet fs, IPath path) {
			IPath globalSource = fs.getGlobalSourcePath();
			IPath fsRelative = path.removeFirstSegments(globalSource.segmentCount());
			ArchiveContainerResource parent = find(fs, globalSource, fsRelative.removeLastSegments(1), false);
			if( parent != null ) 
				parent.removeFilesetPathAsChild(fs, path);
		}
		
		protected ArchiveContainerResource find(IArchiveFileSet fs, IPath globalSource, IPath fsRelative, boolean create) {
			ArchiveContainerResource resource = this;
			ArchiveContainerResource tmpResource;
			IPath tmpPath = fs.getRootArchiveRelativePath();
			int count = fsRelative.segmentCount();
			for( int i = 0; i < count; i++ ) {
				tmpPath = tmpPath.append(fsRelative.segment(i));
				tmpResource = (ArchiveContainerResource)resource.getChild(tmpPath);
				if( tmpResource == null ) {
					if( !create )
						return null;
					tmpResource = new ArchiveContainerResource(tmpPath.lastSegment(), fs, tmpPath);
					resource.addChild(tmpResource);
				}
				resource = tmpResource;
			}
			return resource;
		}
		
		public IModuleResource[] members() {
			Collection c = members.values();
			return (IModuleResource[]) c.toArray(new IModuleResource[c.size()]);
		}

		public IPath getModuleRelativePath() {
			return moduleRelativePath;
		}

		public String getName() {
			return name;
		}

		public Object getAdapter(Class adapter) {
			return null;
		}

		public IPath getDeepDestination() {
			IPath tmp = node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FILESET ?  
					((IArchiveFileSet)node).getRootArchiveRelativePath(folderGlobalPath) : node.getRootArchiveRelativePath();
			return node.getRootArchive().getGlobalDestinationPath().append(tmp);
		}
		public IPath getConcreteDestFile() {
			return ModelUtil.getBaseDestinationFile(node,folderGlobalPath);
		}
		public IArchiveNode getNode() {
			return node;
		}

		public IPath getSourcePath() {
			return null;
		}
		
	}

	public static class ExtendedModuleFile extends ModuleFile implements IExtendedModuleResource {
		private IPath srcPath;
		private IArchiveFileSet node;
		public ExtendedModuleFile(String name, IPath relativePath, long stamp, 
				IPath srcPath, IArchiveFileSet fs) {
			super(name, relativePath, stamp);
			this.srcPath = srcPath;
			this.node = fs;
		}
		public int hashCode() {
			return name.hashCode() * 37 + path.hashCode();
		}
		
		public IPath getPath() { return srcPath; }
		public IArchiveNode getNode() { return node; }
		public IPath getDeepDestination() {
			return node.getRootArchive().getGlobalDestinationPath().append(node.getRootArchiveRelativePath(path));
		}
		public IPath getSourcePath() {
			return this.srcPath;
		}

		public IPath getConcreteDestFile() {
			return ModelUtil.getBaseDestinationFile(node, srcPath);
		}

		public boolean equals(Object other) {
			if (other instanceof IModuleFile) {
				IModuleFile resource = (IModuleFile) other;
				return resource.getModuleRelativePath().equals(getModuleRelativePath());
			}
			return false;
		}
		
	}
	
	public static class PackagedModuleDelegate extends ModuleDelegate {
		private IArchive pack;
		private IModuleResource rootResource;
		private DelegateInitVisitor initVisitor;
		public PackagedModuleDelegate(IArchive pack) {
			this.pack = pack;
			initVisitor = new DelegateInitVisitor(pack);
		}
		
		public IArchive getPackage() {
			return pack;
		}
		public IModule[] getChildModules() {
			return new IModule[0];
		}
		
		protected void init() {
			initVisitor.reset();
			pack.accept(initVisitor);
			rootResource = initVisitor.getRootResource();
		}

		public IModuleResource[] members() throws CoreException {
			init();
			return new IModuleResource[] { rootResource };
		}
		
		public IStatus validate() {
			return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
					0, "", null);
		}
	}

}
