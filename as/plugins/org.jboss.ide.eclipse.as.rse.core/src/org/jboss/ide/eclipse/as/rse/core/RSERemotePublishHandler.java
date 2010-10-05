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
package org.jboss.ide.eclipse.as.rse.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.wst.common.project.facet.core.util.internal.ProgressMonitorUtil;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;

public class RSERemotePublishHandler implements IPublishCopyCallbackHandler {
	protected IPath root;
	protected RSEPublishMethod method;
	private ArrayList<IPath> createdFolders = new ArrayList<IPath>(); 
	public RSERemotePublishHandler(IPath path, RSEPublishMethod method) {
		this.root = path;
		this.method = method;
	}
	private boolean shouldRestartModule = false;
	public boolean shouldRestartModule() {
		return shouldRestartModule;
	}
	public IStatus[] copyFile(IModuleFile mf, IPath path,
			IProgressMonitor monitor) throws CoreException {
		File file = PublishUtil.getFile(mf);
		shouldRestartModule |= PublishCopyUtil.checkRestartModule(file);
		IPath remotePath = root.append(path);
		try {
			method.getFileService().upload(file, remotePath.removeLastSegments(1).toString(), 
					remotePath.lastSegment(), true, null, null, monitor);
		} catch( SystemMessageException sme ) {
			System.err.println("failed to copy to " + remotePath.toString()); //$NON-NLS-1$
		}
		return null;
	}

	public IStatus[] deleteResource(IPath path, IProgressMonitor monitor)
			throws CoreException {
		IPath remotePath = root.append(path);
		try {
			method.getFileService().delete(remotePath.removeLastSegments(1).toString(), remotePath.lastSegment(), monitor);
		} catch( SystemMessageException sme ) {
			System.err.println("failed to delete " + remotePath.toString()); //$NON-NLS-1$
		}
		return null;
	}

	public IStatus[] makeDirectoryIfRequired(IPath dir,
			IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Make directory " + dir.toString(), 100); //$NON-NLS-1$
		if( dir.segmentCount() > 0 )
			makeDirectoryIfRequired(dir.removeLastSegments(1), ProgressMonitorUtil.submon(monitor, 70));
		IPath toMake = root.append(dir);
		if( createdFolders.contains(toMake)) 
			return new IStatus[]{Status.OK_STATUS};
		try {
			if( toMake.segmentCount() > 0 ) {
				method.getFileService().createFolder(toMake.removeLastSegments(1).toString(), 
						toMake.lastSegment(), ProgressMonitorUtil.submon(monitor, 30));
			}
		} catch( SystemMessageException sme ) {
			System.err.println("failed to make folder " + toMake.toString()); //$NON-NLS-1$
		}
		createdFolders.add(toMake);
		monitor.done();
		return null;
	}

	public IStatus[] touchResource(IPath path) {
		IPath file = root.append(path);
		try {
			IRemoteFile rf = method.getFileServiceSubSystem().getRemoteFileObject(file.toString(), new NullProgressMonitor());
			if( !rf.exists()) {
				rf = method.getFileServiceSubSystem().getRemoteFileObject(root.toString(), new NullProgressMonitor());
			}
			method.getFileServiceSubSystem().setLastModified(rf, new Date().getTime(), null);
		} catch(SystemMessageException sme) {
		}
		return null;
	}
}

