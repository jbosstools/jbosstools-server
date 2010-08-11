package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;
import org.jboss.tools.test.util.JobUtils;

public class JSTZippedDeploymentTester extends TestCase {
	
	IProject project;
	IServer server;
	final String MODULE_NAME = "newModule";
	final String CONTENT_DIR = "contentDirS"; 
	final String TEXT_FILE = "test.txt";
	final IPath CONTENT_TEXT_FILE = new Path(CONTENT_DIR).append(TEXT_FILE);
	public void setUp() throws Exception {
		project = createProject();
		server = createServer();
		IServerWorkingCopy wc = server.createWorkingCopy();
		ServerAttributeHelper helper = new ServerAttributeHelper(server, wc);
		helper.setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, true);
		server = wc.save(true, new NullProgressMonitor());
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
		
		File srcFile = AbstractDeploymentTest.getFileLocation("projectPieces/mvel2.jar");
		IPath contentDir = p.getFolder(CONTENT_DIR).getLocation();
		File destFile = new File(contentDir.toFile(), "mvel2.jar");
		FileUtil.fileSafeCopy(srcFile, destFile);
		p.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
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
		wc.setLocation(ASTest.getDefault().getStateLocation());
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

	protected void verifyJSTZippedPublisher(IModule[] module) {
		IJBossServerPublisher publisher = ExtensionManager
			.getDefault().getPublisher(server, module, "local");
		assertTrue(publisher.getClass().getName().contains("WTPZippedPublisher"));
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
		addModule(mod);
		publish();
		IPath projLoc = project.getLocation();
		System.out.println(projLoc);
		IPath deployRoot = new Path(getDeployRoot(server));
		IPath zipped = deployRoot.append(MODULE_NAME + ".ear");
		assertTrue(zipped.toFile().exists());
		assertTrue(zipped.toFile().isFile());
		IPath unzip1 = ASTest.getDefault().getStateLocation().append("unzip1");
		IPath unzip2 = ASTest.getDefault().getStateLocation().append("unzip2");
		unzipFile(zipped,unzip1);
		assertTrue(unzip1.toFile().list().length == 1);
		unzipFile(unzip1.append("mvel2.jar"), unzip2);
		assertTrue(unzip2.toFile().list().length > 1);
		System.out.println("end");
		
		removeModule(mod);
		publish();
		assertFalse(zipped.toFile().exists());
	}
	
	
	public static void unzipFile(IPath zipped, IPath toLoc) {
		toLoc.toFile().mkdirs();
		final int BUFFER = 2048;
		try {
			  BufferedOutputStream dest = null;
		      FileInputStream fis = new 
		 	  FileInputStream(zipped.toFile());
			  ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
	          ZipEntry entry;
	          while((entry = zis.getNextEntry()) != null) {
	             int count;
	             byte data[] = new byte[BUFFER];
	             // write the files to the disk
	             toLoc.append(entry.getName()).toFile().getParentFile().mkdirs();
	             if( !toLoc.append(entry.getName()).toFile().exists()) {
		             FileOutputStream fos = new FileOutputStream(toLoc.append(entry.getName()).toOSString());
		             dest = new BufferedOutputStream(fos, BUFFER);
		             while ((count = zis.read(data, 0, BUFFER)) != -1) {
		                dest.write(data, 0, count);
		             }
		             dest.flush();
		             dest.close();
	             }
	          }
	          zis.close();
	       } catch(Exception e) {
	          e.printStackTrace();
	       }
	}
}
