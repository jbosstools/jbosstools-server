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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.xpl.ModulePackager;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentTypePrefs;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;

public class PublishUtil extends ModuleResourceUtil {
	/**
	 * All preferences are stored in the "local" setting as it was decided
	 * having to replicate deploy paths for each publish method was not good
	 * 
	 * This API should be renamed, as it actually gets a value stored at a property key
	 * 
	 * Practically, this method will find the deploy root folder or 
	 * the temp deploy root folder for a given module  by checking
	 * the override model
	 * 
	 * @param moduleTree the module 
	 * @param server the server
	 * @param defaultFolder the default folder if per-module settings DNE
	 * @param moduleProperty key representing folder or temp folder 
	 * @return
	 */
	/* This api should be renamed */
	private static String getDeployRootFolder(IModule[] moduleTree, 
			IDeployableServer server, String defaultFolder, String moduleProperty) {
		String folder = defaultFolder;
		// TODO bug 286699
		DeploymentPreferences prefs = DeploymentPreferenceLoader.loadPreferencesFromServer(server.getServer());
		DeploymentTypePrefs typePrefs = prefs.getOrCreatePreferences(
				IJBossToolingConstants.DEFAULT_DEPLOYMENT_METHOD_TYPE);
		DeploymentModulePrefs modPrefs = typePrefs.getModulePrefs(moduleTree[0]);
		if( modPrefs != null ) {
			String loc = modPrefs.getProperty(moduleProperty);
			if( loc != null && !loc.equals("") ) { //$NON-NLS-1$
				if( !new Path(loc).isAbsolute()) {
					IPath p = new Path(server.getDeployFolder());
					folder = p.append(loc).toOSString();
				} else {
					folder = loc;
				}
				// TODO translate for variables?
			}
		}
		return folder;
	}

	public static boolean DEEP = true;
	public static boolean ROOT = false;
	public static IPath getDeployPath(IJBossServerPublishMethod method, IModule[] moduleTree, IDeployableServer server, boolean deep) {
		String defaultFolder = method.getPublishDefaultRootFolder(server.getServer());
		String folder = PublishUtil.getDeployRootFolder(
				moduleTree, server, defaultFolder,
				IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC);
		if( deep == ROOT )
			return new Path(folder);
		return getModuleNestedDeployPath(moduleTree, folder, server);
	}

	public static IPath getTempDeployPath(IJBossServerPublishMethod method, IModule[] moduleTree, 
			IDeployableServer server, boolean deep) {
		String defaultFolder = method.getPublishDefaultRootTempFolder(server.getServer());
		String folder = PublishUtil.getDeployRootFolder(
				moduleTree, server, defaultFolder,
				IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC);
		if( deep == ROOT )
			return new Path(folder);
		return getModuleNestedDeployPath(moduleTree, folder, server);
	}

	
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
		IPath root = new Path( rootFolder );
		String type, modName, name, uri, suffixedName;
		for( int i = 0; i < moduleTree.length; i++ ) {	
			boolean found = false;
			if( i == 0 ) {
				// If this is the root module, we can customize the output name
				DeploymentPreferences prefs = DeploymentPreferenceLoader.loadPreferencesFromServer(server.getServer());
				DeploymentTypePrefs typePrefs = prefs.getOrCreatePreferences(LocalPublishMethod.LOCAL_PUBLISH_METHOD);
				DeploymentModulePrefs modPrefs = typePrefs.getModulePrefs(moduleTree[0]);
				if( modPrefs != null ) {
					String outName = modPrefs.getProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_OUTPUT_NAME);
					if( outName != null && !outName.equals("")) { //$NON-NLS-1$
						found = true;
						root = root.append(outName);
					}
				}
			} 
			
			// If it's a child module, or the property is not set,
			// we must respect the deployment model... 
			if( !found ) {
				type = moduleTree[i].getModuleType().getId();
				modName = moduleTree[i].getName();
				name = new Path(modName).lastSegment();
				suffixedName = name + getSuffix(type);
				uri = getParentRelativeURI(moduleTree, i, suffixedName);
				root = root.append(uri);
			}
		}
		return root;
	}
	
	public static String getSuffix(String type) {
		// TODO
		// VirtualReferenceUtilities.INSTANCE. has utility methods to help!!
		String suffix = null;
		if( IWTPConstants.FACET_EAR.equals(type)) 
			suffix = IWTPConstants.EXT_EAR;
		else if( IWTPConstants.FACET_WEB.equals(type) || IWTPConstants.FACET_STATIC_WEB.equals(type)) 
			suffix = IWTPConstants.EXT_WAR;
		else if( IWTPConstants.FACET_WEB_FRAGMENT.equals(type))
			suffix = IWTPConstants.EXT_JAR;
		else if( IWTPConstants.FACET_UTILITY.equals(type)) 
			suffix = IWTPConstants.EXT_JAR;
		else if( IWTPConstants.FACET_CONNECTOR.equals(type)) 
			suffix = IWTPConstants.EXT_RAR;
		else if( IWTPConstants.FACET_ESB.equals(type))
			suffix = IWTPConstants.EXT_ESB;
		else if( "jboss.package".equals(type)) //$NON-NLS-1$ 
			// no suffix required, name already has it
			suffix = ""; //$NON-NLS-1$
		else if( "jboss.singlefile".equals(type)) //$NON-NLS-1$
			suffix = ""; //$NON-NLS-1$
		else if( "jst.jboss.sar".equals(type)) //$NON-NLS-1$
			suffix = IWTPConstants.EXT_SAR;
		if( suffix == null )
			suffix = IWTPConstants.EXT_JAR;
		return suffix;
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

	/* Deprecated */
	@Deprecated
	public static IPath getLocalDeployPath(IModule[] moduleTree, IDeployableServer server) {
		return server.getDeploymentLocation(moduleTree, true);
	}

	@Deprecated
	public static IPath getLocalDeployRootFolder(IModule[] moduleTree, IDeployableServer server) {
		return server.getDeploymentLocation(moduleTree, false);
	}

	@Deprecated
	public static IPath getLocalTempDeployPath(IModule[] moduleTree, IDeployableServer server) {
		return server.getTempDeploymentLocation(moduleTree, true);
	}

	@Deprecated
	public static IPath getLocalTempDeployRootFolder(IModule[] moduleTree, IDeployableServer server) {
		return server.getTempDeploymentLocation(moduleTree, false);
	}
	
	@Deprecated
	public static IPath getDeployPath(IJBossServerPublishMethod method, IModule[] moduleTree, IDeployableServer server) {
		return server.getDeploymentLocation(moduleTree, true);
	}

	@Deprecated
	public static IPath getTempDeployPath(IJBossServerPublishMethod method, IModule[] moduleTree, 
			IDeployableServer server) {
		return server.getTempDeploymentLocation(moduleTree, true);
	}
}
