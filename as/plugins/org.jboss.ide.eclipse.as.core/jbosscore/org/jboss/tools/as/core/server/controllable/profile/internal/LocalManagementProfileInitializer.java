/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
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
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerProfileInitializer;

public class LocalManagementProfileInitializer implements
		IServerProfileInitializer {

	public LocalManagementProfileInitializer() {
	}

	@Override
	public void initialize(IServerWorkingCopy wc) throws CoreException {
		wc.setAttribute(IJBossToolingConstants.PROPERTY_ADD_DEPLOYMENT_SCANNERS, false);
		wc.setAttribute(IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, false);
		wc.setAttribute(IJBossToolingConstants.LISTEN_ALL_HOSTS, true);
		wc.setAttribute(IJBossToolingConstants.EXPOSE_MANAGEMENT_SERVICE, true);
	}

}
