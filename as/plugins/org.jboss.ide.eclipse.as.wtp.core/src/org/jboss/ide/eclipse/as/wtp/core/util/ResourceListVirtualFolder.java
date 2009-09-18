package org.jboss.ide.eclipse.as.wtp.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualFile;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualResource;

public class ResourceListVirtualFolder extends VirtualFolder {

	private ArrayList<IResource> resources;
	public ResourceListVirtualFolder(
			IProject aComponentProject,
			IPath aRuntimePath, 
			IResource[] containers) {
		super(aComponentProject, aRuntimePath);
		this.resources = new ArrayList<IResource>();
		this.resources.addAll(Arrays.asList(containers));
	}

	protected void addResource(IResource c) {
		//this.resources.add(c);
	}
	
	public IResource getUnderlyingResource() {
		// Since I'm a container, pretend I'm a container I guess
		return getUnderlyingFolder();
	}
	
	public IResource[] getUnderlyingResources() {
		return getUnderlyingFolders();
	}

	public IContainer getUnderlyingFolder() { 
		IResource[] r = (IResource[]) resources.toArray(new IResource[resources.size()]);
		for( int i = 0; i < r.length; i++ )
			if( r[i] instanceof IContainer )
				return (IContainer)r[i];
		return null;
	}
	
	public IContainer[] getUnderlyingFolders() {
		IResource[] r = (IResource[]) resources.toArray(new IResource[resources.size()]);
		ArrayList<IContainer> c = new ArrayList<IContainer>();
		for( int i = 0; i < r.length; i++ )
			if( r[i] instanceof IContainer )
				c.add((IContainer)r[i]);
		return (IContainer[]) c.toArray(new IContainer[c.size()]);
	}
		
	public IVirtualResource[] members(int memberFlags) throws CoreException {
		List<IVirtualResource> virtualResources = new ArrayList<IVirtualResource>(); // result
		Set allNames = new HashSet();
		IResource[] containers2 = (IResource[]) this.resources.toArray(new IResource[this.resources.size()]);
		for( int i = 0; i < containers2.length; i++ ) {
			IResource realResource = containers2[i];
			if ((realResource != null) && (realResource.getType() == IResource.FOLDER || realResource.getType() == IResource.PROJECT)) {
				IContainer realContainer = (IContainer) realResource;
				IResource[] realChildResources = realContainer.members(memberFlags);
				for (int realResourceIndex = 0; realResourceIndex < realChildResources.length; realResourceIndex++) {
					IResource child = realChildResources[realResourceIndex];
					String localName = child.getName();
					if (allNames.add(localName)) {
						IPath newRuntimePath = getRuntimePath().append(localName);
						if (child instanceof IFile) {
							virtualResources.add(new VirtualFile(getProject(), newRuntimePath, (IFile) child));
						} else if( child instanceof IContainer ){
							IContainer childContainer = (IContainer)child;
							IResource[] members = childContainer.members();
							ResourceListVirtualFolder childFolder = 
								new ResourceListVirtualFolder(getProject(), newRuntimePath, members);
							virtualResources.add(childFolder);
						}
					}
				}
			} else if(realResource != null && realResource instanceof IFile) {
				// An IResource.FILE would be an error condition (as this is a container)
				virtualResources.add(new VirtualFile(getProject(), 
						getRuntimePath().append(((IFile)realResource).getName()), (IFile)realResource));				
			}
		}
		return virtualResources.toArray(new IVirtualResource[virtualResources.size()]);
	}

	
}
