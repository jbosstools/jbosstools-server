/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.archives.webtools.modules;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher;

@Deprecated
public class PackagesPublisher extends JBoss7JSTPublisher {
	public boolean accepts(String method, IServer server, IModule[] module) {
//		if( LocalPublishMethod.LOCAL_PUBLISH_METHOD.equals(method) && 
//				verifyModuleType(module, PackageModuleFactory.MODULE_TYPE))
//			return true;
		return false;
	}
}
