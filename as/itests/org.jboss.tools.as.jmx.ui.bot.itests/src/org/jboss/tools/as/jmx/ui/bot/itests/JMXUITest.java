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
package org.jboss.tools.as.jmx.ui.bot.itests;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.reddeer.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.impl.tree.DefaultTree;
import org.jboss.tools.jmx.reddeer.core.JMXConnection;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionItem;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionState;
import org.junit.After;
import org.junit.Test;

/**
 * Basic UI tests for JMX tooling
 * @author odockal
 *
 */
public class JMXUITest extends JMXTestTemplate {
	
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
	public void testConnectingJMXConnection() {

		assertTrue("There must be available at least on eclipse connection in JMX Navigator. ", local.getConnections().size() > 1);
		
		assertNotNull("There must be available at least on eclipse connection in JMX Navigator. ", connection);

		assertThat("Expected state of eclipse connection is disconnected.", connection.getLabel().getState(), is(JMXConnectionState.DISCONNECTED));
		connection.connect();
		
		assertThat("Expected state of eclipse connection is connected.", connection.getLabel().getState(), is(JMXConnectionState.CONNECTED));
		connection.disconnect();
		
		assertThat("Expected state of eclipse connection is disconnected.", connection.getLabel().getState(), is(JMXConnectionState.DISCONNECTED));
	}
	
	@Test
	public void testLocalEclipseConnectionProperties() {
		connection.connect();
		PropertySheet properties = new PropertySheet();
		properties.open();
		
		properties.selectTab("Overview");
		properties.activate();
		DefaultTree overview = new DefaultTree(properties.getCTabItem());
		List<String> items = new ArrayList<>();
		for (TreeItem item : overview.getItems()) {
			items.add(item.getText());
		}
		assertThat(items, hasItems("Runtime", "Memory", "Thread", "Class loading", "Compilation"));
	}

}
