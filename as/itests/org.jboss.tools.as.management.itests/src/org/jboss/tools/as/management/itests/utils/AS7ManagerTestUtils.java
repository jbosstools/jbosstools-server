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
package org.jboss.tools.as.management.itests.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.IJBossManagerServiceProvider;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.tools.as.management.itests.Activator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import static org.mockito.Mockito.*;

/**
 * @author André Dietisheim
 */
public class AS7ManagerTestUtils {

	public static final String MINIMALISTIC_WAR = "minimalistic.war";

	public static final String LOCALHOST = "localhost";
	public static final int WEB_PORT = 8080;
	public static final int MGMT_PORT = 9999;
	

	private static final String WAR_FOLDER = "/wars/";

	private static final int RESPONSE_TIMEOUT = 10 * 1024;
	private static final long WAIT_TIMEOUT = 10 * 1024;

	private AS7ManagerTestUtils() {
		// inhibit instantiation
	}

	public static File getWarFile(String name) throws URISyntaxException, IOException {
		return getBundleFile(name);
	}
	public static File getBundleFile(String name) throws URISyntaxException, IOException {
		Bundle bundle = Platform.getBundle(Activator.BUNDLE_ID);
		URL entryUrl = bundle.getEntry(WAR_FOLDER + name);
		return new File(FileLocator.resolve(entryUrl).toURI());
	}

	public static String waitForRespose(String name, String host, int port) throws IOException {
		HttpURLConnection response1 = waitForResponseCode(200, name, host, port);
		response1.disconnect();
		String result = getResponse(name, host, port);
		return result;
	}

	public static void quietlyUndeploy(String name, IJBoss7ManagerService manager, IAS7ManagementDetails details) {
		try {
			waitUntilFinished(manager.undeploySync(details, 
					name, true, new NullProgressMonitor()));
		} catch (Exception e) {
			// ignore
		}
	}

	public static void quietlyRemove(String name, IJBoss7ManagerService manager, IAS7ManagementDetails details) {
		try {
			waitUntilFinished(manager.removeDeployment(
					details, name, new NullProgressMonitor()));
		} catch (Exception e) {
			// ignore
		}
	}

	public static void waitUntilFinished(IJBoss7DeploymentResult result) throws JBoss7ManangerException {
		result.getStatus(); // wait for operation to finish
	}

	public static String getResponse(String name, String host, int port) throws IOException {
		URL url = new URL("http://" + host + ":" + port + "/" + name);
		HttpURLConnection connection = connect(url);
		String s = toString(new BufferedInputStream(connection.getInputStream()));
		connection.disconnect();
		return s;
	}

	public static HttpURLConnection waitForResponseCode(int code, String name, String host, int port)
			throws IOException {
		URL url = new URL("http://" + host + ":" + port + "/" + name);
		long until = System.currentTimeMillis() + WAIT_TIMEOUT;
		int resetCount = 0;
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
			} catch( SocketException se ) {
				resetCount++;
				if( resetCount >= 10 )
					throw se;
			} finally {
				connection.disconnect();
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

	public static boolean isListening(String host, int port) throws UnknownHostException, IOException {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			return socket.isConnected();
		} catch (ConnectException e) {
			return false;
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}
	
	public static class MockAS7ManagementDetails extends AS7ManagementDetails {
		private String host;
		private int port;
		public MockAS7ManagementDetails(String host, int port, String serverHome) {
			this(host, port, mockServer(serverHome));
		}
		public MockAS7ManagementDetails(String host, int port, IServer server) {
			super(server);
			this.host = host;
			this.port = port;
		}
		public static IServer mockServer(String serverHome) {
			String serverType = ParameterUtils.getServerType(serverHome);
			IServerType st = mock(IServerType.class);
			when(st.getId()).thenReturn(serverType);
			
			IServer s = mock(IServer.class);
			when(s.getServerType()).thenReturn(st);
			return s;
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

	public static IJBoss7ManagerService findService(String runtimeType) {
		return findService(ServerCore.findRuntimeType(runtimeType));
	}
	
	public static IJBoss7ManagerService findService(IRuntimeType runtimeType) {
		forceStart("org.jboss.ide.eclipse.as.core");
		forceStart("org.jboss.ide.eclipse.as.management.as7");
		forceStart("org.jboss.ide.eclipse.as.management.wildfly9");
		forceStart("org.jboss.ide.eclipse.as.management.wf11");
		forceStart("org.jboss.ide.eclipse.as.management.eap61plus");
		IJBossManagerServiceProvider serviceProvider = (IJBossManagerServiceProvider)Platform.getAdapterManager().getAdapter(runtimeType, IJBossManagerServiceProvider.class);
		if( serviceProvider != null ) {
			return serviceProvider.getManagerService();
		}
		return null;
	}
	
	private static boolean forceStart(String bundleId) {
		Bundle bundle = Platform.getBundle(bundleId); //$NON-NLS-1$
		if (bundle != null && bundle.getState() != Bundle.ACTIVE) {
			try {
				bundle.start();
			} catch (BundleException e) {
				// ignore
			}
		}
		return bundle.getState() == Bundle.ACTIVE;
	}
}
