/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.subsystems;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.rse.core.subsystems.RSEFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.LocalFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.IOUtil;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This test will test the rse filesystem subsystem. 
 * It does not check resolution of the subsystem, but rather
 * simply intsantiates it and tests it that way. 
 * 
 */
@RunWith(value = Parameterized.class)
public class RSEFilesystemSubsystemTest extends TestCase {
	private String serverType;
	private IServer server;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(new Object[]{IJBossToolingConstants.DEPLOY_ONLY_SERVER});
	}
	 
	public RSEFilesystemSubsystemTest(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() throws Exception {
		try {
			server = ServerCreationTestUtils.createServerWithRuntime(serverType, getClass().getName() + serverType);
			IServerWorkingCopy wc = server.createWorkingCopy();
			ServerProfileModel.setProfile(wc, "rse");
			server = wc.save(true, new NullProgressMonitor());
		} catch(CoreException ce) {
			ce.printStackTrace();
			throw ce;
		}
	}

	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
	}
		
	@Test
	public void  testCopyFile() throws Exception {
		IPath tmpfile = getStateLocationPath("tmpfile1.txt");
		IPath destination = getStateLocationPath("dest/copiedFile.txt");
		IOUtil.setContents(tmpfile.toFile(), "hello");
		TestRSEFilesystemController controller = new TestRSEFilesystemController();
		controller.initialize(server, null, createEnvironmentTempFolder1());
		controller.copyFile(tmpfile.toFile(), destination, new NullProgressMonitor());
		String copiedContents = IOUtil.getContents(destination.toFile());
		assertEquals(copiedContents, "hello");
	}
	
	@Test
	public void testDeleteResource() throws Exception {
		IPath tmpfile = getStateLocationPath("tmpfile2.txt");
		IOUtil.setContents(tmpfile.toFile(), "hello");
		TestRSEFilesystemController controller = new TestRSEFilesystemController();
		controller.initialize(server, null, createEnvironmentTempFolder1());
		controller.deleteResource(tmpfile, new NullProgressMonitor());
		assertFalse(tmpfile.toFile().exists());
	}	

	@Test
	public void testDeleteEmptyFolder() throws Exception {
		IPath tmpFolder = getStateLocationPath("tmpFolder");
		tmpFolder.toFile().mkdirs();
		TestRSEFilesystemController controller = new TestRSEFilesystemController();
		controller.initialize(server, null, createEnvironmentTempFolder1());
		controller.deleteResource(tmpFolder, new NullProgressMonitor());
		assertFalse(tmpFolder.toFile().exists());
	}	

	@Test
	public void testDeleteFolderWithContents() throws Exception {
		IPath tmpFolder = getStateLocationPath("tmpFolder");
		tmpFolder.toFile().mkdirs();
		IPath file1 = tmpFolder.append("Blah.txt");
		IOUtil.setContents(file1.toFile(), "hello");
		TestRSEFilesystemController controller = new TestRSEFilesystemController();
		controller.initialize(server, null, createEnvironmentTempFolder1());
		controller.deleteResource(tmpFolder, new NullProgressMonitor());
		assertFalse(tmpFolder.toFile().exists());
		assertFalse(file1.toFile().exists());
	}	

	
	@Test
	public void testDeleteFolderWithInnerFolderWithContents() throws Exception {
		IPath tmpFolder = getStateLocationPath("tmpFolder");
		tmpFolder.toFile().mkdirs();
		IPath innerFolder = tmpFolder.append("inner");
		innerFolder.toFile().mkdirs();
		IPath file1 = innerFolder.append("Blah.txt");
		IOUtil.setContents(file1.toFile(), "hello");
		TestRSEFilesystemController controller = new TestRSEFilesystemController();
		controller.initialize(server, null, createEnvironmentTempFolder1());
		controller.deleteResource(tmpFolder, new NullProgressMonitor());
		assertFalse(tmpFolder.toFile().exists());
		assertFalse(innerFolder.toFile().exists());
		assertFalse(file1.toFile().exists());
	}	

	
	
	@Test
	public void testIsFile() throws Exception {
		IPath tmpfile = getStateLocationPath("tmpfile3.txt");
		TestRSEFilesystemController controller = new TestRSEFilesystemController();
		controller.initialize(server, null, createEnvironmentTempFolder1());
		assertFalse(controller.isFile(tmpfile, new NullProgressMonitor()));
		IOUtil.setContents(tmpfile.toFile(), "hello");
		assertTrue(controller.isFile(tmpfile, new NullProgressMonitor()));		
	}

	@Test
	public void testMakeDirectoryIfRequired() throws Exception  {
		TestRSEFilesystemController controller = new TestRSEFilesystemController();
		controller.initialize(server, null, createEnvironmentTempFolder1());
		IPath tmpfile = getStateLocationPath("someFolder");
		assertFalse(tmpfile.toFile().exists());
		controller.makeDirectoryIfRequired(tmpfile, new NullProgressMonitor());
		assertTrue(tmpfile.toFile().exists());
	}

	@Test
	public void testMakeInnerDirectoryIfRequired() throws Exception  {
		TestRSEFilesystemController controller = new TestRSEFilesystemController();
		controller.initialize(server, null, createEnvironmentTempFolder1());
		IPath tmpFolder = getStateLocationPath("someFolder");
		IPath innerFolder = tmpFolder.append("inner");
		assertFalse(tmpFolder.toFile().exists());
		assertFalse(innerFolder.toFile().exists());
		controller.makeDirectoryIfRequired(innerFolder, new NullProgressMonitor());
		assertTrue(tmpFolder.toFile().exists());
		assertTrue(innerFolder.toFile().exists());
	}

	@Test
	public void testMakeDirectoryIfRequiredDeep() throws Exception  {
		TestRSEFilesystemController controller = new TestRSEFilesystemController();
		controller.initialize(server, null, createEnvironmentTempFolder1());
		IPath tmpfile = getStateLocationPath("someFolder/inner/three");
		assertFalse(tmpfile.toFile().exists());
		controller.makeDirectoryIfRequired(tmpfile, new NullProgressMonitor());
		assertTrue(tmpfile.toFile().exists());
	}
	
	@Test
	public void testTouchResource() throws Exception {
		IPath tmpfile = getStateLocationPath("tmpfile5.txt");
		TestRSEFilesystemController controller = new TestRSEFilesystemController();
		controller.initialize(server, null, createEnvironmentTempFolder1());
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
	
	public class TestRSEFilesystemController extends RSEFilesystemController {
	}
}
