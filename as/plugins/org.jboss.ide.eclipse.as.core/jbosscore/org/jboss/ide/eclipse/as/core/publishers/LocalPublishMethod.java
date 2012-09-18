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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.server.xpl.LocalCopyCallback;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;


public class LocalPublishMethod extends AbstractPublishMethod {
	public static final String LOCAL_PUBLISH_METHOD = IJBossToolingConstants.DEFAULT_DEPLOYMENT_METHOD_TYPE;
	
	public String getPublishMethodId() {
		return LOCAL_PUBLISH_METHOD;
	}

	public IPublishCopyCallbackHandler getCallbackHandler(IPath path, IServer server) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		String tempDeployPath = ds.getTempDeployFolder();
		return getCallbackHandler(path, new Path(tempDeployPath), server);
	}
	
	public IPublishCopyCallbackHandler getCallbackHandler(IPath deployPath,
			IPath tmpFolder, IServer server) {
		LocalCopyCallback handler = new LocalCopyCallback(
				server, deployPath, tmpFolder);
		return handler;
	}


	public String getPublishDefaultRootFolder(IServer server) {
		IDeployableServer s = ServerConverter.getDeployableServer(server);
		return s.getDeployFolder();
	}

	public String getPublishDefaultRootTempFolder(IServer server) {
		IDeployableServer s = ServerConverter.getDeployableServer(server);
		return s.getTempDeployFolder();
	}

}
