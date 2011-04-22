package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7Server;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class MockJSTPublisherTest extends AbstractJSTDeploymentTester {
	public void testNormalLogic() throws CoreException, IOException {
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		MockPublishMethod.reset();
		theTest(2,1, "");
	}

	public void testForced7Logic() throws CoreException, IOException {
		server = ServerRuntimeUtils.createMockJBoss7Server();
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		MockPublishMethod.reset();
		theTest(3,1, "newModule.ear" + JBoss7JSTPublisher.DEPLOYED);
	}
	
	private void theTest(int initialPublish, int remove, String relativePath) throws CoreException, IOException {
		
		IModule mod = ServerUtil.getModule(project);
		IModule[] module = new IModule[] { mod };
		server = ServerRuntimeUtils.addModule(server,mod);
		ServerRuntimeUtils.publish(server);
		// one additional for doDeploy
		assertEquals(initialPublish, MockPublishMethod.getChanged().length);
		MockPublishMethod.reset();
		
		IFile textFile = project.getFile(CONTENT_TEXT_FILE);
		IOUtil.setContents(textFile, 0);
		assertEquals(0, MockPublishMethod.getChanged().length);
		ServerRuntimeUtils.publish(server);
		assertEquals(2, MockPublishMethod.getChanged().length);
		MockPublishMethod.reset();
		IOUtil.setContents(textFile, 1);
		ServerRuntimeUtils.publish(server);
		assertEquals(2, MockPublishMethod.getChanged().length);
		MockPublishMethod.reset();
		textFile.delete(true, null);
		ServerRuntimeUtils.publish(server);
		assertEquals(1, MockPublishMethod.getRemoved().length);
		MockPublishMethod.reset();

		server = ServerRuntimeUtils.removeModule(server, mod);
		assertEquals(0, MockPublishMethod.getRemoved().length);
		
		// Still just one delete, but should be the .deployed file
		ServerRuntimeUtils.publish(server);
		assertEquals(remove, MockPublishMethod.getRemoved().length);
	}

}
