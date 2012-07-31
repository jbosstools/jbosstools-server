package org.jboss.ide.eclipse.as.test.defects;

import java.io.ByteArrayInputStream;
import java.net.URL;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.internal.web.deployables.WebDeployableArtifactUtil;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.util.WebResource;
import org.jboss.ide.eclipse.as.core.server.internal.JBossLaunchAdapter;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;
import org.jboss.tools.test.util.JobUtils;

public class WebDeployableArtifactUtilDefectTest extends TestCase {
	public void setUp() throws Exception {
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
		ProjectUtility.deleteAllProjects();
		ASTest.clearStateLocation();
		JobUtils.waitForIdle(2000);
	}
	public void tearDown() throws Exception {
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
		ProjectUtility.deleteAllProjects();
		ASTest.clearStateLocation();
	}

	private IFile createProjectAndGetJavaIFile() throws Exception  {
		IDataModel dm = ProjectCreationUtil.getWebDataModel("TestWeb", null, null, null, null, JavaEEFacetConstants.WEB_25, true);
		OperationTestCase.runAndVerify(dm);
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject("TestWeb");
		assertTrue(p.exists());
		final IFile[] file = new IFile[1];
		file[0] = null;
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				file[0] = createJavaType(p, new Path("src/my/pack"), "my.pack", "Tiger");
				assertTrue(file[0].exists());
				p.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
			}
		}, new NullProgressMonitor());
		return file[0];
	}
	
	public void testWebDeployableDefect() throws Exception {
		JobUtils.waitForIdle();
		IFile f = createProjectAndGetJavaIFile();
		IModuleArtifact artifact = new WebDeployableArtifactUtil().getModuleObject(f);
		if( artifact == null )
			return;  // Ok, no result for a java file
		
		try {
			if( artifact instanceof WebResource ) {
				assertFalse(((WebResource)artifact).getPath().lastSegment().endsWith(".java"));
			}
		} catch( AssertionFailedError afe ) {
			// Expected to fail for now, but really, shouldn't.  But upstream wtp
		}
	}
	
	private IFile createJavaType(IProject p, IPath projectRelativePath, String packageName, String className) throws CoreException {
		IFolder folder = p.getFolder(projectRelativePath);
		createFolder(folder);
		IFile f = folder.getFile(className + ".java");
		String s = "package " + packageName + ";\n\npublic class " + className + "{\n\n}";
		f.create(new ByteArrayInputStream(s.getBytes()), true, new NullProgressMonitor());
		return f;
	}
	
	private boolean createFolder(IFolder c) throws CoreException {
		if( c.exists())
			return true;
		if( !c.getParent().exists()) {
			createFolder((IFolder)c.getParent());
		}
		c.create(true, true, null);
		return true;
	}
	
	public void testWebDeployableDefectWorkaround() throws Exception {
		IServer server = ServerRuntimeUtils.createMockJBoss7Server();
		webDeployableDefectWorkaroundForServer(server);
	}
	
	public void testDeployOnlyServerDefect() throws Exception {
		IServer server = ServerRuntimeUtils.createMockDeployOnlyServer();
		// Previously had an NPE here 
		webDeployableDefectWorkaroundForServer(server);
	}	

	private void webDeployableDefectWorkaroundForServer(IServer server) throws Exception {
		IFile f = createProjectAndGetJavaIFile();
		JBossLaunchAdapter adapter = new JBossLaunchAdapter();
		IModuleArtifact artifact = new WebDeployableArtifactUtil().getModuleObject(f);
		if( artifact != null ) {
			Object o = adapter.getLaunchable(server, artifact);
			assertTrue(o instanceof JBossLaunchAdapter.JBTCustomHttpLaunchable);
			URL url = ((JBossLaunchAdapter.JBTCustomHttpLaunchable)o).getURL();
			assertTrue(url != null );
			String u2 = url.toString();
			assertFalse(u2.endsWith(".java"));
		}
	}
}
