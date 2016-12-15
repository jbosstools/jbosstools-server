/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.core.subsystems;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.ide.eclipse.as.rse.core.IFileServiceProvider;
import org.jboss.ide.eclipse.as.rse.core.RSEFrameworkUtils;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.rse.core.util.RemoteCallWrapperUtility;
import org.jboss.ide.eclipse.as.rse.core.util.RemoteCallWrapperUtility.NamedRunnableWithProgress;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;

public class RSEFilesystemController extends AbstractSubsystemController implements IFilesystemController, IFileServiceProvider  {
	/**
	 * This variable is here to help ensure that multiple remote requests to create
	 * the same directory are not made. This ensures each folder is only
	 * created once. 
	 */
	private ArrayList<IPath> createdFolders = new ArrayList<IPath>(); 
	
	/**
	 * A file subsystem for the given server
	 */
	private IFileServiceSubSystem fileSubSystem = null;

	
	public RSEFilesystemController() {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController#copyFile(java.io.File, org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus copyFile(final File file, final IPath absolutePath,
			final IProgressMonitor monitor) throws CoreException {
		NamedRunnableWithProgress run = new NamedRunnableWithProgress("Copy file to remote location: " + absolutePath.toOSString()) {
			public Object run(IProgressMonitor monitor) throws CoreException,
					SystemMessageException, RuntimeException {
				getFileService().upload(file, absolutePath.removeLastSegments(1).toOSString(), 
						absolutePath.lastSegment(), true, null, null, monitor);
				return Status.OK_STATUS;
			}
		};
		
		return RemoteCallWrapperUtility.wrapRemoteCall(getServer(), run, absolutePath.toOSString(), "failed to copy to {0} on host {1}",true, monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController#deleteResource(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus deleteResource(final IPath remotePath, final IProgressMonitor monitor)
			throws CoreException {
		NamedRunnableWithProgress run = new NamedRunnableWithProgress("Delete remote file: " + remotePath.toOSString()) {
			public Object run(IProgressMonitor monitor) throws CoreException,
					SystemMessageException, RuntimeException {
				getFileService().delete(remotePath.removeLastSegments(1).toOSString(), remotePath.lastSegment(), monitor);
				return Status.OK_STATUS;
			}
		};
		
		return RemoteCallWrapperUtility.wrapRemoteCall(getServer(), run, remotePath.toOSString(), "failed to delete {0} on host {1}", false, monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController#makeDirectoryIfRequired(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus makeDirectoryIfRequired(final IPath absolutePath,
			final IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Make directory " + absolutePath.toOSString(), 100); //$NON-NLS-1$
		
		if(createdFolders.contains(absolutePath)) 
			return Status.OK_STATUS;
		if(exists(absolutePath, monitor))
			return Status.OK_STATUS;
		if( absolutePath.segmentCount() > 0 )
			makeDirectoryIfRequired(absolutePath.removeLastSegments(1), ProgressMonitorUtil.submon(monitor, 70));

		NamedRunnableWithProgress run = new NamedRunnableWithProgress("Create Remote Directory: " + absolutePath.toString()) {
			public Object run(IProgressMonitor monitor) throws CoreException,
					SystemMessageException, RuntimeException {
				if( absolutePath.segmentCount() > 0 ) {
					getFileService().createFolder(absolutePath.removeLastSegments(1).toString(), 
							absolutePath.lastSegment(), monitor);
					createdFolders.add(absolutePath);
				}
				return Status.OK_STATUS;
			}
		};
		return RemoteCallWrapperUtility.wrapRemoteCall(getServer(), run, absolutePath.toString(), "failed to create folder {0} on host {1}", false, monitor);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController#touchResource(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus touchResource(final IPath absolutePath, IProgressMonitor monitor) {
		NamedRunnableWithProgress run = new NamedRunnableWithProgress("Touch remote resource" + absolutePath.toOSString()) {
			public Object run(IProgressMonitor monitor) throws CoreException,
					SystemMessageException, RuntimeException {
				IRemoteFile rf = 	getFileServiceSubSystem().getRemoteFileObject(absolutePath.toOSString(), new NullProgressMonitor());
				if( !rf.exists()) {
					getFileServiceSubSystem().createFile(rf, new NullProgressMonitor());
				} else {
					// Is this logical? Should we use the current system's time?
					getFileServiceSubSystem().setLastModified(rf, new Date().getTime(), null);
				}
				return Status.OK_STATUS;
			}
		};
		try {
			return RemoteCallWrapperUtility.wrapRemoteCall(getServer(), run, absolutePath.toString(), "failed to touch resource {0} on host {1}", false, monitor);
		} catch(CoreException ce) {
			return ce.getStatus();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController#isFile(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean isFile(final IPath absolutePath, final IProgressMonitor monitor)
			throws CoreException {
		
		final Boolean[] boolRet = new Boolean[1];
		boolRet[0] = null;
		
		NamedRunnableWithProgress run = new NamedRunnableWithProgress("Verify remote file exists: " + absolutePath.toOSString()) {
			public Object run(IProgressMonitor monitor) throws CoreException,
					SystemMessageException, RuntimeException {
				IRemoteFile rf = getFileServiceSubSystem().getRemoteFileObject(absolutePath.toOSString(), new NullProgressMonitor());
				boolRet[0] = rf.exists() && rf.isFile();
				return Status.OK_STATUS;
			}
		};

		RemoteCallWrapperUtility.wrapRemoteCall(getServer(), run, absolutePath.toOSString(), "failed to verify the existence of {0} on host {1}", true, monitor);
		return boolRet[0];
	}
	
	
	/*
	 * Load the file service subsystem
	 */
	public IFileServiceSubSystem getFileServiceSubSystem() throws CoreException {
		if( fileSubSystem == null ) {
			// initialize 
			Trace.trace(Trace.STRING_FINER, "Ensuring RSE is initialized");
			RSEFrameworkUtils.waitForFullInit();
			String connectionName = RSEUtils.getRSEConnectionName(getServer());
			IHost host = RSEFrameworkUtils.findHost(connectionName);
			if( host == null )
				throw new CoreException(new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, "RSE Host Not Found."));
			fileSubSystem = RSEFrameworkUtils.findFileTransferSubSystem(host);
		}
		return fileSubSystem;
	}
	
	/*
	 * Get the file service
	 */
	public IFileService getFileService() throws CoreException {
		return getFileServiceSubSystem().getFileService();
	}

	@Override
	public boolean exists(final IPath absolutePath, IProgressMonitor monitor)
			throws CoreException {
		
		final Boolean[] boolRet = new Boolean[1];
		boolRet[0] = null;
		
		NamedRunnableWithProgress run = new NamedRunnableWithProgress("Verify remote file exists: " + absolutePath.toOSString()) {
			public Object run(IProgressMonitor monitor) throws CoreException,
					SystemMessageException, RuntimeException {
				IRemoteFile rf = getFileServiceSubSystem().getRemoteFileObject(absolutePath.toOSString(), new NullProgressMonitor());
				boolRet[0] = rf.exists();
				return Status.OK_STATUS;
			}
		};

		RemoteCallWrapperUtility.wrapRemoteCall(getServer(), run, absolutePath.toOSString(), "failed to verify the existence of {0} on host {1}", true, monitor);
		return boolRet[0];
	}
}

