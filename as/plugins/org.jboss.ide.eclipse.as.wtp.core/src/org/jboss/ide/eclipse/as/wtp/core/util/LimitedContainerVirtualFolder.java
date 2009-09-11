package org.jboss.ide.eclipse.as.wtp.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualFile;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualResource;

public class LimitedContainerVirtualFolder extends VirtualFolder {

	private IContainer[] containers;
	public LimitedContainerVirtualFolder(
			IProject aComponentProject,
			IPath aRuntimePath, 
			IContainer[] containers) {
		super(aComponentProject, aRuntimePath);
		this.containers = containers;
	}

	public IContainer getUnderlyingFolder() { 
		if( containers != null && containers.length > 0 
				&& containers[0] != null )
			return containers[0];
		return null;
	}
	
	public IContainer[] getUnderlyingFolders() {
		return containers == null ? new IContainer[]{} : containers;
	}
	
	public IVirtualResource[] members(int memberFlags) throws CoreException {
		List<IVirtualResource> virtualResources = new ArrayList<IVirtualResource>(); // result
		Set allNames = new HashSet();
		
		for( int i = 0; i < containers.length; i++ ) {
			IResource realResource = containers[i];
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
						} else {
							virtualResources.add(ComponentCore.createFolder(getProject(), newRuntimePath));
						}
					}
				}
			}
			// An IResource.FILE would be an error condition (as this is a container)
		}
		return virtualResources.toArray(new IVirtualResource[virtualResources.size()]);
	}

	
}
