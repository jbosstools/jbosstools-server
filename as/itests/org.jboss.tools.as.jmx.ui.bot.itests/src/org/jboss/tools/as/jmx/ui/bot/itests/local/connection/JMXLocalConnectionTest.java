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

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.swt.api.Button;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.text.DefaultText;
import org.jboss.tools.as.jmx.ui.bot.itests.JMXTestTemplate;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionItem;
import org.jboss.tools.jmx.reddeer.ui.editor.MBeanEditor;
import org.jboss.tools.jmx.reddeer.ui.editor.OperationsPage;
import org.junit.Test;

/**
 * Integration UI test class covering Local Processes JMX features
 * @author odockal
 *
 */
public class JMXLocalConnectionTest extends JMXTestTemplate {

	protected JMXConnectionItem local;
	
	private static final Logger log = Logger.getLogger(JMXLocalConnectionTest.class);

	@Override
	public void setUpJMXConnection() {
		local = view.getLocalProcessesItem();
		try {
			connection = local.getConnectionsIgnoreCase("eclipse").get(0);
		} catch (Exception e) {
			log.error(e.getMessage());
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testLocalEclipseConnectionOperation() {
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
