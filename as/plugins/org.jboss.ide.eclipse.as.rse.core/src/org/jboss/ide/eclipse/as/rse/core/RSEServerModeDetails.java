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
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerModeDetails;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class RSEServerModeDetails implements IServerModeDetails {
	private IServer server;
	public RSEServerModeDetails(IServer server) {
		this.server = server;
	}

	@Override
	public String getProperty(String prop) {
		if( PROP_SERVER_HOME.equals(prop)) {
			return RSEUtils.getRSEHomeDir(server);
		}
		if( PROP_CONFIG_NAME.equals(prop)) {
			return RSEUtils.getRSEConfigName(server);
		}
		if( PROP_CONFIG_LOCATION.equals(prop)) {
			// Currently AS7 does not support custom configurations
			if (isAS7Structure()) {
				IPath p = new Path(IJBossRuntimeResourceConstants.AS7_STANDALONE).append(IJBossRuntimeResourceConstants.CONFIGURATION);
				return RSEUtils.makeGlobal(server, p).toString();
			} else {
				// Remote servers at this time do not allow arbitrary configuration locations. 
				// This would require UI changes and API additions. 
				IPath p = getServerLT6RelativeConfigPath(IConstants.SERVER, null);
				return p == null ? null : p.toString();
			}
		}
		if( PROP_SERVER_DEPLOYMENTS_FOLDER_REL.equals(prop) || PROP_SERVER_DEPLOYMENTS_FOLDER_ABS.equals(prop)) {
			boolean relative = PROP_SERVER_DEPLOYMENTS_FOLDER_REL.equals(prop);
			IPath relPath = null;
			// Currently AS7 does not support custom configurations
			if (isAS7Structure()) {
				relPath = new Path(IJBossRuntimeResourceConstants.AS7_STANDALONE).append(IJBossRuntimeResourceConstants.AS7_DEPLOYMENTS);
			} else {
				// Remote servers at this time do not allow arbitrary configuration locations. 
				// This would require UI changes and API additions. 
				relPath = getServerLT6RelativeConfigPath(IConstants.SERVER, IJBossRuntimeResourceConstants.DEPLOY);
			}
			if( relPath == null ) 
				return null;
			if( !relative) 
				return RSEUtils.makeGlobal(server, relPath).toString();
			return RSEUtils.makeRelative(server, relPath).toString();
		}
		if( PROP_SERVER_TMP_DEPLOYMENTS_FOLDER_REL.equals(prop) || PROP_SERVER_TMP_DEPLOYMENTS_FOLDER_ABS.equals(prop)) {
			boolean relative = PROP_SERVER_TMP_DEPLOYMENTS_FOLDER_REL.equals(prop);
			IPath relPath = null;
			if( isAS7Structure()) {
				relPath = new Path(IJBossRuntimeResourceConstants.AS7_STANDALONE)
					.append(IJBossRuntimeResourceConstants.FOLDER_TMP).makeRelative();
			} else {
				relPath = getServerLT6RelativeConfigPath(IConstants.SERVER, IJBossToolingConstants.TMP + "/" + IJBossToolingConstants.JBOSSTOOLS_TMP);
			}
			if( relPath == null ) 
				return null;
			if( !relative) 
				return RSEUtils.makeGlobal(server, relPath).toString();
			return RSEUtils.makeRelative(server, relPath).toString();
		}

		
		if( PROP_AS7_CONFIG_FILE.equals(prop)) {
			return RSEUtils.getRSEConfigFile(server);
		}
		return null;
	}
	
	/* Get a global path which includes the config name */
	private IPath getServerLT6RelativeConfigPath(String prefix, String suffix) {
		String config = RSEUtils.getRSEConfigName(server);
		if( config == null )
			return null;
		IPath p = new Path(prefix).append(config);
		if( suffix != null )
			p = p.append(suffix);
		return RSEUtils.makeGlobal(server, p);
	}
	
	private boolean isAS7Structure() {
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		if (sep != null && sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS) {
			return true;
		}
		return false;
	}
}
