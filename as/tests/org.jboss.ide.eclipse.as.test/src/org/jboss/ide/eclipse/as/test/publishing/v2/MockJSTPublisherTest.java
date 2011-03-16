package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class MockJSTPublisherTest extends AbstractJSTDeploymentTester {
	public void testNormalLogic() throws CoreException, IOException {
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		MockPublishMethod.HANDLER.reset();
		theTest(2,1, "");
	}

	public void testForced7Logic() throws CoreException, IOException {
		server = ServerRuntimeUtils.createMockJBoss7Server();
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		MockPublishMethod.HANDLER.reset();
		theTest(3,1, "newModule.ear.isDeployed");
	}
	
	private void theTest(int initialPublish, int remove, String relativePath) throws CoreException, IOException {
		
		IModule mod = ServerUtil.getModule(project);
		IModule[] module = new IModule[] { mod };
		server = ServerRuntimeUtils.addModule(server,mod);
		ServerRuntimeUtils.publish(server);
		// one additional for doDeploy
		assertEquals(initialPublish, MockPublishMethod.HANDLER.getChanged().length);
		MockPublishMethod.HANDLER.reset();
		
		IFile textFile = project.getFile(CONTENT_TEXT_FILE);
		IOUtil.setContents(textFile, 0);
		assertEquals(0, MockPublishMethod.HANDLER.getChanged().length);
		ServerRuntimeUtils.publish(server);
		assertEquals(2, MockPublishMethod.HANDLER.getChanged().length);
		MockPublishMethod.HANDLER.reset();
		IOUtil.setContents(textFile, 1);
		ServerRuntimeUtils.publish(server);
		assertEquals(2, MockPublishMethod.HANDLER.getChanged().length);
		MockPublishMethod.HANDLER.reset();
		textFile.delete(true, null);
		ServerRuntimeUtils.publish(server);
		assertEquals(1, MockPublishMethod.HANDLER.getRemoved().length);
		MockPublishMethod.HANDLER.reset();

		server = ServerRuntimeUtils.removeModule(server, mod);
		assertEquals(0, MockPublishMethod.HANDLER.getRemoved().length);
		
		// Still just one delete, but should be the .deployed file
		ServerRuntimeUtils.publish(server);
		assertEquals(remove, MockPublishMethod.HANDLER.getRemoved().length);
	}

}
