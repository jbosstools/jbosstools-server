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
package org.jboss.ide.eclipse.as.management.as7.tests;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.as7.deployment.DetypedDeployer;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class DetypedDeployerIntegrationTest {

	@Test
	public void canDeploy() throws Exception {
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		try {
			DetypedDeployer.deploy(warFile, AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.MGMT_PORT);

			String response = AS7ManagerTestUtils.waitForRespose("minimalistic", AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.WEB_PORT);
			assertTrue(response.indexOf("minimalistic") >= 0);

		} finally {
			quietlyUndeploy(warFile);
		}
	}

	@Test(expected = JBoss7ManangerException.class)
	public void cannotDeployWarTwice() throws Exception {
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		try {
			DetypedDeployer.deploy(warFile, AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.MGMT_PORT);
			DetypedDeployer.deploy(warFile, AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.MGMT_PORT);
		} finally {
			quietlyUndeploy(warFile);
		}
	}

	@Test
	public void canReplaceWar() throws Exception {
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		File warFile2 = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.GWT_HELLOWORLD_WAR);
		String name = warFile.getName();
		try {
			DetypedDeployer.deploy(name, warFile, AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.MGMT_PORT);
			DetypedDeployer.replace(name, warFile2, AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.MGMT_PORT);
			String response = AS7ManagerTestUtils.waitForRespose(
					"minimalistic", AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.WEB_PORT);
			assertTrue(response.indexOf("GWT") >= 0);
		} finally {
			quietlyUndeploy(warFile);
		}
	}

	@Test(expected = JBoss7ManangerException.class)
	public void cannotUndeployNondeployed() throws JBoss7ManangerException {
		DetypedDeployer.undeploy("inexistant", AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.MGMT_PORT);
	}

	@Test
	public void canQueryDeploymentdeployedState() throws Exception {
		File warFile = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.MINIMALISTIC_WAR);
		File warFile2 = AS7ManagerTestUtils.getWarFile(AS7ManagerTestUtils.GWT_HELLOWORLD_WAR);
		try {
			DetypedDeployer.deploy(warFile, AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.MGMT_PORT);
			DetypedDeployer.deploy(warFile2, AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.MGMT_PORT);
			List<String> deployments = DetypedDeployer.getDeployments(AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.MGMT_PORT);
			assertThat(deployments.size(), is(2));
			assertThat(deployments, hasItems(AS7ManagerTestUtils.MINIMALISTIC_WAR, AS7ManagerTestUtils.GWT_HELLOWORLD_WAR));
		} finally {
			quietlyUndeploy(warFile);
			quietlyUndeploy(warFile2);
		}
	}

	private void quietlyUndeploy(File file) {
		try {
			DetypedDeployer.undeploy(file.getName(), AS7ManagerTestUtils.HOST, AS7ManagerTestUtils.MGMT_PORT);
		} catch (Exception e) {
			e.printStackTrace();
			// ignore
		}
	}
}
