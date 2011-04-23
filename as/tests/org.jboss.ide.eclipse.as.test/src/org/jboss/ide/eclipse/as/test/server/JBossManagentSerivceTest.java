package org.jboss.ide.eclipse.as.test.server;

import static org.junit.Assert.assertNotNull;

import java.util.Dictionary;
import java.util.Hashtable;

import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7DeploymentManager;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7ManagementInterface;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7ManagementService;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManagentServiceProxy;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManangementException;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

public class JBossManagentSerivceTest {

	@Test
	public void canUseService() throws JBoss7ManangementException, InvalidSyntaxException {
		BundleContext context = ASTest.getContext();
		JBoss7ManagentServiceProxy serviceProxy = new JBoss7ManagentServiceProxy(context,
				IJBoss7ManagementService.AS_VERSION_700);
		serviceProxy.open();
		IJBoss7DeploymentManager manager = serviceProxy.getDeploymentManager();
		assertNotNull(manager);
	}

	@Test
	public void canUseServiceEvenIfAlternativeIsRegistered() throws JBoss7ManangementException, InvalidSyntaxException {
		BundleContext context = ASTest.getDefault().getBundle().getBundleContext();
		registerFakeASService("710");
		JBoss7ManagentServiceProxy serviceProxy = new JBoss7ManagentServiceProxy(context,
				IJBoss7ManagementService.AS_VERSION_700);
		serviceProxy.open();
		IJBoss7DeploymentManager manager = serviceProxy.getDeploymentManager();
		assertNotNull(manager);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void canUseAlternative() throws JBoss7ManangementException, InvalidSyntaxException {
		BundleContext context = ASTest.getDefault().getBundle().getBundleContext();
		registerFakeASService("710");
		JBoss7ManagentServiceProxy serviceProxy =
				new JBoss7ManagentServiceProxy(context, "710");
		serviceProxy.open();
		IJBoss7DeploymentManager manager = serviceProxy.getDeploymentManager();
		assertNotNull(manager);
	}

	private void registerFakeASService(String version) {
		Dictionary<String, String> serviceProperties = new Hashtable<String, String>();
		serviceProperties.put(IJBoss7ManagementService.AS_VERSION_PROPERTY, version);
		ASTest.getContext().registerService(IJBoss7ManagementService.class, new JBoss71ManagementService(),
				serviceProperties);
	}

	private static class JBoss71ManagementService implements IJBoss7ManagementService {

		@Override
		public IJBoss7DeploymentManager getDeploymentManager() throws JBoss7ManangementException {
			throw new UnsupportedOperationException();
		}

		@Override
		public IJBoss7ManagementInterface getManagementInterface() throws JBoss7ManangementException {
			throw new UnsupportedOperationException();
		}

	}
}
