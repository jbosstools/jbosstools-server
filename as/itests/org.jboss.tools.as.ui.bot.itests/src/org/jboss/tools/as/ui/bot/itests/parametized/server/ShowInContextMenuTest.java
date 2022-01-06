 /*******************************************************************************
 * Copyright (c) 2007-2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.ui.bot.itests.parametized.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.eclipse.condition.BrowserContainsText;
import org.eclipse.reddeer.eclipse.debug.ui.views.launch.LaunchView;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.Server;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersView2;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.closeeditors.CloseAllEditorsRequirement.CloseAllEditors;
import org.eclipse.reddeer.requirements.server.ServerRequirementState;
import org.eclipse.reddeer.swt.api.Browser;
import org.eclipse.reddeer.swt.api.MenuItem;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.PageIsLoaded;
import org.eclipse.reddeer.swt.impl.browser.InternalBrowser;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.eclipse.reddeer.workbench.core.exception.WorkbenchCoreLayerException;
import org.eclipse.reddeer.workbench.impl.editor.DefaultEditor;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.InternalBrowserRequirement.UseInternalBrowser;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerRequirement.JBossServer;
import org.jboss.tools.jmx.reddeer.core.JMXConnection;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionItem;
import org.jboss.tools.jmx.reddeer.ui.view.JMXNavigatorView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for testing options in Show In context menu
 * 
 * @author Lukas Grossmann
 * 
 * TODO: Add more tests for Show In context menu options
 */
@RunWith(RedDeerSuite.class)
@CloseAllEditors
@UseInternalBrowser
@JBossServer(state=ServerRequirementState.PRESENT)
public class ShowInContextMenuTest {
	
	private ServersView2 sv;
	private Server server;
	
	@Before
	public void selectServer() {
		sv = new ServersView2();
		sv.open();
		server = sv.getServer("WildFly 24+ Server");
		server.select();
	}
	
	@After
	public void cleanUp() {
		if(server.getLabel().getState().isRunningState()) {
			server.stop();
		}
		sv.close();
		try {
			new DefaultEditor().close();
		} catch (WorkbenchCoreLayerException exc) {
			// no editor opened
		}
	}
	
	//Stopped server check
	
	@Test
	public void showInWebBrowserIsDisabledOnStoppedServer(){
		assertFalse(errorMsg("Web Browser", false), isEnabled("Web Browser"));
	}
	
	@Test
	public void showInConsoleIsDisabledOnStoppedServer() {
		assertFalse(errorMsg("Console", false), isEnabled("Console"));
	}
	
	@Test
	public void showInDebugIsDisabledOnStoppedServer() {
		assertFalse(errorMsg("Debug", false), isEnabled("Debug"));
	}
	
	@Test
	public void showInJMXNavigatorIsDisabledOnStoppedServer() {
		assertFalse(errorMsg("JMX Navigator", false), isEnabled("JMX Navigator"));
	}
	
	@Test
	public void showInFileBrowserIsEnabledOnStoppedServer() {
		assertTrue("File Browser option in Show In context menu is inactive. Should be alway active.", 
				isEnabled("File Browser"));
	}
	
	@Test
	public void showInWebManagementConsoleIsHiddenOnDisabledServer() {
		assertFalse("Web Management Console is shown in context menu, but should not be.", isInContextMenu());
	}
	
	//Running server check
	
	@Test
	public void showInWebBrowserIsEnabledOnRunningServer() {
		startAndSelectServer();
		
		assertTrue(errorMsg("Web Browser", true), isEnabled("Web Browser"));
	} 
	
	@Test
	public void showInConsoleIsEnabledOnRunningServer() {
		startAndSelectServer();
		
		assertTrue(errorMsg("Console", true), isEnabled("Console"));
	}
	
