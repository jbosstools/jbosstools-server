/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.WILDFLY8_MANAGEMENT_PORT_DEFAULT_PORT;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT_DEFAULT_XPATH;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT_DETECT;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS7_MANAGEMENT_PORT_DETECT_XPATH;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class Wildfly8Server extends JBoss7Server {
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		setAttribute(IJBossToolingConstants.STARTUP_POLLER_KEY, JBoss7ManagerServicePoller.WILDFLY_POLLER_ID);
	}
	
	public int getManagementPort() {
		return getPortOffset() + findPort(AS7_MANAGEMENT_PORT, AS7_MANAGEMENT_PORT_DETECT, AS7_MANAGEMENT_PORT_DETECT_XPATH, 
				AS7_MANAGEMENT_PORT_DEFAULT_XPATH, WILDFLY8_MANAGEMENT_PORT_DEFAULT_PORT);
	}
}
