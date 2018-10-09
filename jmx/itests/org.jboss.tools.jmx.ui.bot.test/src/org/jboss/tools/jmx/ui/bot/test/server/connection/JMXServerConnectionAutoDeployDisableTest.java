/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.jmx.ui.bot.test.server.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.requirements.server.ServerRequirementState;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerRequirement.JBossServer;
import org.jboss.tools.jmx.reddeer.ui.editor.AttributesPage;
import org.jboss.tools.jmx.reddeer.ui.editor.MBeanEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class covering auto deploy disabling on running server
 * @author odockal
 *
 */
@JBossServer(state=ServerRequirementState.PRESENT, cleanup=true)
public class JMXServerConnectionAutoDeployDisableTest extends JMXServerTestTemplate {

	private static final String WEB_APP = "SimpleWebApp";
	
	private static final String LOCALHOST = "http://localhost:8080";
	
	private static final Path WAR_FILE = Paths.get(System.getProperty("user.dir"), "projects", WEB_APP + ".war");
	
	private Path deploymentPath;
	
	private static final Logger log = Logger.getLogger(JMXServerConnectionAutoDeployDisableTest.class);
	
	@Before
	public void setupServer() {
		setUpView();
		
		deploymentPath = Paths.get(serverConfig.getConfiguration().getRuntime(), "standalone", "deployments");
		wildfly = getServer(serverConfig.getServerName());
		serverItem = view.getServerConnectionsItem();
	}
	
	@After
	public void tearDownServer() {
		setAutoDeployment(true, false, false);
		stopServer();
		deleteDeploymentFiles();
	}
	
	@Test
	public void testDisablingServerAutoDeployAttribute() {
		startServer();
		
		getServerJMXConnection();
		connection.connect();
		
		setAutoDeployment(true, false, true);
		
		// deploying of the app on server is working
		deployWarIntoServer();
		assertTrue("Project was not deployed sucessfully", checkProjectIsDeployed());
		assertTrue("WebApp is not running", checkAppIsRunning());
		
		deleteDeploymentFiles();
		
		stopServer();
		
		startServer();
		getServerJMXConnection();
		connection.connect();
		
		setAutoDeployment(false, true, true);
		
		// deploy app into server's deployment folder - should not work
		deployWarIntoServer();
		assertFalse("Project should not have been deployed", checkProjectIsDeployed());
		assertFalse("It is not expected that web app is being run on server", checkAppIsRunning());
	}
	
	private void setAutoDeployment(boolean setValue, boolean checkExpected, boolean expected) {
		connection.openMBeanObjectEditor(0, "jboss.as", "deployment-scanner", "default");
		MBeanEditor editor = new MBeanEditor("jboss.as:scanner=default,subsystem=deployment-scanner");
		editor.activate();
		
		AttributesPage ap = editor.getAttributesPage();
		
		assertTrue("Choosen operation is not available. ", ap.containsTableItem("scanEnabled"));
		ap.selectTableItem("scanEnabled");
		String value = ap.getTable().getItem("scanEnabled").getText(ap.getTable().getHeaderIndex("Value")).toString();
		if (checkExpected) {
			assertEquals("Default value for server is to have scan enabled", Boolean.toString(expected), value);
		}
		new DefaultCombo(ap.getDetailsSection()).setSelection(Boolean.toString(setValue));
		editor.close();
	}
	
	private void deployWarIntoServer() {
		try {
			Path destination = deploymentPath.resolve(WAR_FILE.getFileName());
			if (!Files.exists(destination)) {
				Files.createFile(destination);
			}
			Files.copy(WAR_FILE, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			fail("Copying war into server deployments failed with: " + e.getMessage());
		}
		new WaitUntil(new FileExists(deploymentPath.resolve(WAR_FILE.getFileName())), TimePeriod.DEFAULT);
	}
	
	private boolean checkAppIsRunning() {
		boolean isRunning = false;
		try {
			isRunning = appAtUrlIsRunning(LOCALHOST + "/" + WEB_APP, 200);
		} catch (IOException e) {
			fail("Failed to verify deployed web app. " + e.getMessage());	
		}
		return isRunning;
	}
	
	private boolean checkProjectIsDeployed() {
		assertNotNull("There is no war archive in deployemnts", getWebAppDeploymentFiles(WEB_APP + ".war", true));
		FileExists deployed = new FileExists(Paths.get(deploymentPath.resolve(WAR_FILE.getFileName()).toString()+ ".deployed"));
		new WaitUntil(deployed, false);
		if (deployed.getResult()) {
			return true;
		}
		return false;
	}
	
	private void deleteDeploymentFiles() {
		File [] deploymentFiles = getWebAppDeploymentFiles(WEB_APP, false);
		for (File file : deploymentFiles) {
			file.delete();
		}
	}
	
	private File [] getWebAppDeploymentFiles(String app, boolean strictFilter) {
		return deploymentPath.toFile().listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				return strictFilter ? name.equals(app) : name.contains(app);
			}
		});
	}
	
    private boolean appAtUrlIsRunning(String uri, int expexctedStatusCode) throws IOException {
    	log.info("URL to check: " + uri);
    	HttpURLConnection connection = constructHttpRequest(uri, "GET");
		if (expexctedStatusCode != connection.getResponseCode()) {
			return false;
		}
		StringBuilder data = null;
		try {
			data = processInputStream(connection.getInputStream());
		} catch (IOException e) {
			fail("Failed to read data from HttpConnection with " + e.getMessage());
		} finally {
			connection.disconnect();
		}
		return data == null || !data.toString().contains("hello") ? false : true;
    }
    
    private StringBuilder processInputStream(InputStream in) throws IOException {
		StringBuilder result = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while((line = reader.readLine()) != null) {
			result.append(line);
		}
		return result;
    } 
    
    private HttpURLConnection constructHttpRequest(String uri, String method) throws IOException {
    	URL url = null;
    	try {
			url = new URL(uri);
		} catch (MalformedURLException e) {
			fail("Malformed URL: " + uri);
		}
    	return setupHttpURLConnection(url, method);
    }
    
    private HttpURLConnection setupHttpURLConnection(URL url, String method) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		return connection;
    }
    
    private class FileExists extends AbstractWaitCondition {

    	private Path file;
    	private Boolean exists = false;
    	
    	public FileExists(Path file) {
			this.file = file;
		}
    	
		@Override
		public boolean test() {
			if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
				exists = true;
				return true;
			}
			return false;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Boolean getResult() {
			return exists;
		}
    	
    }

}
