package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;

public class JSTDeploymentWarUpdateXML extends AbstractJSTDeploymentTester {
	
	protected IProject createProject() throws Exception {
		IDataModel dm = ProjectCreationUtil.getWebDataModel(MODULE_NAME, null, null, CONTENT_DIR, null, JavaEEFacetConstants.WEB_25, true);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(MODULE_NAME);
		assertTrue(p.exists());
		return p;
	}
		
	public void testMain() throws CoreException, IOException {
		IModule mod = ServerUtil.getModule(project);
		IModule[] module = new IModule[] { mod };
		verifyJSTPublisher(module);
		IFile f = project.getFolder(CONTENT_DIR).getFolder("WEB-INF").getFile("web.xml");

		server = ServerRuntimeUtils.addModule(server,mod);
		ServerRuntimeUtils.publish(server);
		IPath deployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		IPath rootFolder = deployRoot.append(MODULE_NAME + ".war");
		assertTrue(rootFolder.toFile().exists());

		long workspaceModified = project.getFolder(CONTENT_DIR).getFolder("WEB-INF").getFile("web.xml").getLocation().toFile().lastModified();
		long publishedModified = rootFolder.append("WEB-INF").append("web.xml").toFile().lastModified();
		
		// FULL PUBLISH and verify web xml's timestamp
		ServerRuntimeUtils.publish(IServer.PUBLISH_FULL, server);
		try {
			Thread.sleep(400);
		} catch(InterruptedException ie) {}
		
		long publishedModified2 = rootFolder.append("WEB-INF").append("web.xml").toFile().lastModified();
		assertNotSame(publishedModified, publishedModified2);
		
		server = ServerRuntimeUtils.removeModule(server, mod);
		assertTrue(rootFolder.toFile().exists());
		ServerRuntimeUtils.publish(server);
		assertFalse(rootFolder.toFile().exists());
	}
	
	public void testWarUpdateMockPublishMethod() throws CoreException, IOException {
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		testMockPublishMethod(7,1,"");
	}
	
	public void testWarUpdateMockPublishMethodJBoss7() throws CoreException, IOException {
		server = ServerRuntimeUtils.createMockJBoss7Server();
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		testMockPublishMethod(8,1,"newModule.war" + JBoss7JSTPublisher.DEPLOYED);
	}
	
	private void testMockPublishMethod(int initial, int remove, String removedFile) throws CoreException, IOException {
		IModule mod = ServerUtil.getModule(project);
		server = ServerRuntimeUtils.addModule(server,mod);
		ServerRuntimeUtils.publish(server);
		assertEquals(initial, MockPublishMethod.getChanged().length);
		MockPublishMethod.reset();
		
		server = ServerRuntimeUtils.removeModule(server, mod);
		ServerRuntimeUtils.publish(server);
		assertEquals(remove, MockPublishMethod.getRemoved().length);
		assertEquals(removedFile, MockPublishMethod.getRemoved()[0].toString());
		MockPublishMethod.reset();
	}
}
