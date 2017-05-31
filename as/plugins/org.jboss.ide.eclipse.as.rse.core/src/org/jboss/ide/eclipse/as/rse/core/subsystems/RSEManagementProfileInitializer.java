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
package org.jboss.ide.eclipse.as.rse.core.subsystems;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManagerServicePoller;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerProfileInitializer;

public class RSEManagementProfileInitializer implements IServerProfileInitializer {

	@Override
	public void initialize(IServerWorkingCopy wc) throws CoreException {
		boolean as7Style = isJBoss7Style(wc);
		String pollId = WebPortPoller.WEB_POLLER_ID;
		if( as7Style ) {
			// as7 rse has following settings
			wc.setAttribute(IJBossToolingConstants.LISTEN_ALL_HOSTS, true);
			wc.setAttribute(IJBossToolingConstants.EXPOSE_MANAGEMENT_SERVICE, true);

			// adding deployment scanners in as7/wf requires management, but, we do management-based deployment,
			// and so we don't add deployment scanners for filesystem paths. 
			wc.setAttribute(IJBossToolingConstants.PROPERTY_ADD_DEPLOYMENT_SCANNERS, false);
			wc.setAttribute(IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, false);
			pollId = getPollerType(wc.getServerType());
		} 
		wc.setAttribute(IJBossToolingConstants.STARTUP_POLLER_KEY, pollId);
		wc.setAttribute(IJBossToolingConstants.SHUTDOWN_POLLER_KEY, pollId);
	}
	
	private String getPollerType(IServerType type) {
		String[] serverTypesJBoss7 = new String[] {"org.jboss.ide.eclipse.as.70",
				"org.jboss.ide.eclipse.as.71",
				"org.jboss.ide.eclipse.as.eap.60","org.jboss.ide.eclipse.as.eap.61"};
		if( Arrays.asList(serverTypesJBoss7).contains(type.getId())) {
			return JBoss7ManagerServicePoller.POLLER_ID;
		}
		return JBoss7ManagerServicePoller.WILDFLY_POLLER_ID;
	}
	
	private static boolean isJBoss7Style(IServerWorkingCopy server) {
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		if( sep == null )
			return false;
		boolean as7Style = sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS; 
		return as7Style;
	}
}
