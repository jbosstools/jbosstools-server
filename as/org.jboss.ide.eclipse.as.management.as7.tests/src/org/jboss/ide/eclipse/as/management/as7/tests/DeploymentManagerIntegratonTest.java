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

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.as.management.as7.deployment.DeployerException;
import org.jboss.ide.eclipse.as.management.as7.deployment.DeploymentManager;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * 
 * @author André Dietisheim
 */
public class DeploymentManagerIntegratonTest {

	private static final String WAR_FOLDER = "/wars/";
	private static final String BUNDLE_ID = "org.jboss.ide.eclipse.as7.deployment.tests";

	private static final int WEBAPP_RESPONSE_TIMEOUT = 10 * 1024;

	private static final String HOST = "localhost";
	private static final int MGMT_PORT = 9999;
	private static final int WEB_PORT = 8080;

	@Test
	public void canDeploy() throws Exception {
		DeploymentManager deployer = null;
		File warFile = getWarFile("minimalistic.war");
		try {
			deployer = new DeploymentManager(HOST, MGMT_PORT);
			deployer.deploy(warFile).execute();

			String response = getServerResponse(new URL(
					MessageFormat.format(
							"http://{0}:{1}/{2}", HOST, String.valueOf(WEB_PORT), "minimalistic")));
			assertTrue(response.indexOf("minimalistic") >= 0);

		} finally {
			quietlyUndeploy(warFile, deployer);
		}
	}

	@Test(expected = DeployerException.class)
	public void cannotDeployWarTwice() throws Exception {
		DeploymentManager deployer = null;
		File warFile = getWarFile("minimalistic.war");
		try {
			deployer = new DeploymentManager(HOST, MGMT_PORT);
			deployer.deploy(warFile).execute();
			deployer.deploy(warFile).execute();
		} finally {
			quietlyUndeploy(warFile, deployer);
		}
	}

	private File getWarFile(String name) throws URISyntaxException, IOException {
		Bundle bundle = Platform.getBundle(BUNDLE_ID);
		URL entryUrl = bundle.getEntry(WAR_FOLDER + name);
		return new File(FileLocator.resolve(entryUrl).toURI());
	}

	private String getServerResponse(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setAllowUserInteraction(false);
		connection.setConnectTimeout(WEBAPP_RESPONSE_TIMEOUT);
		connection.setInstanceFollowRedirects(true);
		connection.setDoOutput(false);
		BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
		return toString(in);
	}

	private String toString(InputStream in) throws IOException {
		StringWriter writer = new StringWriter();
		for (int data = -1; ((data = in.read()) != -1);) {
			writer.write(data);
		}
		return writer.toString();
	}

	private void quietlyUndeploy(File file, DeploymentManager deployer) {
		try {
			if (deployer != null) {
				deployer.undeploy(file).execute();
				deployer.dispose();
			}
		} catch (Exception e) {
			e.printStackTrace();
			// ignore
		}
	}
}
