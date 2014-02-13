/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.publishers;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.xpl.ModulePackager;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.tools.as.core.internal.modules.ModuleDeploymentPrefsUtil;

public class PublishUtil extends ModuleResourceUtil {

	public static boolean DEEP = true;
	public static boolean ROOT = false;
	
	
	/**
	 * Given a folder to deploy this module, append the various
	 * subparts of this module[] into the name. For example, if we
	 * know that this specific module should be published to 
	 *  /home/user/custom/path, then append the module names to yield
	 *  a return value of /home/user/custom/path/some.ear/some.war
	 * 
	 * @param moduleTree
	 * @param deployFolder
	 * @param server
	 * @return
	 */
	public static IPath getModuleNestedDeployPath(IModule[] moduleTree, String rootFolder, IDeployableServer server) {
		return new ModuleDeploymentPrefsUtil().getModuleNestedDeployPath(moduleTree, rootFolder, server.getServer());
	}
	
	public static String getSuffix(String type) {
		return new ModuleDeploymentPrefsUtil().getDefaultSuffix(type);
	}
	
	
	public static boolean deployPackaged(IModule[] moduleTree) {
		String moduleTypeId = moduleTree[moduleTree.length-1].getModuleType().getId(); 
		if( moduleTypeId.equals(IWTPConstants.FACET_UTILITY)) {
			return true;
		} else if( moduleTypeId.equals(IWTPConstants.FACET_APP_CLIENT)) { 
			return true;
		} else if( moduleTypeId.equals(IWTPConstants.FACET_WEB_FRAGMENT)) {
			return true;
		} else if( moduleTypeId.equals(IWTPConstants.FACET_EJB) && moduleTree.length > 1) {
			 String parentModuleTypeId = moduleTree[moduleTree.length - 2].getModuleType().getId(); 
			 if( !parentModuleTypeId.equals(IWTPConstants.FACET_EAR)) {
				 return true; 
			 }
		}
		return false;
	}
	/*
	 * Just package into a jar raw.  Don't think about it, just do it
	 */
	public static IStatus[] packModuleIntoJar(IModule module, IPath destination)throws CoreException {
		ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, null);
		IModuleResource[] resources = pm.members();
		return packModuleIntoJar(module.getName(), resources, destination);
	}
	
	public static IStatus[] packModuleIntoJar(String moduleName, IModuleResource[] resources, IPath destination) throws CoreException {
		return packModuleIntoJar(moduleName, resources, destination, null);
	}
	
	/**
	 * @since 2.4
	 */
	public static IStatus[] packModuleIntoJar(String moduleName, IModuleResource[] resources, 
			IPath destination, IModulePathFilter filter) throws CoreException {
	
		String dest = destination.toOSString();
		ModulePackager packager = null;
		try {
			packager = new ModulePackager(dest, false);
			for (int i = 0; i < resources.length; i++) {
				if( filter == null || filter.shouldInclude(resources[i]))
					doPackModule(resources[i], packager, filter);
			}
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_ASSEMBLE_FAIL,
					"unable to assemble module " + moduleName, e); //$NON-NLS-1$
			return new IStatus[]{status};
		}
		finally{
			try{
				if( packager != null ) 
					packager.finished();
			}
			catch(IOException e){
				IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_ASSEMBLE_FAIL,
						"unable to assemble module "+ moduleName, e); //$NON-NLS-1$
				return new IStatus[]{status};
			}
		}
		return new IStatus[]{};
	}

	
	/* Add one file or folder to a jar */
	public static void doPackModule(IModuleResource resource, ModulePackager packager) throws CoreException, IOException{
		doPackModule(resource, packager, null);
	}
	
	/**
	 * @since 2.4
	 */
	public static void doPackModule(IModuleResource resource, ModulePackager packager, IModulePathFilter filter) throws CoreException, IOException{
		if (resource instanceof IModuleFolder) {
			IModuleFolder mFolder = (IModuleFolder)resource;
			IModuleResource[] resources = mFolder.members();

			packager.writeFolder(resource.getModuleRelativePath().append(resource.getName()).toPortableString());

			for (int i = 0; resources!= null && i < resources.length; i++) {
				doPackModule(resources[i], packager, filter);
			}
		} else {
			if( filter == null || filter.shouldInclude(resource)) {
				String destination = resource.getModuleRelativePath().append(resource.getName()).toPortableString();
				IFile file = (IFile) resource.getAdapter(IFile.class);
				if (file != null)
					packager.write(file, destination);
				else {
					File file2 = (File) resource.getAdapter(File.class);
					packager.write(file2, destination);
				}
			}
		}
	}
}
