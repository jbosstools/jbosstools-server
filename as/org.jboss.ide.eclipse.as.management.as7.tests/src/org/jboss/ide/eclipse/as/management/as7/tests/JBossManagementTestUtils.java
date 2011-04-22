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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManangementException;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.as7.deployment.AS7Manager;
import org.osgi.framework.Bundle;

/**
 * @author André Dietisheim
 */
public class JBossManagementTestUtils {

	public static final String GWT_HELLOWORLD_WAR = "gwt-helloworld.war";
	public static final String MINIMALISTIC_WAR = "minimalistic.war";

	public static final String HOST = "localhost";
	public static final int MGMT_PORT = 9999;
	public static final int WEB_PORT = 8080;

	private static final String WAR_FOLDER = "/wars/";
	private static final String BUNDLE_ID = "org.jboss.ide.eclipse.as.management.as7.tests";

	private static final int RESPONSE_TIMEOUT = 10 * 1024;
	private static final long WAIT_TIMEOUT = 10 * 1024;

	public static File getWarFile(String name) throws URISyntaxException, IOException {
		Bundle bundle = Platform.getBundle(BUNDLE_ID);
		URL entryUrl = bundle.getEntry(WAR_FOLDER + name);
		return new File(FileLocator.resolve(entryUrl).toURI());
	}

	public static String waitForRespose(String name, String host, int port) throws IOException {
		waitForResponseCode(200, name, host, port);
		return getResponse(name, host, port);
	}

	public static void quietlyUndeploy(File file, AS7Manager manager) {
		quietlyUndeploy(file.getName(), manager);
	}

	public static void quietlyUndeploy(String name, AS7Manager manager) {
		try {
			// DetypedDeployer.undeploy(name, JBossManagementTestUtils.HOST, JBossManagementTestUtils.MGMT_PORT);
			waitUntilFinished(manager.undeploy(name));
		} catch (Exception e) {
			// ignore
		}
	}

	public static void quietlyRemove(String name, AS7Manager manager) {
		try {
			//DetypedDeployer.remove(name, JBossManagementTestUtils.HOST, JBossManagementTestUtils.MGMT_PORT);
			waitUntilFinished(manager.remove(name));
		} catch (Exception e) {
			// ignore
		}
	}

	public static void waitUntilFinished(IJBoss7DeploymentResult result) throws JBoss7ManangementException {
		result.getStatus(); // wait for operation to finish
	}

	
	public static String getResponse(String name, String host, int port) throws IOException {
		URL url = new URL("http://" + host + ":" + port + "/" + name);
		HttpURLConnection connection = connect(url);
		return toString(new BufferedInputStream(connection.getInputStream()));
	}

	public static HttpURLConnection waitForResponseCode(int code, String name, String host, int port)
			throws IOException {
		URL url = new URL("http://" + host + ":" + port + "/" + name);
		long until = System.currentTimeMillis() + WAIT_TIMEOUT;
		while (System.currentTimeMillis() < until) {
			HttpURLConnection connection = connect(url);
			try {
				if (connection.getResponseCode() == code) {
					return connection;
				}
			} catch (FileNotFoundException e) {
				if (code == 404) {
					return connection;
				}
				throw e;
			}
		}
		throw new RuntimeException("wait on url " + url + " for response code " + code + " timed out.");
	}

	private static HttpURLConnection connect(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setAllowUserInteraction(false);
		connection.setConnectTimeout(RESPONSE_TIMEOUT);
		connection.setInstanceFollowRedirects(true);
		connection.setDoOutput(false);
		return connection;
	}

	public static String toString(InputStream in) throws IOException {
		StringWriter writer = new StringWriter();
		for (int data = -1; ((data = in.read()) != -1);) {
			writer.write(data);
		}
		return writer.toString();
	}
}
