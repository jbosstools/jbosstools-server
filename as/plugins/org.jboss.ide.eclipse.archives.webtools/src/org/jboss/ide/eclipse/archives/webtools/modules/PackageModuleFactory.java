/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.webtools.modules;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.eclipse.wst.server.core.util.ModuleFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeVisitor;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PackageModuleFactory extends ModuleFactoryDelegate {

	public static final String FACTORY_TYPE_ID = "org.jboss.ide.eclipse.as.core.PackageModuleFactory";//$NON-NLS-1$
	public static final String MODULE_TYPE = "jboss.package";//$NON-NLS-1$
	public static final String VERSION = "1.0";//$NON-NLS-1$

	public static final String MODULE_ID_PROPERTY_KEY = "org.jboss.ide.eclipse.as.core.packages.ModuleIDPropertyKey";//$NON-NLS-1$

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
	protected static String getStamp(IArchive pack) {
		return getStamp(pack, false);
	}

	/**
	 * Get the module ID of this IArchive if it exists.
	 * If create is true, and the ID is not set, set a default ID.
	 * @param pack
	 * @param create
	 * @return
	 */
	protected static String getStamp(IArchive pack, boolean create) {
		String propVal = pack.getProperty(MODULE_ID_PROPERTY_KEY);
		if( propVal == null && create ) {
			propVal = "" + new Date().getTime();//$NON-NLS-1$
		}
		return propVal;
	}

	protected static String getId(IArchive pack) {
		IPath p = pack.getModelRootNode().getDescriptor().append(getStamp(pack));
		return p.toString();
	}

	public IModule findModule(String id) {
		IModule m = super.findModule(id);
		IModule[] allModules = getModules();
		for( int i = 0; i < allModules.length; i++ ) {
			Path p = new Path( allModules[i].getId() );
			if( p.lastSegment().equals(id))
				return allModules[i];
		}
		return m;
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
		return getProjectName(pack) + "/" + pack.getName();//$NON-NLS-1$
	}



	protected final ArchivesModelModuleContributor moduleContributor
			= new ArchivesModelModuleContributor(this);
	public PackageModuleFactory() {
		super();
	}
	
	public void refreshProject(IPath projectLoc) {
		moduleContributor.refreshProject(projectLoc);
	}

	protected IModule createModule2(IArchive pack, IProject project) {
		return createModule(getId(pack), getName(pack), MODULE_TYPE, VERSION, project);
	}

	public IModule[] getModules(IProject project) {
		moduleContributor.refreshProject(project.getLocation());
		return super.getModules(project);
	}
	public IModule[] getModules() {
		return moduleContributor.getModules();
	}

	public ModuleDelegate getModuleDelegate(IModule module) {
		return moduleContributor.getModuleDelegate(module);
	}

	public static class DelegateInitVisitor implements IArchiveNodeVisitor {

		private IArchive pack;
		private HashMap<IArchiveNode, IModuleResource> members;  // node -> imoduleresource
		private HashMap<IPath, IArchiveNode> pathToNode; // path -> node
		public DelegateInitVisitor(IArchive pack) {
			this.pack = pack;
			reset();
		}
		public void reset() {
			members = new HashMap<IArchiveNode, IModuleResource>();
			pathToNode = new HashMap<IPath, IArchiveNode>();
		}
		
		protected boolean addZippedPackage(IArchiveNode node) {
			IPath pathWithName = node.getRootArchiveRelativePath().removeFirstSegments(1);
			IPath path = pathWithName;
			if( path.segmentCount() > 0 )
				path = path.removeLastSegments(1);
			File root = ModelUtil.getBaseDestinationFile(node).toFile();
			IModuleFile mf = new ModuleFile(root, root.getName(), path);
			members.put(node, mf);
			pathToNode.put(pathWithName, node);
			if( node.getRootArchive() != node ) 
				((PackagesModuleFolder)members.get(node.getParent())).addChild(mf);
			return false;
		}
		
		protected boolean addFolder(IArchiveNode node) {
			IPath pathWithName = node.getRootArchiveRelativePath().removeFirstSegments(1);
			IPath path = pathWithName;
			if( path.segmentCount() > 0 )
				path = path.removeLastSegments(1);
			File root = ModelUtil.getBaseDestinationFile(node).toFile();
			PackagesModuleFolder mf = new PackagesModuleFolder(root, root.getName(), path);
			members.put(node, mf);
			pathToNode.put(pathWithName, node);
			if( node.getRootArchive() != node ) 
				((PackagesModuleFolder)members.get(node.getParent())).addChild(mf);
			return true;
		}
		
		public boolean addFilesetAsChild(IArchiveFileSet fs) {
			FileWrapper[] files = fs.findMatchingPaths(); // file-system based source paths
			if( files != null ) {
				for( int i = 0; i < files.length; i++ ) {
					addFilesetPathAsChild(fs, files[i]);
				}
			}
			return true;
		}

		public void addFilesetPathAsChild(IArchiveFileSet fs, FileWrapper file) {
			IPath fsRelative = new Path(file.getFilesetRelative());
			IPath path = fs.getRootArchiveRelativePath().removeFirstSegments(1).append(fsRelative);
			PackagesModuleFolder parent = find(fs, fsRelative.removeLastSegments(1), true);
			IModuleFile mf = new ModuleFile(file, file.getName(), path.removeLastSegments(1));
			parent.addChild(mf);
		}

		protected PackagesModuleFolder find(IArchiveFileSet fs, IPath fsRelative, boolean create) {
			PackagesModuleFolder folder = (PackagesModuleFolder)members.get(fs.getParent());
			IPath folderPathWithName = folder.getModuleRelativePath().append(folder.getName());
			
			PackagesModuleFolder tmpFolder;
			IPath tmpPath = fs.getRootArchiveRelativePath().removeFirstSegments(1);
			int count = fsRelative.segmentCount();
			for( int i = 0; i < count; i++ ) {
				tmpPath = tmpPath.append(fsRelative.segment(i));
				tmpFolder = (PackagesModuleFolder)folder.getChild(fsRelative.segment(i));
				if( tmpFolder == null ) {
					if( !create )
						return null;
					tmpFolder = new PackagesModuleFolder(null, tmpPath.lastSegment(), tmpPath.removeLastSegments(1));
					folder.addChild(tmpFolder);
				}
				folder = tmpFolder;
			}
			return folder;
		}
		
		public boolean visit(IArchiveNode node) {
			try { 
				if( node == pack ) // we're at the root
					if( !pack.isExploded())
						return addZippedPackage(pack);
				
				int type = node.getNodeType();
				if( type == IArchiveNode.TYPE_ARCHIVE && !((IArchive)node).isExploded())
					return addZippedPackage(node);
	
				if( type == IArchiveNode.TYPE_ARCHIVE || type == IArchiveNode.TYPE_ARCHIVE_FOLDER) 
					return addFolder(node);
				
				if( type == IArchiveNode.TYPE_ARCHIVE_FILESET )
					return addFilesetAsChild((IArchiveFileSet)node);
			} catch( Exception e) {
				e.printStackTrace();
			}
			return true;
		}

		public IModuleResource getRootResource() {
			return members.get(pack);
		}

		public IModuleResource getResourceForNode(IArchiveNode node) {
			return members.get(node);
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
			if( !pack.isExploded() )
				return new IModuleResource[] { rootResource };
			return ((IModuleFolder)rootResource).members();
		}

		public IStatus validate() {
			return Status.OK_STATUS;
		}
	}
	
	public static class PackagesModuleFolder extends ModuleFolder {
		private File file;
		private HashMap<String, IModuleResource> children = new HashMap<String, IModuleResource>();
		public PackagesModuleFolder(File f, String name, IPath path) {
			super(null, name, path);
			this.file = f;
		}
		public Object getAdapter(Class cl) {
			if( File.class.equals(cl))
				return file;
			return null;
		}
		
		public void addChild(IModuleResource resource) {
			children.put(resource.getName(), resource);
			Collection<IModuleResource> l = children.values();
			setMembers((IModuleResource[]) l.toArray(new IModuleResource[l.size()]));
		}
		
		public PackagesModuleFolder getChild(String name) {
			Object o = children.get(name);
			if( o != null && o instanceof PackagesModuleFolder)
				return (PackagesModuleFolder)o;
			return null;
		}
	}
}
