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

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.impl.tree.DefaultTree;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionItem;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionState;
import org.junit.Test;

/**
 * Basic UI tests for JMX tooling
 * @author odockal
 *
 */
public class JMXUITest extends JMXTestTemplate {
	
	protected JMXConnectionItem local;
	
	private static final Logger log = Logger.getLogger(JMXUITest.class);

	@Override
	public void setUpJMXConnection() {
		local = view.getLocalProcessesItem();
		try {
			connection = local.getConnections("eclipse").get(0);
		} catch (Exception e) {
			log.error(e.getMessage());
			fail(e.getMessage());
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
