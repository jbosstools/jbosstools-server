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
package org.jboss.tools.as.jmx.ui.bot.itests.server.connection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.Server;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersView2;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersViewEnums.ServerState;
import org.eclipse.reddeer.junit.requirement.inject.InjectRequirement;
import org.eclipse.reddeer.swt.api.Button;
import org.eclipse.reddeer.swt.api.MenuItem;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.swt.widgets.Shell;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerRequirement;
import org.jboss.tools.as.jmx.ui.bot.itests.JMXTestTemplate;
import org.jboss.tools.jmx.reddeer.core.JMXConnection;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionItem;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionState;
import org.jboss.tools.jmx.reddeer.ui.editor.MBeanEditor;
import org.jboss.tools.jmx.reddeer.ui.editor.OperationsPage;

public abstract class JMXServerTestTemplate extends JMXTestTemplate {

	protected JMXConnectionItem serverItem;
	protected Server server;
	
	private static final Logger log = Logger.getLogger(RemoteServerJMXConnectionTest.class);
	
	@InjectRequirement
	protected static ServerRequirement serverConfig;

	@Override
	public void setUpJMXConnection() {
		// suppressed
	}
	
	public void startServer() {
		getServer(serverConfig.getServerName()).start();
	}
	
	public void getServerJMXConnection() {
		try {
			connection = serverItem.getConnections(serverConfig.getServerName()).get(0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("There is no " + serverConfig.getServerName() + " connection");
		}
	}

	public void stopServer() {
		Server server = getServer(serverConfig.getServerName());
		if (server.getLabel().getState() != ServerState.STOPPED) {
			try {
				server.stop();
			} catch (WaitTimeoutExpiredException exc) {
				log.error(exc.getMessage() + ", Server cannot be stopped, terminating");
				// workaround for 
				ShellIsAvailable shell = new ShellIsAvailable("Terminate Server");
				new WaitUntil(shell, false);
				if (shell.getResult() != null) {
					processShell(shell.getResult());
				}
				terminateServer(server);
				if (server.getLabel().getState().isRunningState()) {
					terminateServer(server);
				}
			}
		}
	}
	
	private void terminateServer(Server server) {
		ServersView2 view = new ServersView2();
		view.open();
		server.select();
		MenuItem item = new ContextMenu().getItem("Show In", "Console");
		if (item.isEnabled()) {
			item.select();
			new ConsoleView().terminateConsole();
		}
	}
	
	private void processShell(Shell shell) {
		DefaultShell dialog = new DefaultShell(shell);
		new PushButton("Cancel").click();
		new WaitWhile(new ShellIsAvailable(dialog), false);
	}
	
	public Server getServer(String name) {
		ServersView2 view = new ServersView2();
		view.open();
		return view.getServer(name);
	}
	
	public void verifyServerOperation(Runnable run) {
		run.run();
	}
	
	public void verifyJMXServerConnection(String serverName) {
		List<TreeItem> items = server.getTreeItem().getItems();
		JMXConnection serverConnection = null;
		for (TreeItem item : items) {
			if (item.getText().contains("JMX")) {
				item.select();
				serverConnection = new JMXConnection(item);
			}
		}
		if (serverConnection == null) {
			fail("There was no JMX connection item defined under server: " + serverName);
		}
		serverConnection.select();
		serverConnection.getLabel().getState().equals(JMXConnectionState.DISCONNECTED);

		startServer();
		getServerJMXConnection();
		
		assertThat(serverItem.getConnections().size(), is(1));
		
		JMXConnection jmxServerConnection = serverItem.getConnections().get(0);
		
		assertTrue(jmxServerConnection.getLabel().getName().contains(serverName));
		assertThat(jmxServerConnection.getLabel().getState(), is(JMXConnectionState.DISCONNECTED));
		serverConnection.connect();
		assertThat(jmxServerConnection.getLabel().getState(), is(JMXConnectionState.CONNECTED));
	}
	
	public OperationsPage openOperationsPage(JMXConnection connection) {
		connection.openMBeanObjectEditor(0, "jboss.as", "deployment-scanner", "default");
		MBeanEditor editor = new MBeanEditor("jboss.as:scanner=default,subsystem=deployment-scanner");
		editor.activate();
		
		return editor.getOperationsPage();
	}
	
	public Runnable verifyRunScanOperation(final OperationsPage page) {
		final String operationName = "runScan";
		return new Runnable() {
			
			@Override
			public void run() {
				assertTrue("Choosen operation is not available. ", page.containsTableItem(operationName));
				page.selectTableItem(operationName);
				
				Button button = new PushButton(page.getDetailsSection(), operationName);
				button.click();
				ShellIsAvailable result = new ShellIsAvailable("Result");
				new WaitUntil(result, TimePeriod.MEDIUM, false);
				if (result.getResult() != null) {
					new PushButton("OK").click();
					new WaitWhile(result, TimePeriod.MEDIUM);
				} else {
					fail("No result shell was thrown");
				}				
			}
		};
	}

}
