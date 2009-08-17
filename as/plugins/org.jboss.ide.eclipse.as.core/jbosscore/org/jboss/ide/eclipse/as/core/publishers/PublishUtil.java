/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.publishers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentTypePrefs;

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


	public static int countMembers(IModule module) {
		try {
			ModuleDelegate delegate = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
			return delegate == null ? 0 : countMembers(delegate.members());
		} catch( CoreException ce ) {}
		return 0;
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

	private static String getDeployRootFolder(IModule[] moduleTree, 
			IDeployableServer server, String defaultFolder, String moduleProperty) {
		String folder = defaultFolder;
		DeploymentPreferences prefs = DeploymentPreferenceLoader.loadPreferences(server.getServer());
		DeploymentTypePrefs typePrefs = prefs.getOrCreatePreferences("local"); //$NON-NLS-1$
		DeploymentModulePrefs modPrefs = typePrefs.getModulePrefs(moduleTree[0]);
		if( modPrefs != null ) {
			String loc = modPrefs.getProperty(moduleProperty);
			if( loc != null && !loc.equals("") ) { //$NON-NLS-1$
				if( !new Path(loc).isAbsolute()) {
					folder = server.getServer().getRuntime().getLocation().append(loc).toString();
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
		return getDeployPath(moduleTree, folder);
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
	
	private static IPath getDeployPath(IModule[] moduleTree, String deployFolder) {
		IPath root = new Path( deployFolder );
		String type, modName, name, uri, suffixedName;
		for( int i = 0; i < moduleTree.length; i++ ) {
			type = moduleTree[i].getModuleType().getId();
			modName = moduleTree[i].getName();
			name = new Path(modName).lastSegment();
			suffixedName = name + getSuffix(type);
			uri = getParentRelativeURI(moduleTree, i, suffixedName);
			root = root.append(uri);
		}
		return root;
	}
	
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
	
	private static String getSuffix(String type) {
		String suffix = null;
		if( IJBossServerConstants.FACET_EAR.equals(type)) 
			suffix = IJBossServerConstants.EXT_EAR;
		else if( IJBossServerConstants.FACET_WEB.equals(type)) 
			suffix = IJBossServerConstants.EXT_WAR;
		else if( IJBossServerConstants.FACET_CONNECTOR.equals(type)) 
			suffix = IJBossServerConstants.EXT_RAR;
		else if( IJBossServerConstants.FACET_ESB.equals(type))
			suffix = IJBossServerConstants.EXT_ESB;
		else
			suffix = IJBossServerConstants.EXT_JAR;
		return suffix;
	}
	
	// TODO This can also change to find the isBinaryModule method 
	public static boolean isBinaryObject(IModule[] moduleTree) {
		String name;
		for( int i = 0; i < moduleTree.length; i++ ) {
			name = moduleTree[i].getName();
			if( new Path(name).segmentCount() > 1 )
				// we strongly suspect this is a binary object and not a project
				return true;
		}
		return false;
	}
	
	public static IModuleResource[] getResources(IModule module) throws CoreException {
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
		IModuleResource[] members = md.members();
		return members;
	}
	public static IModuleResource[] getResources(IModule[] tree) throws CoreException {
		return getResources(tree[tree.length-1]);
	}
	
	public static java.io.File getFile(IModuleFile mf) {
		return (IFile)mf.getAdapter(IFile.class) != null ? 
					((IFile)mf.getAdapter(IFile.class)).getLocation().toFile() :
						(java.io.File)mf.getAdapter(java.io.File.class);
	}
}
