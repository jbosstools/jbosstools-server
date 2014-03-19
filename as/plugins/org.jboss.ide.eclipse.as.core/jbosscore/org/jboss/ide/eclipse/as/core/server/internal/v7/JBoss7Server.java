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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.IManagementPortProvider;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.tools.as.core.server.controllable.systems.IPortsController;

public class JBoss7Server extends JBossServer implements IJBoss7Deployment, IManagementPortProvider {
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		setAttribute(DEPLOY_DIRECTORY_TYPE, DEPLOY_SERVER);
		setUsername(null);
		setPassword(null);
		setAttribute(IJBossToolingConstants.STARTUP_POLLER_KEY, JBoss7ManagerServicePoller.POLLER_ID);
		setAttribute(IJBossToolingConstants.SHUTDOWN_POLLER_KEY, JBoss7ManagerServicePoller.POLLER_ID);
	}
	public boolean hasJMXProvider() {
		return getExtendedProperties().getJMXProviderType() != ServerExtendedProperties.JMX_NULL_PROVIDER;
	}
	
	public int getManagementPort() {
		return findPort(IPortsController.KEY_MANAGEMENT_PORT, 9999);
	}
	
	@Override
	protected int getPortOffset() {
		return findPort(IPortsController.KEY_PORT_OFFSET, 0);
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
			if( rt != null ) {
				LocalJBoss7ServerRuntime jb7rt = (LocalJBoss7ServerRuntime)rt.loadAdapter(LocalJBoss7ServerRuntime.class, null);
				IPath p = new Path(jb7rt.getBaseDirectory()).append(FOLDER_TMP);
				return ServerUtil.makeGlobal(rt, p).toString();
			}
		}
		return getTempDeployFolder(this, type);
	}
}
