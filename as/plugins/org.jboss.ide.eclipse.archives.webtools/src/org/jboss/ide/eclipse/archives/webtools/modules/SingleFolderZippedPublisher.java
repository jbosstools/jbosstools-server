/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
 ******************************************************************************/ 
package org.jboss.ide.eclipse.archives.webtools.modules;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.wtp.core.modules.IJBTModule;

@Deprecated
public class SingleFolderZippedPublisher extends WTPZippedPublisher {
	public boolean accepts(String method, IServer server, IModule[] module) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		if( module != null && module.length > 0 
				&& module[module.length-1] != null  
				&& module[module.length-1].getModuleType().getId().equals(SingleDeployableFactory.MODULE_TYPE)) {
			IModule mod = module[module.length-1];
			IJBTModule del = (IJBTModule)mod.loadAdapter(IJBTModule.class, new NullProgressMonitor());
			if( del != null && !del.isBinary() && ds.zipsWTPDeployments())
				return true; // we have a folder
		}
		return false;
	}
}