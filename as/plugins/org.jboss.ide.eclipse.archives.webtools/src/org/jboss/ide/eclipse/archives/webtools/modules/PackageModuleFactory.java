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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

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
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeVisitor;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;
import org.jboss.ide.eclipse.archives.webtools.IntegrationPlugin;

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

	public static interface IExtendedModuleResource extends IModuleResource {
		public IPath getSourcePath();
		public IArchiveNode getNode();
		// deep destination is the full path this resource represents, even if it's inside a jar
		public IPath getDeepDestination();
		// the concrete file this resource is part of... so the top most zipped jar or, if all is exploded, the file itself
		public IPath getConcreteDestFile();
	}

	public static class DelegateInitVisitor implements IArchiveNodeVisitor {

		private IArchive pack;
		private HashMap<IArchiveNode, ArchiveContainerResource> members;  // node -> imoduleresource
		private HashMap<IPath, IArchiveNode> pathToNode; // path -> node
		public DelegateInitVisitor(IArchive pack) {
			this.pack = pack;
			reset();
		}
		public void reset() {
			members = new HashMap<IArchiveNode, ArchiveContainerResource>();
			pathToNode = new HashMap<IPath, IArchiveNode>();
		}
		public boolean visit(IArchiveNode node) {
			int type = node.getNodeType();
			// not sure why this is giving wrong values but it seems to go deep into IArchive code
			IPath rootRelative = node.getRootArchiveRelativePath().removeFirstSegments(1);
			if( type == IArchiveNode.TYPE_ARCHIVE && ((IArchive)node).isTopLevel()) {
				members.put(node, new ArchiveContainerResource(((IArchive)node).getName(), node, rootRelative));
				pathToNode.put(rootRelative, node);
			} else if( type == IArchiveNode.TYPE_ARCHIVE || type == IArchiveNode.TYPE_ARCHIVE_FOLDER) {
				String name = type == IArchiveNode.TYPE_ARCHIVE ? ((IArchive)node).getName() : ((IArchiveFolder)node).getName();
				// if we're any other archive or a folder, create us and add to parent
				IArchiveNode parent = node.getParent();
				ArchiveContainerResource parentAsResource = members.get(parent);
				members.put(node, new ArchiveContainerResource(name, node, rootRelative));
				pathToNode.put(rootRelative, node);
				parentAsResource.addChild(members.get(node));
			} else if( type == IArchiveNode.TYPE_ARCHIVE_FILESET ) {
				ArchiveContainerResource parentAsResource = members.get(node.getParent());
				parentAsResource.addFilesetAsChild((IArchiveFileSet)node);
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

	public static class ArchiveContainerResource implements IModuleFolder, IExtendedModuleResource {

		protected IPath moduleRelativePath;
		protected IPath fsRelative;
		protected IArchiveNode node;
		protected String name;
		private HashMap<IPath, IModuleResource> members;

		// represents source folder on disk. only used if node is fileset
//		private IPath folderGlobalPath = null;
		public ArchiveContainerResource(String name,IArchiveNode node,IPath moduleRelativePath ) {
			this.name = name;
			this.node = node;
			this.moduleRelativePath = moduleRelativePath;
			members = new HashMap<IPath, IModuleResource>();
			if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FILESET ) {
				fsRelative = moduleRelativePath.removeFirstSegments(node.getParent().getRootArchiveRelativePath().removeFirstSegments(1).segmentCount());
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
			return members.get(path);
		}

		public void addFilesetAsChild(IArchiveFileSet fs) {
			FileWrapper[] files = fs.findMatchingPaths(); // file-system based source paths
			if( files != null ) {
				for( int i = 0; i < files.length; i++ ) {
					addFilesetPathAsChild(fs, files[i]);
				}
			}
		}

		public void addFilesetPathAsChild(IArchiveFileSet fs, FileWrapper file) {
			IPath fsRelative = new Path(file.getFilesetRelative());
			ArchiveContainerResource parent = find(fs, fsRelative.removeLastSegments(1), true);
			ExtendedModuleFile emf = new ExtendedModuleFile(file, fs);
			parent.addChild(emf);
		}

		protected ArchiveContainerResource find(IArchiveFileSet fs, IPath fsRelative, boolean create) {
			ArchiveContainerResource resource = this;
			ArchiveContainerResource tmpResource;
			IPath tmpPath = fs.getRootArchiveRelativePath().removeFirstSegments(1);
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
			Collection<IModuleResource> c = members.values();
			return c.toArray(new IModuleResource[c.size()]);
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
			IPath tmp = node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FILESET
					?  moduleRelativePath : node.getRootArchiveRelativePath();
			return PathUtils.getGlobalLocation(node.getRootArchive()).append(tmp);
		}

		public IPath getConcreteDestFile() {
			if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FILESET )
				return ModelUtil.getBaseDestinationFile((IArchiveFileSet)node,fsRelative);
			else
				return ModelUtil.getBaseDestinationFile((IArchiveFileSet)node);
		}
		public IArchiveNode getNode() {
			return node;
		}

		public IPath getSourcePath() {
			return null;
		}

	}

	public static class ExtendedModuleFile extends ModuleFile implements IExtendedModuleResource {
		private FileWrapper wrapper;
		private IArchiveFileSet node;
		public ExtendedModuleFile(FileWrapper wrapper, IArchiveFileSet fs) {
			super(wrapper.getOutputName(), wrapper.getRootArchiveRelative(), wrapper.lastModified());
			this.node = fs;
			this.wrapper = wrapper;
		}
		public int hashCode() {
			return getName().hashCode() * 37 + getPath().hashCode();
		}

		public IPath getPath() { return new Path(wrapper.getAbsolutePath()); }
		public IArchiveNode getNode() { return node; }
		public IPath getDeepDestination() {
			return PathUtils.getGlobalLocation(node.getRootArchive()).append(wrapper.getRootArchiveRelative());
		}
		public IPath getSourcePath() {
			return new Path(this.wrapper.getAbsolutePath());
		}

		public IPath getConcreteDestFile() {
			return ModelUtil.getBaseDestinationFile(node, new Path(this.wrapper.getFilesetRelative()));
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
			return ((IModuleFolder)rootResource).members();
		}

		public IStatus validate() {
			return Status.OK_STATUS;
		}
	}

}