	@Test
	public void showInDebugIsEnabledOnRunningServer() {
		startAndSelectServer();
		
		assertTrue(errorMsg("Debug", true), isEnabled("Debug"));
	}
	
	
	@Test
	public void showInJMXNavigatorIsEnabledOnRunningServer() {
		startAndSelectServer();
		
		assertTrue(errorMsg("JMX Navigator", true), isEnabled("JMX Navigator"));
	}
	
	@Test
	public void showInFileBrowserIsEnabledOnRunningServer() {
		startAndSelectServer();
		
		assertTrue("File Browser option in Show In context menu is inactive. Should be alway active.",
				isEnabled("File Browser"));
	}
	
	@Test
	public void showInWebManagementConsoleIsShownOnRunningServer() {
		startAndSelectServer();
		
		assertTrue("Web Management Console is missing from the context menu", isInContextMenu());
	} 
	
	//Options do what are they supposed to do
	
	@Test
	public void showInWebBrowserOpensProperPage() throws InterruptedException {
		startAndSelectServer();
		
		selectItem("Web Browser");
		
		testBrowser("WildFly");
	}
	
	@Test
	public void showInWebManagementConsoleOpensProperPage() throws InterruptedException {
		startAndSelectServer();
		
		selectItem("Web Management Console");
		
		testBrowser("Management Console");
	}
	
	@Test
	public void showInConsoleOpensProperPage() {
		startAndSelectServer();
		
		selectItem("Console");
		
		ConsoleView cv = new ConsoleView();
		
		assertTrue("Console does not contain proper text: It has " + cv.getConsoleText(),
				cv.getConsoleText().contains("wildfly"));
	}
	
	@Test
	public void showInDebugOpensProperPage() {
		startAndSelectServer();
		
		selectItem("Debug");
		
		sv.open();
		server.select();
		
		selectItem("Debug");
		
		LaunchView dv = new LaunchView();
			
		boolean containsWildfly = false;
		
		for (TreeItem item : dv.getSelectedItem().getParent().getAllItems()) {
			if (item.getText().contains("WildFly")) {
				containsWildfly = true;
				break;
			}
		}
		
		assertTrue("Debug View does not contain WildFly server", containsWildfly);
	}
	
	@Test
	public void showInJMXNavigatorOpensProperPage() {
		startAndSelectServer();
		
		selectItem("JMX Navigator");
		
		JMXNavigatorView jmx = new JMXNavigatorView();
		
		JMXConnectionItem jmxConnectionsItem = jmx.getServerConnectionsItem();
		
		boolean containsWildfly = false;
		
		for (JMXConnection connection : jmxConnectionsItem.getConnections()) {
			if (connection.getLabel().getName().contains("WildFly")){
				containsWildfly = true;
				break;
			}
		}
		
		assertTrue("JMX Navigator does not contain WildFly server", containsWildfly);
	}
	
	//Methods for simpler code
	
	private void startAndSelectServer() {
		if(!server.getLabel().getState().isRunningState()) {
			server.start();
		}
		
		sv.open();
		server = sv.getServer("WildFly 24+ Server");
	}
	
	private void selectItem(String item) {
		new ContextMenu().getItem("Show In",item).select();
	}
	
	private boolean isEnabled(String item) {
		return new ContextMenu().getItem("Show In", item).isEnabled();
	}
	
	private String errorMsg(String option, boolean state) {
		String status = (state) ? "active" : "inactive";
		String reverseStatus = (state) ? "inactive" : "active";
		
		return option + " option in Show in context menu is " + status + ". Should be " + reverseStatus + ".";
	}
	
	private boolean isInContextMenu() {
		List<MenuItem> items = new ContextMenu().getItem("Show In").getChildItems();
		
		for (MenuItem item : items) {
			if (item.getText().contains("Web Management Console")) {
				return true;
			}
		}
		
		return false;
	}
	
	private void testBrowser(String text) {
		new WaitUntil(new BrowserContainsText(text), TimePeriod.MEDIUM, false);
		Browser browser = new InternalBrowser();
		String browserText = browser.getText();
		assertTrue("Web browser does not contain proper text: It has " + browserText, browserText.contains(text));
	}
}
