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
package org.jboss.tools.as.test.core.parametized.server.publishing.defect;

import junit.framework.TestCase;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.validation.ValidationFramework;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.classpath.WorkspaceTestUtil;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.internal.utils.wtp.OperationTestCase;
import org.jboss.tools.as.test.core.internal.utils.wtp.ProjectUtility;
import org.jboss.tools.test.util.JobUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
public class ClosedProjectPublishTest extends TestCase {
	private IServer server;
	private IModule module;
	
	public ClosedProjectPublishTest() {
	}

	private static boolean preValidation, preAutoBuild;
	@BeforeClass
	public static void beforeClassSetup() {
		preValidation = ValidationFramework.getDefault().isSuspended();
		preAutoBuild = WorkspaceTestUtil.isAutoBuildEnabled();
		ValidationFramework.getDefault().suspendAllValidation(true);
		WorkspaceTestUtil.setAutoBuildEnabled(false);
	}
	
	@AfterClass
	public static void afterClassTeardown() {
		ValidationFramework.getDefault().suspendAllValidation(preValidation);
		WorkspaceTestUtil.setAutoBuildEnabled(preAutoBuild);
	}
	
	@Before
	public void setUp() throws Exception {
		String param_serverType = IJBossToolingConstants.SERVER_AS_71;
		IServer s = ServerCreationTestUtils.createMockServerWithRuntime(param_serverType, getClass().getName() + param_serverType);
    	
    	IDataModel dyn1Model = CreateProjectOperationsUtility.getWebDataModel("cpptd1v", null, null, null, null, JavaEEFacetConstants.WEB_23, true);
    	OperationTestCase.runAndVerify(dyn1Model);
    	IModule m = ServerUtil.getModule(ResourcesPlugin.getWorkspace().getRoot().getProject("cpptd1v"));
    	server = s;
    	module = m;
    	addOrRemoveModuleWithPublish(new IModule[]{module}, true);
	}
	
	@After
	public void tearDown() throws Exception {
		JobUtils.waitForIdle(100);
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
		ProjectUtility.deleteAllProjects();
		ASMatrixTests.clearStateLocation();
		JobUtils.waitForIdle();
	}
	
	private void addOrRemoveModuleWithPublish(IModule[] module, boolean add) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		if( add )
			wc.modifyModules(module, new IModule[]{}, new NullProgressMonitor());
		else
			wc.modifyModules(new IModule[]{}, module, new NullProgressMonitor());
		server = wc.save(true, new NullProgressMonitor());
		JobUtils.waitForIdle();
		server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
	}
	
	
	@Test
	public void testOpenClosedProject() throws Exception {
		IDeployableServer ds = (IDeployableServer)server.loadAdapter(IDeployableServer.class, null);
		IPath p = ds.getDeploymentLocation(new IModule[]{module}, true);
		assertTrue(p.append("WEB-INF").append("web.xml").toFile().exists());
		
		module.getProject().close(new NullProgressMonitor() );
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
		JobUtils.waitForIdle();
		
		// publish, verify the contents are still there
		server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		JobUtils.waitForIdle();
		assertTrue(p.append("WEB-INF").append("web.xml").toFile().exists());
		
		server.publish(IServer.PUBLISH_FULL, new NullProgressMonitor());
		JobUtils.waitForIdle();
		assertTrue(p.append("WEB-INF").append("web.xml").toFile().exists());
		
		addOrRemoveModuleWithPublish(new IModule[]{module}, false);
		
		JobUtils.waitForIdle();
		assertFalse(p.append("WEB-INF").append("web.xml").toFile().exists());
		
		addOrRemoveModuleWithPublish(new IModule[]{module}, true);
		JobUtils.waitForIdle();
		assertFalse(p.append("WEB-INF").append("web.xml").toFile().exists());
		
		
		module.getProject().open(new NullProgressMonitor() );
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
		JobUtils.waitForIdle();
		assertFalse(p.append("WEB-INF").append("web.xml").toFile().exists());
		server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		JobUtils.waitForIdle();
		assertTrue(p.append("WEB-INF").append("web.xml").toFile().exists());
		
	}

}
