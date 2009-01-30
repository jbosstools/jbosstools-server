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
package org.jboss.ide.eclipse.archives.test.util;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.util.internal.TrueZipUtil;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.osgi.framework.Bundle;

public class TruezipUtilTest extends TestCase {
	private Bundle bundle;
	private IPath bundlePath;
	protected void setUp() {
		if( bundlePath == null ) {
			try {
				bundle = ArchivesTest.getDefault().getBundle();
				URL bundleURL = FileLocator.toFileURL(bundle.getEntry(""));
				bundlePath = new Path(bundleURL.getFile());
			} catch( IOException ioe) {}
		}
	}
	
	protected void tearDown() {
		try {
			de.schlichtherle.io.File.umount();
		} catch( Exception e ) {}
		FileIOUtil.clearFolder(bundlePath.append("tmp").toFile().listFiles());
	}
	
	
	public void testPathExists() {
		IPath inputs = bundlePath.append("inputs");
		assertTrue(TrueZipUtil.pathExists(bundlePath));
		assertTrue(TrueZipUtil.pathExists(inputs));
		assertTrue(TrueZipUtil.pathExists(inputs.append("fileTrees")));
		assertTrue(TrueZipUtil.pathExists(inputs.append("fileTrees").append("flatTextFiles").append("one.txt")));
		
		assertFalse(TrueZipUtil.pathExists(bundlePath.append("doesNotExist")));
		assertFalse(TrueZipUtil.pathExists(inputs.append("doesNotExist")));
		assertFalse(TrueZipUtil.pathExists(inputs.append("fileTrees").append("flatTextFiles").append("doesNotExist.txt")));
	}
	
	public void testCreationAndDeletion() {
		IPath tmpDir = bundlePath.append("tmp");
		
		// create archives via both APIs, 
		IPath creationArchive1 = tmpDir.append("creationArchive1.jar");
		IPath creationArchive2 = tmpDir.append("creationArchive2.zip");
		IPath creationArchive3 = tmpDir.append("creationArchive3");
		
		assertFalse(TrueZipUtil.pathExists(creationArchive1));
		TrueZipUtil.createArchive(creationArchive1);
		assertTrue(TrueZipUtil.pathExists(creationArchive1));
		assertFalse(creationArchive1.toFile().isDirectory());
		
		assertFalse(TrueZipUtil.pathExists(creationArchive2));
		TrueZipUtil.createArchive(creationArchive2);
		assertTrue(TrueZipUtil.pathExists(creationArchive2));
		assertFalse(creationArchive2.toFile().isDirectory());

		// and also create an archive with no suffix
		assertFalse(TrueZipUtil.pathExists(creationArchive3));
		TrueZipUtil.createArchive(creationArchive3);
		assertTrue(TrueZipUtil.pathExists(creationArchive3));
		assertFalse(creationArchive3.toFile().isDirectory());

		
		// create folders with various names
		IPath creationFolder1 = tmpDir.append("creationFolder1.jar");
		IPath creationFolder2 = tmpDir.append("creationFolder2.zip");
		IPath creationFolder3 = tmpDir.append("creationFolder3");

		assertFalse(TrueZipUtil.pathExists(creationFolder1));
		TrueZipUtil.createFolder(creationFolder1);
		assertTrue(TrueZipUtil.pathExists(creationFolder1));
		assertTrue(creationFolder1.toFile().isDirectory());
		
		assertFalse(TrueZipUtil.pathExists(creationFolder2));
		TrueZipUtil.createFolder(creationFolder2);
		assertTrue(TrueZipUtil.pathExists(creationFolder2));
		assertTrue(creationFolder2.toFile().isDirectory());
		
		assertFalse(TrueZipUtil.pathExists(creationFolder3));
		TrueZipUtil.createFolder(creationFolder3);
		assertTrue(TrueZipUtil.pathExists(creationFolder3));
		assertTrue(creationFolder3.toFile().isDirectory());
		
		
		// folders and archives of all extensions work.
		// keep one folder and one archive and add to each 
		// another folder, archive, and file
		
		IPath archiveFolder = creationArchive1.append("folderName");
		IPath archiveArchive = creationArchive1.append("archiveName.war");
		
		IPath folderFolder = creationFolder1.append("folderName");
		IPath folderArchive = creationFolder1.append("archiveName.war");

		
		assertFalse(TrueZipUtil.pathExists(archiveFolder));
		assertFalse(TrueZipUtil.pathExists(archiveArchive));
		TrueZipUtil.createFolder(archiveFolder);
		TrueZipUtil.createArchive(archiveArchive);
		try {de.schlichtherle.io.File.umount();} catch( Exception e ) {}
		assertTrue(TrueZipUtil.pathExists(archiveFolder));
		assertTrue(TrueZipUtil.pathExists(archiveArchive));
		assertFalse(archiveFolder.toFile().exists());
		assertFalse(archiveArchive.toFile().exists());
		

		assertFalse(TrueZipUtil.pathExists(folderFolder));
		assertFalse(TrueZipUtil.pathExists(folderArchive));
		TrueZipUtil.createFolder(folderFolder);
		TrueZipUtil.createArchive(folderArchive);
		try {de.schlichtherle.io.File.umount();} catch( Exception e ) {}
		assertTrue(TrueZipUtil.pathExists(archiveFolder));
		assertTrue(TrueZipUtil.pathExists(archiveArchive));
		assertTrue(folderFolder.toFile().exists());
		assertTrue(folderArchive.toFile().exists());

		// copy a file into them
		IPath archiveFile = creationArchive1.append("file.txt");
		IPath folderFile = creationArchive1.append("file.txt");
		
		String srcPath = bundlePath.append("inputs").append("fileTrees")
				.append("flatTextFiles").append("two.txt").toOSString();
		try {
			assertFalse(TrueZipUtil.pathExists(archiveFile));
			assertFalse(TrueZipUtil.pathExists(folderFile));
			TrueZipUtil.copyFile(srcPath, archiveFile);
			TrueZipUtil.copyFile(srcPath, folderFile);
			assertTrue(TrueZipUtil.pathExists(archiveFile));
			assertTrue(TrueZipUtil.pathExists(folderFile));
		} catch( Exception e ) {
			fail("IOException: " + e.getMessage());
		}
		
		de.schlichtherle.io.File f = TrueZipUtil.getFile(creationFolder1); 
		int numChildren = f.list().length;
		assertEquals(2, numChildren);
		TrueZipUtil.deleteEmptyChildren(f);
		int numChildren2 = f.list().length;
		assertEquals(0, numChildren2);
		
		de.schlichtherle.io.File f2 = TrueZipUtil.getFile(creationArchive1); 
		TrueZipUtil.deleteAll(creationArchive1);
		assertFalse(TrueZipUtil.pathExists(f2));
	}
	
	protected IPath bundleEntryToGlobalPath(String entry) {
		try {
			return new Path(FileLocator.toFileURL(bundle.getEntry(entry)).getFile());
		} catch( IOException e ) {
			return null;
		}
	}
}
