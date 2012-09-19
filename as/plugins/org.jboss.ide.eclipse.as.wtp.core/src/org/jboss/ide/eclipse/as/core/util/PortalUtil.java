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
package org.jboss.ide.eclipse.as.core.util;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;


public class PortalUtil {
	public static int TYPE_PORTAL_UNKNOWN = 0;
	public static int TYPE_PORTAL = 1;
	public static int TYPE_PORTAL_CLUSTER = 2;
	public static int TYPE_PORTLET_CONTAINER = 3;
	public static int TYPE_GATE_IN = 4;
	
	private static final String SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_SAR = "deploy/jboss-portal.sar"; //$NON-NLS-1$
	private static final String SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_HA_SAR = "deploy/jboss-portal-ha.sar"; //$NON-NLS-1$
	private static final String SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL = "deploy/simple-portal"; //$NON-NLS-1$
	private static final String SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL_SAR = "deploy/simple-portal.sar"; //$NON-NLS-1$
	private static final String SERVER_DEFAULT_DEPLOY_GATEIN = "deploy/gatein.ear"; //$NON-NLS-1$
	
	private static final String SIMPLE_PORTAL_PATH = "simple-portal"; //$NON-NLS-1$
	private static final String PORTAL_PATH = "portal"; //$NON-NLS-1$
	
	public static int getServerPortalType(IServer server) {
		IRuntime rt = server.getRuntime();
		IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		return getServerPortalType(jbsrt);
	}
	
	public static int getServerPortalType(IJBossServerRuntime runtime) {
		IPath configPath = runtime.getConfigurationFullPath();
		File configFile = configPath.toFile();
		
		// JBoss Portal server
		if (exists(configFile, SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_SAR)) {
			return TYPE_PORTAL;
		}
		// JBoss Portal clustering server
		if (exists(configFile, SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_HA_SAR)) {
			return TYPE_PORTAL_CLUSTER;
		}
		// JBoss portletcontainer
		if (exists(configFile,SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL) ||
				exists(configFile,SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL_SAR)) {
			return TYPE_PORTLET_CONTAINER;
		}
		// GateIn Portal Server
		if (exists(configFile, SERVER_DEFAULT_DEPLOY_GATEIN)) {
			return TYPE_GATE_IN;
		}
		return TYPE_PORTAL_UNKNOWN;
	}
	
	public static String getPortalSuffix(IJBossServerRuntime runtime) {
		int type = getServerPortalType(runtime);
		if( type != TYPE_PORTAL_UNKNOWN) {
			if( type == TYPE_PORTLET_CONTAINER) 
				return SIMPLE_PORTAL_PATH;
			return PORTAL_PATH;
		}
		return null;
	}
	
	private static boolean exists(final File location,String portalDir) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			portalDir = portalDir.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		File file = new File(location,portalDir);
		return file.exists();
	}

}
