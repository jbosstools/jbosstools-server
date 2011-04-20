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

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.jboss.ide.eclipse.as.management.as7.deployment.DeployerException;
import org.jboss.ide.eclipse.as.management.as7.deployment.DetypedDeployer;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class DetypedDeployerIntegrationTest {

	@Test
	public void canDeploy() throws Exception {
		File warFile = DeployerTestUtils.getWarFile(DeployerTestUtils.MINIMALISTIC_WAR);
		try {
			DetypedDeployer.deploy(warFile, DeployerTestUtils.HOST, DeployerTestUtils.MGMT_PORT);

			String response = DeployerTestUtils.getWebappResponse("minimalistic", DeployerTestUtils.HOST, DeployerTestUtils.WEB_PORT);
			assertTrue(response.indexOf("minimalistic") >= 0);

		} finally {
			quietlyUndeploy(warFile);
		}
	}

	@Test(expected = DeployerException.class)
	public void cannotDeployWarTwice() throws Exception {
		File warFile = DeployerTestUtils.getWarFile(DeployerTestUtils.MINIMALISTIC_WAR);
		try {
			DetypedDeployer.deploy(warFile, DeployerTestUtils.HOST, DeployerTestUtils.MGMT_PORT);
			DetypedDeployer.deploy(warFile, DeployerTestUtils.HOST, DeployerTestUtils.MGMT_PORT);
		} finally {
			quietlyUndeploy(warFile);
		}
	}

	@Test
	public void canReplaceWar() throws Exception {
		File warFile = DeployerTestUtils.getWarFile(DeployerTestUtils.MINIMALISTIC_WAR);
		File warFile2 = DeployerTestUtils.getWarFile(DeployerTestUtils.GWT_HELLOWORLD_WAR);
		String name = warFile.getName();
		try {
			DetypedDeployer.deploy(name, warFile, DeployerTestUtils.HOST, DeployerTestUtils.MGMT_PORT);
			DetypedDeployer.replace(name, warFile2, DeployerTestUtils.HOST, DeployerTestUtils.MGMT_PORT);
			String response = DeployerTestUtils.getWebappResponse(
					"minimalistic", DeployerTestUtils.HOST, DeployerTestUtils.WEB_PORT);
			assertTrue(response.indexOf("GWT") >= 0);
		} finally {
			quietlyUndeploy(warFile);
		}
	}

	@Test(expected = DeployerException.class)
	public void cannotUndeployNondeployed() throws DeployerException {
		DetypedDeployer.undeploy("inexistant", DeployerTestUtils.HOST, DeployerTestUtils.MGMT_PORT);
	}

	@Test
	public void canQueryDeploymentdeployedState() throws Exception {
		File warFile = DeployerTestUtils.getWarFile(DeployerTestUtils.MINIMALISTIC_WAR);
		File warFile2 = DeployerTestUtils.getWarFile(DeployerTestUtils.GWT_HELLOWORLD_WAR);
		try {
			DetypedDeployer.deploy(warFile, DeployerTestUtils.HOST, DeployerTestUtils.MGMT_PORT);
			DetypedDeployer.deploy(warFile2, DeployerTestUtils.HOST, DeployerTestUtils.MGMT_PORT);
			List<String> deployments = DetypedDeployer.getDeployments(DeployerTestUtils.HOST, DeployerTestUtils.MGMT_PORT);
			assertThat(deployments.size(), is(2));
			assertThat(deployments, hasItems(DeployerTestUtils.MINIMALISTIC_WAR, DeployerTestUtils.GWT_HELLOWORLD_WAR));
		} finally {
			quietlyUndeploy(warFile);
			quietlyUndeploy(warFile2);
		}
	}

	private void quietlyUndeploy(File file) {
		try {
			DetypedDeployer.undeploy(file.getName(), DeployerTestUtils.HOST, DeployerTestUtils.MGMT_PORT);
		} catch (Exception e) {
			e.printStackTrace();
			// ignore
		}
	}
}
