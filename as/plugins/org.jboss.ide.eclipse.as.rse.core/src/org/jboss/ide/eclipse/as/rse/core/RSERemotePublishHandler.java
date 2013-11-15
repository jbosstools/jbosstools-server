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
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.tools.foundation.core.jobs.BarrierProgressWaitJob;
import org.jboss.tools.foundation.core.jobs.BarrierProgressWaitJob.IRunnableWithProgress;

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
	
	abstract static class NamedRunnableWithProgress implements IRunnableWithProgress {
		private String name;
		public NamedRunnableWithProgress(String name){
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	
	protected static IStatus generateFailStatus(String message, String resource, RSEPublishMethod method, Exception sme) {
		String exceptionMsg = sme.getMessage();
		if( "Missing element for : ''".equals(exceptionMsg)) {
			sme = new Exception("The requested path is not found on the remote system.", sme);
		}
		String connectionName = method == null ? null : RSEUtils.getRSEConnectionName(method.getBehaviour().getServer());
		IHost host = connectionName == null ? null : RSEUtils.findHost(connectionName);
		IStatus s = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL,
				NLS.bind(message, resource, host == null ? null : host.getName()), sme);
		return s;
	}

	private IStatus[] wrapRemoteCall(final NamedRunnableWithProgress runnable, 
			final String remoteResource, final String failErrorMessage, 
			final IProgressMonitor monitor) throws CoreException, RuntimeException  {
		return wrapRemoteCall(runnable, remoteResource, failErrorMessage, true, 
				method, monitor);
	}

	static IStatus[] wrapRemoteCall(final NamedRunnableWithProgress runnable, 
			final String remoteResource, final String failErrorMessage, 
			final boolean alwaysThrow, RSEPublishMethod method, final IProgressMonitor monitor) throws CoreException, RuntimeException  {
		IStatus s = wrapRemoteCall2(runnable, remoteResource, failErrorMessage, 
				alwaysThrow, method, monitor);
		return s == null ? new IStatus[]{} : new IStatus[]{s};
	}
	
	static Exception wrapRemoteCallStatusTimeLimit(final NamedRunnableWithProgress runnable, 
			final String remoteResource, final String failErrorMessage, 
			RSEPublishMethod method, final int maxDelay, final IProgressMonitor monitor) {
		Thread t = new Thread("Remote call timer") {
			public void run() {
				try {
					Thread.sleep(maxDelay);
					monitor.setCanceled(true);
				} catch( InterruptedException ie) {
					// Do Nothing
				}
			}
		};
		t.start();
		try {
			wrapRemoteCall2(runnable, remoteResource, failErrorMessage,
					true, method, monitor);
		} catch (CoreException e) {
			if( e.getStatus().getSeverity() == IStatus.CANCEL ) {
				return new CoreException(new Status(IStatus.CANCEL,RSECorePlugin.PLUGIN_ID, 
						"The remote operation has been canceled because it did not finish in the alloted time (" + maxDelay + "ms)"));
			}
			return e;
		} catch (RuntimeException e) {
			return e;
		} finally {
			t.interrupt();
		}
		return null;
	}
	
	static IStatus wrapRemoteCall2(final NamedRunnableWithProgress runnable, 
			final String remoteResource, final String failErrorMessage, 
			final boolean alwaysThrow, final RSEPublishMethod method, final IProgressMonitor monitor) throws CoreException, RuntimeException  {
		monitor.setTaskName(runnable.getName());
		BarrierProgressWaitJob j = new BarrierProgressWaitJob(runnable.getName(),  runnable);
		j.schedule();
		// This join will also poll the provided monitor for cancelations
		j.monitorSafeJoin(monitor);
		if( j.getReturnValue() != null)
			return (IStatus)j.getReturnValue();
		if( j.getThrowable() != null ) {
			if(j.getThrowable() instanceof SystemMessageException) {
				IStatus stat = generateFailStatus(failErrorMessage, 
						remoteResource, method, ((SystemMessageException)j.getThrowable()));
				if( alwaysThrow )
					throw new CoreException(stat);
				else
					return stat;
			}
			if( j.getThrowable() instanceof CoreException )
				throw new CoreException(((CoreException)j.getThrowable()).getStatus());
			throw new RuntimeException(j.getThrowable());
		}
		return Status.CANCEL_STATUS;
	}
	
	public IStatus[] copyFile(final IModuleFile mf, final IPath path,
			final IProgressMonitor monitor) throws CoreException {
		final File file = PublishUtil.getFile(mf);
		shouldRestartModule |= method.getBehaviour().changedFileRequiresModuleRestart(mf);
		final IPath remotePath = root.append(path);
		
		
		NamedRunnableWithProgress run = new NamedRunnableWithProgress("Copy file to remote location: " + remotePath.toString()) {
			public Object run(IProgressMonitor monitor) throws CoreException,
					SystemMessageException, RuntimeException {
				method.getFileService().upload(file, remotePath.removeLastSegments(1).toString(), 
						remotePath.lastSegment(), true, null, null, monitor);
				return Status.OK_STATUS;
			}
		};
		
		return wrapRemoteCall(run, remotePath.toString(), "failed to copy to {0} on host {1}", monitor);
	}

	public IStatus[] deleteResource(final IPath path, final IProgressMonitor monitor)
			throws CoreException {
		final IPath remotePath = root.append(path);
		NamedRunnableWithProgress run = new NamedRunnableWithProgress("Delete remote file: " + remotePath.toString()) {
			public Object run(IProgressMonitor monitor) throws CoreException,
					SystemMessageException, RuntimeException {
				method.getFileService().delete(remotePath.removeLastSegments(1).toString(), remotePath.lastSegment(), monitor);
				return Status.OK_STATUS;
			}
		};
		
		return wrapRemoteCall(run, remotePath.toString(), "failed to delete {0} on host {1}", false, method, monitor);
	}

	public IStatus[] makeDirectoryIfRequired(final IPath dir,
			final IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Make directory " + dir.toString(), 100); //$NON-NLS-1$
		if( dir.segmentCount() > 0 )
			makeDirectoryIfRequired(dir.removeLastSegments(1), ProgressMonitorUtil.submon(monitor, 70));
		final IPath toMake = root.append(dir);
		if( createdFolders.contains(toMake)) 
			return new IStatus[]{Status.OK_STATUS};

		NamedRunnableWithProgress run = new NamedRunnableWithProgress("Create Remote Directory: " + toMake.toString()) {
			public Object run(IProgressMonitor monitor) throws CoreException,
					SystemMessageException, RuntimeException {
				if( toMake.segmentCount() > 0 ) {
					method.getFileService().createFolder(toMake.removeLastSegments(1).toString(), 
							toMake.lastSegment(), monitor);
					createdFolders.add(toMake);
				}
				return Status.OK_STATUS;
			}
		};
		return wrapRemoteCall(run, toMake.toString(), "failed to create folder {0} on host {1}", false, method, monitor);
	}

	public IStatus[] touchResource(final IPath path, IProgressMonitor monitor) {
		final IPath file = root.append(path);
		
		NamedRunnableWithProgress run = new NamedRunnableWithProgress("Touch remote resource" + file.toString()) {
			public Object run(IProgressMonitor monitor) throws CoreException,
					SystemMessageException, RuntimeException {
				IRemoteFile rf = method.getFileServiceSubSystem().getRemoteFileObject(file.toString(), new NullProgressMonitor());
				if( !rf.exists()) {
					rf = method.getFileServiceSubSystem().getRemoteFileObject(root.toString(), new NullProgressMonitor());
				}
				method.getFileServiceSubSystem().setLastModified(rf, new Date().getTime(), null);
				return Status.OK_STATUS;
			}
		};
		try {
			return wrapRemoteCall(run, file.toString(), "failed to touch resource {0} on host {1}", false, method, monitor);
		} catch(CoreException ce) {
			return new IStatus[]{ce.getStatus()};
		}
	}

	public boolean isFile(final IPath path, final IProgressMonitor monitor)
			throws CoreException {
		final IPath file = root.append(path);
		
		final Boolean[] boolRet = new Boolean[1];
		boolRet[0] = null;
		
		NamedRunnableWithProgress run = new NamedRunnableWithProgress("Verify remote file exists: " + file.toString()) {
			public Object run(IProgressMonitor monitor) throws CoreException,
					SystemMessageException, RuntimeException {
				IRemoteFile rf = method.getFileServiceSubSystem().getRemoteFileObject(file.toString(), new NullProgressMonitor());
				boolRet[0] = rf.exists() && rf.isFile();
				return Status.OK_STATUS;
			}
		};

		wrapRemoteCall(run, file.toString(), "failed to verify the existence of {0} on host {1}", monitor);
		return boolRet[0];
	}
}

