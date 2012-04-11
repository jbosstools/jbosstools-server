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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;

public class JBoss6ModuleStateVerifier /* extends JBossLT6ModuleStateVerifier */ implements IServerModuleStateVerifier {

	@Override
	public boolean isModuleStarted(IServer server, IModule module,
			IProgressMonitor monitor) {
		// NO IDEA
		return true;
	}

	@Override
	public void waitModuleStarted(IServer server, IModule module,
			IProgressMonitor monitor) {
		return;
	}

	@Override
	public void waitModuleStarted(IServer server, IModule module, int maxDelay) {
		return;	
	}
	
	// If proper mbeans are found, uncomment this and customize it
	
//	protected boolean checkNestedWebModuleStarted(IServer server, IModule module, MBeanServerConnection connection) throws Exception {
//		String mbeanName = "jboss.deployment:id=\"jboss.web.deployment:war=/" + module.getName() + "\",type=Component";  //$NON-NLS-1$//$NON-NLS-2$
//		String stateAttribute = "State"; //$NON-NLS-1$
//		Object result = getAttributeResult(connection, mbeanName, stateAttribute);
//		if( result == null || !result.toString().equals("DEPLOYED"))  //$NON-NLS-1$
//			return false;
//		return true;
//	}
//
//	protected boolean checkStandaloneWebModuleStarted(IServer server, IModule module, MBeanServerConnection connection) throws Exception {
//		String mbeanName = "jboss.web:J2EEApplication=none,J2EEServer=none,j2eeType=WebModule,name=//localhost/" + module.getName(); //$NON-NLS-1$
//		String stateAttribute = "state"; //$NON-NLS-1$
//		Object result = getAttributeResult(connection, mbeanName, stateAttribute);
//		if(result == null || !(result instanceof Integer) || ((Integer)result).intValue() != 1 ) {
//			return false;
//		}
//		return true;
//	}

}
