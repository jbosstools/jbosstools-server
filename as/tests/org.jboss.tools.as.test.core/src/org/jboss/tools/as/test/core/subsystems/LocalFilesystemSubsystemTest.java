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
package org.jboss.tools.as.test.core.subsystems;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.LocalFilesystemController;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.IOUtil;
import org.junit.After;
import org.junit.Test;

/**
 * This test will test the local filesystem subsystem. 
 * It does not check resolution of the subsystem, but rather
 * simply intsantiates it and tests it that way. 
 * 
 * Since the local version should not depend on anything from a server 
 * at all, we can not use a server in our mock
 * 
 */
public class LocalFilesystemSubsystemTest extends TestCase {
	public LocalFilesystemSubsystemTest() {
	}
	
	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
	}
	
	@Test
	public void testFilesystemTemporaryFolder() {
		TestLocalFilesystemController controller = new TestLocalFilesystemController();
		controller.initialize(null, null, createEnvironmentTempFolder1());
		assertEquals(controller.getTempFolder(), getStateLocationPath("tmpdir1").toFile());
	}
	
	
	@Test
	public void  testCopyFile() throws Exception {
		IPath tmpfile = getStateLocationPath("tmpfile1.txt");
		IPath destination = getStateLocationPath("dest/copiedFile.txt");
		IOUtil.setContents(tmpfile.toFile(), "hello");
		TestLocalFilesystemController controller = new TestLocalFilesystemController();
		controller.initialize(null, null, createEnvironmentTempFolder1());
		controller.copyFile(tmpfile.toFile(), destination, new NullProgressMonitor());
		String copiedContents = IOUtil.getContents(destination.toFile());
		assertEquals(copiedContents, "hello");
	}
	
	@Test
	public void testDeleteResource() throws Exception {
		IPath tmpfile = getStateLocationPath("tmpfile2.txt");
		IOUtil.setContents(tmpfile.toFile(), "hello");
		TestLocalFilesystemController controller = new TestLocalFilesystemController();
		controller.initialize(null, null, createEnvironmentTempFolder1());
		controller.deleteResource(tmpfile, new NullProgressMonitor());
		assertFalse(tmpfile.toFile().exists());
	}	
	
	@Test
	public void testIsFile() throws Exception {
		IPath tmpfile = getStateLocationPath("tmpfile3.txt");
		TestLocalFilesystemController controller = new TestLocalFilesystemController();
		controller.initialize(null, null, createEnvironmentTempFolder1());
		assertFalse(controller.isFile(tmpfile, new NullProgressMonitor()));
		IOUtil.setContents(tmpfile.toFile(), "hello");
		assertTrue(controller.isFile(tmpfile, new NullProgressMonitor()));		
	}

	@Test
	public void testMakeDirectoryIfRequired() throws Exception  {
		TestLocalFilesystemController controller = new TestLocalFilesystemController();
		controller.initialize(null, null, createEnvironmentTempFolder1());
		IPath tmpfile = getStateLocationPath("someFolder");
		assertFalse(tmpfile.toFile().exists());
		controller.makeDirectoryIfRequired(tmpfile, new NullProgressMonitor());
		assertTrue(tmpfile.toFile().exists());
	}
	
	@Test
	public void testMakeDirectoryIfRequiredDeep() throws Exception  {
		TestLocalFilesystemController controller = new TestLocalFilesystemController();
		controller.initialize(null, null, createEnvironmentTempFolder1());
		IPath tmpfile = getStateLocationPath("someFolder/inner/three");
		assertFalse(tmpfile.toFile().exists());
		controller.makeDirectoryIfRequired(tmpfile, new NullProgressMonitor());
		assertTrue(tmpfile.toFile().exists());
	}

	
	
	@Test
	public void testTouchResource() throws Exception {
		IPath tmpfile = getStateLocationPath("tmpfile5.txt");
		TestLocalFilesystemController controller = new TestLocalFilesystemController();
		controller.initialize(null, null, createEnvironmentTempFolder1());
		assertFalse(tmpfile.toFile().exists());
		controller.touchResource(tmpfile, new NullProgressMonitor());
		assertTrue(tmpfile.toFile().exists());
	}

	
	
	private Map<String, Object> createEnvironmentTempFolder1() {
		HashMap<String, Object> env = new HashMap<String, Object>();
		IPath loc = getStateLocationPath("tmpdir1");
		loc.toFile().mkdirs();
		env.put(LocalFilesystemController.ENV_TEMPORARY_DEPLOY_DIRECTORY, loc);
		return env;
	}
	
	// Get any path inside our test plugin's state location
	private IPath getStateLocationPath(String path) {
		return ASMatrixTests.getDefault().getStateLocation().append(path);
	}
	
	public class TestLocalFilesystemController extends LocalFilesystemController {
		// Since the temp folder is technically an internal implementation detail, 
		// we mock it out here to verify it gets called
		// we dont check here, but will check directly
		protected File getTempFolder() {
			File s = super.getTempFolder();
			return s;
		}
	}
}
