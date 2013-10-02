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

import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.FOLDER_TMP;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT_DEFAULT_PORT;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT_DEFAULT_XPATH;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT_DETECT;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT_DETECT_XPATH;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.PORT_OFFSET;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.PORT_OFFSET_DEFAULT_PORT;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.PORT_OFFSET_DEFAULT_XPATH;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.PORT_OFFSET_DETECT;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.PORT_OFFSET_DETECT_XPATH;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.IManagementPortProvider;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class JBoss7Server extends JBossServer implements IJBoss7Deployment, IManagementPortProvider {
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		setAttribute(DEPLOY_DIRECTORY_TYPE, DEPLOY_SERVER);
		setAttribute(IJBossToolingConstants.WEB_PORT_DETECT, true);
		setAttribute(IJBossToolingConstants.WEB_PORT, IJBossToolingConstants.JBOSS_WEB_DEFAULT_PORT);
		setUsername(null);
		setPassword(null);
		setAttribute(IJBossToolingConstants.STARTUP_POLLER_KEY, JBoss7ManagerServicePoller.POLLER_ID);
	}
	public boolean hasJMXProvider() {
		return getExtendedProperties().getJMXProviderType() != ServerExtendedProperties.JMX_NULL_PROVIDER;
	}
	
	public int getManagementPort() {
		return getPortOffset() + findPort(AS7_MANAGEMENT_PORT, AS7_MANAGEMENT_PORT_DETECT, AS7_MANAGEMENT_PORT_DETECT_XPATH, 
				AS7_MANAGEMENT_PORT_DEFAULT_XPATH, AS7_MANAGEMENT_PORT_DEFAULT_PORT);
	}
	
	/*
	 * Only truly applicable for AS7.1, EAP6, etc. AS7.0 has no support for this, 
	 * however, the findPort will return 0.
	 */
	@Override
	protected int getPortOffset() {
		return findPort(PORT_OFFSET, PORT_OFFSET_DETECT, PORT_OFFSET_DETECT_XPATH, 
				PORT_OFFSET_DEFAULT_XPATH, PORT_OFFSET_DEFAULT_PORT);
	}

	@Override
	public String getDeployLocationType() {
		return getAttribute(DEPLOY_DIRECTORY_TYPE, DEPLOY_SERVER);
	}

	public String getDeployFolder(String type) {
		return super.getDeployFolder(type);
	}
	
	public String getTempDeployFolder() {
		String type = getDeployLocationType();
		if( DEPLOY_SERVER.equals(type)) {
			IRuntime rt = getServer().getRuntime();
			LocalJBoss7ServerRuntime jb7rt = (LocalJBoss7ServerRuntime)rt.loadAdapter(LocalJBoss7ServerRuntime.class, null);
			IPath p = new Path(jb7rt.getBaseDirectory()).append(FOLDER_TMP);
			return ServerUtil.makeGlobal(rt, p).toString();
		}
		return getTempDeployFolder(this, type);
	}
}
