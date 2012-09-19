package org.jboss.ide.eclipse.as.core.util;

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
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Messages;
import org.eclipse.wst.server.core.internal.ProgressUtil;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IDeployableServerBehaviour;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;

public class LocalCopyCallback implements IPublishCopyCallbackHandler {

	private static final File tempDir = ServerPlugin.getInstance().getStateLocation().toFile();
	private static final String TEMPFILE_PREFIX = "tmp"; //$NON-NLS-1$

	private boolean shouldRestartModule = false;
	
	private IServer server;
	private IPath deployRootFolder;
	private IPath tmpDeployRootFolder;
	public LocalCopyCallback(IServer server, IPath deployFolder, IPath temporaryFolder ) {
		this.server = server;
		this.deployRootFolder = deployFolder;
		this.tmpDeployRootFolder = temporaryFolder;
	}
	
	public boolean shouldRestartModule() {
		return shouldRestartModule;
	}
	
	public IStatus[] copyFile(IModuleFile mf, IPath relativePath, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Copying " + relativePath.toString(), 100); //$NON-NLS-1$
		File file = ModuleResourceUtil.getFile(mf);
		IDeployableServerBehaviour beh = ServerConverter.getDeployableServerBehavior(server);
		shouldRestartModule |= beh != null && beh.changedFileRequiresModuleRestart(mf);
		if( file != null ) {
			if( !file.exists()) {
				return new IStatus[] {new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL, 
						NLS.bind(Messages.errorReading, file.getAbsolutePath()), null)};
			}
			InputStream in = null;
			try {
				in = new FileInputStream(file);
			} catch (IOException e) {
				return new IStatus[] {new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL, 
						NLS.bind(Messages.errorReading, file.getAbsolutePath()), e)};
			}
			IStatus ret = copyFile(in, deployRootFolder.append(relativePath), file.lastModified(), mf);
			monitor.worked(100);
			monitor.done();
			if( ret != null && !ret.isOK()) 
				return new IStatus[] { ret };
		} // else silently ignore I guess
		return new IStatus[]{};
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
			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorCopyingFile, new String[] {to, e.getLocalizedMessage()}), e);
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
	private IStatus copyFile(InputStream in, IPath to, long ts, IModuleFile mf) throws CoreException {
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
			IPath path = mf.getModuleRelativePath().append(mf.getName());
			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorCopyingFile, path.toOSString(), e.getLocalizedMessage()), null);
		} finally {
			if (tempFile != null && tempFile.exists())
				tempFile.deleteOnExit();
			try {
				if (in != null)
					in.close();
			} catch (Exception ex) {
				// ignore. We do not return warning statuses. 
			}
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
			throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, IEventCodes.JST_PUB_ASSEMBLE_FAIL, 
					NLS.bind(org.jboss.ide.eclipse.as.wtp.core.Messages.PublishRenameFailure, 
							tempFile.toString(), file.getAbsolutePath()), null));
	}

	private void throwOnErrorStatus(File file, IStatus status) throws CoreException {
		if (!status.isOK()) {
			MultiStatus status2 = new MultiStatus(ServerPlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorDeleting, file.toString()), null);
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
		File f = null;
		if( tmpDeployRootFolder != null ) {
			f = tmpDeployRootFolder.toFile();
		} else if( server == null ) {
			return tempDir;
		} 
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		if( ds == null ) {
			return tempDir;
		}
		String path = ds.getTempDeployFolder();
		f = new File(path);
		if( !f.exists() )
			f.mkdirs();
		return f;
	}
	
	public IStatus[] deleteResource(IPath resource, IProgressMonitor monitor) {
		resource = deployRootFolder.append(resource);
		File file = resource.toFile();
		IStatus[] results = new IStatus[]{};
		if( file.isDirectory()) {
			results = deleteDirectory(resource.toFile(), monitor);
		} else {
			if( !file.delete()) {
				IStatus s = new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorDeleting, resource.toFile().getAbsolutePath()), null);
				results = new IStatus[]{s};
			}
		}
		return results;
	}

	/**
	 * Utility method to recursively delete a directory.
	 *
	 * @param dir a directory
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 */
	private IStatus[] deleteDirectory(File dir, IProgressMonitor monitor) {
		if (!dir.exists() || !dir.isDirectory())
			return new IStatus[] { new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorNotADirectory, dir.getAbsolutePath()), null) };
		
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
						status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorDeleting, files[i].getAbsolutePath()), null));
						deleteCurrent = false;
					}
					monitor.worked(10);
				} else if (current.isDirectory()) {
					monitor.subTask(NLS.bind(Messages.deletingTask, new String[] {current.getAbsolutePath()}));
					IStatus[] stat = deleteDirectory(current, 
							ProgressMonitorUtil.getSubMon(monitor, 10));
					if (stat != null && stat.length > 0) {
						deleteCurrent = false;
						PublishCopyUtil.addArrayToList(status, stat);
					}
				}
			}
			if (deleteCurrent && !dir.delete())
				status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorDeleting, dir.getAbsolutePath()), null));
			monitor.done();
		} catch (Exception e) {
			//Trace.trace(Trace.SEVERE, "Error deleting directory " + dir.getAbsolutePath(), e);
			status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, e.getLocalizedMessage(), null));
		}
		
		return status.toArray(new IStatus[status.size()]);
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
	
	public IStatus[] makeDirectoryIfRequired(IPath relativeDir, IProgressMonitor monitor) {
		deployRootFolder.append(relativeDir).toFile().mkdirs();
		return new IStatus[] {Status.OK_STATUS};
	}

	public IStatus[] touchResource(IPath path, IProgressMonitor monitor) {
		File tmp = deployRootFolder.append(path).toFile();
		if( !tmp.exists())
			tmp = deployRootFolder.toFile();
		tmp.setLastModified(new Date().getTime());
		return null;
	}

	public boolean isFile(IPath path, IProgressMonitor monitor)
			throws CoreException {
		File tmp = deployRootFolder.append(path).toFile();
		return tmp.exists() && tmp.isFile();
	}
	
}