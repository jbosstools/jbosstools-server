/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.ide.eclipse.as.reddeer.server.deploy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.matcher.RegexMatcher;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.condition.WidgetIsFound;
import org.eclipse.reddeer.core.matcher.TreeItemTextMatcher;
import org.eclipse.reddeer.eclipse.condition.ServerModuleHasState;
import org.eclipse.reddeer.eclipse.core.resources.Project;
import org.eclipse.reddeer.eclipse.exception.EclipseLayerException;
import org.eclipse.reddeer.eclipse.ui.browser.BrowserEditor;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ModuleLabel;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.Server;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServerModule;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersView2;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersViewEnums.ServerPublishState;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersViewEnums.ServerState;
import org.eclipse.reddeer.eclipse.wst.server.ui.wizard.ModifyModulesDialog;
import org.eclipse.reddeer.eclipse.wst.server.ui.wizard.ModifyModulesPage;
import org.eclipse.reddeer.jface.wizard.WizardDialog;
import org.eclipse.reddeer.swt.api.Browser;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.PageIsLoaded;
import org.eclipse.reddeer.swt.impl.browser.InternalBrowser;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.swt.impl.tree.DefaultTree;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.core.exception.WorkbenchCoreLayerException;
import org.eclipse.reddeer.workbench.exception.WorkbenchLayerException;
import org.eclipse.reddeer.workbench.impl.editor.DefaultEditor;
import org.jboss.ide.eclipse.as.reddeer.server.view.JBossServer;
import org.jboss.ide.eclipse.as.reddeer.server.view.JBossServerModule;

/**
 * 
 * Class is deploying project on running server and checks, whether the project
 * has been deployed correctly and it is running.
 * 
 * @author rhopp, jkopriva
 *
 */

public class DeployOnServer {

	private static final Logger log = Logger.getLogger(DeployOnServer.class);

	public void checkServerStatus(String serverName) {
		ServersView2 serversView = new ServersView2();
		serversView.open();
		Server server = serversView.getServer(serverName);
		assertTrue("Server has not been started!", server.getLabel().getState() == ServerState.STARTED);
	}

	/**
	 * 
	 * Deploy, check deployment and undeploy project on running server.
	 * 
	 * @param projectToDeploy
	 *            name of project to deploy
	 * @param serverName
	 *            name of server
	 */
	public void deployUndeployProjectToServer(String projectToDeploy, String serverName) {
		try {
			new ConsoleView().clearConsole();
		} catch (WorkbenchLayerException e) {
			//swallow - console view is not opened
		}

		// deploy
		deployProject(projectToDeploy, serverName);
		// check deploy status
		checkDeployedProject(projectToDeploy, serverName);
		// undeploy
		unDeployModule(projectToDeploy, serverName);
	}

	/**
	 * 
	 * Undeploy module from running server.
	 * 
	 * @param moduleName
	 *            name of module to undeploy
	 * @param serverName
	 *            name of server
	 */
	public void unDeployModule(String moduleName, String serverName) {
		log.info("UNDEPLOYING MODULE" + moduleName + " ON SERVER " + serverName);
		ServersView2 serversView = new ServersView2();
		serversView.open();
		Server server = serversView.getServer(serverName);
		ServerModule serverModule = server.getModule(new RegexMatcher(".*" + moduleName + ".*"));
		serverModule.remove();
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}

	/**
	 * 
	 * Deploy project on running server from context menu "Run on Server".
	 * 
	 * @param deployableProject
	 *            name of project to deploy
	 * @param serverName
	 *            name of server
	 */
	public void deployProject(String deployableProject, String serverName) {
		log.info("DEPLOYING " + deployableProject);
		ProjectExplorer explorer = new ProjectExplorer();
		explorer.activate();
		Project project = explorer.getProject(deployableProject);
		project.select();
		new ContextMenuItem("Run As", "1 Run on Server").select();
		selectServer(serverName);
		new WizardDialog("Run On Server").finish();
	}
	
