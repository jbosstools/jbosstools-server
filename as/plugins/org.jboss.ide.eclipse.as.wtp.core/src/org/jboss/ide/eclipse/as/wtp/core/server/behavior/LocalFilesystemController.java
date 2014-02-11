/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.internal.Messages;
import org.eclipse.wst.server.core.internal.ProgressUtil;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.StreamUtils;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

/**
 * This class is an implementation of {@link IFilesystemController} for use 
 * to execute filesystem operations that occur on a local system. 
 */
public class LocalFilesystemController extends AbstractSubsystemController implements IFilesystemController {

	/**
	 * A key for the environment of the subsystem.
	 * This key should point to an IPath for a folder to be used as temporary.
	 * If no value is set, a default will be provided. It is possible, though,
	 * that the default value will be on a different filesystem than the destination
	 * of a file copy, and so the filecopy may fail.  
	 */
	public static final String ENV_TEMPORARY_DEPLOY_DIRECTORY = "ISubsystemController.TemporaryDeployFolder"; //$NON-NLS-1$

	
	/*
	 * By default, our temporary location will be our workspace's metadata folder
	 */
	private static final File tempDir = ServerPlugin.getInstance().getStateLocation().toFile();
	
	// A prefix to use for temporary files
	private static final String TEMPFILE_PREFIX = "tmp"; //$NON-NLS-1$
	
	public LocalFilesystemController() {
	}
	
