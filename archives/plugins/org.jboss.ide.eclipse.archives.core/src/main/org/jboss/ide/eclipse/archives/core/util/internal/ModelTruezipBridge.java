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
import org.eclipse.core.runtime.Path;
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
	public static void deleteArchive(IArchive archive) {
		final File file = getFile(archive);
		boolean b = file.deleteAll();
		TrueZipUtil.sync();
	}
		
	public static void cleanFolder(IArchiveFolder folder) {
		cleanFolder(getFile(folder), true);
	}
	
	public static void cleanFolder(java.io.File folder, boolean sync) {
		TrueZipUtil.deleteEmptyChildren(folder);
		if( sync )
			TrueZipUtil.sync();
	}
	
	public static void fullFilesetBuild(IArchiveFileSet fileset) {
		fullFilesetBuild(fileset, true);
	}
	public static void fullFilesetBuild(final IArchiveFileSet fileset, boolean sync) {
		FileWrapper[] files = fileset.findMatchingPaths();
		copyFiles(fileset, files, false);
		if( sync )
			TrueZipUtil.sync();
	}
		
	public static void fullFilesetsRemove(IArchiveFileSet[] filesets, boolean sync) {
		for( int i = 0; i < filesets.length; i++ )
			fullFilesetRemove(filesets[i], false);
		if( sync )
			TrueZipUtil.sync();
	}
	
	
	// Let them know which files were removed, for events
	public static IPath[] fullFilesetRemove(final IArchiveFileSet fileset, boolean sync) {
		FileWrapper[] files = fileset.findMatchingPaths();
		final ArrayList<FileWrapper> list = new ArrayList<FileWrapper>();
		list.addAll(Arrays.asList(files));
		for( int i = 0; i < files.length; i++ ) {
			if( !ModelUtil.otherFilesetMatchesPathAndOutputLocation(fileset, files[i])) {
				// remove
				deleteFiles(fileset, new FileWrapper[] {files[i]}, false);
			} else {
				list.remove(files[i]);
			}
		}
		
		// kinda ugly here.   delete all empty folders beneath
		cleanFolder(getFile(fileset), false);
		
		// now ensure all mandatory child folders are still there
		fileset.getParent().accept(new IArchiveNodeVisitor() {
			public boolean visit(IArchiveNode node) {
				if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE) {
					createFile(node);
				} else if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FOLDER) {
					createFile(node);
				}
				return true;
			} 
		} );
				
		if( sync ) 
			TrueZipUtil.sync();
		
		return list.toArray(new IPath[list.size()]);
	}

	
	public static void copyFiles(IArchiveFileSet[] filesets, FileWrapper[] files) {
		copyFiles(filesets, files, true);
	}
	
	public static void copyFiles(final IArchiveFileSet[] filesets, final FileWrapper[] files, boolean sync) {
		for( int i = 0; i < filesets.length; i++ ) {
			copyFiles(filesets[i], files, false);
		}
		if( sync ) 
			TrueZipUtil.sync();
		
	}
	
	public static void copyFiles(IArchiveFileSet fileset, final FileWrapper[] files) {
		copyFiles(fileset, files, true);
	}
	
	public static void copyFiles(IArchiveFileSet fileset, final FileWrapper[] files, boolean sync) {
		final File[] destFiles = getFiles(files, fileset);
		for( int i = 0; i < files.length; i++ ) {
			TrueZipUtil.copyFile(files[i].getAbsolutePath(), destFiles[i]);
		}
		if( sync ) 
			TrueZipUtil.sync();
	}
	
	
	/*
	 * Deleting files
	 */
	public static void deleteFiles(IArchiveFileSet[] filesets, FileWrapper[] files ) {
		deleteFiles(filesets, files, true);
	}
	public static void deleteFiles(final IArchiveFileSet[] filesets, final FileWrapper[] files, boolean sync ) {
		for( int i = 0; i < filesets.length; i++ ) {
			deleteFiles(filesets[i], files, false);
		}
		if( sync ) 
			TrueZipUtil.sync();
	}
	
	public static void deleteFiles(IArchiveFileSet fileset, final FileWrapper[] paths ) {
		deleteFiles(fileset, paths, true);
	}
	public static void deleteFiles(IArchiveFileSet fileset, final FileWrapper[] files, boolean sync ) {
		final File[] destFiles = getFiles(files, fileset);
		for( int i = 0; i < files.length; i++ ) {
			TrueZipUtil.deleteAll(destFiles[i]);
		}
		
		if( sync ) 	
			TrueZipUtil.sync();
	}
	
	
	/**
	 * Creates the file, folder, or archive represented by the node.
	 * Does nothing for filesets
	 * @param node
	 */
	public static void createFile(final IArchiveNode node) {
		createFile(node, true);
	}
	public static void createFile(final IArchiveNode node, boolean sync) {
		File f = getFile(node);
		if( f != null ) {
			f.mkdirs();
		}
		if( sync ) 
			TrueZipUtil.sync();
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
