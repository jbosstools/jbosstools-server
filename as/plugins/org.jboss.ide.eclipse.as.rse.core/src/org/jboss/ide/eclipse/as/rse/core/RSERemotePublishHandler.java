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
import org.eclipse.rse.services.clientserver.messages.SystemElementNotFoundException;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.wst.common.project.facet.core.util.internal.ProgressMonitorUtil;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

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
	public IStatus[] copyFile(final IModuleFile mf, final IPath path,
			final IProgressMonitor monitor) throws CoreException {
		final File file = PublishUtil.getFile(mf);
		shouldRestartModule |= method.getBehaviour().changedFileRequiresModuleRestart(mf);
		final IPath remotePath = root.append(path);
		
		final CoreException[] coreEx = new CoreException[1];
		final RuntimeException[] runtEx = new RuntimeException[1];
		coreEx[0] = null;
		runtEx[0] = null;
		
		Thread t = new Thread("RSERemotePublishHandler") {
			public void run() {
				try {
					method.getFileService().upload(file, remotePath.removeLastSegments(1).toString(), 
							remotePath.lastSegment(), true, null, null, monitor);
				} catch( CoreException ce ) { 
					coreEx[0] = ce;
				} catch( SystemMessageException sme ) {
					IStatus s = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL,
							"failed to copy to " + remotePath.toString(), sme);
					coreEx[0] = new CoreException(s);
				} catch( RuntimeException re) {
					runtEx[0] = re;
				} 
			}
		};
		t.start();
		while(!monitor.isCanceled() && t.isAlive()) {
			try {
				Thread.sleep(500);
			} catch(InterruptedException ie) {}
		}
		
		if( monitor.isCanceled()) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		if( runtEx[0] != null ) throw runtEx[0];
		if( coreEx[0] != null ) throw coreEx[0];
		return new IStatus[]{};
	}

	public IStatus[] deleteResource(final IPath path, final IProgressMonitor monitor)
			throws CoreException {
		final IPath remotePath = root.append(path);
		final CoreException[] coreEx = new CoreException[1];
		final RuntimeException[] runtEx = new RuntimeException[1];
		coreEx[0] = null;
		runtEx[0] = null;
		Thread t = new Thread("RSERemotePublishHandler") {
			public void run() {
				try {
					method.getFileService().delete(remotePath.removeLastSegments(1).toString(), remotePath.lastSegment(), monitor);
				} catch(SystemElementNotFoundException senfe ) {
					// ignore, file already does not exist remotely
				} catch( SystemMessageException sme ) {
					IStatus s = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL,
							"failed to delete " + remotePath.toString(), sme);
					coreEx[0] = new CoreException(s);
				} catch(CoreException ce) {
					coreEx[0] = ce;
				} catch(RuntimeException re) {
					runtEx[0] = re;
				}
			}
		};
		t.start();
		while(!monitor.isCanceled() && t.isAlive()) {
			try {
				Thread.sleep(500);
			} catch(InterruptedException ie) {}
		}
		
		if( monitor.isCanceled())
			throw new CoreException(Status.CANCEL_STATUS);
		if( runtEx[0] != null ) throw runtEx[0];
		if( coreEx[0] != null ) throw coreEx[0];
		return new IStatus[]{};
	}

	public IStatus[] makeDirectoryIfRequired(final IPath dir,
			final IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Make directory " + dir.toString(), 100); //$NON-NLS-1$
		if( dir.segmentCount() > 0 )
			makeDirectoryIfRequired(dir.removeLastSegments(1), ProgressMonitorUtil.submon(monitor, 70));
		final IPath toMake = root.append(dir);
		if( createdFolders.contains(toMake)) 
			return new IStatus[]{Status.OK_STATUS};

		final CoreException[] coreEx = new CoreException[1];
		final RuntimeException[] runtEx = new RuntimeException[1];
		final IStatus[] failStat = new IStatus[1];
		coreEx[0] = null;
		runtEx[0] = null;
		failStat[0] = null;
		Thread t = new Thread("RSERemotePublishHandler") {
			public void run() {
				try {
					if( toMake.segmentCount() > 0 ) {
						method.getFileService().createFolder(toMake.removeLastSegments(1).toString(), 
								toMake.lastSegment(), ProgressMonitorUtil.submon(monitor, 30));
					}
				} catch( SystemMessageException sme ) {
					IStatus s = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL,
							"failed to create folder " + toMake.toString(), sme);
					failStat[0] = s;
				} catch( CoreException ce ) {
					coreEx[0] = ce;
				} catch( RuntimeException re) {
					runtEx[0] = re;
				}
				createdFolders.add(toMake);
			}
		};
		t.start();
		while(!monitor.isCanceled() && t.isAlive()) {
			try {
				Thread.sleep(500);
			} catch(InterruptedException ie) {}
		}
		
		if( monitor.isCanceled())
			throw new CoreException(Status.CANCEL_STATUS);
		if( runtEx[0] != null ) throw runtEx[0];
		if( coreEx[0] != null ) throw coreEx[0];
		if( failStat[0] != null ) return failStat;
		monitor.done();
		return new IStatus[]{};
	}

	// TODO DEPRECATE! This needs an IProgressMonitor api to avoid blockage! JBIDE-9384
	public IStatus[] touchResource(final IPath path) {
		final IPath file = root.append(path);
		try {
			IRemoteFile rf = method.getFileServiceSubSystem().getRemoteFileObject(file.toString(), new NullProgressMonitor());
			if( !rf.exists()) {
				rf = method.getFileServiceSubSystem().getRemoteFileObject(root.toString(), new NullProgressMonitor());
			}
			method.getFileServiceSubSystem().setLastModified(rf, new Date().getTime(), null);
		} catch(SystemMessageException sme) {
			IStatus s = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL,
					"failed to touch remote resource " + file.toString(), sme);
			return new IStatus[]{s};
		}
		return new IStatus[]{};
	}

	public boolean isFile(final IPath path, final IProgressMonitor monitor)
			throws CoreException {
		final IPath file = root.append(path);
		final CoreException[] coreEx = new CoreException[1];
		final RuntimeException[] runtEx = new RuntimeException[1];
		final Boolean[] boolRet = new Boolean[1];
		coreEx[0] = null;
		runtEx[0] = null;
		boolRet[0] = null;
		Thread t = new Thread("RSERemotePublishHandler") {
			public void run() {
				try {
					IRemoteFile rf = method.getFileServiceSubSystem().getRemoteFileObject(file.toString(), new NullProgressMonitor());
					boolRet[0] = rf.exists() && rf.isFile();
				} catch(SystemMessageException sme) {
					IStatus s = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL,
							"failed to touch remote resource " + file.toString(), sme);
					coreEx[0] = new CoreException(s);
				} catch( RuntimeException re) {
					runtEx[0] = re;
				}
			}
		};
		t.start();
		while(!monitor.isCanceled() && t.isAlive()) {
			try {
				Thread.sleep(500);
			} catch(InterruptedException ie) {}
		}
		
		if( monitor.isCanceled()) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		if( runtEx[0] != null ) throw runtEx[0];
		if( coreEx[0] != null ) throw coreEx[0];
		monitor.done();
		return boolRet[0];
	}
}

