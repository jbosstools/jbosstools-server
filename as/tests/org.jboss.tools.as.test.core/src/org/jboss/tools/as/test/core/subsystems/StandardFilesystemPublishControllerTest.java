/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.test.core.subsystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.DeploymentMarkerUtils;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.tools.as.core.server.controllable.subsystems.internal.StandardFileSystemPublishController;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.MockPublishMethodFilesystemController;
import org.jboss.tools.as.test.core.internal.utils.IOUtil;
import org.jboss.tools.as.test.core.internal.utils.MockModule;
import org.jboss.tools.as.test.core.internal.utils.MockModuleUtil;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.jboss.tools.as.test.core.parametized.server.publishing.AbstractPublishingTest;
import org.jboss.tools.as.test.core.subsystems.impl.CustomPublishController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This class tests the basic behavior of the {@link StandardFileSystemPublishController}.
 * It does not test resolution of the systems it depends on, but instead returns 
 * hard-coded standard instances of those systems. 
 *
 */
@RunWith(value = Parameterized.class)
public class StandardFilesystemPublishControllerTest extends AbstractPublishingTest {
	@Parameters(name= "{0} - {1} - {2} - {3}")
	public static Collection<Object[]> params() {
		return defaultData();
	}

	private IModule[] module;
	
	public StandardFilesystemPublishControllerTest(String serverType, String zip,
			String deployLoc, String perMod) {
		super(serverType, zip, deployLoc, perMod);
	}
	
	@Before @Override
	public void setUp() throws Exception {
		super.setUp(false);
	}

	@After @Override
	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	@Override
	protected void createProjects() throws Exception {
		module = createSimpleMockWebModule();
		addModuleToServer(module[0]);
	}

