package org.jboss.ide.eclipse.as.test.publishing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.tools.jmx.core.test.util.TestProjectProvider;
import org.osgi.framework.Bundle;

public abstract class AbstractDeploymentTest extends TestCase {
	protected String BUNDLE_NAME = "org.jboss.ide.eclipse.as.test";
	private TestProjectProvider[] provider;
	protected IProject[] workspaceProject;
	protected String[] sourceProjectName;
	protected String[] testProperties;
	protected IRuntime runtime;
	protected IServer server;
	protected String deployLocation;
	protected String tempDeployLocation;
	
	public AbstractDeploymentTest(String[] projectNames, String[] testProperties) {
		try {
			this.sourceProjectName = projectNames;
			this.testProperties = testProperties;
			File to = getFileLocation("/testOutputs");
			File f = new File(to, "inner");
			f.mkdirs();
			File fDeploy = new File(f, "1");
			File fTmpDeploy = new File(f, "2");
			fDeploy.mkdir();
			fTmpDeploy.mkdir();
			deployLocation = fDeploy.getAbsolutePath();
			tempDeployLocation = fDeploy.getAbsolutePath();
			this.provider = new TestProjectProvider[sourceProjectName.length];
			this.workspaceProject = new IProject[sourceProjectName.length];
		} catch( CoreException ce ) {
			fail("Could not access deploy location");
		}
	}
	public AbstractDeploymentTest(String projectName, String testProperties) {
		this(new String[]{projectName}, new String[] {testProperties});
	}
	
	protected void setUp() throws Exception {
		createServer();
		for( int i = 0; i < sourceProjectName.length; i++ ) {
			cleanFolder(getProjectLocation("TempProject").getAbsolutePath());
			assembleInTempProject(i);
			String path = "/projects/TempProject/" + sourceProjectName[i];
			provider[i] = new TestProjectProvider(BUNDLE_NAME, path, sourceProjectName[i], true); 
			workspaceProject[i] = provider[i].getProject();
			workspaceProject[i].refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}
	}
	
	protected void cleanFolder(String folder) throws CoreException {
		cleanFolder(new File(folder));
	}
	protected void cleanFolder(File folder) throws CoreException {
		if( folder != null && folder.exists() ) {
			File[] children = folder.listFiles();
			for( int i = 0; i < children.length; i++ ) 
				FileUtil.safeDelete(children[i]);
		}
	}

	protected File getProjectLocation(String name) throws CoreException {
		return getFileLocation("/projects/" + name);
	}
	
	protected File getFileLocation(String path) throws CoreException {
		Bundle bundle = Platform.getBundle(BUNDLE_NAME);
		URL url = null;
		try {
			url = FileLocator.resolve(bundle.getEntry(path));
		} catch (IOException e) {
			String msg = "Cannot find file " + path + " in " + BUNDLE_NAME;
			IStatus status = new Status(IStatus.ERROR, ASTest.PLUGIN_ID, msg, e);
			throw new CoreException(status);
		}
		String location = url.getFile();
		return new File(location);
	}
	
	
	/* 
	 * The whole reason for all this is so that I don't duplicate
	 * jars or archives all over the place and to keep the size
	 * of the test plugin small
	 */
	protected void assembleInTempProject(int index) throws CoreException {
		String sourceProjectName = this.sourceProjectName[index];
		File tempProject = getProjectLocation("TempProject");
		File srcProject = getProjectLocation(sourceProjectName);
		File destProject = new File(tempProject, sourceProjectName);
		FileUtil.fileSafeCopy(srcProject, destProject);
		
		// now copy files over from the properties file
		if( testProperties[index] != null ) {
			File propertiesFile = getFileLocation("projectPieces/" + testProperties[index]);
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(propertiesFile));
				boolean done = false;
				String srcKey, destKey;
				int i = 1;
				while( !done ) {
					srcKey = "copy" + i + "src";
					destKey = "copy" + i + "dest";
					done = copy(props.getProperty(srcKey), props.getProperty(destKey),i);
					i++;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected boolean copy(String src, String dest,int i) throws CoreException {
		if( src == null || dest == null ) 
			return true;
		
		// do the copy
		File srcFile, destFile, tmp;
		srcFile = getFileLocation("projectPieces/" + src);
		tmp = getFileLocation("projects/TempProject/" + sourceProjectName[i-1]);
		destFile = new File(tmp, dest);
		FileUtil.fileSafeCopy(srcFile, destFile);
		
		return false;
	}
	
	
	protected void createServer() throws CoreException {
		IRuntimeType rt = ServerCore.findRuntimeType("org.jboss.ide.eclipse.as.runtime.stripped");
		IRuntimeWorkingCopy wc = rt.createRuntime("testRuntime", null);
		runtime = wc.save(true, null);
		IServerType st = ServerCore.findServerType("org.jboss.ide.eclipse.as.systemCopyServer");
		ServerWorkingCopy swc = (ServerWorkingCopy) st.createServer("testServer", null, null);
		swc.setServerConfiguration(null);
		swc.setName("testServer");
		swc.setRuntime(runtime);
		swc.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deployLocation);
		swc.setAttribute(DeployableServer.TEMP_DEPLOY_DIRECTORY, tempDeployLocation);
		server = swc.save(true, null);
	}
	
	protected IStatus publish(IModule module) throws CoreException {
		IServerWorkingCopy copy = server.createWorkingCopy();
		copy.modifyModules(new IModule[]{module}, new IModule[0], new NullProgressMonitor());
		server = copy.save(false, new NullProgressMonitor());
		return server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
	}

	protected void tearDown() throws Exception {
		for( int i = 0; i < sourceProjectName.length; i++ ) 
			provider[i].dispose();

		cleanFolder(getProjectLocation("TempProject").getAbsolutePath());
		FileUtil.safeDelete(new File(
				getFileLocation("testOutputs"), "inner"));
		runtime.delete();
		server.delete();
	}	
}
