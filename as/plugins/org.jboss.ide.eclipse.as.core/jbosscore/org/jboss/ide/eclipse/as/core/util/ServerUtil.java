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
package org.jboss.ide.eclipse.as.core.util;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;

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

	public static IPath makeRelative(IJBossServerRuntime rt, IPath p) {
		if( p.isAbsolute() && rt != null) {
			if(rt.getRuntime().getLocation().isPrefixOf(p)) {
				int size = rt.getRuntime().getLocation().toOSString().length();
				return new Path(p.toOSString().substring(size)).makeRelative();
			}
		}
		return p;
	}
	
	public static IPath makeGlobal(IJBossServerRuntime rt, IPath p) {
		if( !p.isAbsolute() && rt != null) {
			return rt.getRuntime().getLocation().append(p).makeAbsolute();
		}
		return p;
	}
	
	public static void cloneConfigToMetadata(IServer server, IProgressMonitor monitor) {
		IPath dest = JBossServerCorePlugin.getServerStateLocation(server);
		dest = dest.append(IJBossServerConstants.CONFIG_IN_METADATA);
		IRuntime rt = server.getRuntime();
		IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		IPath src = rt.getLocation().append(IJBossServerConstants.SERVER).append(jbsrt.getJBossConfiguration());
		dest.toFile().mkdirs();
		
		File[] subFiles = src.toFile().listFiles();
		dest.toFile().mkdirs();
		String[] excluded = IJBossServerConstants.JBOSS_TEMPORARY_FOLDERS;
		for (int i = 0; i < subFiles.length; i++) {
			boolean found = false;
			for( int j = 0; j < excluded.length; j++)
				if( subFiles[i].getName().equals(excluded[j]))
					found = true;
			if( !found ) {
				File newDest = new File(dest.toFile(), subFiles[i].getName());
				FileUtil.fileSafeCopy(subFiles[i], newDest, null);
			}
		}
	}
	
	public static void createStandardFolders(IServer server) {
		// create metadata area
		File location = JBossServerCorePlugin.getServerStateLocation(server).toFile();
		location.mkdirs();
		
		// create temp deploy folder
		JBossServer ds = ( JBossServer)server.loadAdapter(JBossServer.class, null);
		if( ds != null ) {
			File d1 = new File(location, IJBossServerConstants.DEPLOY);
			File d2 = new File(location, IJBossServerConstants.TEMP_DEPLOY);
			d1.mkdirs();
			d2.mkdirs();
			if( !new File(ds.getDeployFolder()).equals(d1)) 
				new File(ds.getDeployFolder()).mkdirs();
			if( !new File(ds.getTempDeployFolder()).equals(d2))
				new File(ds.getTempDeployFolder()).mkdirs();
			IRuntime rt = server.getRuntime();
			IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
			String config = jbsrt.getJBossConfiguration();
			IPath newTemp = new Path(IJBossServerConstants.SERVER).append(config)
				.append(IJBossServerConstants.TMP)
				.append(IJBossServerConstants.JBOSSTOOLS_TMP).makeRelative();
			IPath newTempAsGlobal = ServerUtil.makeGlobal(jbsrt, newTemp);
			newTempAsGlobal.toFile().mkdirs();
		}
	}
}
