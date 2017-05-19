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
package org.jboss.tools.as.management.itests.incremental;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.IncrementalDeploymentManagerService;
import org.jboss.ide.eclipse.as.management.core.IncrementalManagementModel;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerServiceProxy;
import org.jboss.tools.as.management.itests.utils.AS7ManagerTestUtils;
import org.jboss.tools.as.management.itests.utils.AS7ManagerTestUtils.MockAS7ManagementDetails;
import org.jboss.tools.as.management.itests.utils.AssertUtility;
import org.jboss.tools.as.management.itests.utils.ParameterUtils;
import org.jboss.tools.as.management.itests.utils.StartupUtility;
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
public class IncrementalManagerDeployTest extends AssertUtility {

	

	@Parameters
	public static Collection<Object[]> data() {
		ArrayList<Object[]> l = MatrixUtils.toMatrix(new Object[][]{
			ParameterUtils.getIncrementalMgmtDeploymentHomes()});
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
	IncrementalDeploymentManagerService service;
	public IncrementalManagerDeployTest(String home) {
		homeDir = home;
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

		
		// Also assert it's incremental-enabled
		assertTrue(((JBoss7ManagerServiceProxy)service2).supportsIncrementalDeployment());
		
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
		service = (IncrementalDeploymentManagerService)service2;
	}

	@After
	public void tearDown() {
	}
	
	protected File getBundleFile(String name) throws Exception {
		String path = "incrementalmgmt/" + name;
		File f = AS7ManagerTestUtils.getBundleFile(path);
		return f;
	}
	
	@Test
    public void runExplodedTest() throws Exception {
        undeploy("out.war", true);
        IJBoss7DeploymentResult result = deploy("out.war",
        		getBundleFile("out_with_jar.war"),
                new String[]{"out.war", "out.war/WEB-INF/lib/UtilOne.jar"}, true);
        IStatus s = result.getStatus();
        assertTrue(s.isOK());

        String contents = waitForRespose("out/TigerServ", "localhost", 8080);
        System.out.println(contents);
        if (!contents.startsWith("Served jar:")) {
            System.out.println("Failed expected output prefix");
            throw new Exception("Failed");
        }
    }

	@Test
    public void runIncrementalTest() throws Exception {
        try {
            undeploy("out.war", true);

            IJBoss7DeploymentResult result = deploy("out.war",
            		getBundleFile("out_with_jar.war"),
                    new String[]{"out.war"}, true);
            assertTrue(result.getStatus().isOK());
            String contents = waitForRespose("out/TigerServ", "localhost", 8080);
            System.out.println(contents);
            if (!contents.startsWith("Served jar:")) {
                System.out.println("Failed expected output");
                throw new Exception("Failed");
            }

            
            String changedFile = getBundleFile("TigerServ_change1.class").getAbsolutePath();
            IncrementalManagementModel m = new IncrementalManagementModel();
            Map<String, String> changedContent = new HashMap<String, String>();
            List<String> removedContent = new ArrayList<String>();
            changedContent.put("WEB-INF/classes/my/pak/TigerServ.class",
            		changedFile);
            m.setDeploymentChanges("out.war", changedContent, removedContent);
            incrementalPublish("out.war", m, true);

            contents = waitForRespose("out/TigerServ", "localhost", 8080);
            System.out.println(contents);
            if (!contents.startsWith("Served with:")) {
                System.out.println("Failed expected output");
                throw new Exception("Failed");
            }

            // Do a full publish with a war that has a nested jar
            undeploy("out.war", true);

            result = deploy("out.war",
            		getBundleFile("out_with_jar.war"),
                    new String[]{"out.war"}, true);
            assertTrue(result.getStatus().isOK());
            contents = waitForRespose("out/TigerServ", "localhost", 8080);
            System.out.println(contents);
            if (!contents.startsWith("Served jar:")) {
                System.out.println("Failed expected output prefix");
                throw new Exception("Failed");
            }

            // web should return something like:
            // Served jar:1491340851960:/DWe87rbb:Util:0
            String[] split = contents.split(":");
            if (split.length != 5) {
                System.out.println("Failed expected segment count");
                throw new Exception("Failed");
            }
            if (!split[3].equals("Util")) {
                System.out.println("Fourth segment should be 'Util'");
                throw new Exception("Failed");
            }

            contents = incrementalPublishGetServletContents();

            // web should return something like below.  Note the "Util" segment has changed to "Util6"
            // Served jar:1491340851960:/DWe87rbb:Util6:0
            split = contents.split(":");
            if (split.length != 5) {
                System.out.println("Failed expected segment count");
                throw new Exception("Failed");
            }
            if (!split[3].equals("Util6")) {
                System.out.println("Fourth segment should be 'Util6'");
                throw new Exception("Failed");
            }
        } finally {
        }
    }
	
	
	private String incrementalPublishGetServletContents() throws Exception {
		Map<String, String> changedContent;
        List<String> removedContent;
        // incrementally update the class file inside the jar inside the war
		IncrementalManagementModel m = new IncrementalManagementModel();
        changedContent = new HashMap<>();
        removedContent = new ArrayList<>();

        String changedFile = getBundleFile("UtilModel_Change1.class").getAbsolutePath();
        changedContent.put("util/pak/UtilModel.class", changedFile);
        m.addSubDeploymentChanges("out.war", "WEB-INF/lib/UtilOne.jar", changedContent, removedContent);
        
        IJBoss7DeploymentResult r = incrementalPublish("out.war", m, true);
        assertTrue(r.getStatus().isOK());
        String contents = waitForRespose("out/TigerServ", "localhost", 8080);
        System.out.println(contents);
        return contents;
	}
    
    public IJBoss7DeploymentResult incrementalPublish(String deploymentName, 
    		IncrementalManagementModel model,
            boolean redeploy) throws Exception {

    	IJBoss7DeploymentResult result = service.incrementalPublish(createManagementDetails(), 
    			deploymentName, model, redeploy, new NullProgressMonitor());
    	return result;
    }


    public IJBoss7DeploymentResult deploy(String name, File file, 
    		String[] explodedPaths, boolean add)
            throws Exception {
    	IJBoss7DeploymentResult ret = service.deploySync(createManagementDetails(), 
    			name, file, add, explodedPaths, new NullProgressMonitor());
    	return ret;
    }

    public static String waitForRespose(String name, String host, int port) throws IOException {
        HttpURLConnection response1 = waitForResponseCode(200, name, host, port);
        response1.disconnect();
        String result = getResponse(name, host, port);
        return result;
    }

    public static HttpURLConnection waitForResponseCode(int code, String name, String host, int port)
            throws IOException {
        URL url = new URL("http://" + host + ":" + port + "/" + name);
        long until = System.currentTimeMillis() + (10 * 1024);
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
            } catch (SocketException se) {
                resetCount++;
                if (resetCount >= 10) {
                    throw se;
                }
            } finally {
                connection.disconnect();
            }
        }
        throw new RuntimeException("wait on url " + url + " for response code " + code + " timed out.");
    }

    public static String getResponse(String name, String host, int port) throws IOException {
        URL url = new URL("http://" + host + ":" + port + "/" + name);
        HttpURLConnection connection = connect(url);
        String s = toString(new BufferedInputStream(connection.getInputStream()));
        connection.disconnect();
        return s;
    }

    public static String toString(InputStream in) throws IOException {
        StringWriter writer = new StringWriter();
        for (int data = -1; ((data = in.read()) != -1);) {
            writer.write(data);
        }
        return writer.toString();
    }

    private static HttpURLConnection connect(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setAllowUserInteraction(false);
        connection.setConnectTimeout(10 * 1024);
        connection.setInstanceFollowRedirects(true);
        connection.setDoOutput(false);
        return connection;
    }

    public IJBoss7DeploymentResult undeploy(String name, boolean removeFile) throws Exception {
        try {
        	IJBoss7DeploymentResult ret = service.undeploySync(createManagementDetails(), 
        			name, removeFile, new NullProgressMonitor());
        	return ret;
        } catch (Exception e) {
            throw e;
        }
    }
	public IAS7ManagementDetails createManagementDetails() {
		return createManagementDetails(homeDir);
	}
	public static IAS7ManagementDetails createManagementDetails(String homeDir) {
		return new MockAS7ManagementDetails(AS7ManagerTestUtils.LOCALHOST, util.getPort(), homeDir);
	}
}