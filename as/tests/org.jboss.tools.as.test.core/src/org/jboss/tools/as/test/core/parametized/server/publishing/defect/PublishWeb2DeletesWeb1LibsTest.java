package org.jboss.tools.as.test.core.parametized.server.publishing.defect;

import junit.framework.TestCase;

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
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.jmx.core.test.util.TestProjectProvider;
import org.jboss.tools.test.util.JobUtils;
import org.junit.Before;
import org.junit.Test;


public class PublishWeb2DeletesWeb1LibsTest extends TestCase {
	TestProjectProvider[] providers = null;
	IProject[] projects = null;
	IServer server;
	 
	@Before
	public void setUp() throws Exception {
		TestProjectProvider util1Provider = new TestProjectProvider("org.jboss.tools.as.test.core", null, 
				"UserForum1Util1", true); 
		IProject util1Project = util1Provider.getProject();
		util1Project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

		TestProjectProvider util2Provider = new TestProjectProvider("org.jboss.tools.as.test.core", null, 
				"UserForum1Util2", true); 
		IProject util2Project = util2Provider.getProject();
		util2Project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		
		TestProjectProvider ejb1Provider = new TestProjectProvider("org.jboss.tools.as.test.core", null, 
				"UserForum1EJB1", true); 
		IProject ejb1Project = ejb1Provider.getProject();
		ejb1Project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());


		TestProjectProvider web1Provider = new TestProjectProvider("org.jboss.tools.as.test.core", null, 
				"UserForum1Web1", true); 
		IProject web1Project = web1Provider.getProject();
		web1Project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

		TestProjectProvider web2Provider = new TestProjectProvider("org.jboss.tools.as.test.core", null, 
				"UserForum1Web2", true); 
		IProject web2Project = web2Provider.getProject();
		web2Project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		
		JobUtils.waitForIdle();
		projects = new IProject[]{ util1Project, util2Project, ejb1Project, web1Project, web2Project};
		providers = new TestProjectProvider[]{util1Provider, util2Provider, ejb1Provider, web1Provider, web2Provider};
		
		for( int i = 0; i < projects.length; i++ ) {
			assertTrue(projects[i].exists());
			assertTrue(projects[i].isAccessible());
			assertTrue(projects[i].isNatureEnabled( FacetedProjectNature.NATURE_ID ));
		}
		
		server = ServerCreationTestUtils.createServerWithRuntime(IJBossToolingConstants.DEPLOY_ONLY_SERVER, getClass().getName());
	}
	
	private void addModuleAndFullPublish(String projectName) throws CoreException {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
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
