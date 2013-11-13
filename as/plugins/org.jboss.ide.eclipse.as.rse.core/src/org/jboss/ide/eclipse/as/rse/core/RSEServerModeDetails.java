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
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerDetailsController;

public class RSEServerModeDetails extends AbstractSubsystemController implements IServerModeDetails, IServerDetailsController {
	
	public RSEServerModeDetails() {
		super();
	}
	
	@Deprecated
	public RSEServerModeDetails(IServer server) {
	}

	@Override
	public String getProperty(String prop) {
		char sep = RSEUtils.getRemoteSystemSeparatorCharacter(getServerOrWC());
		if( PROP_SERVER_HOME.equals(prop)) {
			return RSEUtils.getRSEHomeDir(getServerOrWC());
		}
		if( PROP_CONFIG_NAME.equals(prop)) {
			return RSEUtils.getRSEConfigName(getServerOrWC());
		}
		if( PROP_SERVER_BASE_DIR_ABS.equals(prop)) {
			if (isAS7Structure()) {
				return RSEUtils.getBaseDirectoryPath(getServerOrWC(), sep).toString();
			}
		}
		
		if( PROP_CONFIG_LOCATION.equals(prop)) {
			// Currently AS7 does not support custom configurations
			if (isAS7Structure()) {
				IPath p = RSEUtils.getBaseDirectoryPath(getServerOrWC(), sep).append(IJBossRuntimeResourceConstants.CONFIGURATION);
				return p.toString();
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
			// AS7's basedir may change, so pull from there
			if (isAS7Structure()) {
				String raw = getServerOrWC().getAttribute(RSEUtils.RSE_BASE_DIR, IJBossRuntimeResourceConstants.AS7_STANDALONE);
				relPath = new Path(raw).append(IJBossRuntimeResourceConstants.AS7_DEPLOYMENTS);
			} else {
				// Remote servers at this time do not allow arbitrary configuration locations. 
				// This would require UI changes and API additions. 
				relPath = getServerLT6RelativeConfigPath(IConstants.SERVER, IJBossRuntimeResourceConstants.DEPLOY);
			}
			if( relPath == null ) 
				return null;
			if( !relative) 
				return RSEUtils.makeGlobal(getServerOrWC(), relPath).toString();
			return RSEUtils.makeRelative(getServerOrWC(), relPath).toString();
		}
		if( PROP_SERVER_TMP_DEPLOYMENTS_FOLDER_REL.equals(prop) || PROP_SERVER_TMP_DEPLOYMENTS_FOLDER_ABS.equals(prop)) {
			boolean relative = PROP_SERVER_TMP_DEPLOYMENTS_FOLDER_REL.equals(prop);
			IPath relPath = null;
			if( isAS7Structure()) {
				String raw = getServerOrWC().getAttribute(RSEUtils.RSE_BASE_DIR, IJBossRuntimeResourceConstants.AS7_STANDALONE);
				if( new Path(raw).isAbsolute() && raw.startsWith(RSEUtils.getRSEHomeDir(getServerOrWC()))) {
					return new Path(raw).makeRelativeTo(new Path(RSEUtils.getRSEHomeDir(getServerOrWC()))).toString();
				}
				relPath = new Path(raw)
					.append(IJBossRuntimeResourceConstants.FOLDER_TMP).makeRelative();
			} else {
				relPath = getServerLT6RelativeConfigPath(IConstants.SERVER, IJBossToolingConstants.TMP + "/" + IJBossToolingConstants.JBOSSTOOLS_TMP);
			}
			if( relPath == null ) 
				return null;
			if( !relative) 
				return RSEUtils.makeGlobal(getServerOrWC(), relPath).toString();
			return RSEUtils.makeRelative(getServerOrWC(), relPath).toString();
		}

		
		if( PROP_AS7_CONFIG_FILE.equals(prop)) {
			return RSEUtils.getRSEConfigFile(getServerOrWC());
		}
		return null;
	}
	
	/* Get a global path which includes the config name */
	private IPath getServerLT6RelativeConfigPath(String prefix, String suffix) {
		String config = RSEUtils.getRSEConfigName(getServerOrWC());
		if( config == null )
			return null;
		IPath p = new Path(prefix).append(config);
		if( suffix != null )
			p = p.append(suffix);
		return RSEUtils.makeGlobal(getServerOrWC(), p);
	}
	
	private boolean isAS7Structure() {
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(getServerOrWC());
		if (sep != null && sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS) {
			return true;
		}
		return false;
	}
}
