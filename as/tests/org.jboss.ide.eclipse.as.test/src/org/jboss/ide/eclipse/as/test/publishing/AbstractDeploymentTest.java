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
import org.jboss.tools.common.test.util.TestProjectProvider;
import org.osgi.framework.Bundle;

public abstract class AbstractDeploymentTest extends TestCase {
	protected String BUNDLE_NAME = "org.jboss.ide.eclipse.as.test";
	private TestProjectProvider provider;
	protected IProject workspaceProject;
	protected String sourceProjectName;
	protected String testProperties;
	protected IRuntime runtime;
	protected IServer server;
	
	public AbstractDeploymentTest(String projectName, String testProperties) {
		this.sourceProjectName = projectName;
		this.testProperties = testProperties;
	}
	
	protected void setUp() throws Exception {
		cleanFolder(getProjectLocation("TempProject").getAbsolutePath());
		assembleInTempProject();
		createServer();
		String path = "/projects/TempProject/" + sourceProjectName;
		provider = new TestProjectProvider(BUNDLE_NAME, path, sourceProjectName, true); 
		workspaceProject = provider.getProject();
		workspaceProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
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
	
	protected void assembleInTempProject() throws CoreException {
		File tempProject = getProjectLocation("TempProject");
		File srcProject = getProjectLocation(sourceProjectName);
		File destProject = new File(tempProject, sourceProjectName);
		FileUtil.fileSafeCopy(srcProject, destProject);
		
		// now copy files over from the properties file
		File propertiesFile = getFileLocation("projectPieces/" + testProperties);
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(propertiesFile));
			boolean done = false;
			String srcKey, destKey;
			int i = 1;
			while( !done ) {
				srcKey = "copy" + i + "src";
				destKey = "copy" + i + "dest";
				done = copy(props.getProperty(srcKey), props.getProperty(destKey));
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected boolean copy(String src, String dest) throws CoreException {
		if( src == null || dest == null ) 
			return true;
		
		// do the copy
		File srcFile, destFile, tmp;
		srcFile = getFileLocation("projectPieces/" + src);
		tmp = getFileLocation("projects/TempProject/" + sourceProjectName);
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
		swc.setAttribute(DeployableServer.DEPLOY_DIRECTORY, getFileLocation("/testOutputs").getAbsolutePath());
		server = swc.save(true, null);
	}
	
	protected IStatus publish(IModule module) throws CoreException {
		IServerWorkingCopy copy = server.createWorkingCopy();
		copy.modifyModules(new IModule[]{module}, new IModule[0], new NullProgressMonitor());
		server = copy.save(false, new NullProgressMonitor());
		return server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
	}

	protected void tearDown() throws Exception {
		provider.dispose();
		cleanFolder(getProjectLocation("TempProject").getAbsolutePath());
		cleanFolder(getFileLocation("testOutputs"));
		runtime.delete();
		server.delete();
	}
	
}
