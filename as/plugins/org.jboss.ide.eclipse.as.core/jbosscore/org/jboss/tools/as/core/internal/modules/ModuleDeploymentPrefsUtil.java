/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.core.internal.modules;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.util.RemotePath;

/**
 * Several utility methods for modules related to deployment preferences
 * not to be exposed to clients
 */
public class ModuleDeploymentPrefsUtil {

	/**
	 * Get a path representing the folder this module should be deployed into
	 * 
	 * @param moduleTree
	 * @param server
	 * @param defaultFolder
	 * @param separator
	 * @return
	 */
	public IPath getModuleRootDestination(IModule[] moduleTree, IServer server, 
			String defaultFolder, char separator) {
		DeploymentPreferences prefs = DeploymentPreferencesLoader.loadPreferencesFromServer(server);
		String folder = getPathPropertyFromDeploymentModulePrefs(
				moduleTree, prefs, defaultFolder,
				IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC, separator);
		return new Path(folder);
	}

	/**
	 * Get the full path of a module once it is published.
	 * This differs from getModuleRootDestination in that 
	 * getModuleRootDestination will return /home/user/some/path,
	 * while this  method will return /home/user/some/path/Important.ear/Web.war
	 * 
	 * 
	 * @param moduleTree
	 * @param server
	 * @param defaultFolder
	 * @param separator
	 * @return
	 */
	public IPath getModuleTreeDestinationFullPath(IModule[] moduleTree, IServerAttributes server, 
			String defaultFolder, char separator) {
		DeploymentPreferences prefs = DeploymentPreferencesLoader.loadPreferencesFromServer(server);
		String folder = getPathPropertyFromDeploymentModulePrefs(
				moduleTree, prefs, defaultFolder,
				IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC, separator);
		IPath ret = getModuleNestedDeployPath(moduleTree, folder, server, separator);
		return new RemotePath(ret.toString(), separator);
	}

	/**
	 * Get the temporary deploy folder for this module
	 * 
	 * @param moduleTree
	 * @param server
	 * @param defaultFolder
	 * @param separator
	 * @return
	 */
	public IPath getModuleTempDeployPath(IModule[] moduleTree, 
			IServerAttributes server, String defaultFolder, char separator) {
		DeploymentPreferences prefs = DeploymentPreferencesLoader.loadPreferencesFromServer(server);
		String folder = getPathPropertyFromDeploymentModulePrefs(
				moduleTree, prefs, defaultFolder,
				IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC, separator);
		return new RemotePath(folder, separator);
	}

	
	/**
	 * Get a property value from the Deployment Preferences. 
	 * The resulting value is assumed to be a path. 
	 * IF the stored path is a relative path, this method will return 
	 *    defaultFolder/pathFromStorage
	 * IF the stored path is an absolute path, that value will be returned
	 * If no path is found, the defaultFolder will be returned. 
	 * 
	 * @param moduleTree
	 * @param server
	 * @param defaultFolder
	 * @param moduleProperty
	 * @param separator
	 * @return
	 */
	private String getPathPropertyFromDeploymentModulePrefs(IModule[] moduleTree, 
			DeploymentPreferences prefs, String defaultFolder, String moduleProperty, char separator) {
		
		// Load our deployment preferences
		DeploymentTypePrefs typePrefs = prefs.getOrCreatePreferences();
		DeploymentModulePrefs modPrefs = typePrefs.getModulePrefs(moduleTree[0]);
		if( modPrefs != null ) {
			String loc = modPrefs.getProperty(moduleProperty);
			if( loc != null && !loc.equals("") ) { //$NON-NLS-1$
				if( !new RemotePath(loc, separator).isAbsolute()) {
					// we have a relative path, so pre-pend the default folder
					IPath p = new RemotePath(defaultFolder, separator);
					String ret = p.append(loc).toOSString();
					return ret;
				} else {
					// We have an absolute path, so use that
					return loc;
				}
			}
		}
		return defaultFolder;
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
	public IPath getModuleNestedDeployPath(IModule[] moduleTree, String rootFolder, IServerAttributes server) {
		return getModuleNestedDeployPath(moduleTree, rootFolder, server, java.io.File.separatorChar);
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
	 * @param separatorChar 
	 * @return
	 */
	public IPath getModuleNestedDeployPath(IModule[] moduleTree, String rootFolder, IServerAttributes server, char separator) {
		IPath root = new RemotePath( rootFolder, separator );
		String modName, name, uri, suffixedName;
		for( int i = 0; i < moduleTree.length; i++ ) {	
			boolean found = false;
			if( i == 0 ) {
				// If this is the root module, we can customize the output name
				DeploymentPreferences prefs = DeploymentPreferencesLoader.loadPreferencesFromServer(server);
				DeploymentTypePrefs typePrefs = prefs.getOrCreatePreferences();
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
				modName = moduleTree[i].getName();
				name = new RemotePath(modName, separator).lastSegment();
				suffixedName = name + getDefaultSuffix(moduleTree[i]);
				uri = ModuleResourceUtil.getParentRelativeURI(moduleTree, i, suffixedName);
				root = root.append(uri);
			}
		}
		return root;
	}
	
	
	
	/**
	 * Get the default suffix for this module
	 * @param module
	 */
	public String getDefaultSuffix(IModule[] module) {
		IModule last = (module == null ? null : module[module.length-1]);
		return getDefaultSuffix(last);
	}

	/**
	 * Get the default suffix for this module
	 * @param module
	 */
	public String getDefaultSuffix(IModule module) {
		String type = null;
		if( module != null ) 
			type = module.getModuleType().getId();
		return getDefaultSuffix(type);
	}
	
	/**
	 * Get the suffix from this module type
	 * @param type
	 * @return
	 */
	public String getDefaultSuffix(String type) {
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
	
	
}
