/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;


/* 
 * Some of this code will need to be abstracted out from JBossServer
 * and turned into a proper API, but in as simple a way as possible
 */
public class RSEUtils {
	public static final String RSE_SERVER_CONFIG = "org.jboss.ide.eclipse.as.rse.core.RSEServerConfig";  //$NON-NLS-1$
	public static final String RSE_SERVER_HOME_DIR = "org.jboss.ide.eclipse.as.rse.core.RSEServerHomeDir";  //$NON-NLS-1$
	public static final String RSE_SERVER_HOST = "org.jboss.ide.eclipse.as.rse.core.ServerHost";  //$NON-NLS-1$
	public static final String RSE_SERVER_DEFAULT_HOST = "Local";  //$NON-NLS-1$
	
	public static String getRSEConnectionName(IServer server) {
		return server.getAttribute(RSEUtils.RSE_SERVER_HOST, RSE_SERVER_DEFAULT_HOST);
	}
	
	public static String getRSEHomeDir(IServerAttributes server) {
		return server.getAttribute(RSEUtils.RSE_SERVER_HOME_DIR, server.getRuntime().getLocation().toString());
	}
	
	public static String getRSEConfigName(IServerAttributes server) {
		IJBossServerRuntime runtime = ServerConverter.getJBossRuntime(server);
		return server.getAttribute(RSEUtils.RSE_SERVER_CONFIG, runtime.getJBossConfiguration());
	}
	
	public static String getDeployRootFolder(JBossServer server) {
		return getDeployRootFolder(server.getServer(), server.getDeployLocationType());
	}
	
	/* Copied from JBossServer.getDeployFolder(etc) */
	public static String getDeployRootFolder(IServer server, String type) {
		if( type.equals(JBossServer.DEPLOY_CUSTOM)) {
			String val = server.getAttribute(JBossServer.DEPLOY_DIRECTORY, (String)null);
			if( val != null ) {
				IPath val2 = new Path(val);
				return makeGlobal(server, val2).toString();
			}
			// if no value is set, default to metadata
			type = JBossServer.DEPLOY_SERVER;
		}
		// TODO error here, or sensible default?
		if( type.equals(JBossServer.DEPLOY_METADATA)) {
			return JBossServerCorePlugin.getServerStateLocation(server).
				append(IJBossServerConstants.DEPLOY).makeAbsolute().toString();
		} else if( type.equals(JBossServer.DEPLOY_SERVER)) {
			String loc = IConstants.SERVER;
			String config = getRSEConfigName(server);
			IPath p = new Path(loc).append(config)
				.append(IJBossServerConstants.DEPLOY);
			return makeGlobal(server, p).toString();
		}
		return null;
	}

	public static IPath makeRelative(IServer server, IPath p) {
		if( p.isAbsolute()) {
			if(new Path(getRSEHomeDir(server)).isPrefixOf(p)) {
				int size = new Path(getRSEHomeDir(server)).toOSString().length();
				return new Path(p.toOSString().substring(size)).makeRelative();
			}
		}
		return p;
	}
	
	public static IPath makeGlobal(IServer server, IPath p) {
		if( !p.isAbsolute()) {
			return new Path(getRSEHomeDir(server)).append(p).makeAbsolute();
		}
		return p;
	}

	public static IHost findHost(String connectionName) {
		// TODO ensure that all hosts are actually loaded, christ
		IHost[] allHosts = RSECorePlugin.getTheSystemRegistry().getHosts();
		for( int i = 0; i < allHosts.length; i++ ) {
			if( allHosts[i].getAliasName().equals(connectionName))
				return allHosts[i];
		}
		return null;
	}
	
}
