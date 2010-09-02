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

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.ssh.server.SSHServerBehaviourDelegate.SSHPublishMethod;

import com.jcraft.jsch.Session;

public class SSHCopyCallback implements IPublishCopyCallbackHandler {

	private IPath root;
	private SSHPublishMethod method;
	public SSHCopyCallback(IPath deployRoot, SSHPublishMethod method) {
		this.root = deployRoot;
		this.method = method;
	}
	private boolean shouldRestartModule = false;
	public boolean shouldRestartModule() {
		return shouldRestartModule;
	}

	public IStatus[] copyFile(IModuleFile mf, IPath path,
			IProgressMonitor monitor) throws CoreException {
		File sourceFile = PublishUtil.getFile(mf);
		shouldRestartModule |= PublishCopyUtil.checkRestartModule(sourceFile);
		IPath destination = root.append(path);
		String parentFolder = destination.removeLastSegments(1).toString();
		SSHCommandUtil.launchCommand(getSession(), "mkdir -p " + parentFolder, new NullProgressMonitor());
		SSHCommandUtil.launchCopyCommand(getSession(), sourceFile.getAbsolutePath(), destination.toString(), new NullProgressMonitor());
		return new IStatus[]{};
	}

	protected Session getSession() {
		return method.getSession();
	}
	
	public IStatus[] deleteResource(IPath path, IProgressMonitor monitor) {
		IPath remotePath = root.append(path);
		try {
			SSHCommandUtil.launchCommand(method.getSession(), "rm -rf " + remotePath.toString(), monitor);
		} catch( CoreException ce ) {
			return new IStatus[]{ce.getStatus()};
		}
		return new IStatus[] {};
	}

	public IStatus[] makeDirectoryIfRequired(IPath dir, IProgressMonitor monitor) {
		IPath remotePath = root.append(dir);
		try {
			SSHCommandUtil.launchCommand(method.getSession(), "mkdir -p " + remotePath.toString(), monitor);
		} catch( CoreException ce ) {
			return new IStatus[]{ce.getStatus()};
		}
		return new IStatus[] {};
	}

	public IStatus[] touchResource(IPath path) {
		// not implemented
		return null;
	}
}