	public IStatus copyFile(File file, IPath absoluteRemotePath, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Copying " + absoluteRemotePath.toString(), 100); //$NON-NLS-1$
		if( file != null ) {
			if( !file.exists()) {
				return new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL, 
						NLS.bind(Messages.errorReading, file.getAbsolutePath()), null);
			}
			InputStream in = null;
			try {
				in = new FileInputStream(file);
			} catch (IOException e) {
				new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL, 
						NLS.bind(Messages.errorReading, file.getAbsolutePath()), e);
			}
			IStatus ret = copyFile(in, absoluteRemotePath, file.lastModified());
			monitor.worked(100);
			monitor.done();
			if( ret != null && !ret.isOK()) 
				return ret;
		} // else silently ignore I guess
		return Status.OK_STATUS;
	}

	/**
	 * Copy a file from a to b. Closes the input stream after use.
	 *
	 * @param in java.io.InputStream
	 * @param to java.lang.String
	 * @return a status
	 */
	private IStatus copyFile(InputStream in, String to) {
		try {
			FileUtil.writeTo(in, to);
			return Status.OK_STATUS;
		} catch (IOException e) {
			//Trace.trace(Trace.SEVERE, "Error copying file", e);
			return new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorCopyingFile, new String[] {to, e.getLocalizedMessage()}), e);
		} finally {
			StreamUtils.safeClose(in);
		}
	}

	
	/**
	 * Copy a file from a to b. Closes the input stream after use.
	 * 
	 * @param in an input stream
	 * @param to a path to copy to. the directory must already exist. This must be an absolute path
	 * @param ts timestamp
	 * @throws CoreException if anything goes wrong
	 */
	private IStatus copyFile(InputStream in, IPath to, long ts) throws CoreException {
		File tempFile = null;
		try {
			File file = to.toFile();
			tempFile = writeToTempFile(in, to);
			moveTempFile(tempFile, file);
			if (ts != IResource.NULL_STAMP && ts != 0)
				file.setLastModified(ts);
		} catch (CoreException e) {
			throw e;
		} catch (Exception e) {
			return new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL,
					NLS.bind(Messages.errorCopyingFile, to.toString(), e.getLocalizedMessage()), null);
		} finally {
			if (tempFile != null && tempFile.exists())
				tempFile.deleteOnExit();
			StreamUtils.safeClose(in);
		}
		return null;
	}
	
	private File writeToTempFile(InputStream in, IPath filePath) throws IOException {			
		File tempFile = File.createTempFile(TEMPFILE_PREFIX, "." + filePath.getFileExtension(), getTempFolder()); //$NON-NLS-1$
		FileUtil.writeTo(in, tempFile);				
		return tempFile;
	}
	
	/**
	 * Utility method to move a temp file into position by deleting the original and
	 * swapping in a new copy.
	 *  
	 * @param tempFile
	 * @param file
	 * @throws CoreException
	 */
	private void moveTempFile(File tempFile, File file) throws CoreException {
		if (file.exists()) {
			if (!safeDelete(file, 2)) {
				// attempt to rewrite an existing file with the tempFile contents if
				// the existing file can't be deleted to permit the move
				try {
					InputStream in = new FileInputStream(tempFile);
					IStatus status = copyFile(in, file.getPath());
					throwOnErrorStatus(file, status);
					return;
				} catch (FileNotFoundException e) {
					// shouldn't occur. Either wya, do nothing, since this isn't worth
					// an exception blowing away the stack, and we return voids
				} finally {
					tempFile.delete();
				}
			}
		}
		// At this point, the file should be guaranteed not to exist. 
		if (!safeRename(tempFile, file, 10))
			throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, IEventCodes.JST_PUB_ASSEMBLE_FAIL, 
					NLS.bind(org.jboss.ide.eclipse.as.wtp.core.Messages.PublishRenameFailure, 
							tempFile.toString(), file.getAbsolutePath()), null));
	}

	private void throwOnErrorStatus(File file, IStatus status) throws CoreException {
		if (!status.isOK()) {
			MultiStatus status2 = new MultiStatus(ASWTPToolsPlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorDeleting, file.toString()), null);
			status2.add(status);
			throw new CoreException(status2);
		}
	}
	
	/**
	 * Safe rename. Will try multiple times before giving up.
	 * 
	 * @param from
	 * @param to
	 * @param retrys number of times to retry
	 * @return <code>true</code> if it succeeds, <code>false</code> otherwise
	 */
	private boolean safeRename(File from, File to, int retrys) {
		// make sure parent dir exists
		File dir = to.getParentFile();
		if (dir != null && !dir.exists())
			dir.mkdirs();
		
		int count = 0;
		while (count < retrys ) {
			if (from.renameTo(to))
				return true;
			
			count++;
			// delay if we are going to try again
			if (count < retrys) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore COPIED FROM WTP
				}
			}
		}
		return false;
	}
	
	protected File getTempFolder() {
		// Grab from an environment var override
		IPath tempFolder = (IPath)getEnvironment().get(ENV_TEMPORARY_DEPLOY_DIRECTORY);
		File f = null;
		if( tempFolder != null ) {
			f = tempFolder.toFile();
		} else {
			// Grab from the server itself
			IDeployableServer ds = ServerConverter.getDeployableServer(getServer());
			if( ds != null ) {
				String t = ds.getTempDeployFolder();
				if( t != null ) {
					f = new File(t);
				}
			}
		}
		
		if( f != null ) {
			if( !f.exists() ) {
				f.mkdirs();
			}
			if( f.exists()) 
				return f;
		}

		// Last resort, return a default
		return tempDir;
	}
	
	public IStatus deleteResource(IPath absolutePath, IProgressMonitor monitor) throws CoreException {
		File file = absolutePath.toFile();
		IStatus results = null;
		if( file.isDirectory()) {
			results = deleteDirectory(absolutePath.toFile(), monitor);
		} else {
			if( !file.delete()) {
				IStatus s = new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, 
						NLS.bind(Messages.errorDeleting, absolutePath.toFile().getAbsolutePath()), null);
				results = s;
			}
		}
		return results == null ? Status.OK_STATUS : results;
	}

	/**
	 * Utility method to recursively delete a directory.
	 *
	 * @param dir a directory
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 */
	private IStatus deleteDirectory(File dir, IProgressMonitor monitor) {
		if (!dir.exists() || !dir.isDirectory())
			return  new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorNotADirectory, dir.getAbsolutePath()), null);
		
		List<IStatus> status = new ArrayList<IStatus>(2);
		
		try {
			File[] files = dir.listFiles();
			int size = files.length;
			monitor = ProgressUtil.getMonitorFor(monitor);
			monitor.beginTask(NLS.bind(Messages.deletingTask, new String[] { dir.getAbsolutePath() }), size * 10);
			
			// cycle through files
			boolean deleteCurrent = true;
			for (int i = 0; i < size; i++) {
				File current = files[i];
				if (current.isFile()) {
					if (!current.delete()) {
						status.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorDeleting, files[i].getAbsolutePath()), null));
						deleteCurrent = false;
					}
					monitor.worked(10);
				} else if (current.isDirectory()) {
					monitor.subTask(NLS.bind(Messages.deletingTask, new String[] {current.getAbsolutePath()}));
					IStatus stat = deleteDirectory(current, 
							ProgressMonitorUtil.getSubMon(monitor, 10));
					if (stat != null && !stat.isOK()) {
						deleteCurrent = false;
						status.add(stat);
					}
				}
			}
			if (deleteCurrent && !dir.delete())
				status.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, 
						NLS.bind(Messages.errorDeleting, dir.getAbsolutePath()), null));
			monitor.done();
		} catch (Exception e) {
			//Trace.trace(Trace.SEVERE, "Error deleting directory " + dir.getAbsolutePath(), e);
			status.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, e.getLocalizedMessage(), null));
		}
		if( status.size() > 0 ) {
			IStatus[] arr =(IStatus[]) status.toArray(new IStatus[status.size()]);
			return new MultiStatus(ASWTPToolsPlugin.PLUGIN_ID,  
					IEventCodes.JST_PUB_FAIL, arr, 
					NLS.bind(Messages.errorDeleting, dir.getAbsolutePath()), null);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Safe delete. Tries to delete multiple times before giving up.
	 * 
	 * @param f
	 * @return <code>true</code> if it succeeds, <code>false</code> otherwise
	 */
	private  boolean safeDelete(File f, int retrys) {
		int count = 0;
		while (count < retrys) {
			if (!f.exists())
				return true;
			
			f.delete();
			
			if (!f.exists())
				return true;
			
			count++;
			// delay if we are going to try again
			if (count < retrys) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		return false;
	}
	
	public IStatus makeDirectoryIfRequired(IPath absolutePath, IProgressMonitor monitor) throws CoreException  {
		absolutePath.toFile().mkdirs();
		return Status.OK_STATUS;
	}

	public IStatus touchResource(IPath absolutePath, IProgressMonitor monitor) throws CoreException {
		File tmp = absolutePath.toFile();
		if( !tmp.exists()) {
			try {
				tmp.createNewFile();
			} catch(IOException ioe) {
				throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, "Unable to create file " + absolutePath));
			}
		}
		tmp.setLastModified(new Date().getTime());
		return Status.OK_STATUS;
	}

	public boolean isFile(IPath absolutePath, IProgressMonitor monitor) throws CoreException {
		File tmp = absolutePath.toFile();
		return tmp.exists() && tmp.isFile();
	}
	
}