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
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerProfileInitializer;

public class LocalProfileInitializer implements IServerProfileInitializer {

	public LocalProfileInitializer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(IServerWorkingCopy wc) throws CoreException {
		wc.setAttribute(IJBossToolingConstants.STARTUP_POLLER_KEY, WebPortPoller.WEB_POLLER_ID);
		wc.setAttribute(IJBossToolingConstants.SHUTDOWN_POLLER_KEY, WebPortPoller.WEB_POLLER_ID);
		wc.setAttribute(IJBossToolingConstants.EXPOSE_MANAGEMENT_SERVICE, true);
	}

}
