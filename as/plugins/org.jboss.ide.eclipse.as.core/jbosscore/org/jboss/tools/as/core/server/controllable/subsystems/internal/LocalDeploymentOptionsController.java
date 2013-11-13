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
package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.FOLDER_TMP;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.JBOSSTOOLS_TMP;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.TEMP_DEPLOY;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.TMP;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.tools.as.core.server.controllable.systems.AbstractJBossDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;

/**
 * The local implementation of a deployment options controller. 
 * 
 */
public class LocalDeploymentOptionsController extends
		AbstractJBossDeploymentOptionsController implements IDeploymentOptionsController {
	
	protected String makeGlobal(IServerAttributes server, String original) {
		IPath p = ServerUtil.makeGlobal(server.getRuntime(), new Path(original));
		return p.toOSString();
	}

	protected String makeRelative(IServerAttributes server, String original) {
		IPath p = ServerUtil.makeRelative(server.getRuntime(), new Path(original));
		return p.toOSString();
	}

	@Override
	public String getCurrentDeploymentLocationType() {
		boolean isDepOnly = getServer().getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER);
		if( isDepOnly )
			return DEPLOY_CUSTOM;
		return getServerOrWC().getAttribute(DEPLOY_DIRECTORY_TYPE, DEPLOY_SERVER);
	}

	protected String getDeployFolder(String type) {
		String ret = null;
		if( type.equals(DEPLOY_CUSTOM)) {
			ret = getServerOrWC().getAttribute(DEPLOY_DIRECTORY, (String)null);
			boolean isDepOnly = getServer().getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER);
			if( isDepOnly ){
				// Deploy-only cannot be 'relative' to anything, so must always be absolute
				//Really think OSPath needs to be an entire clone impl for all the methods to work
				ret = new Path(ret).makeAbsolute().toOSString();
			}
		}
		if( type.equals(DEPLOY_SERVER)) {
			return makeRelative(getServerOrWC(), getExtendedProperties().getServerDeployLocation());
		}
		if( ret == null || type.equals(DEPLOY_METADATA)) {
			ret = getMetadataDeployLocation(getServer());
		} 
		return ret;
	}
	

	protected String getTempDeployFolder(String type) {
		if( type.equals(DEPLOY_CUSTOM)) {
			String ret = getServer().getAttribute(TEMP_DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
			boolean isDepOnly = getServer().getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER);
			if( isDepOnly ){
				return new Path(ret).makeAbsolute().toOSString();
			}
			return ret;
		}
		if( type.equals(DEPLOY_METADATA)) {
			return JBossServerCorePlugin.getServerStateLocation(getServer()).
				append(TEMP_DEPLOY).makeAbsolute().toString();
		} else if( type.equals(DEPLOY_SERVER)) {
			String ret = null;
			if( isAS7Structure()) {
				ret = getAS7StyleServerTempFolder();
			} else {
				ret =getASLessThan7StyleServerTempFolder();
			}
			return makeRelative(getServerOrWC(), ret);
		}
		return null;
	}
	
	protected String getAS7StyleServerTempFolder() {
		IRuntime rt = getServer().getRuntime();
		LocalJBoss7ServerRuntime jb7rt = (LocalJBoss7ServerRuntime)rt.loadAdapter(LocalJBoss7ServerRuntime.class, null);
		IPath p = new Path(jb7rt.getBaseDirectory()).append(FOLDER_TMP);
		return p.toString();
	}
	protected String getASLessThan7StyleServerTempFolder() {
		// AS6 and below
		IJBossServerRuntime jbsrt = RuntimeUtils.getJBossServerRuntime(getServer());
		String loc = jbsrt.getConfigLocation();
		String config = jbsrt.getJBossConfiguration();
		IPath p = new Path(loc).append(config).append(TMP).append(JBOSSTOOLS_TMP);
		return p.toString();
	}

	public char getPathSeparatorCharacter() {
		return java.io.File.separatorChar;
	}
}
