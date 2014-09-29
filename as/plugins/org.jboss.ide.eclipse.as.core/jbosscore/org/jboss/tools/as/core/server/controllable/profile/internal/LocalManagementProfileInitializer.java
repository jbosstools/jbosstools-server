/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.core.server.controllable.profile.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManagerServicePoller;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerProfileInitializer;

public class LocalManagementProfileInitializer implements
		IServerProfileInitializer {

	public LocalManagementProfileInitializer() {
	}

	@Override
	public void initialize(IServerWorkingCopy wc) throws CoreException {
		String pollId = getPollerId(wc);
		wc.setAttribute(IJBossToolingConstants.STARTUP_POLLER_KEY, pollId);
		wc.setAttribute(IJBossToolingConstants.SHUTDOWN_POLLER_KEY, pollId);
		wc.setAttribute(IJBossToolingConstants.PROPERTY_ADD_DEPLOYMENT_SCANNERS, false);
		wc.setAttribute(IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, false);
		wc.setAttribute(IJBossToolingConstants.LISTEN_ALL_HOSTS, true);
		wc.setAttribute(IJBossToolingConstants.EXPOSE_MANAGEMENT_SERVICE, true);
	}
	
	private static String getPollerId(IServerWorkingCopy server) {
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		if( sep != null ) {
			boolean as7Style = sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS; 
			if( as7Style ) {
				return server.getServerType().getId().equals(IJBossToolingConstants.SERVER_WILDFLY_80) 
						? JBoss7ManagerServicePoller.WILDFLY_POLLER_ID : JBoss7ManagerServicePoller.POLLER_ID;
			}
			
		}
		return WebPortPoller.WEB_POLLER_ID;
	}
}
