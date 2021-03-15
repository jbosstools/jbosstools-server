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
package org.jboss.tools.as.test.core.parametized.server.publishing.defect;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.common.project.facet.core.internal.FacetedProjectNature;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.test.util.JobUtils;
import org.jboss.tools.test.util.TestProjectProvider;
import org.junit.Before;
import org.junit.Test;


public class PublishWeb2DeletesWeb1LibsTest {
	List<TestProjectProvider> providers = null;
	IServer server;
	 
	@Before
	public void setUp() throws Exception {
		TestProjectProvider util1Provider = importProject("UserForum1Util1");
		TestProjectProvider util2Provider = importProject("UserForum1Util2");
		TestProjectProvider ejb1Provider = importProject("UserForum1EJB1"); 
		TestProjectProvider web1Provider = importProject("UserForum1Web1"); 
		TestProjectProvider web2Provider = importProject("UserForum1Web2"); 
		
		JobUtils.waitForIdle();
		providers = Arrays.asList(new TestProjectProvider[]{util1Provider, util2Provider, ejb1Provider, web1Provider, web2Provider});
		
		for (TestProjectProvider provider : providers) {
			IProject project = provider.getProject();
			assertTrue(project.exists());
			assertTrue(project.isAccessible());
			assertTrue(project.isNatureEnabled( FacetedProjectNature.NATURE_ID ));
		}
		
		server = ServerCreationTestUtils.createServerWithRuntime(IJBossToolingConstants.DEPLOY_ONLY_SERVER, getClass().getName());
	}

	protected TestProjectProvider importProject(String projectName) throws CoreException {
		TestProjectProvider testProjectProvider = new TestProjectProvider("org.jboss.tools.as.test.core", null, projectName, true); 
		IProject projectImported = testProjectProvider.getProject();
		projectImported.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		return testProjectProvider;
	}
	
	private void addModuleAndFullPublish(String projectName) throws CoreException {
		ServerConverter.getDeployableServer(server);
		server = addModuleToServer(findModuleForProject(projectName));
		server.publish(IServer.PUBLISH_FULL, new NullProgressMonitor());
		JobUtils.waitForIdle();
	}
	
	@Test
	public void testFullPublishes() throws CoreException {
		addModuleAndFullPublish("UserForum1EJB1");
		verifyEJBAndJarExist();
		
		addModuleAndFullPublish("UserForum1Web1");
		verifyEJBAndJarExist();
		verifyWeb1AndLibsExist();

		addModuleAndFullPublish("UserForum1Web2");
		verifyEJBAndJarExist();
		verifyWeb1AndLibsExist();
		verifyWeb2AndLibsExist();
	}
	
	private void verifyEJBAndJarExist() {
		// Make sure ejb and libs exist
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		IPath fullPath = ds.getDeploymentLocation(findArrayForProject("UserForum1EJB1"), true);
		assertTrue(fullPath.toFile().exists());
		assertTrue(fullPath.append("UserForum1Util1.jar").toFile().exists());
	}
	private void verifyWeb1AndLibsExist() {
		// Make sure ejb and libs exist
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		IPath fullPath = ds.getDeploymentLocation(findArrayForProject("UserForum1Web1"), true);
		assertTrue(fullPath.toFile().exists());
		assertTrue(fullPath.append("WEB-INF").append("lib").append("UserForum1Util1.jar").toFile().exists());
		assertTrue(fullPath.append("WEB-INF").append("lib").append("UserForum1Util2.jar").toFile().exists());
	}
	private void verifyWeb2AndLibsExist() {
		// Make sure ejb and libs exist
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		IPath fullPath = ds.getDeploymentLocation(findArrayForProject("UserForum1Web2"), true); 
		assertTrue(fullPath.toFile().exists());
		assertTrue(fullPath.append("WEB-INF").append("lib").append("UserForum1Util1.jar").toFile().exists());
		assertTrue(fullPath.append("WEB-INF").append("lib").append("UserForum1Util2.jar").toFile().exists());
	}
	
	public IModule[] findArrayForProject(String projectName) {
		return new IModule[] { findModuleForProject(projectName) };
	}
	
	public IModule findModuleForProject(String projectName) {
		return ServerUtil.getModule(ResourceUtils.findProject(projectName));
	}
	
	public IServer addModuleToServer(IModule module) throws CoreException  {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.modifyModules(new IModule[]{module}, new IModule[0], new NullProgressMonitor());
		return wc.save(true, null);
	}

}
