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
import org.eclipse.wst.server.core.IModule2;
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;
import org.jboss.tools.as.core.server.controllable.systems.AbstractJBossDeploymentOptionsController;

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
	public IPath getModuleRootDestination(IModule[] moduleTree, IServerAttributes server, 
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
		if( folder == null ) 
			return null;
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
		if( folder == null )
			return null;
		RemotePath rp = new RemotePath(folder, separator);
		System.out.println(rp.toString());
		System.out.println(rp.toOSString());
		
		return rp;
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
		String modName, name, suffixedName;
		IPath root = new RemotePath( rootFolder, separator );
		
		// First handle the root module
		String customName = getOutputNameFromSettings(server, moduleTree[0]);
		if( customName != null ) {
			root = root.append(customName);
		} else {
			modName = null;
			// Check if there's a deploy-name property
			if (moduleTree[0] instanceof IModule2) {
				modName = ((IModule2)moduleTree[0]).getProperty(IModule2.PROP_DEPLOY_NAME);
			}
			if( modName == null ) {
				modName = moduleTree[0].getName();
			}
			
			name = new RemotePath(modName, separator).lastSegment();
			suffixedName = name + getDefaultSuffix(moduleTree[0]);
			root = root.append(suffixedName);
		}
		// now add the rest via the standard model (check each module's path relative to parent etc)
		return root.append(getRootModuleRelativePath(server, moduleTree));
	}
	
	@Deprecated
	public IPath getRootModuleRelativePath(IServerAttributes server, IModule[] moduleTree) {
		return ServerModelUtilities.getRootModuleRelativePath(server, moduleTree);
	}
	
	protected String getOutputNameFromSettings(IServerAttributes server, IModule module) {
		DeploymentPreferences prefs = DeploymentPreferencesLoader.loadPreferencesFromServer(server);
		DeploymentTypePrefs typePrefs = prefs.getOrCreatePreferences();
		DeploymentModulePrefs modPrefs = typePrefs.getModulePrefs(module);
		if( modPrefs != null ) {
			String outName = modPrefs.getProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_OUTPUT_NAME);
			if( outName != null && !outName.equals("")) { //$NON-NLS-1$
				return outName;
			}
		}
		return null;
	}
	
	
	public boolean getPrefersZipFromSettings(IServerAttributes server, IModule module, boolean defaultValue) {
		DeploymentPreferences prefs = DeploymentPreferencesLoader.loadPreferencesFromServer(server);
		DeploymentTypePrefs typePrefs = prefs.getOrCreatePreferences();
		DeploymentModulePrefs modPrefs = typePrefs.getModulePrefs(module);
		if( modPrefs != null ) {
			String ret = modPrefs.getProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_ZIP);
			if( ret != null && !ret.equals("")) { //$NON-NLS-1$
				int ret2 = -1;
				try {
					ret2 = Integer.parseInt(ret);
					if( ret2 == AbstractJBossDeploymentOptionsController.ZIP_DEFAULT)
						return defaultValue;
					return ret2 == AbstractJBossDeploymentOptionsController.ZIP_YES ? true : false;
				} catch(NumberFormatException nfe) {
					return Boolean.parseBoolean(ret);
				}
			}
		}
		return defaultValue;
	}
	
	
	/**
	 * Get the default suffix for the last entry in this module array
	 * @param module
	 */
	public String getDefaultSuffix(IModule[] module) {
		IModule last = (module == null ? null : module[module.length-1]);
		return getDefaultSuffix(last);
	}

	/**
	 * Get the default suffix for this module
	 * @param module
	 * @deprecated
	 */
	public String getDefaultSuffix(IModule module) {
		return ServerModelUtilities.getDefaultSuffixForModule(module);
	}
	
	/**
	 * Get the suffix from this module type
	 * @param type
	 * @return
	 * @deprecated
	 */
	public String getDefaultSuffix(String type) {
		return ServerModelUtilities.getDefaultSuffixForModuleType(type);
	}
	
	
}
