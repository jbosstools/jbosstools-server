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
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.wtp.core.modules.IJBTModule;

/**
 * This class is in charge of RSE zipped publishing for deployable folders
 */
@Deprecated
public class AltMethodZippedFolderPublisher extends AltMethodZippedJSTPublisher {
	public boolean accepts(String method, IServer server, IModule[] module) {
		if( LocalPublishMethod.LOCAL_PUBLISH_METHOD.equals(method))
			return false;
		
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		if( module != null && module.length > 0 
				&& module[module.length-1] != null  
				&& module[module.length-1].getModuleType().getId().equals(SingleDeployableFactory.MODULE_TYPE)) {
			IModule mod = module[module.length-1];
			IJBTModule del = (IJBTModule)mod.loadAdapter(IJBTModule.class, new NullProgressMonitor());
			if( del != null && !del.isBinary())
				return ds != null && ds.zipsWTPDeployments();; // we have a folder
		}
		return false;
	}
}