	private List<TreeItem> getServers() {
		for(TreeItem ti : new DefaultTree().getItems()) {
			if(ti.getCell(0).equals("localhost")) {
				return ti.getItems();
			}
		}
		return new ArrayList<TreeItem>();
	}

	private void selectServer(String server) {
		for (TreeItem ti : getServers()) {
			if(ti.getCell(0).equals(server)) {
				ti.select();
				break;
			}
		}
	}

	/**
	 * 
	 * Deploy project on running server from module selection dialog.
	 * 
	 * @param projectName
	 *            name of project to deploy
	 * @param serverName
	 *            name of server
	 */
	@SuppressWarnings("unchecked")
	public void deployProjectModule(String projectName, String serverName) {
		ServersView2 serversView = new ServersView2();
		serversView.open();
		ModifyModulesDialog modulesDialog = serversView.getServer(JBossServer.class, serverName).addAndRemoveModules();
		String moduleName = new DefaultTreeItem(new TreeItemTextMatcher(new RegexMatcher(".*" + projectName + ".*")))
				.getText();
		new ModifyModulesPage(modulesDialog).add(moduleName);
		modulesDialog.finish();
		new WaitUntil(new WaitForProjectToStartAndSynchronize(moduleName, serverName), TimePeriod.LONG);
	}

	/**
	 * 
	 * Restart running server.
	 * 
	 * 
	 * @param serverName
	 *            name of server
	 */
	public void restartServer(String serverName) {
		ServersView2 serversView = new ServersView2();
		serversView.open();
		Server server = serversView.getServer(serverName);
		server.clean();
		server.restart();
	}

	/**
	 * 
	 * Close browser if it was opened.
	 * 
	 */
	protected static void closeBrowser() {
		try {
			BrowserEditor browser = new BrowserEditor(new RegexMatcher(".*"));
			while (browser != null) {
				browser.close();
				try {
					browser = new BrowserEditor(new RegexMatcher(".*"));
				} catch (WorkbenchCoreLayerException ex) {
					// Browser editor is not opened
					browser = null;
				}
			}
		} catch (WorkbenchCoreLayerException ex) {
			return;
		}
	}

	/**
	 * 
	 * Checks whether is project deployed properly.
	 * 
	 * @param projectName
	 * @param serverNameLabel
	 */
	public void checkDeployedProject(String projectName, String serverNameLabel) {
		closeBrowser();
		ServersView2 serversView = new ServersView2();
		serversView.open();
		String moduleName = projectName;
		JBossServerModule module = serversView.getServer(JBossServer.class, serverNameLabel)
				.getModule(new RegexMatcher(".*" + moduleName + ".*"));
		new WaitUntil(new ServerModuleHasState(module, ServerState.STARTED));
		if (new ContextMenuItem("Show In", "Web Browser").isEnabled()) {
			module.openWebPage();
			new WaitUntil(new WidgetIsFound(org.eclipse.swt.browser.Browser.class), false);

			InternalBrowser browser = new InternalBrowser();
			try {
				new WaitUntil(new PageIsLoaded(browser));
			} catch (WaitTimeoutExpiredException e) {
				// try to refresh browser and wait one more time.
				browser.refresh();
				new WaitUntil(new PageIsLoaded(browser));
			}

			// Now the browser should not be empty. Let's check for error
			// messages
			// (strings like "404")
			checkBrowserForErrorPage(browser);
			assertNotEquals("", getTextFromBrowser(browser));
			new DefaultEditor().close();
		}
		checkConsoleForException();
		checkServerViewForStatus(moduleName, serverNameLabel);
	}

	/**
	 * 
	 * Checks console, if project has been deployed without error.
	 * 
	 */
	protected void checkConsoleForException() {
		ConsoleView consoleView = new ConsoleView();
		consoleView.open();
		assertFalse("Console contains text 'Operation (\"deploy\") failed':\n" + consoleView.getConsoleText(),
				consoleView.getConsoleText().contains("Operation (\"deploy\") failed"));
	}

