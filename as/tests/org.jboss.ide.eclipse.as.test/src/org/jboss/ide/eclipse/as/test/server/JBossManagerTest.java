/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.test.server;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerServiceProxy;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

public class JBossManagerTest {

	@Test
	public void canUseService() throws JBoss7ManangerException, InvalidSyntaxException {
		BundleContext context = ASTest.getContext();
		JBoss7ManagerServiceProxy serviceProxy = new JBoss7ManagerServiceProxy(context,
				IJBoss7ManagerService.AS_VERSION_700);
		serviceProxy.open();
		IJBoss7ManagerService manager = serviceProxy.getService();
		assertNotNull(manager);
	}

	@Test
	public void canUseServiceEvenIfAlternativeIsRegistered() throws JBoss7ManangerException, InvalidSyntaxException {
		BundleContext context = ASTest.getDefault().getBundle().getBundleContext();
		registerFakeASService("710");
		JBoss7ManagerServiceProxy serviceProxy =
				new JBoss7ManagerServiceProxy(context, IJBoss7ManagerService.AS_VERSION_700);
		serviceProxy.open();
		IJBoss7ManagerService manager = serviceProxy.getService();
		assertNotNull(manager);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void canUseAlternative() throws Exception {
		BundleContext context = ASTest.getDefault().getBundle().getBundleContext();
		registerFakeASService("710");
		JBoss7ManagerServiceProxy managerProxy =
				new JBoss7ManagerServiceProxy(context, "710");
		managerProxy.open();
		IJBoss7ManagerService manager = managerProxy.getService();
		assertNotNull(manager);
		manager.getDeploymentState(new MockAS7ManagementDetails("fake", 4242), "fake");
	}

	private void registerFakeASService(String version) {
		Dictionary<String, String> serviceProperties = new Hashtable<String, String>();
		serviceProperties.put(IJBoss7ManagerService.AS_VERSION_PROPERTY, version);
		ASTest.getContext().registerService(IJBoss7ManagerService.class, new JBoss71Manager(),
				serviceProperties);
	}

	private static class JBoss71Manager implements IJBoss7ManagerService {

		public String execute(IAS7ManagementDetails details, String request) throws Exception {
			throw new UnsupportedOperationException();
		}
		public IJBoss7DeploymentResult deployAsync(IAS7ManagementDetails details, String deploymentName, File file,
				IProgressMonitor monitor) throws JBoss7ManangerException {
			throw new UnsupportedOperationException();
		}

		public IJBoss7DeploymentResult deploySync(IAS7ManagementDetails details, String deploymentName, File file,
				IProgressMonitor monitor) throws JBoss7ManangerException {
			throw new UnsupportedOperationException();
		}

		public IJBoss7DeploymentResult undeployAsync(IAS7ManagementDetails details, String deploymentName, boolean removeFile,
				IProgressMonitor monitor) throws JBoss7ManangerException {
			throw new UnsupportedOperationException();
		}

		public IJBoss7DeploymentResult syncUndeploy(IAS7ManagementDetails details, String deploymentName, boolean removeFile,
				IProgressMonitor monitor) throws JBoss7ManangerException {
			throw new UnsupportedOperationException();
		}

		public JBoss7DeploymentState getDeploymentState(IAS7ManagementDetails details, String deploymentName)
				throws JBoss7ManangerException {
			throw new UnsupportedOperationException();
		}
		
		public JBoss7ServerState getServerState(IAS7ManagementDetails details) throws Exception {
			throw new UnsupportedOperationException();
		}

		public boolean isRunning(IAS7ManagementDetails details) {
			throw new UnsupportedOperationException();
		}

		public void stop(IAS7ManagementDetails details) throws JBoss7ManangerException {
		}

		public void dispose() {
		}

		public void init() throws Exception {
		}
	}
	
	public static class MockAS7ManagementDetails extends AS7ManagementDetails {
		private String host;
		private int port;
		public MockAS7ManagementDetails(String host, int port) {
			super(null);
			this.host = host;
			this.port = port;
		}
		public String getHost() {
			return host;
		}
		
		public int getManagementPort() {
			return port;
		}
		public String[] handleCallbacks(String[] prompts) throws UnsupportedOperationException {
			return new String[]{};
		}
	}
}
