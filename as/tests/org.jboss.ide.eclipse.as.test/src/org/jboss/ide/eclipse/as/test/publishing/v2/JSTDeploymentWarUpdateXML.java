package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

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
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.publishers.JstPublisher;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;

public class JSTDeploymentWarUpdateXML extends TestCase {
	
	IProject project;
	IServer server;
	final String MODULE_NAME = "newModule";
	final String CONTENT_DIR = "contentDirS"; 
	final String TEXT_FILE = "test.txt";
	final IPath CONTENT_TEXT_FILE = new Path(CONTENT_DIR).append(TEXT_FILE);
	public void setUp() throws Exception {
		project = createProject();
		server = ServerRuntimeUtils.createMockDeployOnlyServer();
	}
	
	public void tearDown() throws Exception {
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
		ProjectUtility.deleteAllProjects();
		ASTest.clearStateLocation();
	}
	
	protected IProject createProject() throws Exception {
		IDataModel dm = ProjectCreationUtil.getWebDataModel(MODULE_NAME, null, null, CONTENT_DIR, null, JavaEEFacetConstants.WEB_25, true);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(MODULE_NAME);
		assertTrue(p.exists());
		return p;
	}
		
	protected void verifyJSTPublisher(IModule[] module) {
		IJBossServerPublisher publisher = ExtensionManager
			.getDefault().getPublisher(server, module, "local");
		assertTrue(publisher instanceof JstPublisher);
	}

	protected void assertContents(IFile file, int val) throws IOException, CoreException {
		assertContents(file, "" + val);
	}
	
	protected void assertContents(IFile file, String val) throws IOException, CoreException {
		assertEquals(val, IOUtil.getContents(file));
	}

	protected void assertContents(File file, int val) throws IOException, CoreException {
		assertContents(file, "" + val);
	}
	
	protected void assertContents(File file, String val) throws IOException, CoreException {
		assertEquals(val, IOUtil.getContents(file));
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
}
