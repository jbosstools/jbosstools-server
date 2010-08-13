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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.archives.webtools.modules.WTPZippedPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ssh.server.SSHServerBehaviourDelegate.SSHPublishMethod;

import com.jcraft.jsch.Session;

public class SSHZippedJSTPublisher extends WTPZippedPublisher {
	protected String getPublishMethod() {
		return SSHPublishMethod.SSH_PUBLISH_METHOD;
	}
	
	public IStatus publishModule(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		IStatus s = super.publishModule(method, server, module, publishType, delta, monitor);
		// set up needed vars
		IDeployableServer server2 = ServerConverter.getDeployableServer(server);
		String builtArchivesFolder = getDeployRoot(module, ServerConverter.getDeployableServer(server));
		IPath builtArchive = PublishUtil.getDeployPath(module, builtArchivesFolder);
		String defaultDeployRoot = ((SSHPublishMethod)method).getPublishDefaultRootFolder(server);
		String folder = PublishUtil.getDeployRootFolder(
				module, server2, defaultDeployRoot,
				SSHPublishMethod.SSH_PUBLISH_METHOD);
		IPath remoteFile = PublishUtil.getDeployPath(module, folder);
		Session session = ((SSHPublishMethod)method).getSession();

		// Now transfer the file via ssh
		SSHCommandUtil.launchCopyCommand(session, builtArchive.toString(), remoteFile.toString(), monitor);
		return s;
	}
	
	
}
