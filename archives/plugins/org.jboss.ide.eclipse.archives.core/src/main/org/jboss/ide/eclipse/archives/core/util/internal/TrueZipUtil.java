/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.archives.core.util.internal;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;

import de.schlichtherle.io.AbstractArchiveDetector;
import de.schlichtherle.io.ArchiveDetector;
import de.schlichtherle.io.ArchiveException;
import de.schlichtherle.io.archive.spi.ArchiveDriver;
import de.schlichtherle.io.archive.zip.Zip32Driver;

/**
 * Accesses raw files with the truezip filesystem
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 *
 */
public class TrueZipUtil {

	public static de.schlichtherle.io.File getFile(IPath path) {
		return getFile(path, ArchiveDetector.DEFAULT);
	}
	public static de.schlichtherle.io.File getFile(IPath path, ArchiveDetector detector) {
		return new de.schlichtherle.io.File(path.toOSString(), detector);
	}

	public static boolean pathExists(IPath path) {
		return pathExists( getFile(path));
	}
	public static boolean pathExists( de.schlichtherle.io.File file) {
		return file.exists();
	}


	public static long getTimestamp(IPath path) {
		return getTimestamp( getFile(path));
	}

	public static long getTimestamp(de.schlichtherle.io.File file) {
		return file.lastModified();
	}


	public static boolean copyFile(String source, IPath dest) throws IOException {
		return copyFile(source, getFile(dest));
	}

	public static boolean copyFile(String source, de.schlichtherle.io.File file) {
		file.getParentFile().mkdirs();
		boolean b = new de.schlichtherle.io.File(source).copyAllTo(file);
	    return b && updateParentTimestamps(file);
	}

	public static boolean touchFile(IPath path) {
		de.schlichtherle.io.File f = getFile(path);
		boolean b = f.setLastModified(System.currentTimeMillis());
	    return b && updateParentTimestamps(path);
	}


	// Delete methods
	public static boolean deleteAll(IPath path, String fileName) {
		return deleteAll(path.append(fileName));
	}
	public static boolean deleteAll(IPath path) {
		return deleteAll(getFile(path));
	}
	public static boolean deleteAll(de.schlichtherle.io.File file) {
		return file.deleteAll();
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
		boolean b = new de.schlichtherle.io.File(getFile(parent, ArchiveDetector.DEFAULT), folderName, ArchiveDetector.NULL).mkdirs();
		return b && updateParentTimestamps(parent.append(folderName));
	}
	public static boolean createFolder(IPath path) {
		return createFolder(path.removeLastSegments(1), path.lastSegment());
	}
	public static boolean createArchive(IPath parent, String folderName) {
		boolean b = new de.schlichtherle.io.File(getFile(parent, ArchiveDetector.DEFAULT), folderName, getJarArchiveDetector()).mkdirs();
	    return b && updateParentTimestamps(parent.append(folderName));
	}
	public static boolean createArchive(IPath path) {
		return createArchive(path.removeLastSegments(1), path.lastSegment());
	}
	public static void umount() {
		try {
			de.schlichtherle.io.File.umount();
		} catch( ArchiveException ae ) {
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
	public static boolean updateParentTimestamps(de.schlichtherle.io.File file) {
		long time = System.currentTimeMillis();
		de.schlichtherle.io.File parent = file.getEnclArchive();
		boolean b = true;
		while( parent != null ) {
			b &= parent.setLastModified(time);
			parent = parent.getEnclArchive();
		}
		return b;
	}


	private static ArchiveDetector JAR_ARCHIVE_DETECTOR;
	public static ArchiveDetector getJarArchiveDetector() {
		if( JAR_ARCHIVE_DETECTOR == null ) {
			JAR_ARCHIVE_DETECTOR = new JarArchiveDetector();
		}
		return JAR_ARCHIVE_DETECTOR;
	}

	public static class JarArchiveDetector extends AbstractArchiveDetector {
		public ArchiveDriver getArchiveDriver(String arg0) {
			return new Zip32Driver();
		}

	}
}
