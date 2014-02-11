package org.jboss.tools.as.test.core.subsystems;

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class PublishSubsystemResolutionTest extends TestCase {
	private String serverType;
	private String mode;
	private IServer server;
	@Parameters
	public static Collection<Object[]> data() {
		String[] servers = ServerParameterUtils.getAllJBossServerTypeParameters();
		String[] modes = new String[]{"local", "rse", null};
		return MatrixUtils.toMatrix(new Object[][]{servers, modes});
	}
	 
	public PublishSubsystemResolutionTest(String serverType, String mode) {
		this.serverType = serverType;
		this.mode = mode;
	}
	
	@Before
	public void setUp() throws Exception {
		server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, getClass().getName() + serverType);
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(IDeployableServer.SERVER_MODE, mode);
		server = wc.save(false, new NullProgressMonitor());
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
	
	
	@Test
	public void testResolution() throws Exception {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(server);
		assertNotNull(beh);
		ISubsystemController controller = beh.getController(IPublishController.SYSTEM_ID);
		assertNotNull(controller);
		assertEquals(controller.getSubsystemMappedId(), "publish.filesystem.default");
	}
}