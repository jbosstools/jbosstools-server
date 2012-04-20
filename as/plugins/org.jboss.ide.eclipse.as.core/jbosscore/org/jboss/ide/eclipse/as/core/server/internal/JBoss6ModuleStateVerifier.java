/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import javax.management.MBeanServerConnection;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;

public class JBoss6ModuleStateVerifier  extends JBossLT6ModuleStateVerifier implements IServerModuleStateVerifier {

	protected boolean checkNestedWebModuleStarted(IServer server, IModule module, MBeanServerConnection connection) throws Exception {
		boolean val = checkStandaloneWebModuleStarted(server, module, connection);
		return val;
	}

	protected boolean checkStandaloneWebModuleStarted(IServer server, IModule module, MBeanServerConnection connection) throws Exception {
		String mbeanName = "jboss.web:J2EEApplication=none,J2EEServer=none,j2eeType=WebModule,name=//localhost/" + module.getName(); //$NON-NLS-1$
		String stateAttribute = "state"; //$NON-NLS-1$
		Object result = getAttributeResult(connection, mbeanName, stateAttribute);
		if(result == null || !(result instanceof Integer) || ((Integer)result).intValue() != 1 ) {
			return false;
		}
		return true;
	}

}
