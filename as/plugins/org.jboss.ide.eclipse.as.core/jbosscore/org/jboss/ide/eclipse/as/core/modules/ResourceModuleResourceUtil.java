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

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.eclipse.wst.server.core.util.ModuleFolder;

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
		return new ModuleFile(global.toFile(),
				global.lastSegment(), path);
	}
	
	public static IModuleResource createFolder(IContainer cont, IPath path) {
		ModuleFolder folder = new ModuleFolder(cont, cont.getName(), path);
		IModuleResource[] childrenResources = createChildrenResources(cont, path.append(cont.getName()));
		folder.setMembers(childrenResources);
		return folder;
	}
	
	public static IModuleResource[] createChildrenResources(IContainer parent, IPath forcedParentPath) {
		ArrayList<IModuleResource> modChildren = new ArrayList<IModuleResource>();
		IResource[] children = new IResource[]{};
		try {
			children = parent.members();
		} catch(CoreException ce) {
		}
		for( int i = 0; i < children.length; i++ ) {
			modChildren.add(createResource(children[i], forcedParentPath));
		}
		return modChildren.toArray(new IModuleResource[modChildren.size()]);
	}
}
