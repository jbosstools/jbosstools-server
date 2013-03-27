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
package org.jboss.ide.eclipse.as.internal.management.as7.tests;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.ide.eclipse.as.internal.management.as7.tests.utils.AS7ManagerTestUtils;
import org.jboss.ide.eclipse.as.internal.management.as7.tests.utils.AssertUtility;
import org.jboss.ide.eclipse.as.internal.management.as7.tests.utils.ParameterUtils;
import org.jboss.ide.eclipse.as.internal.management.as7.tests.utils.StartupUtility;
import org.jboss.ide.eclipse.as.internal.management.as71.AS71Manager;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;
import org.jboss.ide.eclipse.as.test.server.JBossManagerTest.MockAS7ManagementDetails;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

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
	private AS71Manager manager;
	public AS7ManagerIntegrationTest(String home) {
		homeDir = home;
	}

	@Before
	public void before()  throws IOException  {
		System.out.println("Beginning a new management test");
		if( !homeDir.equals(util.getHomeDir())) {
			util.dispose();
			util.setHomeDir(homeDir);
			util.start(true);
		}
		assertTrue("There is no server at " + AS7ManagerTestUtils.LOCALHOST +
				" that listens on port " + AS71Manager.MGMT_PORT,
				AS7ManagerTestUtils.isListening(AS7ManagerTestUtils.LOCALHOST, AS71Manager.MGMT_PORT));
		this.manager = new AS71Manager( new MockAS7ManagementDetails(
				AS7ManagerTestUtils.LOCALHOST, AS71Manager.MGMT_PORT));
	}

	@After
	public void tearDown() {
		manager.dispose();
	}
	
	
	// And now the tests

	@Test
	public void canDeploy() throws Exception {
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		try {
			AS7ManagerTestUtils.waitUntilFinished(manager.deploy(warFile));

			String response = AS7ManagerTestUtils.waitForRespose(
					"minimalistic", AS7ManagerTestUtils.LOCALHOST, AS7ManagerTestUtils.WEB_PORT);
			assertTrue(response != null
					&& response.indexOf("minimalistic") >= 0);
		} finally {
			AS7ManagerTestUtils.quietlyUndeploy(warFile, manager);
		}
	}

	@Test
	public void deployedWarIsResponding() throws Exception {
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		try {
			AS7ManagerTestUtils.waitUntilFinished(manager.deploy(warFile));

			String response = AS7ManagerTestUtils.waitForRespose(
					"minimalistic", AS7ManagerTestUtils.LOCALHOST, AS7ManagerTestUtils.WEB_PORT);
			assertTrue(response != null
					&& response.indexOf("minimalistic") >= 0);

		} finally {
			AS7ManagerTestUtils.quietlyUndeploy(warFile, manager);
		}
	}

	@Test
	// doesnt work eap6.1
	public void canReplaceWar() throws Exception {
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		File warFile2 = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.GWT_HELLOWORLD_WAR);
		String name = warFile.getName();
		try {
			AS7ManagerTestUtils.waitUntilFinished(manager.deploy(name, warFile));
			AS7ManagerTestUtils.waitUntilFinished(manager.replace(name, warFile2));
			String response = AS7ManagerTestUtils.waitForRespose(
					"minimalistic", AS7ManagerTestUtils.LOCALHOST, AS7ManagerTestUtils.WEB_PORT);
			assertTrue(response != null
					&& response.indexOf("GWT") >= 0);
		} finally {
			AS7ManagerTestUtils.quietlyUndeploy(name, manager);
		}
	}

	@Test
	public void getEnabledStateIfDeploymentIsDeployed() throws URISyntaxException, IOException, JBoss7ManangerException {
		String deploymentName = "testDeployment";
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		try {
			AS7ManagerTestUtils.waitUntilFinished(manager.deploy(deploymentName, warFile));
			JBoss7DeploymentState state = manager.getDeploymentState(deploymentName);
			assertNotNull(state);
			assertTrue(areEqual(state, JBoss7DeploymentState.STARTED));
		} finally {
			AS7ManagerTestUtils.quietlyUndeploy(deploymentName, manager);
		}
	}

	@Test
	public void getDisabledStateIfDeploymentIsOnlyAdded() throws URISyntaxException, IOException,
			JBoss7ManangerException {
		String deploymentName = "testDeployment";
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		try {
			AS7ManagerTestUtils.waitUntilFinished(manager.add(deploymentName, warFile));
			JBoss7DeploymentState state = manager.getDeploymentState(deploymentName);
			assertNotNull(state);
			assertTrue(areEqual(state, JBoss7DeploymentState.STOPPED));
		} finally {
			AS7ManagerTestUtils.quietlyRemove(deploymentName, manager);
		}
	}

	@Test
	public void getErrorIfDeploymentIsNotDeployed() throws URISyntaxException, IOException, JBoss7ManangerException {
		String deploymentName = "testDeployment";
		try {
			JBoss7DeploymentState state = manager.getDeploymentState(deploymentName);
			assertEquals(state, JBoss7DeploymentState.NOT_FOUND);
		} finally {
			AS7ManagerTestUtils.quietlyUndeploy(deploymentName, manager);
		}
	}

	// TODO This test is an issue with upstream. 
	// https://issues.jboss.org/browse/AS7-6779
//	@Test
//	public void canAddDeploymentDirectory() throws URISyntaxException, IOException, JBoss7ManangerException {
//		String deploymentName = getRandomDeploymentName();
//		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
//		try {
//			assertFalse(manager.hasDeployment(deploymentName));
//			File deploymentDir = new File(System.getProperty("java.io.tmpdir"), "as7dir");
//			manager.addDeploymentDirectory(deploymentDir.getAbsolutePath());
//			
//			AS7ManagerTestUtils.waitForResponseCode(200, deploymentName, AS7ManagerTestUtils.LOCALHOST, AS7ManagerTestUtils.WEB_PORT);
//		} finally {
//			AS7ManagerTestUtils.quietlyRemove(deploymentName, manager);
//		}
//	}

	@Test
	public void canGetServerState() throws JBoss7ManangerException {
		assertEquals(JBoss7ServerState.RUNNING, manager.getServerState());
	}
}