/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.xpl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.wst.server.core.internal.Trace;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
/**
 * Utility class with an assortment of useful file methods.
 * <p>
 * This class provides all its functionality through static members.
 * It is not intended to be subclassed or instantiated.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 2.0
 */
public final class PublishCopyUtil {
	// size of the buffer
	private static final int BUFFER = 65536;

	// the buffer
	private static byte[] buf = new byte[BUFFER];

	private static final IStatus[] EMPTY_STATUS = new IStatus[0];

	private static final File tempDir = ServerPlugin.getInstance().getStateLocation().toFile();
	
	private static final String TEMPFILE_PREFIX = "tmp"; //$NON-NLS-1$

	/**
	 * PublishUtil cannot be created. Use static methods.
	 */
	private IServer server;
	private IPath rootDeploy;
	private IPath rootTmpDeploy;
	
	/**
	 * 
	 * @param server The server we're publishing to
	 * @param deploy  The deploy path where they're intended to go
	 * @param tmpDeploy The temporary location on the same partition
	 */
	public PublishCopyUtil(IServer server, IPath deploy, IPath tmpDeploy) {
		this.server = server;
		this.rootDeploy = deploy;
		this.rootTmpDeploy = tmpDeploy;
	}

	/**
	 * Copy a file from a to b. Closes the input stream after use.
	 * 
	 * @param in an input stream
	 * @param to a path to copy to. the directory must already exist
	 * @param ts timestamp
	 * @throws CoreException if anything goes wrong
	 */
	private  void copyFile(InputStream in, IPath to, long ts, IModuleFile mf) throws CoreException {
		OutputStream out = null;
		
		File tempFile = null;
		try {
			File file = to.toFile();
			
			// Change from original PublishUtil, will require 
			tempFile = File.createTempFile(TEMPFILE_PREFIX, "." + to.getFileExtension(), getTempFolder()); //$NON-NLS-1$
			
			out = new FileOutputStream(tempFile);
			
			int avail = in.read(buf);
			while (avail > 0) {
				out.write(buf, 0, avail);
				avail = in.read(buf);
			}
			
			out.close();
			out = null;
			
			moveTempFile(tempFile, file);
			
			if (ts != IResource.NULL_STAMP && ts != 0)
				file.setLastModified(ts);
		} catch (CoreException e) {
			throw e;
		} catch (Exception e) {
			IPath path = mf.getModuleRelativePath().append(mf.getName());
			//Trace.trace(Trace.SEVERE, "Error copying file: " + to.toOSString() + " to " + path.toOSString(), e);
			throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCopyingFile, path.toOSString(), e.getLocalizedMessage()), null));
		} finally {
			if (tempFile != null && tempFile.exists())
				tempFile.deleteOnExit();
			try {
				if (in != null)
					in.close();
			} catch (Exception ex) {
				// ignore
			}
			try {
				if (out != null)
					out.close();
			} catch (Exception ex) {
				// ignore
			}
		}
	}

	/**
	 * Utility method to recursively delete a directory.
	 *
	 * @param dir a directory
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 */
	public  IStatus[] deleteDirectory(File dir, IProgressMonitor monitor) {
		if (!dir.exists() || !dir.isDirectory())
			return new IStatus[] { new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorNotADirectory, dir.getAbsolutePath()), null) };
		
		List status = new ArrayList(2);
		
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
						status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, files[i].getAbsolutePath()), null));
						deleteCurrent = false;
					}
					monitor.worked(10);
				} else if (current.isDirectory()) {
					monitor.subTask(NLS.bind(Messages.deletingTask, new String[] {current.getAbsolutePath()}));
					IStatus[] stat = deleteDirectory(current, ProgressUtil.getSubMonitorFor(monitor, 10));
					if (stat != null && stat.length > 0) {
						deleteCurrent = false;
						addArrayToList(status, stat);
					}
				}
			}
			if (deleteCurrent && !dir.delete())
				status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, dir.getAbsolutePath()), null));
			monitor.done();
		} catch (Exception e) {
			//Trace.trace(Trace.SEVERE, "Error deleting directory " + dir.getAbsolutePath(), e);
			status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), null));
		}
		
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}

	/**
	 * Smart copy the given module resources to the given path.
	 * 
	 * @param resources an array of module resources
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status 
	 */
	public IStatus[] publishSmart(IModuleResource[] resources, IProgressMonitor monitor) {
		return publishSmart(resources, rootDeploy, monitor);
	}

	/**
	 * Smart copy the given module resources to the given path.
	 * 
	 * @param resources an array of module resources
	 * @param path an external path to copy to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status 
	 */
	private IStatus[] publishSmart(IModuleResource[] resources, IPath path, IProgressMonitor monitor) {
		if (resources == null)
			return EMPTY_STATUS;
		
		monitor = ProgressUtil.getMonitorFor(monitor);
		
		List status = new ArrayList(2);
		File toDir = path.toFile();
		int fromSize = resources.length;
		String[] fromFileNames = new String[fromSize];
		for (int i = 0; i < fromSize; i++)
			fromFileNames[i] = resources[i].getName();
		
		//	cache files and file names for performance
		File[] toFiles = null;
		String[] toFileNames = null;
		
		boolean foundExistingDir = false;
		if (toDir.exists()) {
			if (toDir.isDirectory()) {
				foundExistingDir = true;
				toFiles = toDir.listFiles();
				int toSize = toFiles.length;
				toFileNames = new String[toSize];
				
				// check if this exact file exists in the new directory
				for (int i = 0; i < toSize; i++) {
					toFileNames[i] = toFiles[i].getName();
					boolean isDir = toFiles[i].isDirectory();
					boolean found = false;
					for (int j = 0; j < fromSize; j++) {
						if (toFileNames[i].equals(fromFileNames[j]) && isDir == resources[j] instanceof IModuleFolder)
							found = true;
					}
					
					// delete file if it can't be found or isn't the correct type
					if (!found) {
						if (isDir) {
							IStatus[] stat = deleteDirectory(toFiles[i], null);
							addArrayToList(status, stat);
						} else {
							if (!toFiles[i].delete())
								status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, toFiles[i].getAbsolutePath()), null));
						}
						toFiles[i] = null;
						toFileNames[i] = null;
					}
				}
			} else { //if (toDir.isFile())
				if (!toDir.delete()) {
					status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, toDir.getAbsolutePath()), null));
					IStatus[] stat = new IStatus[status.size()];
					status.toArray(stat);
					return stat;
				}
			}
		}
		if (!foundExistingDir && !toDir.mkdirs()) {
			status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorMkdir, toDir.getAbsolutePath()), null));
			IStatus[] stat = new IStatus[status.size()];
			status.toArray(stat);
			return stat;
		}
		
		if (monitor.isCanceled())
			return new IStatus[] { Status.CANCEL_STATUS };
		
		monitor.worked(50);
		
		// cycle through files and only copy when it doesn't exist
		// or is newer
		if (toFiles == null)
			toFiles = toDir.listFiles();
		int toSize = 0;
		if (toFiles != null)
			toSize = toFiles.length;
		
		int dw = 0;
		if (toSize > 0)
			dw = 500 / toSize;
		
		// cache file names and last modified dates for performance
		if (toFileNames == null)
			toFileNames = new String[toSize];
		long[] toFileMod = new long[toSize];
		for (int i = 0; i < toSize; i++) {
			if (toFiles[i] != null) {
				if (toFileNames[i] != null)
					toFileNames[i] = toFiles[i].getName();
				toFileMod[i] = toFiles[i].lastModified();
			}
		}
		
		for (int i = 0; i < fromSize; i++) {
			IModuleResource current = resources[i];
			String name = fromFileNames[i];
			boolean currentIsDir = current instanceof IModuleFolder;
			
			if (!currentIsDir) {
				// check if this is a new or newer file
				boolean copy = true;
				IModuleFile mf = (IModuleFile) current;
				
				long mod = -1;
				IFile file = (IFile) mf.getAdapter(IFile.class);
				if (file != null) {
					mod = file.getLocalTimeStamp();
				} else {
					File file2 = (File) mf.getAdapter(File.class);
					mod = file2.lastModified();
				}
				
				for (int j = 0; j < toSize; j++) {
					if (name.equals(toFileNames[j]) && mod == toFileMod[j])
						copy = false;
				}
				
				if (copy) {
					try {
						copyFile(mf, path.append(name));
					} catch (CoreException ce) {
						status.add(ce.getStatus());
					}
				}
				monitor.worked(dw);
			} else { //if (currentIsDir) {
				IModuleFolder folder = (IModuleFolder) current;
				IModuleResource[] children = folder.members();
				monitor.subTask(NLS.bind(Messages.copyingTask, new String[] {name, name}));
				IStatus[] stat = publishSmart(children, path.append(name), ProgressUtil.getSubMonitorFor(monitor, dw));
				addArrayToList(status, stat);
			}
		}
		if (monitor.isCanceled())
			return new IStatus[] { Status.CANCEL_STATUS };
		
		monitor.worked(500 - dw * toSize);
		monitor.done();
		
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}

	/**
	 * Handle a delta publish.
	 * 
	 * @param delta a module resource delta
	 * @param path the path to publish to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 */
	public IStatus[] publishDelta(IModuleResourceDelta[] delta, IPath path, IProgressMonitor monitor) {
		if (delta == null)
			return EMPTY_STATUS;
		
		monitor = ProgressUtil.getMonitorFor(monitor);
		
		List status = new ArrayList(2);
		int size2 = delta.length;
		for (int i = 0; i < size2; i++) {
			IStatus[] stat = publishDelta(delta[i], path, monitor);
			addArrayToList(status, stat);
		}
		
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}

	/**
	 * Handle a delta publish.
	 * 
	 * @param delta a module resource delta
	 * @param path the path to publish to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 */
	public IStatus[] publishDelta(IModuleResourceDelta delta, IPath path, IProgressMonitor monitor) {
		List status = new ArrayList(2);
		
		IModuleResource resource = delta.getModuleResource();
		int kind2 = delta.getKind();
		
		if (resource instanceof IModuleFile) {
			IModuleFile file = (IModuleFile) resource;
			try {
				if (kind2 == IModuleResourceDelta.REMOVED)
					deleteFile2(path, file);
				else {
					IPath path2 = path.append(file.getModuleRelativePath()).append(file.getName());
					File f = path2.toFile().getParentFile();
					if (!f.exists())
						f.mkdirs();
					
					copyFile(file, path2);
				}
			} catch (CoreException ce) {
				status.add(ce.getStatus());
			}
			IStatus[] stat = new IStatus[status.size()];
			status.toArray(stat);
			return stat;
		}
		
		if (kind2 == IModuleResourceDelta.ADDED) {
			IPath path2 = path.append(resource.getModuleRelativePath()).append(resource.getName());
			File file = path2.toFile();
			if (!file.exists() && !file.mkdirs()) {
				status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorMkdir, path2), null));
				IStatus[] stat = new IStatus[status.size()];
				status.toArray(stat);
				return stat;
			}
		}
		
		IModuleResourceDelta[] childDeltas = delta.getAffectedChildren();
		int size = childDeltas.length;
		for (int i = 0; i < size; i++) {
			IStatus[] stat = publishDelta(childDeltas[i], path, monitor);
			addArrayToList(status, stat);
		}
		
		if (kind2 == IModuleResourceDelta.REMOVED) {
			IPath path2 = path.append(resource.getModuleRelativePath()).append(resource.getName());
			File file = path2.toFile();
			if (file.exists() && !file.delete()) {
				status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, path2), null));
			}
		}
		
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}

	private  void deleteFile2(IPath path, IModuleFile file) throws CoreException {
		//Trace.trace(Trace.PUBLISHING, "Deleting: " + file.getName() + " from " + path.toString());
		IPath path2 = path.append(file.getModuleRelativePath()).append(file.getName());
		if (path2.toFile().exists() && !path2.toFile().delete())
			throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, path2), null));
	}

	private  void copyFile(IModuleFile mf, IPath path) throws CoreException {
		//Trace.trace(Trace.PUBLISHING, "Copying: " + mf.getName() + " to " + path.toString());
		
		IFile file = (IFile) mf.getAdapter(IFile.class);
		if (file != null)
			copyFile(file.getContents(), path, file.getLocalTimeStamp(), mf);
		else {
			File file2 = (File) mf.getAdapter(File.class);
			if( file2 != null ) {
				InputStream in = null;
				try {
					in = new FileInputStream(file2);
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorReading, file2.getAbsolutePath()), e));
				}
				copyFile(in, path, file2.lastModified(), mf);
			} // else silently ignore I guess
		}
	}

	/**
	 * Publish the given module resources to the given path.
	 * 
	 * @param resources an array of module resources
	 * @param path a path to publish to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 */
	public IStatus[] publishFull(IModuleResource[] resources, IPath path, IProgressMonitor monitor) {
		if (resources == null)
			return EMPTY_STATUS;
		
		monitor = ProgressUtil.getMonitorFor(monitor);
		
		List status = new ArrayList(2);
		int size = resources.length;
		for (int i = 0; i < size; i++) {
			IStatus[] stat = copy(resources[i], path, monitor);
			addArrayToList(status, stat);
		}
		
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}

	private  IStatus[] copy(IModuleResource resource, IPath path, IProgressMonitor monitor) {
		String name = resource.getName();
		//Trace.trace(Trace.PUBLISHING, "Copying: " + name + " to " + path.toString());
		List status = new ArrayList(2);
		if (resource instanceof IModuleFolder) {
			IModuleFolder folder = (IModuleFolder) resource;
			IStatus[] stat = publishFull(folder.members(), path, monitor);
			addArrayToList(status, stat);
		} else {
			IModuleFile mf = (IModuleFile) resource;
			path = path.append(mf.getModuleRelativePath()).append(name);
			File f = path.toFile().getParentFile();
			if (!f.exists())
				f.mkdirs();
			try {
				copyFile(mf, path);
			} catch (CoreException ce) {
				status.add(ce.getStatus());
			}
		}
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}

	/**
	 * Creates a new zip file containing the given module resources. Deletes the existing file
	 * (and doesn't create a new one) if resources is null or empty.
	 * 
	 * @param resources an array of module resources
	 * @param path the path where the zip file should be created 
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 */
	public IStatus[] publishZip(IModuleResource[] resources, IPath path, IProgressMonitor monitor) {
		if (resources == null || resources.length == 0) {
			// should also check if resources consists of all empty directories
			File file = path.toFile();
			if (file.exists())
				file.delete();
			return EMPTY_STATUS;
		}
		
		monitor = ProgressUtil.getMonitorFor(monitor);
		
		File tempFile = null;
		try {
			File file = path.toFile();
			tempFile = File.createTempFile(TEMPFILE_PREFIX, "." + path.getFileExtension(), getTempFolder()); //$NON-NLS-1$
			BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(tempFile));
			ZipOutputStream zout = new ZipOutputStream(bout);
			addZipEntries(zout, resources);
			zout.close();
			
			moveTempFile(tempFile, file);
		} catch (CoreException e) {
			return new IStatus[] { e.getStatus() };
		} catch (Exception e) {
			//Trace.trace(Trace.SEVERE, "Error zipping", e);
			return new Status[] { new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCreatingZipFile, path.lastSegment(), e.getLocalizedMessage()), e) };
		} finally {
			if (tempFile != null && tempFile.exists())
				tempFile.deleteOnExit();
		}
		return EMPTY_STATUS;
	}

	private  void addZipEntries(ZipOutputStream zout, IModuleResource[] resources) throws Exception {
		if (resources == null)
			return;
		
		int size = resources.length;
		for (int i = 0; i < size; i++) {
			if (resources[i] instanceof IModuleFolder) {
				IModuleFolder mf = (IModuleFolder) resources[i];
				IModuleResource[] res = mf.members();
				
				IPath path = mf.getModuleRelativePath().append(mf.getName());
				String entryPath = path.toPortableString();
				if (!entryPath.endsWith("/")) //$NON-NLS-1$
					entryPath += '/';
				
				ZipEntry ze = new ZipEntry(entryPath);
				
				long ts = 0;
				IContainer folder = (IContainer) mf.getAdapter(IContainer.class);
				if (folder != null)
					ts = folder.getLocalTimeStamp();
				
				if (ts != IResource.NULL_STAMP && ts != 0)
					ze.setTime(ts);
				
				zout.putNextEntry(ze);
				zout.closeEntry();
				
				addZipEntries(zout, res);
				continue;
			}
			
			IModuleFile mf = (IModuleFile) resources[i];
			IPath path = mf.getModuleRelativePath().append(mf.getName());
			
			ZipEntry ze = new ZipEntry(path.toPortableString());
			
			InputStream in = null;
			long ts = 0;
			IFile file = (IFile) mf.getAdapter(IFile.class);
			if (file != null) {
				ts = file.getLocalTimeStamp();
				in = file.getContents();
			} else {
				File file2 = (File) mf.getAdapter(File.class);
				ts = file2.lastModified();
				in = new FileInputStream(file2);
			}
			
			if (ts != IResource.NULL_STAMP && ts != 0)
				ze.setTime(ts);
			
			zout.putNextEntry(ze);
			
			try {
				int n = 0;
				while (n > -1) {
					n = in.read(buf);
					if (n > 0)
						zout.write(buf, 0, n);
				}
			} finally {
				in.close();
			}
			
			zout.closeEntry();
		}
	}

	/**
	 * Utility method to move a temp file into position by deleting the original and
	 * swapping in a new copy.
	 *  
	 * @param tempFile
	 * @param file
	 * @throws CoreException
	 */
	private  void moveTempFile(File tempFile, File file) throws CoreException {
		if (file.exists()) {
			if (!safeDelete(file, 2)) {
				// attempt to rewrite an existing file with the tempFile contents if
				// the existing file can't be deleted to permit the move
				try {
					InputStream in = new FileInputStream(tempFile);
					IStatus status = copyFile(in, file.getPath());
					if (!status.isOK()) {
						MultiStatus status2 = new MultiStatus(ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, file.toString()), null);
						status2.add(status);
						throw new CoreException(status2);
					}
					return;
				} catch (FileNotFoundException e) {
					// shouldn't occur
				} finally {
					tempFile.delete();
				}
				/*if (!safeDelete(file, 8)) {
					tempFile.delete();
					throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, file.toString()), null));
				}*/
			}
		}
		if (!safeRename(tempFile, file, 10))
			throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorRename, tempFile.toString()), null));
	}

	/**
	 * Copy a file from a to b. Closes the input stream after use.
	 *
	 * @param in java.io.InputStream
	 * @param to java.lang.String
	 * @return a status
	 */
	private  IStatus copyFile(InputStream in, String to) {
		OutputStream out = null;
		
		try {
			out = new FileOutputStream(to);
			
			int avail = in.read(buf);
			while (avail > 0) {
				out.write(buf, 0, avail);
				avail = in.read(buf);
			}
			return Status.OK_STATUS;
		} catch (Exception e) {
			//Trace.trace(Trace.SEVERE, "Error copying file", e);
			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCopyingFile, new String[] {to, e.getLocalizedMessage()}), e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception ex) {
				// ignore
			}
			try {
				if (out != null)
					out.close();
			} catch (Exception ex) {
				// ignore
			}
		}
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
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return false;
	}

	/**
	 * Safe rename. Will try multiple times before giving up.
	 * 
	 * @param from
	 * @param to
	 * @param retrys number of times to retry
	 * @return <code>true</code> if it succeeds, <code>false</code> otherwise
	 */
	private  boolean safeRename(File from, File to, int retrys) {
		// make sure parent dir exists
		File dir = to.getParentFile();
		if (dir != null && !dir.exists())
			dir.mkdirs();
		
		int count = 0;
		while (count < retrys) {
			if (from.renameTo(to))
				return true;
			
			count++;
			// delay if we are going to try again
			if (count < retrys) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return false;
	}

	private  void addArrayToList(List list, IStatus[] a) {
		if (list == null || a == null || a.length == 0)
			return;
		
		int size = a.length;
		for (int i = 0; i < size; i++)
			list.add(a[i]);
	}
	
	protected File getTempFolder() {
		File f = null;
		if( rootTmpDeploy != null ) {
			f = rootTmpDeploy.toFile();
		} else if( server != null ){
			String path = ServerConverter.getDeployableServer(server).getTempDeployFolder();
			f = new File(path);
		} else {
			return tempDir;
		}
		if( !f.exists() )
			f.mkdirs();
		return f;
	}
}