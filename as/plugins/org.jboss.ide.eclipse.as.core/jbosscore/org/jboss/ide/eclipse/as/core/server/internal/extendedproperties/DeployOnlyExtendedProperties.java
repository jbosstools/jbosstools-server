/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class DeployOnlyExtendedProperties extends ServerExtendedProperties {

	public DeployOnlyExtendedProperties(IAdaptable adaptable) {
		super(adaptable);
	}
	
	public boolean hasWelcomePage() {
		return true;
	}
	
	public String getWelcomePageUrl() throws GetWelcomePageURLException {
		try {
			DeployableServer dServer = ServerUtil.checkedGetServerAdapter(server, DeployableServer.class);
			int webPort = dServer.getPublicWebPort();
			String consoleUrl = ServerUtil.createSafeURLString("http", server.getHost(), webPort, null); //$NON-NLS-1$
			return consoleUrl;
		} catch(CoreException ce) {
			return null;
		}
	}
}
