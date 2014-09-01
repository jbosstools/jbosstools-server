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

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.core.server.controllable.subsystems.internal.StandardModuleRestartBehaviorController;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.MockModule;
import org.jboss.tools.as.test.core.internal.utils.MockModuleUtil;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the restart pattern for modules to determine 
 * when a module needs to be restarted
 */
public class ModuleRestartBehaviorControllerTest extends TestCase {
	public ModuleRestartBehaviorControllerTest() {
	}
	private IServer server;
	@Before
	public void setUp() {
		server = ServerCreationTestUtils.createMockServerWithRuntime(IJBossToolingConstants.DEPLOY_ONLY_SERVER, getClass().getName() + IJBossToolingConstants.DEPLOY_ONLY_SERVER);
	}

	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
	}
	
	@Test
	public void testNoPattern() throws CoreException {
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), null);
		StandardModuleRestartBehaviorController controller = new StandardModuleRestartBehaviorController();
		controller.initialize(server, null, null);
		IModule testModule = createTestMockModule();
		ModuleDelegate md = (ModuleDelegate)testModule.loadAdapter(ModuleDelegate.class, null);
		boolean ret = controller.moduleRequiresRestart(null, md.members());
		assertTrue(ret);
	}

	@Test
	public void testTxtPattern() throws CoreException {
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), null);
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_USE_DEFAULT_RESTART_PATTERN,  Boolean.FALSE.toString());
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, "\\.txt$");
		server = wc.save(true, new NullProgressMonitor());
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), "\\.txt$");		
		StandardModuleRestartBehaviorController controller = new StandardModuleRestartBehaviorController();
		controller.initialize(server, null, null);
		IModule testModule = createTestMockModule();
		ModuleDelegate md = (ModuleDelegate)testModule.loadAdapter(ModuleDelegate.class, null);
		boolean ret = controller.moduleRequiresRestart(null, md.members());
		assertTrue(ret);
	}

	
	@Test
	public void testMissingExtPattern() throws CoreException {
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), null);
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_USE_DEFAULT_RESTART_PATTERN,  Boolean.FALSE.toString());
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, 
				"\\.svg$");
		server = wc.save(true, new NullProgressMonitor());
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), 
				"\\.svg$");
		StandardModuleRestartBehaviorController controller = new StandardModuleRestartBehaviorController();
		controller.initialize(server, null, null);
		IModule testModule = createTestMockModule();
		ModuleDelegate md = (ModuleDelegate)testModule.loadAdapter(ModuleDelegate.class, null);
		boolean ret = controller.moduleRequiresRestart(null, md.members());
		assertFalse(ret);
	}

	
	@Test
	public void testNoPatternDelta() throws CoreException {
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), null);
		StandardModuleRestartBehaviorController controller = new StandardModuleRestartBehaviorController();
		controller.initialize(server, null, null);
		IModule testModule = createTestMockModule();
		boolean ret = controller.moduleRequiresRestart(null, createDelta(testModule));
		assertTrue(ret);
	}

	@Test
	public void testTxtPatternDelta() throws CoreException {
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), null);
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_USE_DEFAULT_RESTART_PATTERN,  Boolean.FALSE.toString());
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, 
				"\\.txt$");
		server = wc.save(true, new NullProgressMonitor());
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), 
				"\\.txt$");
		StandardModuleRestartBehaviorController controller = new StandardModuleRestartBehaviorController();
		controller.initialize(server, null, null);
		IModule testModule = createTestMockModule();
		boolean ret = controller.moduleRequiresRestart(null, createDelta(testModule));
		assertTrue(ret);
	}
	
	@Test
	public void testUseDefaultButTxtPatternDelta() throws CoreException {
		// The txt pattern should match, but, we're setting it to use default, 
		// so the txt pattern should be ignored and only .jar should be used
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), null);
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_USE_DEFAULT_RESTART_PATTERN,  Boolean.TRUE.toString());
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN,  "\\.txt$");
		server = wc.save(true, new NullProgressMonitor());
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), "\\.txt$");
		StandardModuleRestartBehaviorController controller = new StandardModuleRestartBehaviorController();
		controller.initialize(server, null, null);
		IModule testModule = createTestMockModuleNoJar();
		boolean ret = controller.moduleRequiresRestart(null, createDelta(testModule));
		assertFalse(ret);
	}

	
	@Test
	public void testMissingExtPatternDelta() throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), null);
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_USE_DEFAULT_RESTART_PATTERN,  Boolean.FALSE.toString());
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, 
				"\\.svg$");
		server = wc.save(true, new NullProgressMonitor());
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), 
				"\\.svg$");
		StandardModuleRestartBehaviorController controller = new StandardModuleRestartBehaviorController();
		controller.initialize(server, null, null);
		IModule testModule = createTestMockModule();
		boolean ret = controller.moduleRequiresRestart(null, createDelta(testModule));
		assertFalse(ret);
	}
	
	

	@Test
	public void testDeepTxtPatternDelta() throws CoreException {
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), null);
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_USE_DEFAULT_RESTART_PATTERN,  Boolean.FALSE.toString());
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, 
				"F.*txt$");
		server = wc.save(true, new NullProgressMonitor());
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), 
				"F.*txt$");
		StandardModuleRestartBehaviorController controller = new StandardModuleRestartBehaviorController();
		controller.initialize(server, null, null);
		IModule testModule = createTestMockModule();
		boolean ret = controller.moduleRequiresRestart(null, createDelta(testModule));
		assertTrue(ret);
	}
	
	@Test
	public void testDeepTxtPatternDeltaNotFound() throws CoreException {
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), null);
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_USE_DEFAULT_RESTART_PATTERN,  Boolean.FALSE.toString());
		wc.setAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, 
				"b.*txt$");
		server = wc.save(true, new NullProgressMonitor());
		assertEquals(server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, (String)null), 
				"b.*txt$");
		StandardModuleRestartBehaviorController controller = new StandardModuleRestartBehaviorController();
		controller.initialize(server, null, null);
		IModule testModule = createTestMockModule();
		boolean ret = controller.moduleRequiresRestart(null, createDelta(testModule));
		assertFalse(ret);
	}

	

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

	private IModule createTestMockModule() {
		// Create a custom mock project structure
		MockModule m = MockModuleUtil.createMockWebModule();
		IPath[] leafs = getLeafs(true);
		IModuleResource[] all = MockModuleUtil.createMockResources(leafs, new IPath[0]);
		m.setMembers(all);
		return m;
	}

	private IModule createTestMockModuleNoJar() {
		// Create a custom mock project structure
		MockModule m = MockModuleUtil.createMockWebModule();
		IPath[] leafs = getLeafs(false);
		IModuleResource[] all = MockModuleUtil.createMockResources(leafs, new IPath[0]);
		m.setMembers(all);
		return m;
	}
	
	private IPath[] getLeafs(boolean includeJar) {
		ArrayList<IPath> leafs = new ArrayList<IPath>();
		leafs.add(new Path("w"));
		leafs.add(new Path("x"));
		leafs.add(new Path("y"));
		leafs.add(new Path("z"));
		leafs.add(new Path("a/a1"));
		leafs.add(new Path("a/a2"));
		leafs.add(new Path("a/q1"));
		leafs.add(new Path("a/q2"));
		leafs.add(new Path("b/b1"));
		leafs.add(new Path("b/b2"));
		leafs.add(new Path("b/b3"));
		leafs.add(new Path("b/b4"));
		leafs.add(new Path("c/y1"));
		leafs.add(new Path("c/y2.png"));
		leafs.add(new Path("c/y3.jpg"));
		leafs.add(new Path("c/y4.pdf"));
		if( includeJar ) 
			leafs.add(new Path("d/F/f1.jar"));
		leafs.add(new Path("d/F/f2.txt"));
		leafs.add(new Path("d/F/f3.txt"));
		leafs.add(new Path("d/F/f4.txt"));
		return (IPath[]) leafs.toArray(new IPath[leafs.size()]);
	}
	
}