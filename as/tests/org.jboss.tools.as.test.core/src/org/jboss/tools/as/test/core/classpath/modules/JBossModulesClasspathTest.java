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
package org.jboss.tools.as.test.core.classpath.modules;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.validation.ValidationFramework;
import org.jboss.ide.eclipse.as.classpath.core.runtime.CustomRuntimeClasspathModel;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.internal.ProjectRuntimeClasspathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.LayeredProductPathProvider;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.internal.utils.wtp.OperationTestCase;
import org.jboss.tools.test.util.JobUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class JBossModulesClasspathTest  extends TestCase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		ValidationFramework.getDefault().suspendAllValidation(true);
	}
	
	@AfterClass
	public static void tearDownClass() {
		ValidationFramework.getDefault().suspendAllValidation(false);
	}
	@After
	public void tearDown() throws Exception {
		ASMatrixTests.getDefault().cleanup();
	} 
	
	/**
	 * First test to make sure our testing structure is doing the right thing, making a mocked patch installation, etc
	 */
	public void testBasicModuleSlot() throws Exception {
		// create a server + runtime + project
		IServer s2 = MockJBossModulesUtil.createMockServerWithRuntime(IJBossToolingConstants.SERVER_EAP_61, "TestOne");
		IRuntime rt = s2.getRuntime();
		// Clear the list of modules to add 
		CustomRuntimeClasspathModel.getInstance().savePathProviders(rt.getRuntimeType(), new IRuntimePathProvider[]{});
		
		// Create project
		IDataModel dm = CreateProjectOperationsUtility.getWebDataModel("WebProject1", 
				null, null, null, null, JavaEEFacetConstants.WEB_24, false);
		IProject p = createSingleProject(dm, "WebProject1");
		
		// Ensure no results
		ProjectRuntimeClasspathProvider provider = new ProjectRuntimeClasspathProvider();
		IClasspathEntry[] entries = provider.resolveClasspathContainer(p, rt);
		if( entries.length != 0 ) {
			System.out.println("Debugging failing test:  ");
			System.out.println("content kind: " + entries[0].getContentKind());
			System.out.println("entry kind: " + entries[0].getEntryKind());
			System.out.println("path: " + entries[0].getPath());
		}
		assertEquals(entries.length,0);
		
		// add a path provider, verify 1 result
		LayeredProductPathProvider pathPro = new LayeredProductPathProvider("org.jboss.as.server", null);
		CustomRuntimeClasspathModel.getInstance().savePathProviders(rt.getRuntimeType(), new IRuntimePathProvider[]{pathPro});
		entries = provider.resolveClasspathContainer(p, rt);
		assertEquals(entries.length,1);
		
		
		// Let's add a new module 
		IPath modules = rt.getLocation().append("modules");
		IPath base = modules.append("system").append("layers").append("base");
		MockJBossModulesUtil.cloneModule(base, "org.jboss.as.server", base, "org.max.wonka");
		
		// Make a global moduleslot path provider for this rt-type
		LayeredProductPathProvider wonkaProvider = new LayeredProductPathProvider("org.max.wonka", null);
		CustomRuntimeClasspathModel.getInstance().savePathProviders(rt.getRuntimeType(), new IRuntimePathProvider[]{pathPro, wonkaProvider});
		entries = provider.resolveClasspathContainer(p, rt);
		assertEquals(entries.length,2);
		
	}
	
	
	/**
	 * Test additions via manifest.mf
	 */
	public void testManifestAdditions() throws Exception {
		// create a server + runtime + project
		IServer s2 = MockJBossModulesUtil.createMockServerWithRuntime(IJBossToolingConstants.SERVER_EAP_61, "TestOne");
		IRuntime rt = s2.getRuntime();
		// Clear the list of modules to add 
		CustomRuntimeClasspathModel.getInstance().savePathProviders(rt.getRuntimeType(), new IRuntimePathProvider[]{});
		
		// Create project
		IDataModel dm = CreateProjectOperationsUtility.getWebDataModel("WebProject1", 
				null, null, null, null, JavaEEFacetConstants.WEB_24, false);
		IProject p = createSingleProject(dm, "WebProject1");
		
		// Make sure the project is targeted to a runtime
		IFacetedProject facetedProject = ProjectFacetsManager.create(p);
		IFacetedProjectWorkingCopy workingCopy = facetedProject.createWorkingCopy();
		workingCopy.addTargetedRuntime(RuntimeManager.getRuntime(rt.getName()));
		workingCopy.commitChanges(null);
		
		IFile manifest = p.getFile("MANIFEST.MF");
		String contents = "Dependencies: org.jboss.as.server\n";
		setContentsAndWaitForPropagation(manifest, contents);
		
		// Ensure 1 result
		ProjectRuntimeClasspathProvider provider = new ProjectRuntimeClasspathProvider();
		IClasspathEntry[] entries = provider.resolveClasspathContainer(p, rt);
		assertEquals(entries.length,1);
		
		
		// Let's add a new module 
		IPath modules = rt.getLocation().append("modules");
		IPath base = modules.append("system").append("layers").append("base");
		MockJBossModulesUtil.cloneModule(base, "org.jboss.as.server", base, "org.max.wonka");

		contents = "Dependencies: org.jboss.as.server, org.max.wonka\n";
		setContentsAndWaitForPropagation(manifest, contents);
		JobUtils.waitForIdle();
		System.out.println("Idle over");
		provider = new ProjectRuntimeClasspathProvider();
		entries = provider.resolveClasspathContainer(p, rt);
		System.out.println("Asserting");
		assertEquals(entries.length,2);

	
		// Remove it
		contents = "Dependencies: org.jboss.as.server\n";
		setContentsAndWaitForPropagation(manifest, contents);

		provider = new ProjectRuntimeClasspathProvider();
		entries = provider.resolveClasspathContainer(p, rt);
		assertEquals(entries.length,1);
	}
	
	private void setContentsAndWaitForPropagation(IFile file, String contents) throws Exception {
		if( file.exists()) {
			file.setContents(new ByteArrayInputStream(contents.getBytes()), IResource.FORCE, new NullProgressMonitor());
		} else {
			file.create(new ByteArrayInputStream(contents.getBytes()), true, new NullProgressMonitor());
		}
		JobUtils.waitForIdle(1500);
	}
	
	/**
	 * Test additions via manifest.mf
	 */
	public void testManifestVersionAdditions() throws Exception {
		// create a server + runtime + project
		IServer s2 = MockJBossModulesUtil.createMockServerWithRuntime(IJBossToolingConstants.SERVER_EAP_61, "TestOne");
		IRuntime rt = s2.getRuntime();
		// Clear the list of modules to add 
		CustomRuntimeClasspathModel.getInstance().savePathProviders(rt.getRuntimeType(), new IRuntimePathProvider[]{});
		
		// Create project
		IDataModel dm = CreateProjectOperationsUtility.getWebDataModel("WebProject1", 
				null, null, null, null, JavaEEFacetConstants.WEB_24, false);
		IProject p = createSingleProject(dm, "WebProject1");
		
		// Make sure the project is targeted to a runtime
		IFacetedProject facetedProject = ProjectFacetsManager.create(p);
		IFacetedProjectWorkingCopy workingCopy = facetedProject.createWorkingCopy();
		workingCopy.addTargetedRuntime(RuntimeManager.getRuntime(rt.getName()));
		workingCopy.commitChanges(null);
		
		IFile manifest = p.getFile("MANIFEST.MF");
		String contents = "Dependencies: org.jboss.as.server\n";
		setContentsAndWaitForPropagation(manifest, contents);
		
		
		// Ensure 1 result
		ProjectRuntimeClasspathProvider provider = new ProjectRuntimeClasspathProvider();
		IClasspathEntry[] entries = provider.resolveClasspathContainer(p, rt);
		assertEquals(entries.length,1);
		
		// add main slot
		contents = "Dependencies: org.jboss.as.server:main\n";
		setContentsAndWaitForPropagation(manifest, contents);

		provider = new ProjectRuntimeClasspathProvider();
		entries = provider.resolveClasspathContainer(p, rt);
		assertEquals(entries.length,1);

	
		// Test on a slot that doesn't exist
		contents = "Dependencies: org.jboss.as.server:1.0\n";
		setContentsAndWaitForPropagation(manifest, contents);

		provider = new ProjectRuntimeClasspathProvider();
		entries = provider.resolveClasspathContainer(p, rt);
		if( entries.length != 0 ) {
			System.out.println("Debugging failing test:  ");
			System.out.println("content kind: " + entries[0].getContentKind());
			System.out.println("entry kind: " + entries[0].getEntryKind());
			System.out.println("path: " + entries[0].getPath());
		}
		assertEquals(entries.length,0);

		
		// Lets add a new slot for our module and repeat it
		IPath modules = rt.getLocation().append("modules");
		IPath base = modules.append("system").append("layers").append("base");
		MockJBossModulesUtil.duplicateToSlot(base, "org.jboss.as.server", "1.0");
		// We have to re-set the contents or the cache won't know to update
		contents = "Dependencies: org.jboss.as.server:1.0\n";
		setContentsAndWaitForPropagation(manifest, contents);

		provider = new ProjectRuntimeClasspathProvider();
		entries = provider.resolveClasspathContainer(p, rt);
		assertEquals(entries.length,1);
		IPath path = entries[0].getPath();
		assertTrue(path.toOSString().contains("1.0"));
	}
	

	protected IProject createSingleProject(IDataModel dm, String name) throws Exception {
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourceUtils.findProject(name);
		if(!p.exists())
			fail();
		return p;
	}
}
