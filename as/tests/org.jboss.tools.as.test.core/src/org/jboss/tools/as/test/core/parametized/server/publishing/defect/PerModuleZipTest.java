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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModule2;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.archives.core.util.TrueZipUtil;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.IOUtil;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.jboss.tools.as.test.core.internal.utils.MockModule;
import org.jboss.tools.as.test.core.internal.utils.MockModuleUtil;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.jboss.tools.as.test.core.parametized.server.publishing.AbstractPublishingTest;
import org.jboss.tools.as.test.core.subsystems.impl.CustomPublishController;
import org.jboss.tools.runtime.core.extract.ExtractUtility;
import org.jboss.tools.runtime.core.extract.IOverwrite;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This class tests the basic behavior of the what deploy name we use for root and nested modules.
 * We trust the deploy-name of a given module only when it is a root module, or the child module 
 * has no relative uri to its parent already coded in the component model. 
 *
 */
@RunWith(value = Parameterized.class)
public class PerModuleZipTest extends AbstractPublishingTest {
	@Parameters(name = "{0}, {1}")
	public static Collection<Object[]> params() {
		Object[] zipDefault = new String[]{ServerParameterUtils.UNZIPPED, ServerParameterUtils.ZIPPED};
		Object[] zipModule = new String[]{ServerParameterUtils.UNZIPPED, ServerParameterUtils.ZIPPED, ServerParameterUtils.ZIP_OPTION_UNSET};
		Object[][] allOptions = new Object[][] {zipDefault, zipModule};
		return MatrixUtils.toMatrix(allOptions);
	}

	private IModule[] module;
	private String zipModule;
	public PerModuleZipTest(String zip, String zipModule) {
		super(IJBossToolingConstants.SERVER_AS_71 , zip, ServerParameterUtils.DEPLOY_META, ServerParameterUtils.DEPLOY_PERMOD_DEFAULT);
		this.zipModule = zipModule;
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
		module = createUtilInWebModule("TestWeb", "TestUtil");
		addModuleToServer(module[0]);
		Boolean zipMod = null;
		if( ServerParameterUtils.ZIPPED.equals(zipModule)) {
			zipMod = new Boolean(true);
		} else if( ServerParameterUtils.UNZIPPED.equals(zipModule)) {
			zipMod = new Boolean(false);
		}
		setCustomZipForModule(wc, module[module.length-1], zipMod);
	}

