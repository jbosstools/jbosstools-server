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

import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.AS7_DEPLOYMENTS;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.AS7_STANDALONE;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.FOLDER_TMP;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT_DEFAULT_PORT;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT_DEFAULT_XPATH;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT_DETECT;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT_DETECT_XPATH;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class JBoss7Server extends JBossServer implements IJBoss7Deployment {
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		setAttribute(DEPLOY_DIRECTORY_TYPE, DEPLOY_SERVER);
		setAttribute(IJBossToolingConstants.WEB_PORT_DETECT, true);
		setAttribute(IJBossToolingConstants.WEB_PORT, IJBossToolingConstants.JBOSS_WEB_DEFAULT_PORT);
		setUsername(null);
		setPassword(null);
		// In an emergency, we can switch pollers for EAP
		if( isEAP(getServer()))
			setAttribute(IJBossToolingConstants.STARTUP_POLLER_KEY, WebPortPoller.WEB_POLLER_ID);
		else
			setAttribute(IJBossToolingConstants.STARTUP_POLLER_KEY, JBoss7ManagerServicePoller.POLLER_ID);
	}
	public boolean hasJMXProvider() {
		return false;
	}
	
	public int getManagementPort() {
		return findPort(AS7_MANAGEMENT_PORT, AS7_MANAGEMENT_PORT_DETECT, AS7_MANAGEMENT_PORT_DETECT_XPATH, 
				AS7_MANAGEMENT_PORT_DEFAULT_XPATH, AS7_MANAGEMENT_PORT_DEFAULT_PORT);
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
		IRuntime rt = getServer().getRuntime();
		IPath p = rt.getLocation().append(AS7_STANDALONE).append(FOLDER_TMP);
		return ServerUtil.makeGlobal(rt, p).toString();
	}
}
