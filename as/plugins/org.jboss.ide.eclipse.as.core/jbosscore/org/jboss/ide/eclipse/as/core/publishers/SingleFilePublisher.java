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
package org.jboss.ide.eclipse.as.core.publishers;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

@Deprecated
public class SingleFilePublisher extends JBoss7JSTPublisher {
	protected boolean forceZipModule(IModule[] moduleTree) {
		return false;
	}
	
	public boolean accepts(String method, IServer server, IModule[] module) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
//		if( verifyModuleType(module, SingleDeployableFactory.MODULE_TYPE)) {
//			IJBTModule del = (IJBTModule)module[module.length-1].loadAdapter(IJBTModule.class, new NullProgressMonitor());
//			if( del != null && (del.isBinary() || !ds.zipsWTPDeployments())) {
//				return true;
//			}
//		}
		return false;
	}
}
