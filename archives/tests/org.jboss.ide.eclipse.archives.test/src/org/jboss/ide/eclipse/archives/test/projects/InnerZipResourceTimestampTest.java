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
package org.jboss.ide.eclipse.archives.test.projects;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.util.internal.TrueZipUtil;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.osgi.framework.Bundle;

import de.schlichtherle.io.AbstractArchiveDetector;
import de.schlichtherle.io.ArchiveDetector;
import de.schlichtherle.io.File;
import de.schlichtherle.io.archive.spi.ArchiveDriver;

/**
 * This class tests first and foremost
 * the presence of a ${archives_current_project}
 * extension to allow the currently building
 * project to be agnostic
 *
 * During this JIRA, workspace paths became conscious of
 * their absolute / relative status and are now interpreted
 * differently according to their status.
 * @author rob
 *
 */
public class InnerZipResourceTimestampTest extends TestCase {
	private long startTime;
	private Bundle bundle;
	protected void setUp() throws Exception {
		startTime = new Date().getTime();
		bundle = ArchivesTest.getDefault().getBundle();
	}
	
	protected void tearDown() throws Exception {
	}
	
	protected java.io.File findSomeJar() {
		try {
			URL bundleURL = FileLocator.toFileURL(bundle.getEntry(""));
			IPath bundlePath = new Path(bundleURL.getFile());
			return bundlePath.append("libs").append("some.jar").toFile();
		} catch(IOException ioe){}
		return null;
	}

	public void testRawTruezipTimestamps() {
		java.io.File someJar = findSomeJar();
		IPath src = new Path(someJar.getAbsolutePath());
		IPath dest = ArchivesTest.getDefault().getStateLocation().append("some.jar");
		//File destFile = new de.schlichtherle.io.File(dest.toOSString(), new PureSourceArchiveDetector());
		File destFile = new de.schlichtherle.io.File(dest.toOSString(), ArchiveDetector.NULL);

		boolean copySuccess = new de.schlichtherle.io.File(someJar).archiveCopyAllTo(destFile);
		destFile.setLastModified(new Date().getTime());
		TrueZipUtil.umount();
		assertTrue(copySuccess);
		java.io.File destNonTruezip = new java.io.File(destFile.getAbsolutePath());
		assertTrue(destNonTruezip.exists());
		assertTrue(destNonTruezip.isFile());
		assertTrue(destNonTruezip.lastModified() > someJar.lastModified());
		
		verifyEquals(src, dest, "plugin.xml");
		verifyEquals(src, dest, "plugin.properties");
		verifyEquals(src, dest, "about.html");
		verifyEquals(src, dest, ".options");
		verifyEquals(src, dest, ".api_description");
		verifyEquals(src, dest, "jdtCompilerAdapter.jar");
		
		IPath tmp1 = ArchivesTest.getDefault().getStateLocation().append("tmp1");
		IPath tmp2 = ArchivesTest.getDefault().getStateLocation().append("tmp2");
		tmp1.toFile().mkdir();
		tmp2.toFile().mkdir();
		unzipFile(src, tmp1);
		unzipFile(dest, tmp2);
		
		String[] children = tmp1.toFile().list();
		for( int i = 0; i < children.length; i++ ) {
			assertTrue(tmp1.append(children[i]).toFile().exists());
			assertTrue(tmp1.append(children[i]).toFile().isFile());
			assertTrue(tmp2.append(children[i]).toFile().isFile());
			assertTrue(tmp2.append(children[i]).toFile().isFile());
			assertEquals(tmp1.append(children[i]).toFile().length(), 
					tmp2.append(children[i]).toFile().length());
		}
		System.out.println("DONE");
	}
	
	private void verifyEquals(IPath src, IPath dest, String relative) {
		File prePath = new File(src.append(relative).toOSString());
		File postPath = new File(dest.append(relative).toOSString());
		assertEquals(prePath.lastModified(), postPath.lastModified());
		assertEquals(prePath.length(), postPath.length());
	}
	
	
	
	public static void unzipFile(IPath zipped, IPath toLoc) {
		toLoc.toFile().mkdirs();
		final int BUFFER = 2048;
		try {
			  BufferedOutputStream dest = null;
		      FileInputStream fis = new 
		 	  FileInputStream(zipped.toFile());
			  ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
	          ZipEntry entry;
	          while((entry = zis.getNextEntry()) != null) {
	             int count;
	             byte data[] = new byte[BUFFER];
	             // write the files to the disk
	             toLoc.append(entry.getName()).toFile().getParentFile().mkdirs();
	             if( !toLoc.append(entry.getName()).toFile().exists()) {
		             FileOutputStream fos = new FileOutputStream(toLoc.append(entry.getName()).toOSString());
		             dest = new BufferedOutputStream(fos, BUFFER);
		             while ((count = zis.read(data, 0, BUFFER)) != -1) {
		                dest.write(data, 0, count);
		             }
		             dest.flush();
		             dest.close();
	             }
	          }
	          zis.close();
	       } catch(Exception e) {
	          e.printStackTrace();
	       }
	}

	
	private static class PureSourceArchiveDetector extends AbstractArchiveDetector {
		public ArchiveDriver getArchiveDriver(String path) {
			return null;
		}
	}
}
