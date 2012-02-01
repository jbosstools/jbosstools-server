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
import org.jboss.ide.eclipse.as.core.util.ServerConverter;


public class LocalPublishMethod extends AbstractPublishMethod {
	/**
	 * TODO: refactor to "LOCAL" since we have local publishing and local 
	 * launching/stopping which both have to be mapped in equal ways
	 */
	public static final String LOCAL_PUBLISH_METHOD = "local";  //$NON-NLS-1$
	
	public String getPublishMethodId() {
		return LOCAL_PUBLISH_METHOD;
	}

	public IPublishCopyCallbackHandler getCallbackHandler(IPath path, IServer server) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		String tempDeployPath = ds.getTempDeployFolder();
		LocalCopyCallback handler = new LocalCopyCallback(server, 
				path, new Path(tempDeployPath));
		return handler;
	}

	public String getPublishDefaultRootFolder(IServer server) {
		IDeployableServer s = ServerConverter.getDeployableServer(server);
		return s.getDeployFolder();
	}
}
