 /*******************************************************************************
 * Copyright (c) 2007-2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.ui.bot.itests.parametized.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.reddeer.common.matcher.VersionMatcher;
import org.eclipse.reddeer.common.util.Display;
import org.eclipse.reddeer.direct.preferences.Preferences;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersView2;
import org.eclipse.reddeer.junit.annotation.RequirementRestriction;
import org.eclipse.reddeer.junit.internal.runner.ParameterizedRequirementsRunnerFactory;
import org.eclipse.reddeer.junit.requirement.matcher.RequirementMatcher;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.jre.JRERequirement.JRE;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.workbench.handler.WorkbenchShellHandler;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.as.reddeer.server.view.JBossServer;
import org.jboss.tools.as.ui.bot.itests.AbstractTest;
import org.jboss.tools.as.ui.bot.itests.Activator;
import org.jboss.tools.as.ui.bot.itests.SuiteConstants;
import org.jboss.tools.as.ui.bot.itests.download.RuntimeDownloadTestUtility;
import org.jboss.tools.as.ui.bot.itests.parametized.server.ServerRuntimeUIConstants.EditorPort;
import org.jboss.tools.as.ui.bot.itests.reddeer.util.DeployJSPProjectTemplate;
import org.jboss.tools.as.ui.bot.itests.reddeer.util.DetectRuntimeTemplate;
import org.jboss.tools.as.ui.bot.itests.reddeer.util.DisableSecureStorageRequirement.DisableSecureStorage;
import org.jboss.tools.as.ui.bot.itests.reddeer.util.OperateServerTemplate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;



/**
 * This test is trying to optimize and clean up the huge number of tests that were here. 
 * 
 * Pre-reqs:
 *   1) -Djbosstools.test.jre.8={java8home}
 *   2) -Djboss.org.username={yourusername}
 *   3) -Djboss.org.password={hunter2}
 *   4) -Druntimes.suite.scope={smoke | latestMajors | allFree | all}
 *   
 *  Items 4 and 5 are optional and only necessary if testing runtimes that 
 *  require $0 subscription. 
 *  
 *  This test no longer needs to download any runtimes in advance. It will download them all via UI.
 *  
 *  There are 3 primary tests here:
 *    1) acquireAndDetect	(download the runtime via ui and make sure it appears)
 *    2) detect				(use the just-downloaded fs-location and use runtime detection)
 *    3) operate			(import via rt-detection and start/stop it)
 *    
 *  JRE requirement is no longer necessary. We simply make sure to add a JRE
 *  for java 6, 7, and 8 in advance, and check to make sure that the given server
 *  starts without the user needing to customize or change the jre at all. 
 *  
 *  Adding new runtimes to the test is as easy as modifying 
 *  ServerRuntimeUIConstants. You should:
 *    1) Declare a constant representing the UI string to download a given runtime
 *    2) Add that constant to the various arrays it should belong to (free, smoke, etc)
 *    3) Add a line to the initialize() method representing the various expected dl-rt values
 */


@RunWith(RedDeerSuite.class)
@JRE(cleanup=true, setDefault = true)
@UseParametersRunnerFactory(ParameterizedRequirementsRunnerFactory.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)//first acquireAndDetect, then detect, then operate
@DisableSecureStorage
public class ServerRuntimesTest extends AbstractTest {

    @Parameters(name = "{0}, {1}")
    public static Collection<Object[]> data(){
    	String scope = System.getProperty(SuiteConstants.SYSPROP_KEY);
    	ArrayList<Object[]> ret = (ArrayList<Object[]>) ServerRuntimeUIConstants.getParametersForScope(scope);
    	return fixCredentialsForEAP(ret);
    }
    
	private static  ArrayList<Object[]> fixCredentialsForEAP(ArrayList<Object[]> paths) {
		ArrayList<Object[]> pathsModified = new ArrayList<Object[]>();
		for(Object[] object:paths) {
			String server = (String) object[0];
			boolean mustUseCredentials = RuntimeDownloadTestUtility.mustUseCredentials(server);
			boolean free = ((Boolean)object[1]).booleanValue() && !mustUseCredentials;
			Object[] modified = { object[0], free};
			pathsModified.add(modified);
		}	
		return pathsModified;
	}

	@RequirementRestriction
	public static RequirementMatcher getRestrictionMatcher() {
	  return new RequirementMatcher(JRE.class, "version", new VersionMatcher("1.8"));
	}

    @BeforeClass
    public static void deleteRuntimesAddedByRuntimeDetection() {
    	Preferences.set("org.eclipse.debug.ui", "Console.limitConsoleOutput", "false");
    	deleteRuntimes();
    }
    
