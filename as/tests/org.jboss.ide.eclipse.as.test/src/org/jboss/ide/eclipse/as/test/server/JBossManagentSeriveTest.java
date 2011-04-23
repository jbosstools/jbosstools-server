package org.jboss.ide.eclipse.as.test.server;

import static org.junit.Assert.assertNotNull;

import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7DeploymentManager;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7ManagementService;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManagentServiceProxy;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManangementException;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.junit.Test;
import org.osgi.framework.BundleContext;

public class JBossManagentSeriveTest {

	@Test
	public void canUseService() throws JBoss7ManangementException {
		BundleContext context = ASTest.getDefault().getBundle().getBundleContext();
		JBoss7ManagentServiceProxy serviceProxy = new JBoss7ManagentServiceProxy(context, IJBoss7ManagementService.AS_VERSION_700);
		serviceProxy.open();
		IJBoss7DeploymentManager manager = serviceProxy.getDeploymentManager();
		assertNotNull(manager);
	}
	
}
