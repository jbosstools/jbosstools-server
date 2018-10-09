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
package org.jboss.tools.jmx.ui.bot.test.server.connection;

import static org.junit.Assert.fail;

import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.Server;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersView2;
import org.eclipse.reddeer.junit.requirement.inject.InjectRequirement;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerRequirement;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionItem;
import org.jboss.tools.jmx.ui.bot.test.JMXTestTemplate;

public class JMXServerTestTemplate extends JMXTestTemplate {

	protected JMXConnectionItem serverItem;
	protected Server wildfly;
	
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
			connection = serverItem.getConnections("WildFly 14").get(0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("There is no WildFly 14 connection");
		}
	}

	public void stopServer() {
		getServer(serverConfig.getServerName()).stop();
	}
	
	public Server getServer(String name) {
		ServersView2 view = new ServersView2();
		view.open();
		return view.getServer(name);
	}

}