    private String runtimeString;
    private boolean dlType;

    public ServerRuntimesTest(String type, boolean free) {
    	runtimeString = type;
    	dlType = free;
    }
    
    protected File getDownloadPath() {
    	return Activator.getDownloadFolder(runtimeString);
    }
    
    @Test
    public void acquireAndDetect(){
        System.out.println(runtimeString);
        RuntimeDownloadTestUtility util = new RuntimeDownloadTestUtility(getDownloadPath());
    	if( dlType == SuiteConstants.FREE) {
    		util.downloadRuntimeNoCredentials(runtimeString);
    	} else {
    		util.downloadRuntimeWithCredentials(runtimeString);
    	}
    }
    
    @Test
    public void detect(){
    	DetectRuntimeTemplate.detectRuntime(getDownloadPath().getAbsolutePath(), ServerRuntimeUIConstants.getRuntimesForDownloadable(runtimeString));
    	DetectRuntimeTemplate.removePath(getDownloadPath().getAbsolutePath());
    	
    	
    	// Let's also verify the ports situation in the editor
    	String serverName = ServerRuntimeUIConstants.getServerName(runtimeString);
    	ServersView2 jbsview = new ServersView2();
    	if(!jbsview.isOpen()) {
    		jbsview.open();
    	}
		JBossServer server = jbsview.getServer(JBossServer.class, serverName);
		server.open();
		
		EditorPort[] ports = ServerRuntimeUIConstants.getPorts(runtimeString);
		assertNotNull(ports);
		for( int i = 0; i < ports.length; i++ ) {
			assertThat(new LabeledText(ports[i].getLabel()).getText(), is(ports[i].getValue()));
		}
    }
    

    

    
    
    @Test
    public void operate(){
    	DetectRuntimeTemplate.detectRuntime(getDownloadPath().getAbsolutePath(), ServerRuntimeUIConstants.getRuntimesForDownloadable(runtimeString));
    	DetectRuntimeTemplate.removePath(getDownloadPath().getAbsolutePath());
    	String serverName = ServerRuntimeUIConstants.getServerName(runtimeString);
    	OperateServerTemplate operate = new OperateServerTemplate(serverName);
    	operate.setUp();
    	try {
    		operate.operateServer();
    	} finally {
    		operate.cleanServerAndConsoleView();
    	}
    }

    @Test
    public void operateDeploy(){
    	DetectRuntimeTemplate.detectRuntime(getDownloadPath().getAbsolutePath(), ServerRuntimeUIConstants.getRuntimesForDownloadable(runtimeString));
    	DetectRuntimeTemplate.removePath(getDownloadPath().getAbsolutePath());
    	String serverName = ServerRuntimeUIConstants.getServerName(runtimeString);
    	boolean usesJakartaProject = ServerRuntimeUIConstants.usesJakartaNamespaceDeployments(runtimeString);
    	String projectZip = usesJakartaProject ? "projects/jsp-project.zip" : "projects/jsp-project_old.zip"; 
    	OperateServerTemplate operate = new OperateServerTemplate(serverName);
    	operate.setUp();
    	try {
    		operate.startServerSafe();
    		DeployJSPProjectTemplate djsppt = new DeployJSPProjectTemplate();
    		djsppt.clearConsole();
    		djsppt.importProject("jsp-project", projectZip, serverName + " Runtime");
    		
    		String depString = ServerRuntimeUIConstants.getDeployString(runtimeString, "jsp-project", ".war");
    		djsppt.deployProject("jsp-project", serverName, depString);
    		
    		// Now try a hot-deploy
    		djsppt.hotDeployment("jsp-project");
    		
    		// Now try to undeploy
    		String undepString = ServerRuntimeUIConstants.getUndeployString(runtimeString, "jsp-project", ".war");
    		djsppt.undeployProject(serverName, "jsp-project", undepString);
    		
    	} finally {
    		// Cleanup everything
    		operate.stopAndDeleteServer();
    		operate.cleanServerAndConsoleView();
    	}
    }

    

    @After
    public void postTest() {
    	Display.syncExec(new Runnable() {
			@Override
			public void run() {
		    	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true);
			}
		});
    	new RuntimeDownloadTestUtility(getDownloadPath()).clean(false);
    	//Close windows, if opened.
    	WorkbenchShellHandler.getInstance().closeAllNonWorbenchShells();
    }
    
    @AfterClass
    public static void postClass() {
    	new RuntimeDownloadTestUtility(Activator.getStateFolder().toFile()).clean(true);
    }
        

}
