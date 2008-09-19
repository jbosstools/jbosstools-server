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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeVisitor;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;

import de.schlichtherle.io.ArchiveDetector;
import de.schlichtherle.io.File;

/**
 * This class is meant to bridge between the model
 * and the raw true-zip utility class. It is a higher level
 * API which deals with filesets and packages instead of
 * raw Strings and paths.
 *
 * It will also make sure that a de.schlichtherle.io.File is
 * created with the proper ArchiveDetector for each and every
 * level, rather than the TrueZipUtil class, which not accurately
 * create the proper File type for exploded archives.
 *
 * @author rstryker
 *
 */
public class ModelTruezipBridge {
	public static class FileWrapperStatusPair {
		public FileWrapper[] f;
		public IStatus[] s;
		public FileWrapperStatusPair(FileWrapper[] files, IStatus[] statuses) {
			this.f = files;
			this.s = statuses;
		}
	}
	public static FileWrapperStatusPair fullFilesetBuild(final IArchiveFileSet fileset, IProgressMonitor monitor, boolean sync) {
		FileWrapper[] files = fileset.findMatchingPaths();
		IStatus[] s = copyFiles(fileset, files, monitor, false);
		if( sync )
			TrueZipUtil.sync();
		return new FileWrapperStatusPair( files, s );
	}

