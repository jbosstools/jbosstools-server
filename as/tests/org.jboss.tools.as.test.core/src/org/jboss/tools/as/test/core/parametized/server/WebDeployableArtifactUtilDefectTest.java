package org.jboss.tools.as.test.core.parametized.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.internal.web.deployables.WebDeployableArtifactUtil;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.util.WebResource;
import org.jboss.ide.eclipse.as.core.server.internal.JBossLaunchAdapter;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.internal.utils.wtp.OperationTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class WebDeployableArtifactUtilDefectTest extends TestCase {
	@Parameters
	public static Collection<Object[]> data() {
		ArrayList<Object[]> options = new ArrayList<Object[]>();
		Collection<Object[]> all = ServerParameterUtils.asCollection(ServerParameterUtils.getAllJBossServerTypeParamterers());
		options.addAll(all);
		// add a null option for no server
		options.add(new Object[]{null});
		return options;
	}
	
	private String serverType;
	public WebDeployableArtifactUtilDefectTest(String serverType) {
		this.serverType  = serverType;
	}

	@Test
	public void testWebDeployableDefectWorkaround() throws Exception {
		IFile f = createProjectAndGetJavaIFile();
		if( serverType == null ) {
			testWebDeployableDefect(f);
		} else {
			webDeployableDefectWorkaroundForServer(f);
		}
	}

	private void testWebDeployableDefect(IResource f) throws Exception {
		IModuleArtifact artifact = new WebDeployableArtifactUtil().getModuleObject(f);
		if( artifact != null && artifact instanceof WebResource ) {
			// should be fixed by now
			assertFalse(((WebResource)artifact).getPath().lastSegment().endsWith(".java"));
		}
	}

	private void webDeployableDefectWorkaroundForServer(IResource f) throws Exception {
		IServer server = ServerCreationTestUtils.createServerWithRuntime(serverType, serverType);
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

	
	private IFile createProjectAndGetJavaIFile() throws Exception  {
		IDataModel dm = CreateProjectOperationsUtility.getWebDataModel("TestWeb", null, null, null, null, JavaEEFacetConstants.WEB_25, true);
		OperationTestCase.runAndVerify(dm);
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject("TestWeb");
		assertTrue(p.exists());
		final IFile[] file = new IFile[1];
		file[0] = null;
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				file[0] = ResourceUtils.createJavaType(p, new Path("src/my/pack"), "my.pack", "Tiger");
				assertTrue(file[0].exists());
				p.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
			}
		}, new NullProgressMonitor());
		return file[0];
	}

	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
	}
}
