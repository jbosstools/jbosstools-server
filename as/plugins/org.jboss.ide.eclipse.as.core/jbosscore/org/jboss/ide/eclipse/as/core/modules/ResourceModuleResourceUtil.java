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
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.eclipse.wst.server.core.util.ModuleFolder;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.patterns.ModuleDirectoryScannerPathFilter;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

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


	
	/**
	 * Utility method for just quickly discovering the filtered member list
	 * @since 2.3
	 */
	public static IModuleResource[] getFilteredMembers(IModule module, String inc, String exc) throws CoreException {
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, null);
		ModuleDirectoryScannerPathFilter filter = new ModuleDirectoryScannerPathFilter(md.members(), inc, exc);
		return filter.getFilteredMembers();
	}
	
	/**
	 * Get a proper includes / excludes filter for this project if it exists
	 * or null
	 * @since 2.3
	 */
	public static IModulePathFilter findDefaultModuleFilter(IModule module) {
		if( ServerModelUtilities.isBinaryModule(module) )
			return null;
		String[] incExc = getProjectIncludesExcludes(module);
		String inclusionPatterns = incExc[0];
		String exclusionPatterns = incExc[1];
		if (exclusionPatterns == null 
		    && (inclusionPatterns == null || ALL_RESOURCES_PATTERN.equals(inclusionPatterns))) {
		  //No filtering necessary, everything is included. That way we avoid unnecessary scans
		  return null;
		}
		try {
			ModuleDirectoryScannerPathFilter filter = 
					new ModuleDirectoryScannerPathFilter(getMembers(module), 
					    inclusionPatterns, exclusionPatterns);
			return filter;
		} catch( CoreException ce ) {
			JBossServerCorePlugin.getDefault().getLog().log(ce.getStatus());
		}
		return null;
	}
	
	private static IModuleResource[] getMembers(IModule module) throws CoreException {
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
		return md == null ? new IModuleResource[0] : md.members();
	}
	
	
	/**
	 * @since 2.3
	 */
	public static final String COMPONENT_INCLUSIONS_PATTERN = "component.inclusion.patterns"; //$NON-NLS-1$
	/**
	 * @since 2.3
	 */
	public static final String COMPONENT_EXCLUSIONS_PATTERN = "component.exclusion.patterns"; //$NON-NLS-1$
	
	private static final String ALL_RESOURCES_PATTERN = "**"; //$NON-NLS-1$
	/**
	 * Does this project have the proper settings that call for 
	 * include and exclude patterns in the virtual component metadata?
	 * 
	 * Return the includes / excludes pattern if yes.
	 * If no, return a two-length array of null objects
	 * 
	 * @param module
	 * @return
	 */
	private static String[] getProjectIncludesExcludes(IModule module) {
		IProject p = module.getProject();
		if( p != null ) {
			IVirtualComponent vc = ComponentCore.createComponent(p);
			if( vc != null ) {
				Properties props = vc.getMetaProperties();
				String exclusionPatterns = getPatternValue(props, COMPONENT_EXCLUSIONS_PATTERN);
				String inclusionPatterns = getPatternValue(props, COMPONENT_INCLUSIONS_PATTERN);
				return new String[]{  inclusionPatterns, exclusionPatterns }; 
			}
		}
		return new String[]{null, null};
	}

	/**
	 * Returns the trimmed property value if it exists and is not empty, or null otherwise
	 */
  private static String getPatternValue(Properties props,  String propertyName) {
    String value = props.getProperty(propertyName);
    if (value != null) {
      value = value.trim();
      if (value.length() == 0) {
        value = null;
      }
    }
    return value;
  }
}
