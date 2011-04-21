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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import org.jboss.ide.eclipse.as.management.as7.deployment.DeployerException;
import org.jboss.ide.eclipse.as.management.as7.deployment.TypedDeployer;
import org.jboss.ide.eclipse.as.management.as7.deployment.TypedDeployer.DeploymentResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author André Dietisheim
 */
public class TypedDeployerIntegrationTest {

	private TypedDeployer deployer;

	@Before
	public void setUp() throws UnknownHostException {
		this.deployer = new TypedDeployer(DeployerTestUtils.HOST, DeployerTestUtils.MGMT_PORT);
	}
	
	@After
	public void tearDown() {
		deployer.dispose();
	}
	
	@Test
	public void canDeploy() throws Exception {
		File warFile = DeployerTestUtils.getWarFile(DeployerTestUtils.MINIMALISTIC_WAR);
		try {
			waitUntilFinished(deployer.deploy(warFile));
			
			String response = DeployerTestUtils.getWebappResponse(
					"minimalistic", DeployerTestUtils.HOST, DeployerTestUtils.WEB_PORT);
			assertTrue(response != null 
					&& response.indexOf("minimalistic") >= 0);

		} finally {
			quietlyUndeploy(warFile);
		}
	}

	@Test
	public void deployedWarIsResponding() throws Exception {
		File warFile = DeployerTestUtils.getWarFile(DeployerTestUtils.MINIMALISTIC_WAR);
		try {
			waitUntilFinished(deployer.deploy(warFile));
			
			String response = DeployerTestUtils.getWebappResponse(
					"minimalistic", DeployerTestUtils.HOST, DeployerTestUtils.WEB_PORT);
			assertTrue(response != null 
					&& response.indexOf("minimalistic") >= 0);

		} finally {
			quietlyUndeploy(warFile);
		}
	}

	@Test(expected = DeployerException.class)
	public void cannotDeployWarTwice() throws Exception {
		File warFile = DeployerTestUtils.getWarFile(DeployerTestUtils.MINIMALISTIC_WAR);
		try {
			waitUntilFinished(deployer.deploy(warFile));
			waitUntilFinished(deployer.deploy(warFile));
		} finally {
			quietlyUndeploy(warFile);
		}
	}

	@Test(expected = DeployerException.class)
	public void cannotUndeployNondeployed() throws DeployerException, InterruptedException, ExecutionException {
		waitUntilFinished(deployer.undeploy("inexistant"));
	}

	@Test
	public void canReplaceWar() throws Exception {
		File warFile = DeployerTestUtils.getWarFile(DeployerTestUtils.MINIMALISTIC_WAR);
		File warFile2 = DeployerTestUtils.getWarFile(DeployerTestUtils.GWT_HELLOWORLD_WAR);
		String name = warFile.getName();
		try {
			waitUntilFinished(deployer.deploy(name, warFile));
			waitUntilFinished(deployer.replace(name, warFile2));
			String response = DeployerTestUtils.getWebappResponse(
					"minimalistic", DeployerTestUtils.HOST, DeployerTestUtils.WEB_PORT);
			assertTrue(response != null 
					&& response.indexOf("GWT") >= 0);
		} finally {
			quietlyUndeploy(name);
		}
	}
	
	@Test
	public void canQueryDeploymentState() throws URISyntaxException, IOException, DeployerException {
		String deploymentName = "testDeployment";
		File warFile = DeployerTestUtils.getWarFile(DeployerTestUtils.MINIMALISTIC_WAR);
		try {
			waitUntilFinished(deployer.deploy(deploymentName, warFile));
			String state = deployer.getDeploymentState(deploymentName);
			assertNotNull(state);
		} finally {
			quietlyUndeploy(deploymentName);
		}
	}
	
	private void quietlyUndeploy(String name) {
		try {
			waitUntilFinished(deployer.undeploy(name));
		} catch (Exception e) {
			e.printStackTrace();
			// ignore
		}
	}
	
	private void quietlyUndeploy(File file) {
		quietlyUndeploy(file.getName());
	}
	
	private void waitUntilFinished(DeploymentResult result) throws DeployerException {
		result.getStatus(); // wait for operation to finish
	}
}
