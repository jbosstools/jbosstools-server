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
package org.jboss.ide.eclipse.as.core.server.internal;

import org.jboss.ide.eclipse.as.core.server.IJBoss6Server;
import org.jboss.tools.as.core.server.controllable.systems.IPortsController;

public class JBoss6Server extends JBossServer implements IJBoss6Server {

	public int getJMXRMIPort() {
		return findPort(IPortsController.KEY_JMX_RMI, 1090);
	}

}
