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
package org.jboss.tools.as.jmx.ui.bot.itests.local.connection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.swt.api.Button;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.text.DefaultText;
import org.jboss.tools.as.jmx.ui.bot.itests.JMXTestTemplate;
import org.jboss.tools.jmx.reddeer.core.JMXConnection;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionItem;
import org.jboss.tools.jmx.reddeer.ui.editor.MBeanEditor;
import org.jboss.tools.jmx.reddeer.ui.editor.OperationsPage;
import org.junit.After;
import org.junit.Test;

/**
 * Integration UI test class covering Local Processes JMX features
 * @author odockal
 *
 */
public class JMXLocalConnectionTest extends JMXTestTemplate {

	protected JMXConnectionItem local;

	@Override
	public void setUpJMXConnection() {
		local = view.getLocalProcessesItem();
		if (local == null) {
			fail("There are no local processes");
		}
		List<JMXConnection> foundConnestions = local.getConnectionsIgnoreCase(JAVA_APP);
		if (!foundConnestions.isEmpty()) {
			connection = foundConnestions.get(0);
		} else {
			String connections = local.getConnections().stream()
			.map( item -> item.getLabel().getName())
			.collect( Collectors.joining("\r\n"));
			fail("There is no connection like searched one: " + JAVA_APP + 
					"Only available connections are: " + connections);
		}
	}
	
	@After
	public void cleanUp() {
		if (local != null) {
			local = null;
		}
	}	
	
	@Test
	public void testLocalJavaConnectionOperation() {
		connection.connect();
		connection.openMBeanObjectEditor(0, "com.sun.management", "DiagnosticCommand");
		MBeanEditor editor = new MBeanEditor("com.sun.management:type=DiagnosticCommand");
		editor.activate();
		
		OperationsPage op = editor.getOperationsPage();
		
		assertTrue("Choosen operation is not available. ", op.containsTableItem("vmVersion"));
		op.selectTableItem("vmVersion");
		
		Button button = new PushButton(op.getDetailsSection(), "vmVersion");
		button.click();
		ShellIsAvailable result = new ShellIsAvailable("Result");
		new WaitUntil(result, TimePeriod.MEDIUM, false);
		if (result.getResult() != null) {
			DefaultText text = new DefaultText();
			assertTrue("VmVersion operation contains 'JDK' string", 
					text.getText().toLowerCase().contains("jdk"));
			new PushButton("OK").click();
			new WaitWhile(result, TimePeriod.MEDIUM);
		} else {
			fail("No result shell was thrown");
		}
	}

}
