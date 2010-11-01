package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.publishing.AbstractDeploymentTest;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;

public class JSTZippedDeploymentTester extends TestCase {
	
	IProject project;
	IServer server;
	final String MODULE_NAME = "newModule";
	final String CONTENT_DIR = "contentDirS"; 
	final String TEXT_FILE = "test.txt";
	final IPath CONTENT_TEXT_FILE = new Path(CONTENT_DIR).append(TEXT_FILE);
	public void setUp() throws Exception {
		project = createProject();
		server = ServerRuntimeUtils.createMockDeployOnlyServer();
		IServerWorkingCopy wc = server.createWorkingCopy();
		ServerAttributeHelper helper = new ServerAttributeHelper(server, wc);
		helper.setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, true);
		server = wc.save(true, new NullProgressMonitor());
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
		
		File srcFile = AbstractDeploymentTest.getFileLocation("projectPieces/mvel2.jar");
		IPath contentDir = p.getFolder(CONTENT_DIR).getLocation();
		File destFile = new File(contentDir.toFile(), "mvel2.jar");
		FileUtil.fileSafeCopy(srcFile, destFile);
		p.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		return p;
	}
		
	protected void verifyJSTZippedPublisher(IModule[] module) {
		IJBossServerPublisher publisher = ExtensionManager
			.getDefault().getPublisher(server, module, "local");
		assertTrue(publisher.getClass().getName().contains("WTPZippedPublisher"));
	}
	
	protected void setContents(IFile file, int val) throws IOException , CoreException{
		IOUtil.setContents(file, "" + val);
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
		IModule[] mods = ServerUtil.getModules(project);
		IModule mod = null;
		// find hte right module ugh
		for( int i = 0; i < mods.length && mod == null; i++ ) {
			if( mods[i].getModuleType().getId().equals("jst.ear"))
				mod = mods[i];
		}
		assertNotNull(mod);
		IModule[] module = new IModule[] { mod };
		verifyJSTZippedPublisher(module);
		server = ServerRuntimeUtils.addModule(server, mod);
		ServerRuntimeUtils.publish(server);
		IPath projLoc = project.getLocation();
		System.out.println(projLoc);
		IPath deployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		IPath zipped = deployRoot.append(MODULE_NAME + ".ear");
		assertTrue(zipped.toFile().exists());
		assertTrue(zipped.toFile().isFile());
		IPath unzip1 = ASTest.getDefault().getStateLocation().append("unzip1");
		IPath unzip2 = ASTest.getDefault().getStateLocation().append("unzip2");
		IOUtil.unzipFile(zipped,unzip1);
		assertTrue(unzip1.toFile().list().length == 1);
		IOUtil.unzipFile(unzip1.append("mvel2.jar"), unzip2);
		assertTrue(unzip2.toFile().list().length > 1);
		System.out.println("end");
		
		server = ServerRuntimeUtils.removeModule(server, mod);
		ServerRuntimeUtils.publish(server);
		assertFalse(zipped.toFile().exists());
	}
}
