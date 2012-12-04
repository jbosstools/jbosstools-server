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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IJ2EEModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;
import org.jboss.ide.eclipse.as.wtp.core.modules.IJBTModule;

public class ServerModelUtilities {
	
	public static IModule[] getParentModules(IServer server, IModule module) {
		// get all supported modules
		IModule[] supported = 
			org.eclipse.wst.server.core.ServerUtil.getModules(
					server.getServerType().getRuntimeType().getModuleTypes());
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
		if( module.getModuleType().getId().equals(IWTPConstants.FACET_EAR)) {
			return CustomProjectInEarWorkaroundUtil.getSafeChildrenModules(module);
		}
		ModuleDelegate md = CustomProjectInEarWorkaroundUtil
					.getCustomProjectSafeModuleDelegate(module);
		IModule[] children = md == null ? null : md.getChildModules();
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
			if( module[i].isExternal() ) {
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
	
}
