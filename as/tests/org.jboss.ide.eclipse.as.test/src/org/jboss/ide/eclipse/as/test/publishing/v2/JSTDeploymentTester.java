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

public class JSTDeploymentTester extends TestCase {
	
	protected IProject project;
	protected IServer server;
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
		IDataModel dm = ProjectCreationUtil.getEARDataModel(MODULE_NAME, CONTENT_DIR, null, null, JavaEEFacetConstants.EAR_5, false);
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
		server = ServerRuntimeUtils.addModule(server,mod);
		ServerRuntimeUtils.publish(server);
		IPath deployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		IPath rootFolder = deployRoot.append(MODULE_NAME + ".ear");
		assertTrue(rootFolder.toFile().exists());
		assertTrue(IOUtil.countFiles(rootFolder.toFile()) == 0);
		assertTrue(IOUtil.countAllResources(rootFolder.toFile()) == 1);
		IFile textFile = project.getFile(CONTENT_TEXT_FILE);
		IOUtil.setContents(textFile, 0);
		assertEquals(IOUtil.countFiles(rootFolder.toFile()), 0);
		assertTrue(IOUtil.countAllResources(rootFolder.toFile()) == 1);
		ServerRuntimeUtils.publish(server);
		assertEquals(IOUtil.countFiles(rootFolder.toFile()), 1);
		assertTrue(IOUtil.countAllResources(rootFolder.toFile()) == 2);
		assertContents(rootFolder.append(TEXT_FILE).toFile(), 0);
		IOUtil.setContents(textFile, 1);
		ServerRuntimeUtils.publish(server);
		assertContents(rootFolder.append(TEXT_FILE).toFile(), 1);
		textFile.delete(true, null);
		assertEquals(IOUtil.countFiles(rootFolder.toFile()), 1);
		assertTrue(IOUtil.countAllResources(rootFolder.toFile()) == 2);
		ServerRuntimeUtils.publish(server);
		assertEquals(IOUtil.countFiles(rootFolder.toFile()), 0);
		assertTrue(IOUtil.countAllResources(rootFolder.toFile()) == 1);
		server = ServerRuntimeUtils.removeModule(server, mod);
		assertTrue(rootFolder.toFile().exists());
		ServerRuntimeUtils.publish(server);
		assertFalse(rootFolder.toFile().exists());
	}
}
