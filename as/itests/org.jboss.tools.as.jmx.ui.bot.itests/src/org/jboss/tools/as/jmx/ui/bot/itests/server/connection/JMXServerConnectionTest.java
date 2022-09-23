/******************************************************************************* 
 * Copyright (c) 2018-2019 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.jmx.ui.bot.itests.server.connection;

import org.eclipse.reddeer.junit.annotation.RequirementRestriction;
import org.eclipse.reddeer.junit.requirement.matcher.RequirementMatcher;
import org.eclipse.reddeer.requirements.server.ServerRequirementState;
import org.hamcrest.core.IsNull;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerRequirement.JBossServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class covering Server Connections functionality
 * 
 * @author odockal
 *
 */
@JBossServer(state = ServerRequirementState.PRESENT, cleanup = true)
public class JMXServerConnectionTest extends JMXServerTestTemplate {
	
	@RequirementRestriction
	public static RequirementMatcher getRestrictionMatcher() {
		return new RequirementMatcher(JBossServer.class, "remote", new IsNull<Object>());
	}

	@Before
	public void setupServer() {
		setUpView();

		server = getServer(serverConfig.getServerName());
		serverItem = view.getServerConnectionsItem();
	}

	@After
	public void tearDownServer() {
		stopServer();
	}
	

	@Test
	public void testJMXServerConnection() {
		verifyJMXServerConnection(serverConfig.getServerName());
	}

	@Test
	public void testServerOperation() {
		startServer();
		getServerJMXConnection();
		connection.connect();

		verifyServerOperation(verifyRunScanOperation(openOperationsPage(connection)));
	}

}
