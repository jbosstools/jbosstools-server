/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.management.itests;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerServiceProxy;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;
import org.jboss.tools.as.management.itests.utils.AS7ManagerTestUtils;
import org.jboss.tools.as.management.itests.utils.AssertUtility;
import org.jboss.tools.as.management.itests.utils.ParameterUtils;
import org.jboss.tools.as.management.itests.utils.StartupUtility;
import org.jboss.tools.as.management.itests.utils.AS7ManagerTestUtils.MockAS7ManagementDetails;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.osgi.util.tracker.ServiceTracker;

/**
 * 
 */
@RunWith(value = Parameterized.class)
public class AS7ManagerIntegrationTest extends AssertUtility {

	

	@Parameters
	public static Collection<Object[]> data() {
		ArrayList<Object[]> l = MatrixUtils.toMatrix(new Object[][]{ParameterUtils.getAS7ServerHomes()});
		return l;
	}
	
	private static StartupUtility util;
	@BeforeClass 
	public static void init() {
		util = new StartupUtility();
	}
	@AfterClass 
	public static void cleanup() {
		util.dispose();
	}
	
	private String homeDir;
	IJBoss7ManagerService service;
	public AS7ManagerIntegrationTest(String home) {
		homeDir = home;
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

	@Before
	public void before()  throws IOException  {
		assertNotNull(homeDir);
		assertTrue(new Path(homeDir).toFile().exists());
		String rtType = ParameterUtils.serverHomeToRuntimeType.get(homeDir);
		assertNotNull(rtType);
		IJBoss7ManagerService service2 = AS7ManagerTestUtils.findService(rtType);
		assertNotNull("Management Service for runtime type " + rtType + " not found.", service2);
		assertTrue(service2 instanceof JBoss7ManagerServiceProxy);
		assertTrue(service2 instanceof ServiceTracker);
		Object o = ((ServiceTracker)service2).getService();
		assertNotNull(o);

		System.out.println("\n\nBeginning a new management test");
		System.out.println("homedir = " + homeDir);
		if( !homeDir.equals(util.getHomeDir())) {
			System.out.println("disposing previous util");
			util.dispose();
			util.setHomeDir(homeDir);
			System.out.println("launching server for homedir=" + homeDir);
			util.start(true);
		}
		// Make sure a server is up
		assertTrue("There is no server at " + AS7ManagerTestUtils.LOCALHOST +
				" that listens on port " + util.getPort(),
				AS7ManagerTestUtils.isListening(AS7ManagerTestUtils.LOCALHOST, util.getPort()));
		service = service2;
	}

	@After
	public void tearDown() {
	}
	
	
	@Test
	public void deployedWarIsResponding() throws Exception {
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		try {
			
			service.deploySync(createManagementDetails(), 
					warFile.getName(), warFile, true, new NullProgressMonitor());

			String response = AS7ManagerTestUtils.waitForRespose(
					"minimalistic", AS7ManagerTestUtils.LOCALHOST, AS7ManagerTestUtils.WEB_PORT);
			assertTrue(response != null
					&& response.indexOf("minimalistic") >= 0);

		} finally {
			AS7ManagerTestUtils.quietlyUndeploy(warFile.getName(), service, createManagementDetails());
		}
	}

	// TODO This test is an issue with upstream. 
	// https://issues.jboss.org/browse/AS7-6779

//	@Test
//	// doesnt work eap6.1
//	public void canReplaceWar() throws Exception {
//		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
//		File warFile2 = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.GWT_HELLOWORLD_WAR);
//		String name = warFile.getName();
//		try {
//			AS7ManagerTestUtils.waitUntilFinished(manager.deploy(name, warFile));
//			AS7ManagerTestUtils.waitUntilFinished(manager.replace(name, warFile2));
//			String response = AS7ManagerTestUtils.waitForRespose(
//					"minimalistic", AS7ManagerTestUtils.LOCALHOST, AS7ManagerTestUtils.WEB_PORT);
//			assertTrue(response != null
//					&& response.indexOf("GWT") >= 0);
//		} finally {
//			AS7ManagerTestUtils.quietlyUndeploy(name, manager);
//		}
//	}

	@Test
	public void getEnabledStateIfDeploymentIsDeployed() throws URISyntaxException, IOException, JBoss7ManangerException {
		String deploymentName = "testDeployment";
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		try {
			service.deploySync(createManagementDetails(),
					deploymentName, warFile, true, new NullProgressMonitor());
			JBoss7DeploymentState state = service.getDeploymentState(createManagementDetails(), deploymentName);
			assertNotNull(state);
			assertTrue(areEqual(state, JBoss7DeploymentState.STARTED));
		} finally {
			AS7ManagerTestUtils.quietlyUndeploy(deploymentName, service, createManagementDetails());
		}
	}

	@Test
	public void getDisabledStateIfDeploymentIsOnlyAdded() throws URISyntaxException, IOException,
			JBoss7ManangerException {
		String deploymentName = "testDeployment";
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		try {
			service.addDeployment(createManagementDetails(), deploymentName, warFile, new NullProgressMonitor());
			JBoss7DeploymentState state = service.getDeploymentState(createManagementDetails(), deploymentName);
			assertNotNull(state);
			assertTrue(areEqual(state, JBoss7DeploymentState.STOPPED));
		} finally {
			AS7ManagerTestUtils.quietlyRemove(deploymentName, service, createManagementDetails());
		}
	}

	@Test
	public void getErrorIfDeploymentIsNotDeployed() throws URISyntaxException, IOException, JBoss7ManangerException {
		String deploymentName = "testDeployment";
		try {
			JBoss7DeploymentState state = service.getDeploymentState(createManagementDetails(), deploymentName);
			assertEquals(state, JBoss7DeploymentState.NOT_FOUND);
		} finally {
			AS7ManagerTestUtils.quietlyUndeploy(deploymentName, service, createManagementDetails());
		}
	}

	@Test
	public void canGetServerState() throws JBoss7ManangerException {
		assertEquals(JBoss7ServerState.RUNNING, service.getServerState(createManagementDetails()));
	}
	
	public static IAS7ManagementDetails createManagementDetails() {
		return new MockAS7ManagementDetails(AS7ManagerTestUtils.LOCALHOST, util.getPort());
	}
	
}