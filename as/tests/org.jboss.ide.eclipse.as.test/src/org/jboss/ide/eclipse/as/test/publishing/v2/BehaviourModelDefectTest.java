package org.jboss.ide.eclipse.as.test.publishing.v2;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.BehaviourModel;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class BehaviourModelDefectTest extends TestCase {
	public void tearDown() {
		try {
			ServerRuntimeUtils.deleteAllServers();
			ServerRuntimeUtils.deleteAllRuntimes();
		} catch(CoreException ce) {}
	}
	
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
	
	public void test32BehaviourModel() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_32);
	}
	public void test40BehaviourModel() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_40);
	}
	public void test42BehaviourModel() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_42);
	}
	public void test50BehaviourModel() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_50);
	}
	public void test51BehaviourModel() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_51);
	}
	public void test60BehaviourModel() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_60);
	}
	public void test70BehaviourModel() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_70);
	}
	public void test71BehaviourModel() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_71);
	}
	public void testEap43BehaviourModel() {
		serverTestImpl(IJBossToolingConstants.SERVER_EAP_43);
	}	
	public void testEap50BehaviourModel() {
		serverTestImpl(IJBossToolingConstants.SERVER_EAP_50);
	}
	public void testEap60BehaviourModel() {
		serverTestImpl(IJBossToolingConstants.SERVER_EAP_60);
	}

}
