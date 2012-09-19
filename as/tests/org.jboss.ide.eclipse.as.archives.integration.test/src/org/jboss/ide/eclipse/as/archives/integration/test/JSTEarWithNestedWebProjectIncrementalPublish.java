package org.jboss.ide.eclipse.as.archives.integration.test;

import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.publishing.v2.MockPublishMethod;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;

public class JSTEarWithNestedWebProjectIncrementalPublish extends
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
	private IProject earProj, webProj;
	public void testZippedPublish() throws CoreException, IOException, Exception {
		server = ServerRuntimeUtils.createMockDeployOnlyServer();
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		setZipFlag();
		MockPublishMethod.reset();
		
		
		IDataModel dm = ProjectCreationUtil.getEARDataModel("EAR", "EarContent",  null, null, JavaEEFacetConstants.EAR_5, false);
		OperationTestCase.runAndVerify(dm);
		earProj = ResourcesPlugin.getWorkspace().getRoot().getProject("EAR");
		assertTrue(earProj.exists());
		
		IDataModel dm2 = ProjectCreationUtil.getWebDataModel("Web", "EAR", null, null, null, JavaEEFacetConstants.WEB_24, false);
		OperationTestCase.runAndVerify(dm2);
		webProj = ResourcesPlugin.getWorkspace().getRoot().getProject("Web");
		assertTrue(webProj.exists());
		
		IModule mod = ServerUtil.getModule(earProj);
		server = ServerRuntimeUtils.addModule(server, mod);
		ServerRuntimeUtils.publish(server);

		ArrayList<IPath> changed = MockPublishMethod.changed;
		assertTrue(changed.size() == 1);
		MockPublishMethod.reset();
		
		IFile textFile = webProj.getFile(new Path("WebContent").append("out.html"));
		IOUtil.setContents(textFile, 0);
		
		ServerRuntimeUtils.publish(server);
		changed = MockPublishMethod.changed;
		assertTrue(changed.size() == 1);
		MockPublishMethod.reset();
	}
	
	private void setZipFlag() throws IOException, CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		ServerAttributeHelper helper = new ServerAttributeHelper(server, wc);
		helper.setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, true);
		server = wc.save(true, new NullProgressMonitor());
	}
	
	

}