	/*
	 * 	Returns an Object array as follows:
	 *  Object[] {
	 *     FileWrapper[] removedPaths,
	 *     IStatus[] errors
	 *  }
	 */
	public static FileWrapperStatusPair fullFilesetRemove(final IArchiveFileSet fileset, IProgressMonitor monitor, boolean sync) {
		monitor.beginTask("Removing fileset: " + fileset.toString(), 2500);
		FileWrapper[] files = fileset.findMatchingPaths();
		final ArrayList<IStatus> errors = new ArrayList<IStatus>();
		final ArrayList<FileWrapper> list = new ArrayList<FileWrapper>();
		list.addAll(Arrays.asList(files));
		IProgressMonitor filesMonitor = new SubProgressMonitor(monitor, 2000);
		filesMonitor.beginTask("Removing " + files.length + " files", files.length * 100);
		for( int i = 0; i < files.length; i++ ) {
			if( !ModelUtil.otherFilesetMatchesPathAndOutputLocation(fileset, files[i])) {
				// remove
				errors.addAll(Arrays.asList(deleteFiles(fileset, new FileWrapper[] {files[i]}, new NullProgressMonitor(), false)));
			} else {
				list.remove(files[i]);
			}
			filesMonitor.worked(100);
		}
		filesMonitor.done();

		// kinda ugly here.   delete all empty folders beneath
		File folder = getFile(fileset);
		if( !cleanFolder(folder, false) ) {
			IStatus e = new Status(IStatus.ERROR, ArchivesCore.PLUGIN_ID, "Error emptying folder " + folder.toString());
			errors.add(e);
		}
		monitor.worked(250);

		// now ensure all mandatory child folders are still there
		fileset.getParent().accept(new IArchiveNodeVisitor() {
			public boolean visit(IArchiveNode node) {
				boolean b = true;
				if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE) {
					b = createFile(node);
				} else if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FOLDER) {
					b = createFile(node);
				}
				if( !b ) {
					IStatus e = new Status(IStatus.ERROR, ArchivesCore.PLUGIN_ID, "Error creating file " + getFile(node).toString());
					errors.add(e);
				}
				return true;
			}
		} );

		if( sync )
			TrueZipUtil.sync();
		monitor.worked(250);
		monitor.done();

		IStatus[] errorsArr = errors.toArray(new IStatus[errors.size()]);
		FileWrapper[] files2 = list.toArray(new FileWrapper[list.size()]);
		return new FileWrapperStatusPair( files2, errorsArr);
	}

	public static IStatus[] copyFiles(IArchiveFileSet fileset, final FileWrapper[] files, IProgressMonitor monitor, boolean sync) {
		monitor.beginTask("Copying " + files.length + " files", files.length * 100);
		boolean b = true;
		ArrayList<IStatus> list = new ArrayList<IStatus>();
		final File[] destFiles = getFiles(files, fileset);
		for( int i = 0; i < files.length; i++ ) {
			b = TrueZipUtil.copyFile(files[i].getAbsolutePath(), destFiles[i]);
			if( b == false ) {
				list.add(new Status(IStatus.ERROR, ArchivesCore.PLUGIN_ID, "File copy failed. Source=" + files[i].getAbsolutePath() + ", dest=" + destFiles[i]));
			}
			monitor.worked(100);
		}
		if( sync )
			TrueZipUtil.sync();
		monitor.done();
		return list.toArray(new IStatus[list.size()]);
	}

	/*
	 * Deleting files
	 */
	public static IStatus[] deleteFiles(IArchiveFileSet fileset, final FileWrapper[] files, IProgressMonitor monitor, boolean sync ) {
		monitor.beginTask("Deleting " + files.length + " files", files.length * 100);
		final File[] destFiles = getFiles(files, fileset);
		ArrayList<IStatus> list = new ArrayList<IStatus>();
		for( int i = 0; i < files.length; i++ ) {
			if( !TrueZipUtil.deleteAll(destFiles[i]) ) {
				IStatus e = new Status(IStatus.ERROR, ArchivesCore.PLUGIN_ID, "Error deleting file " + destFiles[i].toString());
				list.add(e);
			}
			monitor.worked(100);
		}
		if( sync )
			TrueZipUtil.sync();
		monitor.done();
		return list.toArray(new IStatus[list.size()]);
	}


	public static boolean deleteArchive(IArchive archive) {
		final File file = getFile(archive);
		boolean b = file.deleteAll();
		TrueZipUtil.sync();
		return b;
	}

	public static boolean cleanFolder(IArchiveFolder folder) {
		return cleanFolder(getFile(folder), true);
	}

	public static boolean cleanFolder(java.io.File folder, boolean sync) {
		boolean b = TrueZipUtil.deleteEmptyChildren(folder);
		if( sync )
			TrueZipUtil.sync();
		return b;
	}

	/**
	 * Creates the file, folder, or archive represented by the node.
	 * Does nothing for filesets
	 * @param node
	 */
	public static boolean createFile(final IArchiveNode node) {
		return createFile(node, true);
	}
	public static boolean createFile(final IArchiveNode node, boolean sync) {
		File f = getFile(node);
		boolean b = true;
		if( f != null ) {
			b = f.mkdirs();
		}
		if( sync )
			TrueZipUtil.sync();
		return b;
	}



	/**
	 * Gets all properly-created de.sch destination files for a fileset
	 * @param inputFiles
	 * @param fs
	 * @return
	 */
	private static File[] getFiles(FileWrapper[] inputFiles, IArchiveFileSet fs ) {
		String filesetRelative;
		File fsFile = getFile(fs);
		if( fsFile == null )
			return new File[]{};

		File[] returnFiles = new File[inputFiles.length];
		for( int i = 0; i < inputFiles.length; i++ ) {
			if( fs.isFlattened() )
				filesetRelative = inputFiles[i].getOutputName();
			else
				filesetRelative = inputFiles[i].getFilesetRelative();

			File parentFile;
			if(new Path(filesetRelative).segmentCount() > 1 ) {
				String tmp = new Path(filesetRelative).removeLastSegments(1).toString();
				parentFile = new File(fsFile, tmp, ArchiveDetector.NULL);
				if( parentFile.getEnclArchive() != null )
					parentFile = new File(fsFile, tmp, ArchiveDetector.DEFAULT);
			} else {
				parentFile = fsFile;
			}
			returnFiles[i] = new File(parentFile, new Path(filesetRelative).lastSegment(), ArchiveDetector.DEFAULT);
		}
		return returnFiles;
	}


	/**
	 * This should go through the tree and create a file that is
	 * correctly perceived at each step of the way.
	 *
	 * To just create a new File would let the Archive Detector have too
	 * much control, and *ALL* war's and jars, including exploded ones,
	 * would be treated as archives instead of folders.
	 * @param node
	 * @return
	 */
	private static File getFile(IArchiveNode node) {
		if( node == null ) return null;

		if( node.getNodeType() == IArchiveNode.TYPE_MODEL_ROOT ) return null;

		if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FILESET)
			return getFile(node.getParent());

		File parentFile = getFile(node.getParent());
		if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE ) {
			IArchive node2 = ((IArchive)node);
			boolean exploded = ((IArchive)node).isExploded();
			ArchiveDetector detector = exploded ? ArchiveDetector.NULL : TrueZipUtil.getJarArchiveDetector();
			if( parentFile == null ) {
				// we're a root archive, and so the destination folder must be a real folder
				IPath p = PathUtils.getGlobalLocation(node2);
				if( p == null ) return null;
				parentFile = new File(p.toOSString(), ArchiveDetector.NULL);
			}
			return new File(parentFile, node2.getName(), detector);
		}
		if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FOLDER ) {
			return new File(parentFile, ((IArchiveFolder)node).getName(), ArchiveDetector.NULL);
		}
		return null;
	}

}
