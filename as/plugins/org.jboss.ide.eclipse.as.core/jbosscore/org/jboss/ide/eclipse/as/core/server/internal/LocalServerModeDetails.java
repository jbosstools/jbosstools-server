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
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.IServerModeDetails;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

/**
 * Local servers pull most of their critical configuration locations from the runtime.
 * Technically the server is not required, only the runtime is. But for other types
 * like RSE, which store their data in the server object, the server itself is needed. 
 * 
 * Eventually even this class will be broken up on a per-version basis, 
 * but at this point it is not necessary. 
 * @since 3.0
 */
public class LocalServerModeDetails implements IServerModeDetails {

	private IServer server;
	public LocalServerModeDetails(IServer server) {
		this.server = server;
	}
	
	@Override
	public String getProperty(String prop) {
		IRuntime rt = server.getRuntime();
		if( PROP_SERVER_HOME.equals(prop)) {
			return rt == null ? null : rt.getLocation().toOSString();
		}
		IJBossServerRuntime jbrt = RuntimeUtils.getJBossServerRuntime(rt);
		if( PROP_SERVER_BASE_DIR_ABS.equals(prop)) {
			if( jbrt instanceof LocalJBoss7ServerRuntime)
				return ((LocalJBoss7ServerRuntime)jbrt).getBaseDirectory();
		}
		if( PROP_CONFIG_NAME.equals(prop)) {
			return jbrt == null ? null : jbrt.getJBossConfiguration();
		}
		if( PROP_CONFIG_LOCATION.equals(prop)) {
			return jbrt == null ? null : jbrt.getConfigLocation();
		}
		if( PROP_AS7_CONFIG_FILE.equals(prop)) {
			if( isAS7Structure()) {
				return ((LocalJBoss7ServerRuntime)jbrt).getConfigurationFile();
			}
		}
		if( PROP_SERVER_DEPLOYMENTS_FOLDER_REL.equals(prop)) {
			ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
			String s = ((JBossExtendedProperties)sep).getServerDeployLocation();
			return ServerUtil.makeRelative(server.getRuntime(), new Path(s)).toString();
		}
		if( PROP_SERVER_DEPLOYMENTS_FOLDER_ABS.equals(prop)) {
			ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
			String s = ((JBossExtendedProperties)sep).getServerDeployLocation();
			return ServerUtil.makeGlobal(server.getRuntime(), new Path(s)).toString();
		}
		if( PROP_SERVER_TMP_DEPLOYMENTS_FOLDER_REL.equals(prop) || PROP_SERVER_TMP_DEPLOYMENTS_FOLDER_ABS.equals(prop)) {
			boolean relative = PROP_SERVER_TMP_DEPLOYMENTS_FOLDER_REL.equals(prop);
			IPath p = null;
			if( isAS7Structure()) {
				LocalJBoss7ServerRuntime jb7rt = (LocalJBoss7ServerRuntime)server.getRuntime().loadAdapter(LocalJBoss7ServerRuntime.class, null);
				String basedir = jb7rt.getBaseDirectory();
				p = new Path(basedir)
					.append(IJBossRuntimeResourceConstants.FOLDER_TMP);
			} else {
				p = new Path(jbrt.getConfigLocation()).append(jbrt.getJBossConfiguration())
				.append(IJBossToolingConstants.TMP)
				.append(IJBossToolingConstants.JBOSSTOOLS_TMP);
			}
			if( relative )
				return ServerUtil.makeRelative(server.getRuntime(), p).toString();
			return ServerUtil.makeGlobal(server.getRuntime(), p).toString();
		}
		return null;
	}
	private boolean isAS7Structure() {
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		if (sep != null && sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS) {
			return true;
		}
		return false;
	}

}