	/**
	 * 
	 * Checks server view, if module has been started.
	 * 
	 * @param moduleName
	 *            name of project to deploy
	 * @param serverName
	 *            name of server
	 */
	protected void checkServerViewForStatus(String moduleName, String serverNameLabel) {
		ServersView2 serversView = new ServersView2();
		serversView.open();
		Server server = serversView.getServer(serverNameLabel);
		ServerModule serverModule = server.getModule(new RegexMatcher(".*" + moduleName + ".*"));
		ModuleLabel moduleLabel = serverModule.getLabel();
		ServerState moduleState = moduleLabel.getState();
		org.junit.Assert.assertTrue("Module has not been started!", moduleState == ServerState.STARTED);
	}

	/**
	 * 
	 * Checks browser, if module is running and address is correct.
	 * 
	 * @param browserEditor
	 */
	public static void checkBrowserForErrorPage(Browser browser) {
		evaluateBrowserPage(getTextFromBrowser(browser));
	}
	
	private static String getTextFromBrowser(Browser browser) {
		new WaitUntil(new PageIsLoaded(browser));

		if (Platform.getOS().startsWith(Platform.OS_WIN32)) {
			return browser.getText();
		} else {
			// Workaround for webkit issues with method browser.getText(), e.g.
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=514719
			String pageHTML = "";
			if (!StringUtils.isEmpty(browser.getURL())) {
				pageHTML = (String) browser.evaluate("return document.documentElement.innerHTML;");
			}
			return pageHTML;
		}
		
	}

	/**
	 * 
	 * Evaluate page text, if it not empty or without error.
	 * 
	 * @param browserPage
	 */
	private static void evaluateBrowserPage(String browserPage) {
		ConsoleView consoleView = new ConsoleView();
		consoleView.open();
		assertFalse(
				"Browser contains text 'Status 404'\n Console output:\n" + consoleView.getConsoleText()
						+ System.getProperty("line.separator") + "Browser contents:" + browserPage,
				browserPage.contains("Status 404") || browserPage.contains("404 - Not Found"));
		assertFalse(
				"Browser contains text 'Error processing request'\n Console output:\n" + consoleView.getConsoleText()
						+ System.getProperty("line.separator") + "Browser contents:" + browserPage,
				browserPage.contains("Error processing request"));
		assertFalse(
				"Browser contains text 'Forbidden'\n Console output:\n" + consoleView.getConsoleText()
						+ System.getProperty("line.separator") + "Browser contents:" + browserPage,
				browserPage.contains("Forbidden"));

	}

	/**
	 * 
	 * Wait condition if project is synchronized and started on server.
	 * 
	 */
	class WaitForProjectToStartAndSynchronize extends AbstractWaitCondition {

		String projectName;
		String serverNameLabel;
		JBossServerModule module = null;

		public WaitForProjectToStartAndSynchronize(String projectName, String serverNameLabel) {
			this.projectName = projectName;
			this.serverNameLabel = serverNameLabel;
		}

		public boolean test() {
			boolean synch = getModule().getLabel().getPublishState().compareTo(ServerPublishState.SYNCHRONIZED) == 0;
			boolean started = getModule().getLabel().getState().compareTo(ServerState.STARTED) == 0;
			return synch && started;
		}

		public String description() {
			return "Waiting for module to be started-synchronized, but was " + getModule().getLabel().getState() + "-"
					+ getModule().getLabel().getPublishState();
		}

		private JBossServerModule getModule() {
			int counter = 0;
			while (module == null && counter < 5) {
				ServersView2 serversView = new ServersView2();
				serversView.open();
				try {
					module = serversView.getServer(JBossServer.class, serverNameLabel).getModule(projectName);
				} catch (EclipseLayerException ex) {
					// module not found
					counter++;
				}
			}
			return module;
		}
	}

}