	@Test
	public void testPublish() throws Exception { 
		// Create a publish controller
		CustomPublishController controller = new CustomPublishController();
		controller.initialize(server, null, null);
		
		IModule[] web = new IModule[]{module[0]};
		IModule[] utilInWeb = new IModule[]{module[0], module[1]};
		setUnderlyingVersion(1);
		setUtilUnderlyingVersion(1);
		
		/*
		 * Publish util in web
		 */
		controller.publishStart(new NullProgressMonitor());
		controller.publishServer(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		int resultWeb = controller.publishModule(IServer.PUBLISH_FULL, ServerBehaviourDelegate.ADDED, web, new NullProgressMonitor());
		int resultUtil = controller.publishModule(IServer.PUBLISH_FULL, ServerBehaviourDelegate.ADDED, utilInWeb, new NullProgressMonitor());
		controller.publishFinish(new NullProgressMonitor());
		
		// Verify all were published without fs errors
		assertEquals(resultWeb, IServer.PUBLISH_STATE_NONE);
		assertEquals(resultUtil, IServer.PUBLISH_STATE_NONE);
		
		// verify the util jar exists
		IPath webPath = controller.getDeployPathController().getDeployDirectory(web);
		assertTrue(webPath.toString().contains("TestWeb"));
		assertTrue(webPath.toFile().exists());
		IPath utilJar = webPath.append("WEB-INF/lib/util.jar");
		
		// If we're not zipped, the util jar should exist in fs
		boolean zipWar = param_zip.equals(ServerParameterUtils.ZIPPED);
		boolean zipUtil = zipWar || ServerParameterUtils.ZIPPED.equals(zipModule) || 
				(ServerParameterUtils.ZIP_OPTION_UNSET.equals(zipModule) && PublishUtil.deployPackaged(module, server));
		
		if( zipWar ) {
			// Verify the war is a zip file
			assertTrue(webPath.toFile().isFile());
			assertTrue(webPath.toFile().exists());
			// util jar doesn't exist bc it's inside the war
			assertFalse(utilJar.toFile().exists());
			//extract the war
			ExtractUtility ex = new ExtractUtility(webPath.toFile(), ExtractUtility.FORMAT_ZIP);
			ex.extract(webPath.toFile().getParentFile(), new IOverwrite() {
				@Override
				public int overwrite(File file) {
					return IOverwrite.YES;
				}
			}, new NullProgressMonitor());
			IPath utilJar2 = webPath.removeLastSegments(1).append("WEB-INF/lib/util.jar");
			assertTrue(utilJar2.toFile().exists());
			assertTrue(utilJar2.toFile().isFile()); // Inside a war, jars should never be exploded
		} else {
			assertTrue(webPath.toFile().isDirectory());
			assertTrue(utilJar.toFile().exists());
			assertEquals(utilJar.toFile().isFile(), zipUtil);
		}
	}
	
	/*
	 * Utility methods for this test class are below
	 */
	
	private IPath getUnderlying() {
		IPath underlying = ASMatrixTests.getDefault().getStateLocation().append("underlying.txt");
		return underlying;
	}

	private IPath getUtilUnderlying() {
		IPath underlying = ASMatrixTests.getDefault().getStateLocation().append("笨.txt");
		return underlying;
	}

	private IPath setUnderlyingVersion(int v) throws Exception {
		IPath underlying = getUnderlying();
		underlying.toFile().getParentFile().mkdirs();
		IOUtil.setContents(underlying.toFile(), "version" + v);
		return underlying;
	}

	private IPath setUtilUnderlyingVersion(int v) throws Exception {
		IPath underlying = getUtilUnderlying();
		underlying.toFile().getParentFile().mkdirs();
		IOUtil.setContents(underlying.toFile(), "version" + v);
		return underlying;
	}

	private IPath[] getUtilLeafPaths() {
		return new IPath[]{
				new Path("笨.txt")
		};
	}
	private IPath[] getWebLeafPaths() {
		IPath[] leafs = new IPath[] {
				new Path("w.txt"),
		};
		return leafs;
	}
	
	private MockModule[] createUtilInWebModule(String webName, String utilName) throws Exception {
		IPath underlying = setUnderlyingVersion(1);
		IPath utilUnderlying = setUtilUnderlyingVersion(1);
		
		MockModule web = MockModuleUtil.createMockWebModule();
		web.setProperty(IModule2.PROP_DEPLOY_NAME, webName);
		MockModule util = MockModuleUtil.createMockUtilModule();
		util.setProperty(IModule2.PROP_DEPLOY_NAME, utilName);
		web.addChildModule(util, "WEB-INF/lib/util.jar");
		
		
		IPath[] webLeafs = getWebLeafPaths();
		IModuleResource[] webR = MockModuleUtil.createMockResources(webLeafs, new IPath[0], underlying.toFile());
		web.setMembers(webR);
		web.setExists(true);

		IPath[] utilLeafs = getUtilLeafPaths();
		IModuleResource[] utilR = MockModuleUtil.createMockResources(utilLeafs, new IPath[0], utilUnderlying.toFile());
		util.setMembers(utilR);
		util.setExists(true);
		return new MockModule[]{ web, util};
	}
	


	protected void verifyListRelativePath(IPath root, List<IPath> list, boolean exists) {
		ArrayList<IPath> list2 = new ArrayList<IPath>();
		for(Iterator<IPath> i = list.iterator(); i.hasNext(); ) {
			list2.add(root.append(i.next()));
		}
		super.verifyList(root, list2, exists);
	}
	
}