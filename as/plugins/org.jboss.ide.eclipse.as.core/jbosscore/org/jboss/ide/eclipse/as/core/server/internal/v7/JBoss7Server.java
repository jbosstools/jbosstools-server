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
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class JBoss7Server extends JBossServer implements IJBoss7Deployment {
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		setAttribute(DEPLOY_DIRECTORY_TYPE, DEPLOY_SERVER);
		setAttribute(IJBossToolingConstants.WEB_PORT_DETECT, false);
		setAttribute(IJBossToolingConstants.WEB_PORT, IJBossToolingConstants.JBOSS_WEB_DEFAULT_PORT);
		setAttribute(IJBossToolingConstants.STARTUP_POLLER_KEY, JBoss7ManagerServicePoller.POLLER_ID);
	}
	public boolean hasJMXProvider() {
		return false;
	}
	
	@Override
	public String getDeployLocationType() {
		return getAttribute(DEPLOY_DIRECTORY_TYPE, DEPLOY_SERVER);
	}

	public String getDeployFolder(String type) {
		if( type.equals(DEPLOY_SERVER) ) {
			// TODO make sure this is correct?! Upstream APIs have this wrong for as7
			IRuntime rt = getServer().getRuntime();
			IPath p = rt.getLocation().append(AS7_STANDALONE).append(AS7_DEPLOYMENTS);
			return ServerUtil.makeGlobal(rt, p).toString();
		}
		return getDeployFolder(this, type);
	}
	
	// Just force it to be in metadata location, for now
	public String getTempDeployFolder() {
		IPath p = JBossServerCorePlugin.getServerStateLocation(getServer()).
				append(IJBossServerConstants.TEMP_DEPLOY).makeAbsolute();
		if( !p.toFile().exists()) {
			p.toFile().mkdirs();
		}
		return p.toString();
	}
}
