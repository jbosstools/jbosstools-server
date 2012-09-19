package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;

public class StandaloneUtilProjectPublish extends
		TestCase {
	public void setUp() throws Exception {
	}
	public void tearDown() throws Exception {
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
		ProjectUtility.deleteAllProjects();
		ASTest.clearStateLocation();
	}

	private IServer server;
	private IProject utilProject;
	public void testZippedPublish() throws CoreException, IOException, Exception {
		server = ServerRuntimeUtils.createMockDeployOnlyServer();
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		MockPublishMethod.reset();
		
		
		IDataModel dm = ProjectCreationUtil.getUtilityProjectCreationDataModel("Util", null);
		OperationTestCase.runAndVerify(dm);
		utilProject = ResourcesPlugin.getWorkspace().getRoot().getProject("Util");
		assertTrue(utilProject.exists());
		
		IModule mod = ServerUtil.getModule(utilProject);
		server = ServerRuntimeUtils.addModule(server, mod);
		IStatus s = ServerRuntimeUtils.publish(server);
		assertTrue(s.isOK());
	}
	
	private void setZipFlag() throws IOException, CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		ServerAttributeHelper helper = new ServerAttributeHelper(server, wc);
		helper.setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, true);
		server = wc.save(true, new NullProgressMonitor());
	}
	
	

}
