package org.jboss.ide.eclipse.as.wtp.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualFile;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
import org.eclipse.wst.common.componentcore.resources.IVirtualResource;

// TODO THis class is fucked and incorrectly works. 
// The first instance (top) treats 'resources' as folders to search
// Others try to treat it as items that are inside.  This is bad. 
public class ResourceListVirtualFolder extends VirtualFolder {

	private ArrayList<IResource> children;
	private ArrayList<IContainer> underlying;
	public ResourceListVirtualFolder(
			IProject aComponentProject,
			IPath aRuntimePath) {
		super(aComponentProject, aRuntimePath);
		this.children = new ArrayList<IResource>();
		this.underlying = new ArrayList<IContainer>();
	}

	public ResourceListVirtualFolder(
			IProject aComponentProject,
			IPath aRuntimePath, IContainer[] underlyingContainers) {
		this(aComponentProject, aRuntimePath);
		this.underlying.addAll(Arrays.asList(underlyingContainers));
	}

	public ResourceListVirtualFolder(
			IProject aComponentProject,
			IPath aRuntimePath, IContainer[] underlyingContainers, 
			IResource[] looseResources) {
		this(aComponentProject, aRuntimePath, underlyingContainers);
		this.children.addAll(Arrays.asList(looseResources));
	}

	protected void addUnderlyingResource(IResource resource) {
		if( underlying instanceof IContainer ) { 
			underlying.add((IContainer)resource);
			try {
				IResource[] newChildren = ((IContainer)resource).members();
				for( int i = 0; i < newChildren.length; i++ ) {
					children.add(newChildren[i]);
				}
			} catch( CoreException ce) {
				// TODO log
			}
		}
	}

	protected void addUnderlyingResource(IResource[] resources) {
		for( int i = 0; i < resources.length; i++ ) {
			addUnderlyingResource(resources[i]);
		}
	}
	
	protected void addChild(IResource resource) {
		this.children.add(resource);
	}

	protected void addChildren(IResource[] resources) {
		this.children.addAll(Arrays.asList(resources));
	}
	
	
	public IResource getUnderlyingResource() {
		return getUnderlyingFolder();
	}
	
	public IResource[] getUnderlyingResources() {
		return getUnderlyingFolders();
	}

	public IContainer getUnderlyingFolder() { 
		return underlying.size() > 0 ? underlying.get(0) : null;
	}
	
	public IContainer[] getUnderlyingFolders() {
		return (IContainer[]) underlying.toArray(new IContainer[underlying.size()]);
	}
		

	public IVirtualResource[] members(int memberFlags) throws CoreException {
		HashMap<String, IVirtualResource> virtualResources = new HashMap<String, IVirtualResource>(); // result
		IResource[] resources = (IResource[]) this.children.toArray(new IResource[this.children.size()]);
		for( int i = 0; i < resources.length; i++ ) {
			handleResource(resources[i], virtualResources, memberFlags);
		}
		Collection c = virtualResources.values();
		return (IVirtualResource[]) c.toArray(new IVirtualResource[c.size()]);
	}

	protected void handleResource(IResource resource, HashMap<String, IVirtualResource> map, int memberFlags) throws CoreException {
		if( resource instanceof IFile ) {
			if( !map.containsKey(resource.getName()) ) {
				IVirtualFile virtFile = new VirtualFile(getProject(), 
						getRuntimePath().append(((IFile)resource).getName()), (IFile)resource);
				map.put(resource.getName(), virtFile);
				return;
			} 
		}// end file
		else if( resource instanceof IContainer ) {
			IContainer realContainer = (IContainer) resource;
			IResource[] realChildResources = realContainer.members(memberFlags);
			IVirtualResource previousValue = map.get(resource.getName());
			if( previousValue != null && previousValue instanceof ResourceListVirtualFolder ) {
				((ResourceListVirtualFolder)previousValue).addUnderlyingResource(realContainer);
			} else if( previousValue == null ) {
				ResourceListVirtualFolder childFolder = 
					new ResourceListVirtualFolder(getProject(), getRuntimePath().append(resource.getName()));
				childFolder.addUnderlyingResource(realContainer);
				map.put(resource.getName(), childFolder);
			}
		} // end container
	}
}
