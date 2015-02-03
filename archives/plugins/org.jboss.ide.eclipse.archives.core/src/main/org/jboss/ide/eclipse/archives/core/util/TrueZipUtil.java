/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.core.util;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.archives.core.ArchivesCorePlugin;

import de.schlichtherle.io.AbstractArchiveDetector;
import de.schlichtherle.io.ArchiveDetector;
import de.schlichtherle.io.ArchiveException;
import de.schlichtherle.io.archive.spi.ArchiveDriver;
import de.schlichtherle.io.archive.zip.JarDriver;
import de.schlichtherle.key.KeyManager;

/**
 * Accesses raw files with the truezip filesystem
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 *
 */
public class TrueZipUtil {

	public static de.schlichtherle.io.File getFile(IPath path) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			return getFile(path, ArchiveDetector.DEFAULT);
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}
	public static de.schlichtherle.io.File getFile(IPath path, ArchiveDetector detector) {
		
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			return new de.schlichtherle.io.File(path.toOSString(), detector);
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}

	public static boolean pathExists(IPath path) {
		return pathExists( getFile(path));
	}
	public static boolean pathExists( de.schlichtherle.io.File file) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			return file.exists();
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}


	public static long getTimestamp(IPath path) {
		return getTimestamp( getFile(path));
	}

	public static long getTimestamp(de.schlichtherle.io.File file) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			return file.lastModified();
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}


	public static boolean copyFile(String source, IPath dest) throws IOException {
		return copyFile(source, getFile(dest), true);
	}

	public static boolean copyFile(String source, de.schlichtherle.io.File file, boolean updateTimestamps) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			file.getParentFile().mkdirs();
			boolean b = new de.schlichtherle.io.File(source, ArchiveDetector.NULL).archiveCopyAllTo(file);
			return  b && (updateTimestamps ? updateParentTimestamps(file) : true);
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}

	public static boolean touchFile(IPath path) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			de.schlichtherle.io.File f = getFile(path);
			boolean b = f.setLastModified(System.currentTimeMillis());
		    return b && updateParentTimestamps(path);
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}


	// Delete methods
	public static boolean deleteAll(IPath path, String fileName) {
		return deleteAll(path.append(fileName));
	}
	public static boolean deleteAll(IPath path) {
		return deleteAll(getFile(path));
	}
	public static boolean deleteAll(de.schlichtherle.io.File file) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			return file.deleteAll();
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}

	public static boolean deleteEmptyChildren(java.io.File file) {
		boolean b = true;
		if( file.isDirectory() ) {
			java.io.File[] children = file.listFiles();
			for( int i = 0; i < children.length; i++ )
				b &= deleteEmptyFolders(children[i]);
		}
		return b;
	}
	public static boolean deleteEmptyFolders(java.io.File file ) {
		boolean b = true;
		if( file.isDirectory() ) {
			java.io.File[] children = file.listFiles();
			for( int i = 0; i < children.length; i++ )
				b &= deleteEmptyFolders(children[i]);
			if( file.listFiles().length == 0 )
				file.delete();
		}
		return b;
	}


	public static boolean createFolder(IPath parent, String folderName) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			boolean b = new de.schlichtherle.io.File(getFile(parent, ArchiveDetector.DEFAULT), folderName, ArchiveDetector.NULL).mkdirs();
			return b && updateParentTimestamps(parent.append(folderName));
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}
	public static boolean createFolder(IPath path) {
		return createFolder(path.removeLastSegments(1), path.lastSegment());
	}
	public static boolean createArchive(IPath parent, String folderName) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			boolean b = new de.schlichtherle.io.File(getFile(parent, ArchiveDetector.DEFAULT), folderName, getJarArchiveDetector()).mkdirs();
		    return b && updateParentTimestamps(parent.append(folderName));
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}
	public static boolean createArchive(IPath path) {
		return createArchive(path.removeLastSegments(1), path.lastSegment());
	}
	
	/**
	 * This method takes a parent file (however configured, as jar or not)
	 * and appends one path segment at a time, treating each one as a simple
	 * file or folder.  When reaching the last segment, it treats this last 
	 * one as an archive. 
	 * 
	 * @param parentFile
	 * @param path
	 * @return
	 */
	public static boolean createArchive(java.io.File parentFile, IPath relative) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			de.schlichtherle.io.File archive = getRelativeArchiveFile(parentFile, relative);
			boolean b = archive.mkdirs();
		    return b && updateParentTimestamps(archive);
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}
	
	public static de.schlichtherle.io.File getRelativeArchiveFile(java.io.File parentFile, IPath relative) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			return getRelativeArchiveFileInternal(parentFile, relative);
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}

	}
	private static de.schlichtherle.io.File getRelativeArchiveFileInternal(java.io.File parentFile, IPath relative) {
		de.schlichtherle.io.File working = null;
		if( parentFile instanceof de.schlichtherle.io.File)
			working = (de.schlichtherle.io.File)parentFile;
		else
			working = new de.schlichtherle.io.File(parentFile);

		// IF the path is 0 length, just return now
		if( relative.segmentCount() == 0)
			return working;
		
		// the path of the final file's parent relative to the passed in root
		IPath finalFileRelativeLocationPath = relative.removeLastSegments(1);
		// the parent of the object to be returned
		de.schlichtherle.io.File finalFileLocation = getFileInArchive(working, finalFileRelativeLocationPath);
		
		de.schlichtherle.io.File retval = new de.schlichtherle.io.File(finalFileLocation, 
				relative.lastSegment(), getJarArchiveDetector());
		return retval;
	}
	
	
	
	public static void umount() {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			try {
				de.schlichtherle.io.File.umount();
			} catch( ArchiveException ae ) {
			}
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}

	/**
	 * Sync's with file system after executing a runnable
	 * @param run Runnable or null
	 */
	public static void syncExec(Runnable run) {
		try {
			if( run != null )
				run.run();
		} catch (Exception e ) {}
		
		umount();
	}

	public static void sync() {
		syncExec(null);
	}

	public static boolean updateParentTimestamps(IPath path) {
		return updateParentTimestamps(getFile(path));
	}
	
	// Update only the PARENT timestamps. 
	public static boolean updateParentTimestamps(de.schlichtherle.io.File file) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			long time = System.currentTimeMillis();
			de.schlichtherle.io.File parent = (de.schlichtherle.io.File)file.getParentFile();
			boolean b = true;
			while( parent != null ) {
				b &= parent.setLastModified(time);
				parent = parent.getEnclArchive();
			}
			return b;
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}


	private static ArchiveDetector JAR_ARCHIVE_DETECTOR;
	public static ArchiveDetector getJarArchiveDetector() {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			if( JAR_ARCHIVE_DETECTOR == null ) {
				JAR_ARCHIVE_DETECTOR = new JarArchiveDetector();
			}
			return JAR_ARCHIVE_DETECTOR;
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}

	public static ArchiveDetector getNullArchiveDetector() {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			return ArchiveDetector.NULL;
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}
	
	public static boolean archiveCopyAllTo(java.io.File source, ArchiveDetector detector, java.io.File destination) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			return 	new de.schlichtherle.io.File(source, detector).archiveCopyAllTo(destination);

		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}
	
	private static class JarArchiveDetector extends AbstractArchiveDetector {
		public ArchiveDriver getArchiveDriver(String arg0) {
			return new JarDriver();
		}

	}
	
	public static boolean javaIODeleteDir(java.io.File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = javaIODeleteDir(new java.io.File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    // The directory is now empty so delete it
	    return dir.delete();
	}

	public static de.schlichtherle.io.File getFileInArchive(de.schlichtherle.io.File root, IPath relative) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			while(relative.segmentCount() > 0 ) {
				root = new de.schlichtherle.io.File(root, 
						relative.segment(0), ArchiveDetector.NULL);
				relative = relative.removeFirstSegments(1);
			}
			return root;
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}
	
	
	public static java.io.File getDestinationJar(java.io.File root, IPath relative) {
		ClassLoader bundleLoader = ArchivesCorePlugin.getDefault().getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			//  FILL CODE HERE
			while(relative.segmentCount() > 0 ) {
				if( relative.segmentCount() == 1 ) {
					root = new de.schlichtherle.io.File(root, 
							relative.segment(0), TrueZipUtil.getJarArchiveDetector().ALL);
				} else {
					root = new de.schlichtherle.io.File(root, 
							relative.segment(0), ArchiveDetector.NULL);
				}
				relative = relative.removeFirstSegments(1);
			}
			return root;
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}
	
	
	public static boolean deleteAll(java.io.File f) {
		if( f instanceof de.schlichtherle.io.File ) {
			return ((de.schlichtherle.io.File)f).deleteAll();
		}
		return f.delete();
	}
}
