/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.test.core.parametized.server.publishing;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.j2ee.application.internal.operations.AddReferenceToEnterpriseApplicationDataModelProvider;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualReference;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ComponentReferenceUtils;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.internal.utils.wtp.OperationTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeploymentAssemblyFilesetReferenceTest extends TestCase  {

	private static String PROJECT_PREFIX = "q40Ear";
	private static int PROJECT_ID = 1;

	private String MY_PROJECT_NAME;
	
	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
	}
	
	@Before
	public void setUp() throws Exception  {
		PROJECT_ID++;
		MY_PROJECT_NAME = PROJECT_PREFIX + PROJECT_ID;
		
		IDataModel dm = CreateProjectOperationsUtility.getEARDataModel(MY_PROJECT_NAME, "ourContent", 
				null, null, JavaEEFacetConstants.EAR_5, false);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourceUtils.findProject(MY_PROJECT_NAME);
		if( p == null || !p.exists())
			fail();
		
		p.getFolder("folder1").create(true, true, new NullProgressMonitor());
		p.getFolder("folder2").create(true, true, new NullProgressMonitor());
		p.getFolder("folder3").create(true, true, new NullProgressMonitor());
		
		createFile(p, "folder1", "a", "contents1" );
		createFile(p, "folder1", "b", "contents1" );
		createFile(p, "folder1", "z", "contents1" );

		createFile(p, "folder2", "c", "contents1" );
		createFile(p, "folder2", "d", "contents1" );
		createFile(p, "folder2", "z", "contents1" );

		createFile(p, "folder3", "d", "contents1" );
		createFile(p, "folder3", "e", "contents1" );
		createFile(p, "folder3", "z", "contents1" );
	}
	
	private void createFile(IProject project, String folder, String filename, String contents) throws CoreException {
		IFolder f = project.getFolder(folder);
		IFile f2 = f.getFile(filename);
		ByteArrayInputStream is = new ByteArrayInputStream(contents.getBytes());
		f2.create(is, true, new NullProgressMonitor());
	}

	@Test
	public void testRootFolder() throws Exception {
		runTest(MY_PROJECT_NAME, "**", "", "/", 12);
	}

	@Test
	public void testRootFolderExcludeZ() throws Exception {
		runTest(MY_PROJECT_NAME, "**", "**/z*", "/", 9);
	}

	@Test
	public void testFolderOneOnly() throws Exception {
		runTest(MY_PROJECT_NAME, "folder1/**", "", "/", 3);
	}

	@Test
	public void testFolderTwoOnly() throws Exception {
		runTest(MY_PROJECT_NAME, "folder2/**", "", "/", 3);
	}

	@Test
	public void testFolderThreeOnly() throws Exception {
		runTest(MY_PROJECT_NAME, "folder3/**", "", "/", 3);
	}

	@Test
	public void testZsOnly() throws Exception {
		runTest(MY_PROJECT_NAME, "**/z", "", "/", 3);
	}

	private void runTest(String rootFolder, String inc, String exc, String runtimePath, int expectedCount) throws CoreException {
		IProject p = ResourceUtils.findProject(MY_PROJECT_NAME);
		System.out.println(p.getLocation());
		IVirtualComponent root = ComponentCore.createComponent(p);
		VirtualReference ref = ComponentReferenceUtils.createFilesetComponentReference(root, 
				rootFolder, inc, exc, runtimePath);
		ComponentReferenceUtils.addReferenceToComponent(root, ref, new AddReferenceToEnterpriseApplicationDataModelProvider());
		IModule module = ServerUtil.getModule(p);
		IModuleFile[] allFiles = ResourceUtils.findAllIModuleFiles(module);
		assertEquals(allFiles.length, expectedCount);
	}
	
}
