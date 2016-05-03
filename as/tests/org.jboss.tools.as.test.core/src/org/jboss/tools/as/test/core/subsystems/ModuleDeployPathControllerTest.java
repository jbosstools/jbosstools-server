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

import java.io.File;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.MockModule;
import org.jboss.tools.as.test.core.internal.utils.MockModuleUtil;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.jboss.tools.as.test.core.subsystems.ServerSubsystemTest1.ModelSubclass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class ModuleDeployPathControllerTest extends TestCase {
	private static String SYSTEM = IModuleDeployPathController.SYSTEM_ID;
	private static String SUBSYSTEM = "moduleDeployPath.xmlprefs";

	
	private static String TMP = (Platform.getOS().equals(Platform.OS_WIN32) ? new Path("C:\\home\\user") : new Path("/home/user")).append("tmp").toOSString();
	private static String TMP2 = (Platform.getOS().equals(Platform.OS_WIN32) ? new Path("C:\\home\\user") : new Path("/home/user")).append("tmp2").toOSString();
	private static char WIN_SEP = '\\';
	private static char LIN_SEP = '/';
	private static char LOCAL_SEP = Platform.getOS().equals(Platform.OS_WIN32) ? WIN_SEP : LIN_SEP;
	
	
	public ModuleDeployPathControllerTest() {
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
	public void testResolution() {
		ModelSubclass c = new ModelSubclass();
		String serverType = IJBossToolingConstants.DEPLOY_ONLY_SERVER;
		String system = SYSTEM;
		String subsystem = SUBSYSTEM;
		Object[] types = c.getSubsystemMappings(serverType, system);
		assertTrue(types != null);
		assertTrue(types.length == 1);
		try {
			ISubsystemController controller = c.createSubsystemController(server,system, null, null, null);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals(subsystem));
			assertTrue(controller instanceof IModuleDeployPathController);
			// with no env, the controller is missing critical data
			assertFalse(controller.validate().isOK());
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}
	
	// Now to test the actual system
	@Test
	public void testSimpleEnv() {
		IModuleDeployPathController contr = buildEnv(TMP, TMP2, null);
		assertTrue(contr.validate().isOK());

		contr = buildEnv(TMP, null, null);
		assertFalse(contr.validate().isOK());

		contr = buildEnv(null, TMP, null);
		assertFalse(contr.validate().isOK());
	}
	

	@Test
	public void testGettersNoPrefs() {
		String initialDeployDir = TMP;
		String initialTmpDeployDir = TMP2;
		
		IModuleDeployPathController contr = buildEnv(initialDeployDir, initialTmpDeployDir, null);
		assertTrue(contr.validate().isOK());
		assertEquals(".war", contr.getDefaultSuffix(MockModuleUtil.createMockWebModule()));
		assertEquals(".jar", contr.getDefaultSuffix(MockModuleUtil.createMockUtilModule()));
		assertEquals(".ear", contr.getDefaultSuffix(MockModuleUtil.createMockEarModule()));
		
		String tmpDir = contr.getTemporaryDeployDirectory(asArray(MockModuleUtil.createMockWebModule())).toOSString();
		assertEquals(tmpDir, initialTmpDeployDir);
		String depDir = contr.getDeployDirectory(asArray(MockModuleUtil.createMockWebModule())).toOSString();
		String expected = initialDeployDir + LOCAL_SEP + MockModuleUtil.createMockWebModule().getName() + ".war";
		assertEquals(expected, depDir);
	}

	public void testGettersNoPrefsWarInEar() {
		String initialDeployDir = TMP;
		String initialTmpDeployDir = TMP2;
		
		IModuleDeployPathController contr = buildEnv(initialDeployDir, initialTmpDeployDir, null);
		assertTrue(contr.validate().isOK());
		IModule web = MockModuleUtil.createMockWebModule();
		IModule ear = MockModuleUtil.createMockEarModule();
		
		String earWithSuffix = ear.getName() + ".ear";
		String warWithSuffix = web.getName() + ".war";
		String uri = "nested" + LOCAL_SEP + "inside" + LOCAL_SEP + warWithSuffix;
		
		((MockModule)ear).addChildModule(web, uri);
		IModule[] webInEar = new IModule[]{ear, web};
		
		String tmpDir = contr.getTemporaryDeployDirectory(webInEar).toOSString();
		assertEquals(tmpDir, initialTmpDeployDir);
		String depDir = contr.getDeployDirectory(webInEar).toOSString();
		String expected = initialDeployDir + LOCAL_SEP
				+ earWithSuffix + LOCAL_SEP + uri;
		assertEquals(expected, depDir);
	}
	

	public void testGettersNoPrefsUtilInWar() {
		String initialDeployDir = TMP;
		String initialTmpDeployDir = TMP2;
		
		IModuleDeployPathController contr = buildEnv(initialDeployDir, initialTmpDeployDir, null);
		assertTrue(contr.validate().isOK());
		IModule web = MockModuleUtil.createMockWebModule();
		IModule util = MockModuleUtil.createMockUtilModule();
		
		String utilWithSuffix = util.getName() + ".jar";
		String warWithSuffix = web.getName() + ".war";
		String uri = "nested" + LOCAL_SEP + "inside" + LOCAL_SEP + utilWithSuffix;
		
		((MockModule)web).addChildModule(util, uri);
		IModule[] utilInWeb = new IModule[]{web, util};
		
		String tmpDir = contr.getTemporaryDeployDirectory(utilInWeb).toOSString();
		assertEquals(tmpDir, initialTmpDeployDir);
		String depDir = contr.getDeployDirectory(utilInWeb).toOSString();
		String expected = initialDeployDir + LOCAL_SEP
				+ warWithSuffix + LOCAL_SEP + uri;
		assertEquals(expected, depDir);
	}
	

	public void testGettersNoPrefsUtilInWarInEar() {
		String initialDeployDir = TMP;
		String initialTmpDeployDir = TMP2;
		
		IModuleDeployPathController contr = buildEnv(initialDeployDir, initialTmpDeployDir, null);
		assertTrue(contr.validate().isOK());
		IModule ear = MockModuleUtil.createMockEarModule();
		IModule web = MockModuleUtil.createMockWebModule();
		IModule util = MockModuleUtil.createMockUtilModule();
		
		String earWithSuffix = ear.getName() + ".ear";
		String utilWithSuffix = util.getName() + ".jar";
		String warWithSuffix = web.getName() + ".war";
		String uriWar = "warNest" + LOCAL_SEP + warWithSuffix;
		String uriUtil = "util" + LOCAL_SEP + "Nest" + LOCAL_SEP + utilWithSuffix;
		
		
		((MockModule)web).addChildModule(util, uriUtil);
		((MockModule)ear).addChildModule(web, uriWar);
		IModule[] utilInWebInEar = new IModule[]{ear, web, util};
		
		String tmpDir = contr.getTemporaryDeployDirectory(utilInWebInEar).toOSString();
		assertEquals(tmpDir, initialTmpDeployDir);
		String depDir = contr.getDeployDirectory(utilInWebInEar).toOSString();
		String expected = initialDeployDir + LOCAL_SEP
				+ earWithSuffix + LOCAL_SEP + uriWar + LOCAL_SEP + uriUtil;
		assertEquals(expected, depDir);
	}
	
	public void testSettersFailWithoutWC() {
		String initialDeployDir = TMP;
		String initialTmpDeployDir = TMP2;
		
		IModuleDeployPathController contr = buildEnv(initialDeployDir, initialTmpDeployDir, null);
		assertTrue(contr.validate().isOK());
		IModule web = MockModuleUtil.createMockWebModule();

		try {
			contr.setDeployDirectory(web, "arbitraryPath");
			fail();
		} catch(IllegalStateException ise){} 

		try {
			contr.setOutputName(web, "arbitraryPath");
			fail();
		} catch(IllegalStateException ise){} 

		try {
			contr.setTemporaryDeployDirectory(web, "arbitraryPath");
			fail();
		} catch(IllegalStateException ise){} 
	}	
	
	public void testSetDeployPathLinuxAbsolute() {
		String initial = "/home/user/deploy";
		String initialTmp = "/home/user/deployTmp";
		String propVal = "/home/user/webDeploy";
		String expectedPrefix = "/home/user/webDeploy/";
		setDeployPathShallow(initial, initialTmp, propVal, expectedPrefix, new Character('/'));
	}

	public void testSetDeployPathLinuxRelative() {
		String initial = "/home/user/deploy";
		String initialTmp = "/home/user/deployTmp";
		String propVal = "innerForModule";
		String expectedPrefix = "/home/user/deploy/innerForModule/";
		setDeployPathShallow(initial, initialTmp, propVal, expectedPrefix, new Character('/'));
	}

	public void testSetDeployPathWindowsAbsolute() {
		String initial = "C:\\user\\deploy";
		String initialTmp = "C:\\user\\deployTmp";
		String propVal = "C:\\user\\newDeploy";
		String expectedPrefix = "C:\\user\\newDeploy\\";
		setDeployPathShallow(initial, initialTmp, propVal, expectedPrefix, new Character('\\'));
	}

	public void testSetDeployPathWindowsRelative() {
		String initial = "C:\\user\\deploy";
		String initialTmp = "C:\\user\\deployTmp";
		String propVal = "innerForModule";
		String expectedPrefix = "C:\\user\\deploy\\innerForModule\\";
		setDeployPathShallow(initial, initialTmp, propVal, expectedPrefix, new Character('\\'));
	}

	private void setDeployPathShallow(String initial, String initialTmp, String propVal, String expectedPrefix, Character separator) {
		IServerWorkingCopy wc = server.createWorkingCopy();
		IModuleDeployPathController contr = buildEnv(initial, initialTmp, wc, separator);
		assertTrue(contr.validate().isOK());
		IModule web = MockModuleUtil.createMockWebModule();
		contr.setDeployDirectory(web, propVal);
		try {
			wc.save(true, new NullProgressMonitor());
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
		
		IModuleDeployPathController contr2 = buildEnv(initial, initialTmp, null, separator);
		String depDir = contr2.getDeployDirectory(new IModule[]{web}).toOSString();
		assertEquals(expectedPrefix + web.getName() + ".war", depDir);
	}

	public void testSetTmpDeployPathLinuxAbsolute() {
		String initial = "/home/user/deploy";
		String initialTmp = "/home/user/deployTmp";
		String propVal = "/home/user/deployTmp2";
		String expectedPrefix = "/home/user/deployTmp2";
		setTmpDeployPathShallow(initial, initialTmp, propVal, expectedPrefix, new Character('/'));
	}

	public void testSetTmpDeployPathLinuxRelative() {
		String initial = "/home/user/deploy";
		String initialTmp = "/home/user/deployTmp";
		String propVal = "innerTmp";
		String expectedPrefix = "/home/user/deployTmp/innerTmp";
		setTmpDeployPathShallow(initial, initialTmp, propVal, expectedPrefix, new Character('/'));
	}

	public void testSetTmpDeployPathWindowsAbsolute() {
		String initial = "C:\\user\\deploy";
		String initialTmp = "C:\\user\\deployTmp";
		String propVal = "C:\\user\\newDeploy";
		String expectedPrefix = "C:\\user\\newDeploy";
		setTmpDeployPathShallow(initial, initialTmp, propVal, expectedPrefix, new Character('\\'));
	}

	public void testSetTmpDeployPathWindowsRelative() {
		String initial = "C:\\user\\deploy";
		String initialTmp = "C:\\user\\deployTmp";
		String propVal = "innerForModule";
		String expectedPrefix = "C:\\user\\deployTmp\\innerForModule";
		setTmpDeployPathShallow(initial, initialTmp, propVal, expectedPrefix, new Character('\\'));
	}

	
	private void setTmpDeployPathShallow(String initial, String initialTmp, String propVal, String expectedPrefix, Character separator) {
		IServerWorkingCopy wc = server.createWorkingCopy();
		IModuleDeployPathController contr = buildEnv(initial, initialTmp, wc, separator);
		assertTrue(contr.validate().isOK());
		IModule web = MockModuleUtil.createMockWebModule();
		contr.setTemporaryDeployDirectory(web, propVal);
		try {
			wc.save(true, new NullProgressMonitor());
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
		
		IModuleDeployPathController contr2 = buildEnv(initial, initialTmp, null, separator);
		String tmpDeploy = contr2.getTemporaryDeployDirectory(new IModule[]{web}).toOSString();
		assertEquals(expectedPrefix, tmpDeploy);
	}

	public void testSetDeployPathLinuxAbsoluteDeep() {
		String initialDeployDir = "/home/user/deploy";
		String initialTmpDeployDir = "/home/user/deployTmp";
		String deployDirToSet = "/home/user/earDeploy";
		String expectedPrefix = "/home/user/earDeploy/";
		setDeployPathDeep(initialDeployDir, initialTmpDeployDir, deployDirToSet, expectedPrefix, '/');
	}

	public void testSetDeployPathLinuxRelativeDeep() {
		String initialDeployDir = "/home/user/deploy";
		String initialTmpDeployDir = "/home/user/deployTmp";
		String deployDirToSet = "innerDeploy";
		String expectedPrefix = "/home/user/deploy/innerDeploy/";
		setDeployPathDeep(initialDeployDir, initialTmpDeployDir, deployDirToSet, expectedPrefix, '/');
	}

	public void testSetDeployPathWindowsAbsoluteDeep() {
		String initialDeployDir = "C:\\user\\deploy";
		String initialTmpDeployDir = "C:\\user\\deployTmp";
		String deployDirToSet = "C:\\user\\earDeploy";
		String expectedPrefix = "C:\\user\\earDeploy\\";
		setDeployPathDeep(initialDeployDir, initialTmpDeployDir, deployDirToSet, expectedPrefix, '\\');
	}

	public void testSetDeployPathWindowsRelativeDeep() {
		String initialDeployDir = "C:\\user\\deploy";
		String initialTmpDeployDir = "C:\\user\\deploy2";
		String deployDirToSet = "innerDeploy";
		String expectedPrefix = "C:\\user\\deploy\\innerDeploy\\";
		setDeployPathDeep(initialDeployDir, initialTmpDeployDir, deployDirToSet, expectedPrefix, '\\');
	}

	private void setDeployPathDeep(String initialDeployDir, String initialTmpDeployDir, String deployDirToSet, 
			String expectedPrefix, Character sep) {
		char sep2 = (sep == null ? java.io.File.separatorChar : sep.charValue());
			
		IModuleDeployPathController contr = buildEnv(initialDeployDir, initialTmpDeployDir, null, sep);
		assertTrue(contr.validate().isOK());
		IModule ear = MockModuleUtil.createMockEarModule();
		IModule web = MockModuleUtil.createMockWebModule();
		IModule util = MockModuleUtil.createMockUtilModule();
		
		String earWithSuffix = ear.getName() + ".ear";
		String utilWithSuffix = util.getName() + ".jar";
		String warWithSuffix = web.getName() + ".war";
		String uriWar = "warNest" + sep2 + warWithSuffix;
		String uriUtil = "util"+ sep2 +"Nest" + sep2 + utilWithSuffix;
		
		
		((MockModule)web).addChildModule(util, uriUtil);
		((MockModule)ear).addChildModule(web, uriWar);
		IModule[] utilInWebInEar = new IModule[]{ear, web, util};
		
		String tmpDir = contr.getTemporaryDeployDirectory(utilInWebInEar).toOSString();
		assertEquals(tmpDir, initialTmpDeployDir);
		String expected = initialDeployDir + sep2 
				+ earWithSuffix + sep2 + uriWar + sep2 + uriUtil;
		String depDir = contr.getDeployDirectory(utilInWebInEar).toOSString();
		assertEquals(expected, depDir);
		
		IServerWorkingCopy wc = server.createWorkingCopy();
		IModuleDeployPathController contr2 = buildEnv(initialDeployDir, initialTmpDeployDir, wc, sep);
		contr2.setDeployDirectory(ear, deployDirToSet);
		try {
			wc.save(true, new NullProgressMonitor());
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
		IModuleDeployPathController contr3 = buildEnv(initialDeployDir, initialTmpDeployDir, null, sep);
		String depDirInnerUtil = contr3.getDeployDirectory(utilInWebInEar).toOSString();
		String expectedResult = expectedPrefix + earWithSuffix + sep2 + uriWar + sep2 + uriUtil;
		assertEquals(expectedResult, depDirInnerUtil);
	}


	private IModuleDeployPathController buildEnv(String dep, String tmpDep, IServerWorkingCopy wc) {
		return buildEnv(dep, tmpDep, wc, null);
	}
	
	private IModuleDeployPathController buildEnv(String dep, String tmpDep, IServerWorkingCopy wc, Character targetSystemSeparator) {
		ModelSubclass c = new ModelSubclass();
		String serverType = IJBossToolingConstants.DEPLOY_ONLY_SERVER;
		String system = SYSTEM;
		String subsystem = SUBSYSTEM;
		Object[] types = c.getSubsystemMappings(serverType, system);
		assertTrue(types != null);
		assertTrue(types.length == 1);
		HashMap<String, Object> env = new HashMap<String, Object>();
		env.put(IModuleDeployPathController.ENV_DEFAULT_DEPLOY_FOLDER, dep);
		env.put(IModuleDeployPathController.ENV_DEFAULT_TMP_DEPLOY_FOLDER, tmpDep);
		if( targetSystemSeparator != null )
			env.put(IModuleDeployPathController.ENV_TARGET_OS_SEPARATOR, targetSystemSeparator);
		
		try {
			IServerAttributes serverToUse = (wc == null ? server : wc);
			ISubsystemController controller = c.createSubsystemController(serverToUse,system, null, null, env);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals(subsystem));
			assertTrue(controller instanceof IModuleDeployPathController);
			return (IModuleDeployPathController)controller;
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
		return null;
	}
	
	public void testControllerFoundForAllServers() {
		String[] all = ServerParameterUtils.getAllJBossServerTypeParameters();
		ModelSubclass c = new ModelSubclass();
		String system = SYSTEM;
		String subsystem = SUBSYSTEM;
		for( int i = 0; i < all.length; i++) {
			Object[] types = c.getSubsystemMappings(all[i], system);
			assertTrue(types != null);
			assertTrue(types.length == 1);
			assertEquals(subsystem, c.getSubsystemMappedId(types[0]));
		}

	}
	

	

	/*
	 * This is a utility test to test the related addition of RemotePath
	 */
	public void testRemotePath() {
		// Skip this test if we're on windows
		if( java.io.File.separatorChar == '\\') 
			return;
		
		String normalWindowsAbsolute = "C:\\my\\folder";
		// Path is not normally recognized as absolute
		assertFalse(new Path(normalWindowsAbsolute).isAbsolute());
		IPath RemotePathWindowsAbsolute = new RemotePath(normalWindowsAbsolute, '\\');
		// Our RemotePath instance is, though
		assertTrue(RemotePathWindowsAbsolute.isAbsolute());
		
		// append a segment
		IPath postAppend = RemotePathWindowsAbsolute.append("test");
		// It is still recognized as absolute, even though a new Path(string) would not be
		assertTrue(postAppend.isAbsolute());
		assertFalse(postAppend.isRoot());
		
		// an RemotePath.append(segment) will not be able to properly get an OS-dependent string
		assertNotSame("C:\\my\\folder\\test", postAppend.toOSString());
		
		// but a new RemotePath from the old one will work
		RemotePath postAppend2 = new RemotePath(postAppend.toString(), '\\');
		assertEquals("C:\\my\\folder\\test", postAppend2.toOSString());
		
		// An eclipse IPath will incorrectly count the segments in a windows path
		//but an RemotePath won't, if given the proper separator
		IPath segCount = new Path("C:\\my\\folder\\test");
		IPath osSegCountNoSep = new RemotePath("C:\\my\\folder\\test");
		IPath osSegCount = new RemotePath("C:\\my\\folder\\test", '\\');
		assertEquals(segCount.segmentCount(), 1);
		assertEquals(osSegCountNoSep.segmentCount(), 1);
		assertEquals(osSegCount.segmentCount(), 3);
		
	}
	
	
	/*
	 * Perhaps to be pulled into a common area
	 */
	
	private IModule[] asArray(IModule m) {
		return new IModule[]{m};
	}

}