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

public class JBoss6Server extends JBossServer implements IJBoss6Server {

	public int getJMXRMIPort() {
		return findPort(JMX_RMI_PORT, JMX_RMI_PORT_DETECT, JMX_RMI_PORT_DETECT_XPATH, 
				JMX_RMI_PORT_DEFAULT_XPATH, JMX_RMI_DEFAULT_PORT);
	}

}
