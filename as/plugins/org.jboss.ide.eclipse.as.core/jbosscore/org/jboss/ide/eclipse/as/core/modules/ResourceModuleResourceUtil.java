/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.modules;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.eclipse.wst.server.core.util.ModuleFolder;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

public class ResourceModuleResourceUtil {
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
			JBossServerCorePlugin.log(ce.getStatus());
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
	

}
