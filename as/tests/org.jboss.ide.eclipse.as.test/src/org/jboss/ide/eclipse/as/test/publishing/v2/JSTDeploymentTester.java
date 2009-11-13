package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.jboss.ide.eclipse.as.core.publishers.JstPublisher;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;
import org.jboss.tools.test.util.JobUtils;

public class JSTDeploymentTester extends TestCase {
	
	IProject project;
	IServer server;
	final String MODULE_NAME = "newModule";
	final String CONTENT_DIR = "contentDirS"; 
	final String TEXT_FILE = "test.txt";
	final IPath CONTENT_TEXT_FILE = new Path(CONTENT_DIR).append(TEXT_FILE);
	public void setUp() throws Exception {
		project = createProject();
		server = createServer();
	}
	
	public void tearDown() throws Exception {
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
		ProjectUtility.deleteAllProjects();
		clearStateLocation();
	}
	
	protected IProject createProject() throws Exception {
		IDataModel dm = ProjectCreationUtil.getEARDataModel(MODULE_NAME, CONTENT_DIR, null, null, JavaEEFacetConstants.EAR_5, false);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(MODULE_NAME);
		assertTrue(p.exists());
		return p;
	}
	
	protected IServer createServer() throws CoreException {
		IPath state = ASTest.getDefault().getStateLocation();
		IPath deploy = state.append("testDeployments").append("deploy");
		IPath tmpDeploy = state.append("testDeployments").append("tmpDeploy");
		return createServer(deploy.toOSString(), tmpDeploy.toOSString());
	}
	
	protected void clearStateLocation() {
		IPath state = ASTest.getDefault().getStateLocation();
		if( state.toFile().exists()) {
			File[] children = state.toFile().listFiles();
			for( int i = 0; i < children.length; i++ ) {
				FileUtil.safeDelete(children[i]);
			}
		}
	}
	
	protected String getDeployRoot(IServer server) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		return ds.getDeployFolder();
	}
	
	protected IServer createServer(String deployLocation, String tempDeployLocation) throws CoreException {
		IRuntimeType rt = ServerCore.findRuntimeType("org.jboss.ide.eclipse.as.runtime.stripped");
		IRuntimeWorkingCopy wc = rt.createRuntime("testRuntime", null);
		IRuntime runtime = wc.save(true, null);
		IServerType st = ServerCore.findServerType("org.jboss.ide.eclipse.as.systemCopyServer");
		ServerWorkingCopy swc = (ServerWorkingCopy) st.createServer("testServer", null, null);
		swc.setServerConfiguration(null);
		swc.setName("testServer");
		swc.setRuntime(runtime);
		swc.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deployLocation);
		swc.setAttribute(DeployableServer.TEMP_DEPLOY_DIRECTORY, tempDeployLocation);
		IServer server = swc.save(true, null);
		return server;
	}

	protected IStatus publish() throws CoreException {
		return server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
	}
	
	protected void addModule(IModule module) throws CoreException  {
		IServerWorkingCopy copy = server.createWorkingCopy();
		copy.modifyModules(new IModule[]{module}, new IModule[0], new NullProgressMonitor());
		server = copy.save(false, new NullProgressMonitor());
	}

	protected void removeModule(IModule module) throws CoreException  {
		IServerWorkingCopy copy = server.createWorkingCopy();
		copy.modifyModules(new IModule[]{}, new IModule[] {module}, new NullProgressMonitor());
		server = copy.save(false, new NullProgressMonitor());
	}

	protected void verifyJSTPublisher(IModule[] module) {
		IJBossServerPublisher publisher = ExtensionManager
			.getDefault().getPublisher(server, module, "local");
		assertTrue(publisher instanceof JstPublisher);
	}
	
	public int countFiles(File root) {
		int count = 0;
		if( !root.isDirectory() )
			return 1;
		File[] children = root.listFiles();
		for( int i = 0; i < children.length; i++ ) 
			count += countFiles(children[i]);
		return count;
	}
	
	public int countAllResources(File root) {
		int count = 0;
		if( !root.isDirectory() )
			return 1;
		File[] children = root.listFiles();
		for( int i = 0; i < children.length; i++ ) 
			count += countFiles(children[i]);
		return 1 + count;
	}
	
	

	protected void setContents(IFile file, int val) throws IOException , CoreException{
		setContents(file, "" + val);
	}
	
	protected void setContents(IFile file, String val) throws IOException , CoreException{
		if( !file.exists()) 
			file.create(new ByteArrayInputStream((val).getBytes()), false, null);
		else
			file.setContents(new ByteArrayInputStream((val).getBytes()), false, false, new NullProgressMonitor());
		try {
			Thread.sleep(2000);
		} catch( InterruptedException ie) {}
		JobUtils.waitForIdle(); 
	}
	
	protected void assertContents(IFile file, int val) throws IOException, CoreException {
		assertContents(file, "" + val);
	}
	
	protected void assertContents(IFile file, String val) throws IOException, CoreException {
		String contents = getContents(file);
		assertEquals(val, contents);
	}

	protected void assertContents(File file, int val) throws IOException, CoreException {
		assertContents(file, "" + val);
	}
	
	protected void assertContents(File file, String val) throws IOException, CoreException {
		String contents = getContents(file);
		assertEquals(val, contents);
	}

	protected String getContents(IFile file) throws IOException, CoreException  {
		BufferedInputStream bis = new BufferedInputStream(file.getContents());
        int l = (int)file.getLocation().toFile().length();
        byte[] bs = new byte[l];
        l = bis.read(bs, 0, l);
        bis.close();
        return new String(bs);
	}
	
	public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        byte[] bytes = new byte[(int)file.length()];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        is.close();
        return bytes;
    }
	
	static public String getContents(File aFile) throws IOException {
		return new String(getBytesFromFile(aFile));
	}

	
	public void testMain() throws CoreException, IOException {
		IModule mod = ServerUtil.getModule(project);
		IModule[] module = new IModule[] { mod };
		verifyJSTPublisher(module);
		addModule(mod);
		publish();
		IPath deployRoot = new Path(getDeployRoot(server));
		IPath rootFolder = deployRoot.append(MODULE_NAME + ".ear");
		assertTrue(rootFolder.toFile().exists());
		assertTrue(countFiles(rootFolder.toFile()) == 0);
		assertTrue(countAllResources(rootFolder.toFile()) == 1);
		IFile textFile = project.getFile(CONTENT_TEXT_FILE);
		setContents(textFile, 0);
		assertEquals(countFiles(rootFolder.toFile()), 0);
		assertTrue(countAllResources(rootFolder.toFile()) == 1);
		publish();
		assertEquals(countFiles(rootFolder.toFile()), 1);
		assertTrue(countAllResources(rootFolder.toFile()) == 2);
		assertContents(rootFolder.append(TEXT_FILE).toFile(), 0);
		setContents(textFile, 1);
		publish();
		assertContents(rootFolder.append(TEXT_FILE).toFile(), 1);
		textFile.delete(true, null);
		assertEquals(countFiles(rootFolder.toFile()), 1);
		assertTrue(countAllResources(rootFolder.toFile()) == 2);
		publish();
		assertEquals(countFiles(rootFolder.toFile()), 0);
		assertTrue(countAllResources(rootFolder.toFile()) == 1);
		removeModule(mod);
		assertTrue(rootFolder.toFile().exists());
		publish();
		assertFalse(rootFolder.toFile().exists());
	}
}
