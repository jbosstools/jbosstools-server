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
package org.jboss.ide.eclipse.as.jmx.integration;

import java.util.List;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IProvideCredentials;
import org.jboss.ide.eclipse.as.core.server.IServerProvider;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.launch.UserPassCredentialProvider;

/**
 * 
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class SecurityFailureHandler extends UserPassCredentialProvider implements IProvideCredentials {

	@Override
	public boolean accepts(IServerProvider serverProvider,
			List<String> requiredProperties) {
		if( requiredProperties.size() > 2)
			return false;
		IServer s = serverProvider.getServer();
		IJBossServer jbs = ServerConverter.getJBossServer(s);
		if( jbs != null && jbs.hasJMXProvider())
			return true;
		return false;
	}
}
