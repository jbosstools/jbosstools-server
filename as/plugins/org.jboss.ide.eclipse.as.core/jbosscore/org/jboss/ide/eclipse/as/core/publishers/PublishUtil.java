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
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.project.facet.core.util.internal.ProgressMonitorUtil;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.DeletedModule;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.publishers.patterns.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.xpl.ModulePackager;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentTypePrefs;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;

public class PublishUtil {
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


	/**
	 * All preferences are stored in the "local" setting as it was decided
	 * having to replicate deploy paths for each publish method was not good
	 * 
	 * This API should be renamed, as it actually gets a value stored at a property key
	 * 
	 * @param moduleTree
	 * @param server
	 * @param defaultFolder
	 * @param moduleProperty
	 * @return
	 */
	public static String getDeployRootFolder(IModule[] moduleTree, 
			IDeployableServer server, String defaultFolder, String moduleProperty) {
		return getDeployRootFolder(moduleTree, LocalPublishMethod.LOCAL_PUBLISH_METHOD,
				server, defaultFolder, moduleProperty);
	}
	
	/* This api should be renamed */
	public static String getDeployRootFolder(IModule[] moduleTree, String publishMethod,
			IDeployableServer server, String defaultFolder, String moduleProperty) {
		String folder = defaultFolder;
		// TODO bug 286699
		DeploymentPreferences prefs = DeploymentPreferenceLoader.loadPreferencesFromServer(server.getServer());
		DeploymentTypePrefs typePrefs = prefs.getOrCreatePreferences(publishMethod);
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
	
	public static IPath getDeployPath(IModule[] moduleTree, IDeployableServer server) {
		String folder = getDeployRootFolder(
				moduleTree, server, 
				server.getDeployFolder(), 
				IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC);
		return getDeployPath(moduleTree, folder, server);
	}

	public static IPath getDeployRootFolder(IModule[] moduleTree, IDeployableServer server) {
		String folder = getDeployRootFolder(
				moduleTree, server, 
				server.getDeployFolder(), 
				IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC);
		return new Path(folder);
	}

	public static IPath getTempDeployFolder(IModule[] moduleTree, IDeployableServer server) {
		String folder = getDeployRootFolder(
				moduleTree, server, 
				server.getTempDeployFolder(), 
				IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC);
		return new Path(folder);
	}
	
	public static IPath getDeployPath(IModule[] moduleTree, String deployFolder, IDeployableServer server) {
		IPath root = new Path( deployFolder );
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
	
	/*
	 * This method is deprecated. Please use the following:
	 * @see getDeployPath(IModule[] module, String folder, IDeployableServer server)
	 * @param moduleTree
	 * @param deployFolder
	 * @return
	 */
	// @Deprecated 
	//public static IPath getDeployPath(IModule[] moduleTree, String deployFolder);
	
	private static String getParentRelativeURI(IModule[] tree, int index, String defaultName) {
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
	
	private static ArrayList<String> moduleCoreFactories = new ArrayList<String>();
	static {
		moduleCoreFactories.add("org.eclipse.jst.j2ee.server"); //$NON-NLS-1$
		moduleCoreFactories.add("org.eclipse.jst.jee.server"); //$NON-NLS-1$
	}
	public static void addModuleCoreFactory(String s) {
		if( !moduleCoreFactories.contains(s))
			moduleCoreFactories.add(s);
	}
	public static boolean isModuleCoreProject(IModule[] module) {
		IModule lastmod = module[module.length-1];
		if( lastmod.getProject() == null && lastmod instanceof DeletedModule) {
			int colon = lastmod.getId().indexOf(':');
			String factory = lastmod.getId().substring(0,colon == -1 ? lastmod.getId().length() : colon);
			return moduleCoreFactories.contains(factory);
		}
		return ModuleCoreNature.isFlexibleProject(lastmod.getProject());
	}

	public static IPath getDeployPath(IJBossServerPublishMethod method, IModule[] moduleTree, IDeployableServer server) {
		String defaultFolder = method.getPublishDefaultRootFolder(server.getServer());
		String folder = PublishUtil.getDeployRootFolder(
				moduleTree, server, defaultFolder,
				IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC);
		return PublishUtil.getDeployPath(moduleTree, folder, server);
	}

	public static IPath getTempDeployPath(IJBossServerPublishMethod method, IModule[] moduleTree, IDeployableServer server) {
		String defaultFolder = method.getPublishDefaultRootTempFolder(server.getServer());
		String folder = PublishUtil.getDeployRootFolder(
				moduleTree, server, defaultFolder,
				IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC);
		return PublishUtil.getDeployPath(moduleTree, folder, server);
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
	
	public static IModuleResource[] getResources(IModule module, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Fetching Module Resources", 100); //$NON-NLS-1$
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, ProgressMonitorUtil.submon(monitor, 100));
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
	public static java.io.File getFile(IModuleFile mf) {
		return (IFile)mf.getAdapter(IFile.class) != null ? 
					((IFile)mf.getAdapter(IFile.class)).getLocation().toFile() :
						(java.io.File)mf.getAdapter(java.io.File.class);
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
	 * @since 2.3
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
	 * @since 2.3
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
	
	public static IModule[] combine(IModule[] module, IModule newMod) {
		IModule[] retval = new IModule[module.length + 1];
		for( int i = 0; i < module.length; i++ )
			retval[i]=module[i];
		retval[retval.length-1] = newMod;
		return retval;
	}	
}
