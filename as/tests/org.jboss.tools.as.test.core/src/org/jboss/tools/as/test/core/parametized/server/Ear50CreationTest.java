package org.jboss.tools.as.test.core.parametized.server;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ProjectRuntimeUtil;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.internal.utils.wtp.OperationTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class Ear50CreationTest extends Assert {
	private static String PROJECT_PREFIX = "a1Ear";
	private static int PROJECT_ID = 1;

	 @Parameters
	 public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParamterers());
	 }
	 
	 private String serverType;
	 public Ear50CreationTest(String serverType) {
		 this.serverType = serverType;
	 }
	 /* Verify all servers >  42 do NOT create the application.xml file */
	@Test
	public void testApplicationXML() throws Exception {
		String projectName = PROJECT_PREFIX + PROJECT_ID;
		IServer server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, "server" + PROJECT_ID);
		PROJECT_ID++;
		
		IDataModel dm = CreateProjectOperationsUtility.getEARDataModel(projectName, "contentDirS", null, null, JavaEEFacetConstants.EAR_5, false);
		dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, RuntimeManager.getRuntime(server.getRuntime().getId()));
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject("newModule");
		assertTrue(p.exists());
		assertNotNull(ProjectRuntimeUtil.getRuntime(p));
		IFile f = p.getFile(new Path("contentDirS").append("META-INF").append("application.xml"));
		assertEquals(shouldExist(server), f.exists());
	}
	
	private boolean shouldExist(IServer server) {
		String typeId = server.getServerType().getId();
		if( IJBossToolingConstants.SERVER_AS_32.equals(typeId))
			return true;
		if( IJBossToolingConstants.SERVER_AS_40.equals(typeId))
			return true;
		if( IJBossToolingConstants.SERVER_AS_42.equals(typeId))
			return true;
		
		return false;
	}

	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
	}
}
