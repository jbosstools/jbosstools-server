/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.server.core.IJ2EEModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.wtp.core.modules.IJBTModule;

/**
 * This class should be combined with {@link ModuleResourceUtil}
 */
public class ServerModelUtilities {
	

	/**
	 * Get the path relative to the root module here.
	 * 
	 * For example, if the provided IModule is {WrapEar,TigerWar,ClawUtil},
	 * the returned value should be "TigerWar.war/WEB-INF/lib/ClawUtil.jar" 
	 * @param module
	 * @return
	 */
	public static IPath getRootModuleRelativePath(IServerAttributes server, IModule[] moduleTree) {
		String modName, name, uri, suffixedName;
		IPath working = null;
		for( int i = 1; i < moduleTree.length; i++ ) {
			modName = moduleTree[i].getName();
			name = new RemotePath(modName).lastSegment();
			suffixedName = name + getDefaultSuffixForModule(moduleTree[i]);
			uri = ModuleResourceUtil.getParentRelativeURI(moduleTree, i, suffixedName);
			working = (working == null ? new Path(uri) : working.append(uri));
		}
		return working;
	}
	
	public static IModule[] getParentModules(IServer server, IModule module) {
		// get all supported modules
		IRuntimeType rtt = RuntimeUtils.getRuntimeType(server);
		IModule[] supported = rtt == null ? new IModule[0] : 
			org.eclipse.wst.server.core.ServerUtil.getModules(rtt.getModuleTypes());
		ArrayList<IModule> list = new ArrayList<IModule>();
		
		for( int i = 0; i < supported.length; i++ ) {
			IModule[] childs = ServerModelUtilities.getChildModules(supported[i]);
			for (int j = 0; j < childs.length; j++) {
				if(childs[j].equals(module))
					list.add(supported[i]);
			}
		}
		return list.toArray(new IModule[list.size()]);
	}


	public static ArrayList<IModule[]> getShallowChildren(IServer server, IModule[] root) {
		ArrayList<IModule[]> list = new ArrayList<IModule[]>();
		IModule[] children = server.getChildModules(root, new NullProgressMonitor());
		// children is { aWar, bWar, cWar } projects
		int length = children == null ? 0 : children.length;
		for( int i = 0; i < length; i++ ) {
			ArrayList<IModule> inner = new ArrayList<IModule>();
			inner.addAll(Arrays.asList(root));
			inner.add(children[i]);
			IModule[] innerMods = inner.toArray(new IModule[inner.size()]);
			list.add(innerMods);
		}
		return list;
	}
	
	public static ArrayList<IModule[]> getDeepChildren(IServer server, IModule[] mod) {
		ArrayList<IModule[]> deep = getShallowChildren(server, mod);
		IModule[] toBeSearched;
		for( int i = 0; i < deep.size(); i++ ) {
			toBeSearched = deep.get(i);
			deep.addAll(getShallowChildren(server, toBeSearched));
		}
		return deep;
	}
	
	public static IModule[] getChildModules(IModule[] module) {
		int last = module.length -1;
		if( module[last] != null && module[last].getModuleType() != null)
			return getChildModules(module[last]);
		return new IModule[0];
	}
	
	public static IModule[] getChildModules(IModule module) {
		IModule[] children = ((ModuleDelegate)module.loadAdapter(ModuleDelegate.class, new NullProgressMonitor())).getChildModules();
		return children == null ? new IModule[0] : children;
	}
	
	
	public static boolean isBinaryModule(IModule[] moduleTree) {
		return moduleTree == null ? false : isBinaryModule(moduleTree[moduleTree.length - 1]);
	}
	
	public static boolean isBinaryModule(IModule module) {
		IJ2EEModule jee = (IJ2EEModule) module.loadAdapter(IJ2EEModule.class, null);
		if( jee != null )
			return jee.isBinary();
		IJBTModule jbtMod = (IJBTModule)module.loadAdapter(IJBTModule.class, null);
		if( jbtMod != null )
			return jbtMod.isBinary();
		return false;
	}
	
	public static boolean isAnyDeleted(IModule[] module) {
		boolean deleted = false;
		for( int i = 0; i < module.length; i++ ) {
			if( !module[i].exists()) {
				deleted = true;
				break;
			}
		}
		return deleted;
	}

	
	public static IServer[] findServersFor(IProject p, IServerFilter filter) {
		ArrayList<IServer> match = new ArrayList<IServer>();
		IServer[] allServers = ServerCore.getServers();
		IModule[] mods = ServerUtil.getModules(p);
		for( int i = 0; i < allServers.length; i++ ) {
			if( filter == null || filter.accepts(allServers[i])) {
				IModule[] serversMods = allServers[i].getModules();
				for( int j = 0; j < mods.length; j++ ) {
					if( !isBinaryModule(mods[j]) && moduleListContainsMod(serversMods, mods[j])) {
						if( !match.contains(allServers[i])) {
							match.add(allServers[i]);
						}
					}
				}
			}
		}
		return (IServer[]) match.toArray(new IServer[match.size()]);
	}
	
	public static boolean moduleListContainsMod(IModule[] list, IModule module) {
		for( int i = 0; i < list.length; i++ ) {
			if( list[i].equals(module))
				return true;
		}
		return false;
	}
	
	
	/**
	 * Get the default suffix for this module
	 * @param module
	 */
	public static String getDefaultSuffixForModule(IModule module) {
		String type = null;
		if( module != null ) 
			type = module.getModuleType().getId();
		return getDefaultSuffixForModuleType(type);
	}
	
	/**
	 * Get the suffix from this module type
	 * @param type a module type id
	 * @return
	 */
	public static String getDefaultSuffixForModuleType(String type) {
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
