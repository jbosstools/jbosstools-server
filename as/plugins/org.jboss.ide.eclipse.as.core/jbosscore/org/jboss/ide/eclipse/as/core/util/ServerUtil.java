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
package org.jboss.ide.eclipse.as.core.util;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior.JBossBehaviourDelegate;

public class ServerUtil {
	public static IPath getServerStateLocation(IServer server) {
		return server == null ? JBossServerCorePlugin.getDefault().getStateLocation() :
					getServerStateLocation(server.getId());
	}

	public static IPath getServerStateLocation(String serverID) {
		return serverID == null ? JBossServerCorePlugin.getDefault().getStateLocation() : 
			JBossServerCorePlugin.getDefault().getStateLocation()
			.append(serverID.replace(' ', '_'));
	}

	public static IPath getServerBinDirectory(JBossServer server) throws CoreException {
		return getServerHomePath(server).append(IJBossRuntimeResourceConstants.BIN);
	}
	
	@SuppressWarnings("unchecked")
	public static <ADAPTER> ADAPTER checkedGetServerAdapter(IServer server, Class<ADAPTER> behaviorClass) throws CoreException {
		ADAPTER serverBehavior = (ADAPTER) server.loadAdapter(behaviorClass, new NullProgressMonitor());
		if (serverBehavior == null) {
			throw new CoreException(					
					new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
							NLS.bind(Messages.CouldNotFindServerBehavior, server.getName())));
		}
		return serverBehavior;
	}
	
	public static JBossBehaviourDelegate checkedGetBehaviorDelegate(IServer server) throws CoreException {
		JBossBehaviourDelegate delegate = checkedGetServerAdapter(server, DelegatingServerBehavior.class).getDelegate();
		if (delegate == null) {
			throw new CoreException(					
					new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
							NLS.bind(Messages.CouldNotFindServerBehavior, server.getName())));
		}
		return delegate;
	}
	
	@Deprecated
	public static IPath makeRelative(IJBossServerRuntime rt, IPath p) {
		if( rt != null && rt.getRuntime() != null )
			return makeRelative(rt.getRuntime(), p);
		return p;
	}
	
	public static IPath makeRelative(IRuntime rt, IPath p) {
		if( p.isAbsolute() && rt != null) {
			if(rt.getLocation().isPrefixOf(p)) {
				int size = rt.getLocation().toOSString().length();
				return new Path(p.toOSString().substring(size)).makeRelative();
			}
		}
		return p;
	}
	
	@Deprecated
	public static IPath makeGlobal(IJBossServerRuntime rt, IPath p) {
		if( rt != null && rt.getRuntime() != null )
			return makeGlobal(rt.getRuntime(), p);
		return p;
	}
	
	public static IPath makeGlobal(IRuntime rt, IPath p) {
		if( !p.isAbsolute() ) {
			if( rt != null && rt.getLocation() != null ) {
				return rt.getLocation().append(p).makeAbsolute();
			}
			return p.makeAbsolute();
		}
		return p;
	}
	
//	public static void cloneConfigToMetadata(IServer server, IProgressMonitor monitor) {
//		IPath dest = JBossServerCorePlugin.getServerStateLocation(server);
//		dest = dest.append(IJBossServerConstants.CONFIG_IN_METADATA);
//		IRuntime rt = server.getRuntime();
//		IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
//		IPath src = rt.getLocation().append(IJBossServerConstants.SERVER).append(jbsrt.getJBossConfiguration());
//		dest.toFile().mkdirs();
//		
//		File[] subFiles = src.toFile().listFiles();
//		dest.toFile().mkdirs();
//		String[] excluded = IJBossServerConstants.JBOSS_TEMPORARY_FOLDERS;
//		for (int i = 0; i < subFiles.length; i++) {
//			boolean found = false;
//			for( int j = 0; j < excluded.length; j++)
//				if( subFiles[i].getName().equals(excluded[j]))
//					found = true;
//			if( !found ) {
//				File newDest = new File(dest.toFile(), subFiles[i].getName());
//				FileUtil.fileSafeCopy(subFiles[i], newDest, null);
//			}
//		}
//	}
	
	public static boolean isJBoss7(IServer server) {
		return isJBoss7(server.getServerType());
	}
	
	public static boolean isJBoss7(IServerType type) {
		return type.getId().equals(IJBossToolingConstants.SERVER_AS_70);
	}
	
	public static void createStandardFolders(IServer server) {
		// create metadata area
		File location = JBossServerCorePlugin.getServerStateLocation(server).toFile();
		location.mkdirs();
		File d1 = new File(location, IJBossRuntimeResourceConstants.DEPLOY);
		File d2 = new File(location, IJBossToolingConstants.TEMP_DEPLOY);
		d1.mkdirs();
		d2.mkdirs();
		
		// create temp deploy folder
		JBossServer ds = ( JBossServer)server.loadAdapter(JBossServer.class, null);
		if( ds != null && !isJBoss7(server)) {
			if( !new File(ds.getDeployFolder()).equals(d1)) 
				new File(ds.getDeployFolder()).mkdirs();
			if( !new File(ds.getTempDeployFolder()).equals(d2))
				new File(ds.getTempDeployFolder()).mkdirs();
			IRuntime rt = server.getRuntime();
			IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
			if( jbsrt != null ) {
				String config = jbsrt.getJBossConfiguration();
				IPath newTemp = new Path(IJBossRuntimeResourceConstants.SERVER).append(config)
					.append(IJBossToolingConstants.TMP)
					.append(IJBossToolingConstants.JBOSSTOOLS_TMP).makeRelative();
				IPath newTempAsGlobal = ServerUtil.makeGlobal(jbsrt, newTemp);
				newTempAsGlobal.toFile().mkdirs();
			}
		}
	}
	
	public static IServer findServer(String name) {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			if (name.trim().equals(servers[i].getName()))
				return servers[i];
		}
		return null;
	}
	
	public static String getDefaultServerName(IRuntime rt) {
		String runtimeName = rt.getName();
		String base = null;
		if( runtimeName == null || runtimeName.equals("")) //$NON-NLS-1$
			base = NLS.bind(Messages.serverVersionName, rt.getRuntimeType().getVersion());
		else 
			base = NLS.bind(Messages.serverName, runtimeName);
		
		return getDefaultServerName( base);
	}
	public static String getDefaultServerName( String base) {
		if( ServerUtil.findServer(base) == null ) return base;
		int i = 1;
		while( ServerUtil.findServer(
				NLS.bind(Messages.serverCountName, base, i)) != null )
			i++;
		return NLS.bind(Messages.serverCountName, base, i);
	}
	
	public static String checkedGetServerHome(JBossServer jbs) throws CoreException {
		String serverHome = jbs.getServer().getRuntime().getLocation().toOSString();
		if (serverHome == null)
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(Messages.CannotLocateServerHome, jbs.getServer().getName())));
		return serverHome;
	}
	
	public static IPath getServerHomePath(JBossServer jbs) throws CoreException {
		return new Path(checkedGetServerHome(jbs));
	}
	
}
