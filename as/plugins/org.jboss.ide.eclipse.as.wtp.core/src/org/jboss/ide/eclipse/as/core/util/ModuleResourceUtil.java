package org.jboss.ide.eclipse.as.core.util;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.eclipse.wst.server.core.util.ModuleFolder;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

public class ModuleResourceUtil {
	public static int countChanges(IModuleResourceDelta[] deltas) {
		IModuleResource res;
		int count = 0;
		if( deltas == null ) return 0;
		for( int i = 0; i < deltas.length; i++ ) {
			res = deltas[i].getModuleResource();
			if( res != null && res instanceof IModuleFile)
				count++;
			count += countChanges(deltas[i].getAffectedChildren());
		}
		return count;
	}
	
	public static String getParentRelativeURI(IModule[] tree, int index, String defaultName) {
		if( index != 0 ) {
			IEnterpriseApplication parent = (IEnterpriseApplication)tree[index-1].loadAdapter(IEnterpriseApplication.class, null);
			if( parent != null ) {
				String uri = parent.getURI(tree[index]);
				if(uri != null )
					return uri;
			}
			// TODO if we make our own "enterprise app" interface, do that here
		} 
		// return name with extension
		return defaultName;

	}
	

	public static IModuleResource[] getResources(IModule module, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Fetching Module Resources", 100); //$NON-NLS-1$
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, 
				ProgressMonitorUtil.submon(monitor, 100));
		if( md == null ) {
			// Deleted Module, TODO handle this differently!
			return new IModuleResource[]{};
		}
		IModuleResource[] members = md.members();
		monitor.done();
		return members;
	}
	
	public static IModuleResource[] getResources(IModule[] tree) throws CoreException {
		return getResources(tree[tree.length-1], new NullProgressMonitor());
	}
	
	public static File getFile(IModuleResource resource) {
		File source = (File)resource.getAdapter(File.class);
		if( source == null ) {
			IFile ifile = (IFile)resource.getAdapter(IFile.class);
			if( ifile != null ) 
				source = ifile.getLocation().toFile();
		}
		return source;
	}

	public static java.io.File getFile(IModuleFile mf) {
		return (IFile)mf.getAdapter(IFile.class) != null ? 
					((IFile)mf.getAdapter(IFile.class)).getLocation().toFile() :
						(java.io.File)mf.getAdapter(java.io.File.class);
	}
	
	
	public static IModule[] combine(IModule[] module, IModule newMod) {
		IModule[] retval = new IModule[module.length + 1];
		for( int i = 0; i < module.length; i++ )
			retval[i]=module[i];
		retval[retval.length-1] = newMod;
		return retval;
	}	
	
	public static IModuleResource createResource(IResource resource) {
		return createResource(resource, new Path("/")); //$NON-NLS-1$
	}
	
	public static IModuleResource createResource(IResource resource, IPath path) {
		if( resource instanceof IFile ) {
			return createFile((IFile)resource, path);
		}
		IContainer cont = (IContainer)resource;
		return createFolder(cont, path);
	}
	
	public static IModuleResource createFile(IFile resource, IPath path) {
		IPath global = resource.getLocation();
		return new ModuleFile(global.toFile(), global.lastSegment(), path);
	}
	
	public static IModuleResource createFolder(IContainer cont, IPath path) {
		ModuleFolder folder = new ModuleFolder(cont, cont.getName(), path);
		IModuleResource[] childrenResources = createChildrenResources(cont, path.append(cont.getName()));
		folder.setMembers(childrenResources);
		return folder;
	}
	
	
	/**
	 * Get a list of module resources in this container. 
	 * If the container is closed or does not exist, return an empty array. 
	 * 
	 * @param parent The container to be searched
	 * @param forcedParentPath The parent's module-relative path
	 * @return A list of module resources
	 */
	public static IModuleResource[] createChildrenResources(IContainer parent, IPath forcedParentPath) {
		ArrayList<IModuleResource> modChildren = new ArrayList<IModuleResource>();
		IResource[] children = new IResource[]{};
		try {
			children = parent.members();
		} catch(CoreException ce) {
			ASWTPToolsPlugin.log(ce.getStatus());
		}
		for( int i = 0; i < children.length; i++ ) {
			modChildren.add(createResource(children[i], forcedParentPath));
		}
		return modChildren.toArray(new IModuleResource[modChildren.size()]);
	}
	
	public static IModuleResource[] addFileToModuleResources(IModule[] moduleTree, IPath prevPath,
			IModuleResource[] resources, IPath remainingPath, File childFile) {
		boolean found = false;
		String name = remainingPath.segment(0);
		remainingPath = remainingPath.removeFirstSegments(1);
		
		for( int i = 0; i < resources.length; i++ ) {
			if( resources[i].getName().equals(remainingPath.segment(0))) {
				found = true;
				if( resources[i] instanceof IModuleFile ) {
					resources[i] = new ModuleFile(childFile, name, prevPath);
					return resources;
				} else if( resources[i] instanceof IModuleFolder){
					IModuleFolder mf = (IModuleFolder) resources[i];
					IModuleResource[] mfChildren = mf.members();
					IModuleResource[] newChildren = addFileToModuleResources(moduleTree, 
							prevPath.append(name), mfChildren, remainingPath, childFile);
					((ModuleFolder)mf).setMembers(newChildren);
				}
			}
		}
		
		if( found )
			return resources;

		IModuleResource[] newResources = new IModuleResource[resources.length+1];
		System.arraycopy(resources, 0, newResources, 0, resources.length);
		if( remainingPath.segmentCount() == 0 ) {
			// add a file
			newResources[newResources.length-1] = new ModuleFile(childFile, name, prevPath);
		} else {
			// add a folder
			ModuleFolder mf = new ModuleFolder(null, name, prevPath);
			IModuleResource[] newChildren = addFileToModuleResources(moduleTree, 
					prevPath.append(name), new IModuleResource[]{}, remainingPath, childFile);
			((ModuleFolder)mf).setMembers(newChildren);
			newResources[newResources.length-1] = mf;
		}
		return newResources;
	}
	
	public static int countMembers(IModule module) {
		ModuleDelegate delegate = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
		IModuleResource[] resources;
		try {
			resources = delegate.members();
		} catch(CoreException ce) {
			resources = new IModuleResource[0];
		}
		return delegate == null ? 0 : countMembers(resources);
	}
	
	public static int countMembers(IModuleResource[] resources) {
		int count = 0;
		if( resources == null ) return 0;
		for( int i = 0; i < resources.length; i++ ) {
			if( resources[i] instanceof IModuleFile ) {
				count++;
			} else if( resources[i] instanceof IModuleFolder ) {
				count += countMembers(((IModuleFolder)resources[i]).members());
			}
		}
		return count;
	}

	public static IModuleResource[] getMembers(IModule module) throws CoreException {
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
		return md == null ? new IModuleResource[0] : md.members();
	}
	
}
