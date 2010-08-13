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
package org.jboss.ide.eclipse.as.ssh.server;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.webtools.modules.PackageModuleFactory;
import org.jboss.ide.eclipse.as.core.publishers.AbstractServerToolsPublisher;
import org.jboss.ide.eclipse.as.ssh.server.SSHServerBehaviourDelegate.SSHPublishMethod;

public class SSHPackagesPublisher extends AbstractServerToolsPublisher {
	public boolean accepts(String method, IServer server, IModule[] module) {
		if( SSHPublishMethod.SSH_PUBLISH_METHOD.equals(method) && module != null && module.length > 0
				&& PackageModuleFactory.MODULE_TYPE.equals(module[0].getModuleType().getId()))
			return true;
		return false;
	}
}