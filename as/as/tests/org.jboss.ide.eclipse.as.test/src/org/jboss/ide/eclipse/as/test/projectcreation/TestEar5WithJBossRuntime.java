package org.jboss.ide.eclipse.as.test.projectcreation;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.ProjectRuntimeUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;

/**
 * This class tests the requirement that ear projects with a 
 * JBoss runtime < 5.0 are forced to create an application.xml. 
 * @author rob
 *
 */

public class TestEar5WithJBossRuntime extends TestCase {
	public void testEar5WithJBoss42() throws Exception {
		IServer server = ServerRuntimeUtils.createServer(IJBossToolingConstants.AS_42, 
				IJBossToolingConstants.SERVER_AS_42, ASTest.JBOSS_AS_42_HOME, ServerRuntimeUtils.DEFAULT_CONFIG);
		IDataModel dm = ProjectCreationUtil.getEARDataModel("newModule", "contentDirS", null, null, JavaEEFacetConstants.EAR_5, false);
		dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, RuntimeManager.getRuntime(server.getRuntime().getId()));
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject("newModule");
		assertTrue(p.exists());
		assertNotNull(ProjectRuntimeUtil.getRuntime(p));
		IFile f = p.getFile(new Path("contentDirS").append("META-INF").append("application.xml"));
		assertTrue(f.exists());
		ProjectUtility.deleteAllProjects();
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
	}

	public void testEar5WithJBoss50() throws Exception {
		IServer server = ServerRuntimeUtils.createServer(IJBossToolingConstants.AS_50, 
				IJBossToolingConstants.SERVER_AS_50, ASTest.JBOSS_AS_50_HOME, ServerRuntimeUtils.DEFAULT_CONFIG);
		IDataModel dm = ProjectCreationUtil.getEARDataModel("newModule2", "contentDirS", null, null, JavaEEFacetConstants.EAR_5, false);
		dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, RuntimeManager.getRuntime(server.getRuntime().getId()));
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject("newModule2");
		assertTrue(p.exists());
		assertNotNull(ProjectRuntimeUtil.getRuntime(p));
		IFile f = p.getFile(new Path("contentDirS").append("META-INF").append("application.xml"));
		assertFalse(f.exists());
		ProjectUtility.deleteAllProjects();
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
	}

	public void testEar5WithJBoss51() throws Exception {
		IServer server = ServerRuntimeUtils.createServer(IJBossToolingConstants.AS_51, 
				IJBossToolingConstants.SERVER_AS_51, ASTest.JBOSS_AS_51_HOME, ServerRuntimeUtils.DEFAULT_CONFIG);
		IDataModel dm = ProjectCreationUtil.getEARDataModel("newModule3", "contentDirS", null, null, JavaEEFacetConstants.EAR_5, false);
		dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, RuntimeManager.getRuntime(server.getRuntime().getId()));
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject("newModule3");
		assertTrue(p.exists());
		assertNotNull(ProjectRuntimeUtil.getRuntime(p));
		IFile f = p.getFile(new Path("contentDirS").append("META-INF").append("application.xml"));
		assertFalse(f.exists());
		ProjectUtility.deleteAllProjects();
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
	}

}
