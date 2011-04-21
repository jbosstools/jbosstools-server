/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.management.as7.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import org.jboss.ide.eclipse.as.management.as7.deployment.DeployerException;
import org.jboss.ide.eclipse.as.management.as7.deployment.DeploymentState;
import org.jboss.ide.eclipse.as.management.as7.deployment.AS7Manager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author André Dietisheim
 */
public class JBossManagementIntegrationTest {

	private AS7Manager manager;

	@Before
	public void setUp() throws UnknownHostException {
		this.manager = new AS7Manager(JBossManagementTestUtils.HOST, JBossManagementTestUtils.MGMT_PORT);
	}

	@After
	public void tearDown() {
		manager.dispose();
	}

	@Ignore
	@Test
	public void canDeploy() throws Exception {
		File warFile = JBossManagementTestUtils.getWarFile(JBossManagementTestUtils.MINIMALISTIC_WAR);
		try {
			JBossManagementTestUtils.waitUntilFinished(manager.deploy(warFile));

			String response = JBossManagementTestUtils.waitForRespose(
					"minimalistic", JBossManagementTestUtils.HOST, JBossManagementTestUtils.WEB_PORT);
			assertTrue(response != null
					&& response.indexOf("minimalistic") >= 0);
		} finally {
			JBossManagementTestUtils.quietlyUndeploy(warFile, manager);
		}
	}

	@Ignore
	@Test
	public void deployedWarIsResponding() throws Exception {
		File warFile = JBossManagementTestUtils.getWarFile(JBossManagementTestUtils.MINIMALISTIC_WAR);
		try {
			JBossManagementTestUtils.waitUntilFinished(manager.deploy(warFile));

			String response = JBossManagementTestUtils.waitForRespose(
					"minimalistic", JBossManagementTestUtils.HOST, JBossManagementTestUtils.WEB_PORT);
			assertTrue(response != null
					&& response.indexOf("minimalistic") >= 0);

		} finally {
			JBossManagementTestUtils.quietlyUndeploy(warFile, manager);
		}
	}

	@Ignore
	@Test(expected = DeployerException.class)
	public void cannotDeployWarTwice() throws Exception {
		File warFile = JBossManagementTestUtils.getWarFile(JBossManagementTestUtils.MINIMALISTIC_WAR);
		try {
			JBossManagementTestUtils.waitUntilFinished(manager.deploy(warFile));
			JBossManagementTestUtils.waitUntilFinished(manager.deploy(warFile));
		} finally {
			JBossManagementTestUtils.quietlyUndeploy(warFile, manager);
		}
	}

	@Ignore
	@Test(expected = DeployerException.class)
	public void cannotUndeployNondeployed() throws DeployerException, InterruptedException, ExecutionException {
		JBossManagementTestUtils.waitUntilFinished(manager.undeploy("inexistant"));
	}

	@Ignore
	@Test
	public void canReplaceWar() throws Exception {
		File warFile = JBossManagementTestUtils.getWarFile(JBossManagementTestUtils.MINIMALISTIC_WAR);
		File warFile2 = JBossManagementTestUtils.getWarFile(JBossManagementTestUtils.GWT_HELLOWORLD_WAR);
		String name = warFile.getName();
		try {
			JBossManagementTestUtils.waitUntilFinished(manager.deploy(name, warFile));
			JBossManagementTestUtils.waitUntilFinished(manager.replace(name, warFile2));
			String response = JBossManagementTestUtils.waitForRespose(
					"minimalistic", JBossManagementTestUtils.HOST, JBossManagementTestUtils.WEB_PORT);
			assertTrue(response != null
					&& response.indexOf("GWT") >= 0);
		} finally {
			JBossManagementTestUtils.quietlyUndeploy(name, manager);
		}
	}

	@Test
	public void getEnabledStateIfDeploymentIsDeployed() throws URISyntaxException, IOException, DeployerException {
		String deploymentName = "testDeployment";
		File warFile = JBossManagementTestUtils.getWarFile(JBossManagementTestUtils.MINIMALISTIC_WAR);
		try {
			JBossManagementTestUtils.waitUntilFinished(manager.deploy(deploymentName, warFile));
			DeploymentState state = manager.getDeploymentState(deploymentName);
			assertNotNull(state);
			assertThat(state, equalTo(DeploymentState.STARTED));
		} finally {
			JBossManagementTestUtils.quietlyUndeploy(deploymentName, manager);
		}
	}

	@Test
	public void getDisabledStateIfDeploymentIsOnlyAdded() throws URISyntaxException, IOException, DeployerException {
		String deploymentName = "testDeployment";
		File warFile = JBossManagementTestUtils.getWarFile(JBossManagementTestUtils.MINIMALISTIC_WAR);
		try {
			JBossManagementTestUtils.waitUntilFinished(manager.add(deploymentName, warFile));
			DeploymentState state = manager.getDeploymentState(deploymentName);
			assertNotNull(state);
			assertThat(state, equalTo(DeploymentState.STOPPED));
		} finally {
			JBossManagementTestUtils.quietlyRemove(deploymentName, manager);
		}
	}

	@Test(expected = DeployerException.class)
	public void getErrorIfDeploymentIsNotDeployed() throws URISyntaxException, IOException, DeployerException {
		String deploymentName = "testDeployment";
		try {
			manager.getDeploymentState(deploymentName);
		} finally {
			JBossManagementTestUtils.quietlyUndeploy(deploymentName, manager);
		}
	}
}
