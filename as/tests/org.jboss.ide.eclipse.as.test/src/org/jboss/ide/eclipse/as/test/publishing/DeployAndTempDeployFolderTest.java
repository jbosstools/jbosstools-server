package org.jboss.ide.eclipse.as.test.publishing;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;

public class DeployAndTempDeployFolderTest extends TestCase {
	public void tearDown() throws Exception {
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
		ProjectUtility.deleteAllProjects();
		ASTest.clearStateLocation();
	}

	private IDeployableServer initAS7Server(String type) throws CoreException {
		IServer s = ServerRuntimeUtils.createMockJBoss7Server();
		IServerWorkingCopy wc = s.createWorkingCopy();
		wc.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, type);
		s = wc.save(true, null);
		IDeployableServer ds = ServerConverter.getDeployableServer(s);
		return ds;
	}
	
	public void testAS7xServer() throws CoreException {
		IDeployableServer ds = initAS7Server(IDeployableServer.DEPLOY_SERVER);
		assertEquals("/standalone/deployments", ds.getDeployFolder());
		assertEquals("/standalone/tmp", ds.getTempDeployFolder());
	}

	public void testAS7xMetadata() throws CoreException {
		IDeployableServer ds = initAS7Server(IDeployableServer.DEPLOY_METADATA);
		assertTrue(ds.getDeployFolder().endsWith("org.jboss.ide.eclipse.as.core/org.jboss.ide.eclipse.as.70/deploy"));
		assertTrue(ds.getTempDeployFolder().endsWith("org.jboss.ide.eclipse.as.core/org.jboss.ide.eclipse.as.70/tempDeploy"));
	}
	
	public void testAS7xCustomGlobal() throws CoreException {
		IServer s = ServerRuntimeUtils.createMockJBoss7Server("/rtHome", "/home/test1", "/home/test2");
		IServerWorkingCopy wc = s.createWorkingCopy();
		wc.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, IDeployableServer.DEPLOY_CUSTOM);
		s = wc.save(true, null);
		IDeployableServer ds = ServerConverter.getDeployableServer(s);
		assertEquals("/home/test1",ds.getDeployFolder());
		assertEquals("/home/test2",ds.getTempDeployFolder());
	}

	public void testAS7xCustomRelative() throws CoreException {
		IServer s = ServerRuntimeUtils.createMockJBoss7Server("/rtHome", "home/test1", "home/test2");
		IServerWorkingCopy wc = s.createWorkingCopy();
		wc.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, IDeployableServer.DEPLOY_CUSTOM);
		s = wc.save(true, null);
		IDeployableServer ds = ServerConverter.getDeployableServer(s);
		assertEquals("/rtHome/home/test1",ds.getDeployFolder());
		assertEquals("/rtHome/home/test2",ds.getTempDeployFolder());
	}

	
}