	@Test
	public void testPublish() throws Exception { 
		CustomPublishController controller = new CustomPublishController();
		controller.initialize(server, null, null);
		int result = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, module, null);
		assertEquals(result, IServer.PUBLISH_STATE_NONE);
		String s = controller.getDeployPathController().getDeployDirectory(module).toOSString();
		assertTrue(new Path(s).toFile().exists());
		assertEquals(testIsZip(), new Path(s).toFile().isFile());
		verifyListRelativePath(new Path(s), Arrays.asList(getLeafPaths()), true);
	}

	@Test
	public void testPublishModuleDNE() throws Exception {  // module does not exist, so no action should be taken
		((MockModule)module[0]).setExists(false);
		CustomPublishController controller = new CustomPublishController();
		controller.initialize(server, null, null);
		int result = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, module, null);
		assertEquals(result, IServer.PUBLISH_STATE_UNKNOWN);
		String s = controller.getDeployPathController().getDeployDirectory(module).toOSString();
		assertFalse(new Path(s).toFile().exists());
	}

	
	/*
	 * Binary modules are different from standard modules. A standard module has a name (Some.war) and 
	 * the module resources inside the module are expected to live inside that folder or zip. 
	 * So a resource "index.html" would live in "/some/base/dir/Some.war/index.html".
	 * The parent path that index.html lives in is Some.war.
	 * 
	 * Binary modules on the other hand represent a file that should be copied as-is. 
	 * If the IModule's name is "Servlets.war", and the IModule has one IModuleResource "Servlets.war", 
	 * then we should not publish it to "/some/base/dir/Servlets.war/Servlets.war".
	 * The parent path that "Servlets.war" would live in, however, is not also Servlets.war.
	 */
	@Test
	public void testBinaryModulePublish() throws Exception { 
		module = createSimpleMockBinaryWebModule();
		((MockModule)module[0]).setExists(true);
		((MockModule)module[0]).setBinary(true);
		CustomPublishController controller = new CustomPublishController();
		controller.initialize(server, null, null);
		int result = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, module, null);
		assertEquals(result, IServer.PUBLISH_STATE_NONE);
		IPath f1 = controller.getDeployPathController().getDeployDirectory(module);
		String s = f1.toOSString();
		assertTrue(new Path(s).toFile().exists());
		assertTrue(new Path(s).toFile().isDirectory()); 
		assertTrue(f1.append("some.war").toFile().exists());
		assertTrue(f1.append("some.war").toFile().isFile());
		
		IModuleResource[] resources = ModuleResourceUtil.getResources(module[0], new NullProgressMonitor());
		assertTrue(resources.length == 1);
	}

	// Force nested zips
	@Test
	public void testUtilInWebMockModule() throws Exception {  
		module = createUtilInWebMockModule();
		((MockModule)module[0]).setExists(true);
		((MockModule)module[0]).setExternal(true);
		((MockModule)module[0]).setBinary(false);
		((MockModule)module[1]).setExists(true);
		((MockModule)module[1]).setExternal(true);
		((MockModule)module[1]).setBinary(false);
		
		CustomPublishController controller = new CustomPublishController();
		controller.initialize(server, null, null);
		// Now publish module[0]  and []{module[0],module[1]} 
		int result = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, new IModule[]{module[0]}, null);
		assertEquals(result, IServer.PUBLISH_STATE_NONE);
		int resultUtil = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, module, null);
		assertEquals(resultUtil, IServer.PUBLISH_STATE_NONE);
		
		String webDepDir = controller.getDeployPathController().getDeployDirectory(new IModule[]{module[0]}).toOSString();
		assertTrue(new Path(webDepDir).toFile().exists());
		assertEquals(testIsZip(), new Path(webDepDir).toFile().isFile());
		verifyListRelativePath(new Path(webDepDir), Arrays.asList(new IPath[]{new Path("index.html")}), true);
		
		String utilDepDir = controller.getDeployPathController().getDeployDirectory(module).toOSString();
		if( !testIsZip()) {
			ServerExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
			assertNotNull(props);
			boolean allowExplodedUtil = props.allowExplodedModulesInWarLibs();

			// we're not testing in zip mode, so utilDepDir should exist 
			assertTrue(new Path(utilDepDir).toFile().exists());
			
			// and should be a forced zip file only if we're not allowing exploded utility jars
			assertEquals(!allowExplodedUtil, new Path(utilDepDir).toFile().isFile());
			verifyListRelativePath(new Path(utilDepDir), Arrays.asList(new IPath[]{new Path("Main.class")}), true);
		} else {
			// Our util is inside a zipped war. Verify that exists
			IPath utilRelToWeb = new Path(utilDepDir).removeFirstSegments(new Path(webDepDir).segmentCount());
			verifyListRelativePath(new Path(webDepDir), Arrays.asList(new IPath[]{utilRelToWeb.append("Main.class")}), true);
		}
	}
	
	
	
	@Test
	public void testJBIDE_22878UtilInWebMockModule() throws Exception {  
		module = createDifferentURIBinaryUtilInWebMockModule("simple-ejb-1.5.0.jar", "simple-ejb.jar");
		((MockModule)module[0]).setExists(true);
		((MockModule)module[0]).setExternal(true);
		((MockModule)module[0]).setBinary(false);
		((MockModule)module[1]).setExists(true);
		((MockModule)module[1]).setExternal(true);
		((MockModule)module[1]).setBinary(true);
		
		CustomPublishController controller = new CustomPublishController();
		controller.initialize(server, null, null);
		
		
		// Now publish module[0]  and []{module[0],module[1]} 
		int result = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, new IModule[]{module[0]}, null);
		assertEquals(result, IServer.PUBLISH_STATE_NONE);
		int resultUtil = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, module, null);
		assertEquals(resultUtil, IServer.PUBLISH_STATE_NONE);
		
		String webDepDir = controller.getDeployPathController().getDeployDirectory(new IModule[]{module[0]}).toOSString();
		assertTrue(new Path(webDepDir).toFile().exists());
		assertEquals(testIsZip(), new Path(webDepDir).toFile().isFile());
		verifyListRelativePath(new Path(webDepDir), Arrays.asList(new IPath[]{new Path("index.html")}), true);
		
		String utilDepDir = controller.getDeployPathController().getDeployDirectory(module).toOSString();
		String utilDepJar = controller.getDeployPathController().getDeployDirectory(module, false).toOSString();
		if( !testIsZip()) {
			ServerExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
			assertNotNull(props);

			// we're not testing in zip mode, so utilDepDir should exist 
			assertTrue(new Path(utilDepDir).toFile().exists());
			assertTrue(new Path(utilDepJar).toFile().exists());
			
			// This is a binary module being tested, so should always be a file
			assertEquals(true, new Path(utilDepJar).toFile().isFile());
		} else {
			// Our util is inside a zipped war. Verify that exists
			IPath utilRelToWeb = new Path(utilDepDir).removeFirstSegments(new Path(webDepDir).segmentCount());
			verifyListRelativePath(new Path(webDepDir), Arrays.asList(new IPath[]{utilRelToWeb}), true);
		}
	}
	

	@Test
	public void testUtilInWebInEarMockModule() throws Exception { 
		module = createUtilInWebInEarMockModule();
		((MockModule)module[0]).setExists(true);
		((MockModule)module[0]).setExternal(true);
		((MockModule)module[0]).setBinary(false);
		((MockModule)module[1]).setExists(true);
		((MockModule)module[1]).setExternal(true);
		((MockModule)module[1]).setBinary(false);
		((MockModule)module[2]).setExists(true);
		((MockModule)module[2]).setExternal(true);
		((MockModule)module[2]).setBinary(false);
		
		CustomPublishController controller = new CustomPublishController();
		controller.initialize(server, null, null);
		// Now publish module[0]  and []{module[0],module[1]} 
		int result = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, new IModule[]{module[0]}, null);
		assertEquals(result, IServer.PUBLISH_STATE_NONE);
		int resultWeb = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, new IModule[]{module[0],module[1]}, null);
		assertEquals(resultWeb, IServer.PUBLISH_STATE_NONE);
		int resultUtil = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, module, null);
		assertEquals(resultUtil, IServer.PUBLISH_STATE_NONE);
		
		String earDepDir = controller.getDeployPathController().getDeployDirectory(new IModule[]{module[0]}).toOSString();
		assertTrue(new Path(earDepDir).toFile().exists());
		assertEquals(testIsZip(), new Path(earDepDir).toFile().isFile());
		verifyListRelativePath(new Path(earDepDir), Arrays.asList(new IPath[]{new Path("META-INF/application.xml")}), true);

		
		String webDepDir = controller.getDeployPathController().getDeployDirectory(new IModule[]{module[0],module[1]}).toOSString();
		utilInWebInEarRemovals_verifyWeb(webDepDir, earDepDir, true);
		
		String utilDepDir = controller.getDeployPathController().getDeployDirectory(module).toOSString();
		utilInWebInEarRemovals_verifyUtil(utilDepDir, earDepDir, true);
	}
	
	@Test
	public void testUtilInWebInEarRestartModule() throws Exception { 
		module = createUtilInWebInEarMockModule();
		((MockModule)module[0]).setExists(true);
		((MockModule)module[0]).setBinary(false);
		((MockModule)module[1]).setExists(true);
		((MockModule)module[1]).setBinary(false);
		((MockModule)module[2]).setExists(true);
		((MockModule)module[2]).setBinary(false);
		
		CustomPublishController controller = new CustomPublishController();
		controller.initialize(server, null, null);
		// Now publish module[0]  and []{module[0],module[1]} 
		int result = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, new IModule[]{module[0]}, null);
		assertEquals(result, IServer.PUBLISH_STATE_NONE);
		int resultWeb = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, new IModule[]{module[0],module[1]}, null);
		assertEquals(resultWeb, IServer.PUBLISH_STATE_NONE);
		int resultUtil = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, module, null);
		assertEquals(resultUtil, IServer.PUBLISH_STATE_NONE);
		
		String earDepDir = controller.getDeployPathController().getDeployDirectory(new IModule[]{module[0]}).toOSString();
		assertTrue(new Path(earDepDir).toFile().exists());
		assertEquals(testIsZip(), new Path(earDepDir).toFile().isFile());
		verifyListRelativePath(new Path(earDepDir), Arrays.asList(new IPath[]{new Path("META-INF/application.xml")}), true);
		
		IPath applicationXml = new Path(earDepDir).append("META-INF/application.xml");
		IPath earDep = new Path(earDepDir);
		IPath deployed = earDep.removeLastSegments(1).append(earDep.lastSegment() + DeploymentMarkerUtils.DEPLOYED);
		IPath deployFailed = earDep.removeLastSegments(1).append(earDep.lastSegment() + DeploymentMarkerUtils.FAILED_DEPLOY);
		IPath doDeploy = earDep.removeLastSegments(1).append(earDep.lastSegment() + DeploymentMarkerUtils.DO_DEPLOY);
		if( !DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server)) {
			if( isZipped()) // zipped deployments do not have their stuff touched
				return;
			
			// lets set the last modified to 5 on application.xml, so we can later verify it is updated
			assertNotSame(1000, applicationXml.toFile().lastModified());
			applicationXml.toFile().setLastModified(1000);
			assertEquals(1000, applicationXml.toFile().lastModified());
		} else {
			// Create the .deployed and .deployFailed markers to ensure that a publish Finish actually removes them
			assertFalse(deployed.toFile().exists());
			assertFalse(deployFailed.toFile().exists());
			deployed.toFile().createNewFile();
			deployFailed.toFile().createNewFile();
			assertTrue(deployed.toFile().exists());
			assertTrue(deployFailed.toFile().exists());
		}
		
		// Finish the publish, verify it updates the markers
		controller.publishFinish(new NullProgressMonitor());
		
		if( !DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server)) {
			// lets set the last modified to 5 on application.xml, so we can later verify it is updated
			assertNotSame(1000, applicationXml.toFile().lastModified());
		} else {
			// Create the .deployed and .deployFailed markers to ensure that a publish Finish actually removes them
			assertFalse(deployed.toFile().exists());
			assertFalse(deployFailed.toFile().exists());
			assertTrue(doDeploy.toFile().exists());
		}
	}
	

	private void utilInWebInEarRemovals_verifyWeb(String webDepDir, String earDepDir, boolean shouldExist) {
		if( !testIsZip()) {
			assertTrue(new Path(webDepDir).toFile().exists());
			assertEquals(false, new Path(webDepDir).toFile().isFile());
			verifyListRelativePath(new Path(webDepDir), Arrays.asList(new IPath[]{new Path("index.html")}), true);
		} else {
			// Our war is inside a zipped ear. Verify that exists
			IPath webRelToEar = new Path(webDepDir).removeFirstSegments(new Path(earDepDir).segmentCount());
			verifyListRelativePath(new Path(earDepDir), Arrays.asList(new IPath[]{webRelToEar.append("index.html")}), true);
		}
	}
	
	private void utilInWebInEarRemovals_verifyUtil(String utilDepDir, String earDepDir, boolean shouldExist) {
		ServerExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		assertNotNull(props);
		boolean allowExplodedUtil = props.allowExplodedModulesInWarLibs();
		if( !testIsZip()) {
			// we're not testing in zip mode, so utilDepDir should exist 
			assertEquals(shouldExist, new Path(utilDepDir).toFile().exists());
			// and should be a forced zip file only if we don't allow exploded util jars
			boolean shouldBeFile = !allowExplodedUtil && shouldExist;
			assertEquals(shouldBeFile, new Path(utilDepDir).toFile().isFile());
			verifyListRelativePath(new Path(utilDepDir), Arrays.asList(new IPath[]{new Path("Main.class")}), shouldExist);
		} else {
			// Our util is inside a zipped war inside a zipped ear. Verify that exists
			IPath utilRelToEar = new Path(utilDepDir).removeFirstSegments(new Path(earDepDir).segmentCount());
			verifyListRelativePath(new Path(earDepDir), Arrays.asList(new IPath[]{utilRelToEar.append("Main.class")}), shouldExist);
		}
	}
	
	@Test
	public void testUtilInWebInEarRemovals() throws Exception {  // module does not exist, so no action should be taken
		testUtilInWebInEarMockModule();
		CustomPublishController controller = new CustomPublishController();
		controller.initialize(server, null, null);
		
		// Remove the util and publish again
		((MockModule)module[1]).clearChildren();
		int result = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.CHANGED, new IModule[]{module[0]}, null);
		assertEquals(result, IServer.PUBLISH_STATE_NONE);
		int resultWeb = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.CHANGED, new IModule[]{module[0],module[1]}, null);
		assertEquals(resultWeb, IServer.PUBLISH_STATE_NONE);
		int resultUtil = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.REMOVED, module, null);
		assertEquals(resultUtil, IServer.PUBLISH_STATE_NONE);
		
		// Verify ear still published
		String earDepDir = controller.getDeployPathController().getDeployDirectory(new IModule[]{module[0]}).toOSString();
		assertTrue(new Path(earDepDir).toFile().exists());
		assertEquals(testIsZip(), new Path(earDepDir).toFile().isFile());
		verifyListRelativePath(new Path(earDepDir), Arrays.asList(new IPath[]{new Path("META-INF/application.xml")}), true);

		// verify web still published
		String webDepDir = controller.getDeployPathController().getDeployDirectory(new IModule[]{module[0],module[1]}).toOSString();
		utilInWebInEarRemovals_verifyWeb(webDepDir, earDepDir, true);
		
		// Verify util deleted
		String utilDepDir = controller.getDeployPathController().getDeployDirectory(module).toOSString();
		utilInWebInEarRemovals_verifyUtil(utilDepDir, earDepDir, false);
		
		// Now remove hte web
		((MockModule)module[0]).clearChildren();
		result = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.CHANGED, new IModule[]{module[0]}, null);
		assertEquals(result, IServer.PUBLISH_STATE_NONE);
		resultWeb = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.REMOVED, new IModule[]{module[0],module[1]}, null);
		assertEquals(resultWeb, IServer.PUBLISH_STATE_NONE);

		// Verify ear still published
		assertTrue(new Path(earDepDir).toFile().exists());
		assertEquals(testIsZip(), new Path(earDepDir).toFile().isFile());
		verifyListRelativePath(new Path(earDepDir), Arrays.asList(new IPath[]{new Path("META-INF/application.xml")}), true);

		// verify web NOT still published
		utilInWebInEarRemovals_verifyWeb(webDepDir, earDepDir, false);
	}
	

	private boolean testIsZip() {
		return (param_zip.equals(ServerParameterUtils.ZIPPED));
	}
	
	/*
	 * Utility methods for this test class are below
	 */
	
	


	private IModuleResourceDelta[] createDelta(IModule m) throws CoreException {
		ArrayList<IModuleResource> l = new ArrayList<IModuleResource>();
		IModuleResource[] all = MockModuleUtil.getAllResources(getResources(m));
		l.addAll(Arrays.asList(all));
		return MockModuleUtil.createMockResourceDeltas(l);
	}
	
	private IModuleResource[] getResources(IModule m) throws CoreException {
		ModuleDelegate md = (ModuleDelegate)m.loadAdapter(ModuleDelegate.class, null);
		return md.members();
	}

	private IPath setUnderlyingVersion(int v) throws Exception {
		IPath underlying = ASMatrixTests.getDefault().getStateLocation().append("underlying.txt");
		underlying.toFile().getParentFile().mkdirs();
		IOUtil.setContents(underlying.toFile(), "version" + v);
		return underlying;
	}
	
	private IPath[] getLeafPaths() {
		IPath[] leafs = new IPath[] {
				new Path("w"),
				new Path("x"),
				new Path("y"),
				new Path("z"),
				new Path("a/a1"),
				new Path("a/a2"),
				new Path("a/q1"),
				new Path("a/q2"),
				new Path("b/b1"),
				new Path("b/b2"),
				new Path("b/b3"),
				new Path("b/b4"),
				new Path("c/y1"),
				new Path("c/y2.png"),
				new Path("c/y3.jpg"),
				new Path("c/y4.pdf"),
				new Path("d/F/f1.jar"),
				new Path("d/F/f2.txt"),
				new Path("d/F/f3.txt"),
				new Path("d/F/f4.txt")
		};
		return leafs;
	}
	
	private MockModule[] createSimpleMockWebModule() throws Exception {
		IPath underlying = setUnderlyingVersion(1);
		// Create a custom mock project structure
		MockModule m = MockModuleUtil.createMockWebModule();
		IPath[] leafs = getLeafPaths();
		IModuleResource[] all = MockModuleUtil.createMockResources(leafs, new IPath[0], underlying.toFile());
		m.setMembers(all);
		m.setExists(true);
		return new MockModule[]{m};
	}
	
	private MockModule[] createSimpleMockBinaryWebModule() throws Exception {
		IPath underlying = setUnderlyingVersion(1);
		// Create a custom mock project structure
		MockModule m = MockModuleUtil.createMockWebModule();
		IPath[] leafs = new IPath[]{new Path("some.war")};
		IModuleResource[] all = MockModuleUtil.createMockResources(leafs, new IPath[0], underlying.toFile());
		m.setMembers(all);
		m.setExists(true);
		return new MockModule[]{m};
	}

	private MockModule[] createUtilInWebMockModule() throws Exception {
		IPath underlying = setUnderlyingVersion(1);
		MockModule web = MockModuleUtil.createMockWebModule();
		MockModule util = MockModuleUtil.createMockUtilModule();
		
		String utilWithSuffix = util.getName() + ".jar";
		String warWithSuffix = web.getName() + ".war";
		String uri = "nested/inside/" + utilWithSuffix;
		
		web.addChildModule(util, uri);
		MockModule[] utilInWeb = new MockModule[]{web, util};
		
		// Create a custom mock project structure
		IPath[] webLeafs = new IPath[]{new Path("index.html")};
		IPath[] utilLeafs = new IPath[]{new Path("Main.class")};
		
		IModuleResource[] webResources = MockModuleUtil.createMockResources(webLeafs, new IPath[0], underlying.toFile());
		IModuleResource[] utilResources = MockModuleUtil.createMockResources(utilLeafs, new IPath[0], underlying.toFile());
		
		web.setMembers(webResources);
		util.setMembers(utilResources);
		return utilInWeb;
	}
	
	
	private MockModule[] createDifferentURIBinaryUtilInWebMockModule(
			String utilFileName, String utilNestedName) throws Exception {
		IPath underlying = setUnderlyingVersion(1);
		MockModule web = MockModuleUtil.createMockWebModule();
		MockModule util = MockModuleUtil.createMockUtilModule();
		util.setBinary(true);
		
		String utilWithSuffix = utilNestedName;
		String warWithSuffix = web.getName() + ".war";
		String uri = "nested/inside/" + utilWithSuffix;
		
		web.addChildModule(util, uri);
		MockModule[] utilInWeb = new MockModule[]{web, util};
		
		// Create a custom mock project structure
		IPath[] webLeafs = new IPath[]{new Path("index.html")};
		IPath[] utilLeafs = new IPath[]{new Path(utilFileName)};
		
		IModuleResource[] webResources = MockModuleUtil.createMockResources(webLeafs, new IPath[0], underlying.toFile());
		IModuleResource[] utilResources = MockModuleUtil.createMockResources(utilLeafs, new IPath[0], underlying.toFile());
		
		web.setMembers(webResources);
		util.setMembers(utilResources);
		return utilInWeb;
	}
	

	
	private MockModule[] createUtilInWebInEarMockModule() throws Exception {
		IPath underlying = setUnderlyingVersion(1);
		MockModule ear = MockModuleUtil.createMockEarModule();
		MockModule web = MockModuleUtil.createMockWebModule();
		MockModule util = MockModuleUtil.createMockUtilModule();
		
		String utilWithSuffix = util.getName() + ".jar";
		String warWithSuffix = web.getName() + ".war";
		String earWithSuffix = ear.getName() + ".ear";
		String utilUri = "nested/inside/" + utilWithSuffix;
		
		web.addChildModule(util, utilUri);
		ear.addChildModule(web, warWithSuffix);
		MockModule[] utilInWebInEar = new MockModule[]{ear, web, util};

		// Create a custom mock project structure
		IPath[] earLeafs = new IPath[]{new Path("META-INF/application.xml")};
		IPath[] webLeafs = new IPath[]{new Path("index.html")};
		IPath[] utilLeafs = new IPath[]{new Path("Main.class")};
		
		IModuleResource[] earResources = MockModuleUtil.createMockResources(earLeafs, new IPath[0], underlying.toFile());
		IModuleResource[] webResources = MockModuleUtil.createMockResources(webLeafs, new IPath[0], underlying.toFile());
		IModuleResource[] utilResources = MockModuleUtil.createMockResources(utilLeafs, new IPath[0], underlying.toFile());
		
		ear.setMembers(earResources);
		web.setMembers(webResources);
		util.setMembers(utilResources);
		return utilInWebInEar;
	}

	protected void verifyListRelativePath(IPath root, List<IPath> list, boolean exists) {
		ArrayList<IPath> list2 = new ArrayList<IPath>();
		for(Iterator<IPath> i = list.iterator(); i.hasNext(); ) {
			list2.add(root.append(i.next()));
		}
		super.verifyList(root, list2, exists);
	}
	
}