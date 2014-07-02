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
package org.jboss.ide.eclipse.as.rse.core.subsystems;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.tools.as.core.server.controllable.systems.AbstractJBossDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;

/**
 * The remote implementation of a deployment options controller. 
 * 
 */
public class RSEDeploymentOptionsController extends
		AbstractJBossDeploymentOptionsController implements IDeploymentOptionsController {
	
	private Character separator;
	protected char getTargetSystemSeparator() {
		if( separator == null ) {
			Character c = (Character)getEnvironment().get(ENV_TARGET_OS_SEPARATOR);
			if( c == null ) {
				c = RSEUtils.getRemoteSystemSeparatorCharacter(getServerOrWC());
			}
			separator = c;
		}
		return separator.charValue();
	}
	
	public char getPathSeparatorCharacter() {
		return getTargetSystemSeparator();
	}

	
	public String makeGlobal(String original) { 
		char sep = getTargetSystemSeparator();
		IPath ret = RSEUtils.makeGlobal(getServerOrWC(), new RemotePath(original, sep), sep);
		return new RemotePath(ret.toString(), sep).toOSString();
	}

	public String makeRelative(String original) { 
		char sep = getTargetSystemSeparator();
		String ret = RSEUtils.makeRelativeString(getServerOrWC(), new RemotePath(original, sep), sep);
		return ret;
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
		if (JBossServer.DEPLOY_CUSTOM.equals(type)) {
			ret = getServerOrWC().getAttribute(JBossServer.DEPLOY_DIRECTORY, (String) null);
			if (ret != null) {
				if( isDeployOnlyServer()) {
					// must always be absolute;  RemotePath not good enough yet, cannot chain calls, needs duplication
					return new RemotePath(ret, getTargetSystemSeparator()).makeAbsolute().toOSString();
				}
				return ret;
			}
		}
		
		// only 2 other options are metadata and server.  metadata is not valid for rse
		if(isAS7Structure()){
			String baseDir = RSEUtils.getBaseDirectory(getServerOrWC(), getTargetSystemSeparator());
			IPath ret2 = new RemotePath(baseDir, getTargetSystemSeparator()).append(IJBossRuntimeResourceConstants.AS7_DEPLOYMENTS);
			return RSEUtils.makeRelative(getServerOrWC(), ret2, getTargetSystemSeparator()).toOSString();
		} else {
			String loc = IConstants.SERVER;
			String config = RSEUtils.getRSEConfigName(getServerOrWC());
			if( loc == null || config == null )
				return null;
			String p = new RemotePath(loc, getTargetSystemSeparator()).append(config)
					.append(IJBossRuntimeResourceConstants.DEPLOY).toOSString();
			return p;
		}
	}
	
	protected String getTempDeployFolder(String type) {
		// Unsupported. We do not copy to a temporary remote folder and 
		// then perform atomic moves
		return getDeployFolder(type);
	}
}
