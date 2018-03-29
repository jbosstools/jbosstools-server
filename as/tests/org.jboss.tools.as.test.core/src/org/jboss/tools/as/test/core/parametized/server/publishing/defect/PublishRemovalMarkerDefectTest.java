/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.test.core.parametized.server.publishing.defect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.core.server.controllable.subsystems.StandardFileSystemPublishController;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.IOUtil;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
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
public class PublishRemovalMarkerDefectTest extends AbstractPublishingTest {
	@Parameters(name = "{0}, {1}, {2}, {3}")
	public static Collection<Object[]> params() {
		Object[] servers = new String[] {  IJBossToolingConstants.SERVER_AS_71 };
		Object[] zipOption = ServerParameterUtils.getServerZipOptions();
		Object[] defaultDeployLoc = ServerParameterUtils.getDefaultDeployOptions();
		Object[] perModOverrides = ServerParameterUtils.getPerModuleOverrideOptions();
		Object[][] allOptions = new Object[][] {
				servers, zipOption, defaultDeployLoc, perModOverrides
		};
		return MatrixUtils.toMatrix(allOptions);
	}

	private IModule[] module;
	
	public PublishRemovalMarkerDefectTest(String serverType, String zip,
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
		controller.publishStart(new NullProgressMonitor());
		controller.publishServer(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		int result = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, module, new NullProgressMonitor());
		controller.publishFinish(new NullProgressMonitor());
		
		assertEquals(result, IServer.PUBLISH_STATE_NONE);
		IPath depPath = controller.getDeployPathController().getDeployDirectory(module);
		IPath depFolder = depPath.removeLastSegments(1);
		String name = depPath.lastSegment();
		String nameDoDeployMarker = name + ".dodeploy";
		String nameDeployedMarker = name + ".deployed";
		
		String s = depPath.toOSString();
		assertTrue(new Path(s).toFile().exists());
		assertEquals(testIsZip(), new Path(s).toFile().isFile());
		verifyListRelativePath(new Path(s), Arrays.asList(getLeafPaths()), true);
		assertTrue(depFolder.append(nameDoDeployMarker).toFile().exists());
		
		// Pretend the server picked it up
		depFolder.append(nameDoDeployMarker).toFile().delete();
		depFolder.append(nameDeployedMarker).toFile().createNewFile();
		
		
		// Now remove it
		controller.publishStart(new NullProgressMonitor());
		controller.publishServer(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		result = controller.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.REMOVED, module, null);
		controller.publishFinish(new NullProgressMonitor());
		assertFalse(depFolder.append(nameDeployedMarker).toFile().exists());
	}

	

	private boolean testIsZip() {
		return (param_zip.equals(ServerParameterUtils.ZIPPED));
	}
	
	/*
	 * Utility methods for this test class are below
	 */
	
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
	

	protected void verifyListRelativePath(IPath root, List<IPath> list, boolean exists) {
		ArrayList<IPath> list2 = new ArrayList<IPath>();
		for(Iterator<IPath> i = list.iterator(); i.hasNext(); ) {
			list2.add(root.append(i.next()));
		}
		super.verifyList(root, list2, exists);
	}
	
}