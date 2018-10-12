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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.requirements.server.ServerRequirementState;
import org.eclipse.reddeer.swt.api.Button;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerRequirement.JBossServer;
import org.jboss.tools.jmx.reddeer.core.JMXConnection;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionState;
import org.jboss.tools.jmx.reddeer.ui.editor.MBeanEditor;
import org.jboss.tools.jmx.reddeer.ui.editor.OperationsPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class covering Server Connections functionality
 * @author odockal
 *
 */
@JBossServer(state=ServerRequirementState.PRESENT, cleanup=true)
public class JMXServerConnectionTest extends JMXServerTestTemplate {

	@Before
	public void setupServer() {
		setUpView();
		
		wildfly = getServer(serverConfig.getServerName());
		serverItem = view.getServerConnectionsItem();
	}
	
	@After
	public void tearDownServer() {
		stopServer();
	}
	
	@Test
	public void testJMXServerConnection() {
		List<TreeItem> items = wildfly.getTreeItem().getItems();
		JMXConnection serverConnection = null;
		for (TreeItem item : items) {
			if (item.getText().contains("JMX")) {
				serverConnection = new JMXConnection(item);
			}
		}
		assertNotNull(serverConnection);
		serverConnection.activate();
		serverConnection.getLabel().getState().equals(JMXConnectionState.DISCONNECTED);

		startServer();
		getServerJMXConnection();
		
		assertThat(serverItem.getConnections().size(), is(1));
		
		JMXConnection jmxServerConnection = serverItem.getConnections().get(0);
		
		assertTrue(jmxServerConnection.getLabel().getName().contains("WildFly"));
		assertThat(jmxServerConnection.getLabel().getState(), is(JMXConnectionState.DISCONNECTED));
		serverConnection.connect();
		assertThat(jmxServerConnection.getLabel().getState(), is(JMXConnectionState.CONNECTED));
	}
	
	@Test
	public void testServerOperation() {
		startServer();
		getServerJMXConnection();
		connection.connect();
		
		connection.openMBeanObjectEditor(0, "jboss.as", "deployment-scanner", "default");
		MBeanEditor editor = new MBeanEditor("jboss.as:scanner=default,subsystem=deployment-scanner");
		editor.activate();
		
		OperationsPage op = editor.getOperationsPage();
		
		assertTrue("Choosen operation is not available. ", op.containsTableItem("runScan"));
		op.selectTableItem("runScan");
		
		Button button = new PushButton(op.getDetailsSection(), "runScan");
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

}
