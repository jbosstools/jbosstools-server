package org.jboss.ide.eclipse.as.test.publishing.v2;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.BehaviourModel;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.test.server.SimpleServerImplTest;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class BehaviourModelDefectTest extends SimpleServerImplTest {
	
	public void serverTestImpl(String type) {
		IServer server = ServerRuntimeUtils.createMockServerWithRuntime(type, "server1", "default");
		serverTestBehaviourImpl(server);
		serverTestBehaviourImplLaunches(server);
	}
	public void serverTestBehaviourImpl(IServer server) {
		// all should have a publish method
		String serverType = server.getServerType().getId();
		String behaviourType = DeploymentPreferenceLoader.getCurrentDeploymentMethodTypeId(server);
		assertNotNull(behaviourType);
		assertNotNull(BehaviourModel.getModel().getBehaviour(serverType).getImpl(behaviourType));
		assertNotNull(BehaviourModel.getModel().getBehaviour(serverType).getImpl(behaviourType).createPublishMethod());
	}
	
	public void serverTestBehaviourImplLaunches(IServer server) {
		String serverType = server.getServerType().getId();
		String behaviourType = DeploymentPreferenceLoader.getCurrentDeploymentMethodTypeId(server);
		assertNotNull(behaviourType);
		assertNotNull(BehaviourModel.getModel().getBehaviour(serverType).getImpl(behaviourType).createLaunchDelegate());
	}
	
	public void testDeployOnlyBehaviourModel() throws CoreException {
		IServer server = ServerRuntimeUtils.createMockDeployOnlyServer();
		String serverType = server.getServerType().getId();
		String behaviourType = DeploymentPreferenceLoader.getCurrentDeploymentMethodTypeId(server);
		assertNull(behaviourType);
		assertNotNull(BehaviourModel.getModel().getBehaviour(serverType).getImpl("local"));
		assertNotNull(BehaviourModel.getModel().getBehaviour(serverType).getImpl("local").createPublishMethod());
		assertNull(BehaviourModel.getModel().getBehaviour(serverType).getImpl("local").createLaunchDelegate());
	}
}
