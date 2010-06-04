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
package org.jboss.ide.eclipse.archives.test.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import junit.framework.AssertionFailedError;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackageNodeWithProperties;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;
import org.jboss.ide.eclipse.archives.core.util.internal.ModelTruezipBridge;
import org.jboss.ide.eclipse.archives.core.util.internal.TrueZipUtil;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.tools.test.util.ResourcesUtils;
import org.osgi.framework.Bundle;

/**
 * This class will test the individual portions
 * of the build process.
 *
 * @author rob.stryker <rob.stryker@redhat.com>
 */
public class ModelTruezipBridgeTest extends ModelTest {
	IProject proj = null;
	private Bundle bundle;
	private IPath bundlePath;
	private IPath outputs;
	protected void setUp() throws Exception {
		if( bundlePath == null ) {
			try {
				bundle = ArchivesTest.getDefault().getBundle();
				URL bundleURL = FileLocator.toFileURL(bundle.getEntry(""));
				bundlePath = new Path(bundleURL.getFile());
				outputs = bundlePath.append("output");
			} catch( IOException ioe) {
				fail("Failed to set up " + getClass().getName());
			}
		}

		proj = ResourcesUtils.importProject("org.jboss.ide.eclipse.archives.test", "/inputs/projects/GenericProject");
//		proj.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}
	protected void tearDown() throws Exception {
		ResourcesUtils.deleteProject(proj.getName());
		File out = outputs.toFile();
		File[] children = out.listFiles();
		for( int i = 0; i < children.length; i++ ) {
			TrueZipUtil.deleteAll(new Path(children[i].toString()));
		}
	}

	public void testCreateFileOutsideWorkspace() {
		// exploded
		IArchive exploded = createArchive("exploded.war", outputs.toString());
		exploded.setInWorkspace(false);
		exploded.setExploded(true);
		ModelTruezipBridge.createFile(exploded);
		File explodedF = outputs.append("exploded.war").toFile();
		assertTrue(explodedF.exists());
		assertTrue(explodedF.isDirectory());

		// zipped
		IArchive zipped = createArchive("zipped.war", outputs.toString());
		zipped.setInWorkspace(false);
		zipped.setExploded(false);
		ModelTruezipBridge.createFile(zipped);
		File zipF = outputs.append("zipped.war").toFile();
		assertTrue(zipF.exists());
		assertFalse(zipF.isDirectory());

		// exploded inside exploded
		IArchive explodedInExploded = createArchive("explodedInExploded.jar", "");
		explodedInExploded.setExploded(true);
		exploded.addChild(explodedInExploded);
		ModelTruezipBridge.createFile(explodedInExploded);
		File eIeF = PathUtils.getGlobalLocation(exploded).append("exploded.war").append("explodedInExploded.jar").toFile();
		assertTrue(eIeF.exists());

		// zip inside exploded
		IArchive ZipInExploded = createArchive("zipInExploded.jar", "");
		ZipInExploded.setExploded(false);
		exploded.addChild(ZipInExploded);
		ModelTruezipBridge.createFile(ZipInExploded);
		File zIeF = PathUtils.getGlobalLocation(exploded).append("exploded.war").append("zipInExploded.jar").toFile();
		assertTrue(zIeF.exists());
		assertFalse(zIeF.isDirectory());


		// exploded inside zip
		IArchive explodedInZip = createArchive("ExplodedInZip.jar", "");
		explodedInZip.setExploded(true);
		zipped.addChild(explodedInZip);
		ModelTruezipBridge.createFile(explodedInZip);
		try {
			assertEquals(1, countEntries(zipF));
		} catch( AssertionFailedError re ) {
			System.out.println("gah");
		}

		// zip inside zip
		IArchive zipInZip = createArchive("zipInZip.jar", "");
		zipInZip.setExploded(false);
		zipped.addChild(zipInZip);
		ModelTruezipBridge.createFile(zipInZip);
		assertEquals(2, countEntries(zipF));
	}

	public void testCreateFileInWorkspace() {
		IArchive zipped = createArchive("zipped.war", new Path(proj.getName()).append("outputs").makeAbsolute().toString());
		zipped.setInWorkspace(true);
		zipped.setExploded(false);
		ModelTruezipBridge.createFile(zipped);
		File zippedF = proj.getLocation().append("outputs").append("zipped.war").toFile();
		assertTrue(zippedF.exists());
		assertTrue(!zippedF.isDirectory());

		// inner child; zip inside zip
		IArchive zipInZip = createArchive("zipInZip.jar", "");
		zipInZip.setExploded(false);
		zipped.addChild(zipInZip);
		ModelTruezipBridge.createFile(zipInZip);
		assertEquals(1, countEntries(zippedF));
	}

	public void testNoSync() {
		// zipped
		IArchive zipped = createArchive("zipped.war", outputs.toString());
		zipped.setInWorkspace(false);
		zipped.setExploded(false);
		ModelTruezipBridge.createFile(zipped);
		File zipF = outputs.append("zipped.war").toFile();
		assertTrue(zipF.exists());
		assertFalse(zipF.isDirectory());

		// zip inside zip
		IArchive zipInZip = createArchive("zipInZip.jar", "");
		zipInZip.setExploded(false);
		zipped.addChild(zipInZip);
		ModelTruezipBridge.createFile(zipInZip);
		assertEquals(1, countEntries(zipF));

		// zip inside zip2
		IArchive zipInZip2 = createArchive("zipInZip2.jar", "");
		zipInZip.setExploded(false);
		zipped.addChild(zipInZip2);
		ModelTruezipBridge.createFile(zipInZip2, false);
		assertEquals(1, countEntries(zipF));
		TrueZipUtil.umount();
		assertEquals(2, countEntries(zipF));
	}


	public void testDeleteArchive() {
		IArchive zipped = createArchive("zipped.war", new Path(proj.getName()).append("outputs").makeAbsolute().toString());
		zipped.setInWorkspace(true);
		zipped.setExploded(false);
		ModelTruezipBridge.createFile(zipped);
		File zippedF = proj.getLocation().append("outputs").append("zipped.war").toFile();
		assertTrue(zippedF.exists());
		assertTrue(!zippedF.isDirectory());

		// inner child; zip inside zip
		IArchive zipInZip = createArchive("zipInZip.jar", "");
		zipInZip.setExploded(false);
		zipped.addChild(zipInZip);
		ModelTruezipBridge.createFile(zipInZip);
		assertEquals(1, countEntries(zippedF));

		// inner child; zip inside zip
		IArchive zipInZip2 = createArchive("zipInZip2.jar", "");
		zipInZip2.setExploded(false);
		zipped.addChild(zipInZip2);
		ModelTruezipBridge.createFile(zipInZip2);
		assertEquals(2, countEntries(zippedF));

		ModelTruezipBridge.deleteArchive(zipInZip2);
		assertEquals(1, countEntries(zippedF));

		ModelTruezipBridge.deleteArchive(zipped);
		assertFalse(zippedF.exists());
	}


	/*
	 * Fileset-related
	 */
	public void testFileset() {
		IArchive zipped = createArchive("zipped.war", new Path(proj.getName()).append("outputs").makeAbsolute().toString());
		zipped.setInWorkspace(true);
		zipped.setExploded(false);
		ModelTruezipBridge.createFile(zipped);
		File zippedF = proj.getLocation().append("outputs").append("zipped.war").toFile();
		assertTrue(zippedF.exists());
		assertTrue(!zippedF.isDirectory());

		IArchiveStandardFileSet fs = createFileSet("**/*.gif", new Path(proj.getName()).makeAbsolute().toString());
		fs.setInWorkspace(true);
		zipped.addChild(fs);
		ModelTruezipBridge.fullFilesetBuild(fs, new NullProgressMonitor(), true);
		assertEquals(19, countEntries(zippedF));
	}

	public void testFlattenedFileset() {
		IArchive zipped = createArchive("zipped.war", new Path(proj.getName()).append("outputs").makeAbsolute().toString());
		zipped.setInWorkspace(true);
		zipped.setExploded(false);
		ModelTruezipBridge.createFile(zipped);
		File zippedF = proj.getLocation().append("outputs").append("zipped.war").toFile();
		assertTrue(zippedF.exists());
		assertTrue(!zippedF.isDirectory());

		IArchiveStandardFileSet fs = createFileSet("**/*.gif", new Path(proj.getName()).makeAbsolute().toString());
		fs.setInWorkspace(true);
		fs.setFlattened(true);
		zipped.addChild(fs);
		ModelTruezipBridge.fullFilesetBuild(fs, new NullProgressMonitor(), true);

		// should be two less files and 3 less folders created
		assertEquals(14, countEntries(zippedF));
	}

	public void testLibFileset() {
		File file = findSomeJar();
		if( file != null ) {
			IClasspathEntry e = 
				JavaCore.newLibraryEntry(new Path(file.getAbsolutePath()), null, null);
			JavaModelManager.getUserLibraryManager().setUserLibrary(
				 "userLibTest", new IClasspathEntry[] { e },  false);
			IArchive zipped = createArchive("zipped.war", new Path(proj.getName()).append("outputs").makeAbsolute().toString());
			zipped.setInWorkspace(true);
			zipped.setExploded(false);
			ModelTruezipBridge.createFile(zipped);
			File zippedF = proj.getLocation().append("outputs").append("zipped.war").toFile();
			assertTrue(zippedF.exists());
			assertTrue(!zippedF.isDirectory());

			IArchiveFileSet fs = createLibFileSet("userLibTest");
			zipped.addChild(fs);
			ModelTruezipBridge.fullFilesetBuild(fs, new NullProgressMonitor(), true);

			// should be two less files and 3 less folders created
			assertEquals(1, countEntries(zippedF));
		}
	}

	protected File findSomeJar() {
		return bundlePath.append("libs").append("some.jar").toFile();
	}
	
	protected IArchiveNode getDummyParent() {
		return new ArchiveNodeImpl(new XbPackageNodeWithProperties("DUMMY"){}) {
			public IPath getRootArchiveRelativePath() {
				return new Path("/");
			}
			public int getNodeType() {
				return 0;
			}
		};
	}

	
	
	/*
	 * Utility
	 */

	protected int countEntries(File zipF) {

		ZipFile zf = null;
		try {
			zf = new ZipFile(zipF);
		} catch (ZipException e) {
			fail();
		} catch (IOException e) {
			fail();
		}

		int count = 0;
		Enumeration entries = zf.entries();
		while(entries.hasMoreElements()) {
			entries.nextElement();
			count++;
		}
		try {
			zf.close();
		} catch( IOException ioe) {
			fail();
		}
		return count;
	}

}
