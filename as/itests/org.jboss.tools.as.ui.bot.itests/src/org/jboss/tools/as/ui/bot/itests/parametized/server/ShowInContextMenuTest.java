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
import static org.junit.Assert.fail;

import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.eclipse.condition.BrowserContainsText;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.Server;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersView2;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.server.ServerRequirementState;
import org.eclipse.reddeer.swt.api.Browser;
import org.eclipse.reddeer.swt.impl.browser.InternalBrowser;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerRequirement.JBossServer;
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
	}
	
	@Test
	public void showInWebBrowserIsDisabledOnStoppedServer(){
		assertFalse("Web Browser option in Show In context menu is active even though server is stopped",
				new ContextMenu().getItem("Show In", "Web Browser").isEnabled());
	}
	
	
	@Test
	public void showInWebBrowserIsEnabled() {
		if(!server.getLabel().getState().isRunningState()) {
			server.start();
		}
		
		sv.open();
		server = sv.getServer("WildFly 24+ Server");
		
		assertTrue("Web Browser option in Show In context menu is inactive even though server is running",
				new ContextMenu().getItem("Show In", "Web Browser").isEnabled());
	} 
	@Test
	public void showInWebBrowserOpensProperPage() throws InterruptedException {
		if(!server.getLabel().getState().isRunningState()) {
			server.start();
		}
		
		sv.open();
		server = sv.getServer("WildFly 24+ Server");
		
		new ContextMenu().getItem("Show In", "Web Browser").select();
		
		Browser browser = new InternalBrowser();
		
		try {
			new WaitUntil(new BrowserContainsText("WildFly"));
			
		} catch (WaitTimeoutExpiredException exc) {
			fail("Web browser does not contain proper text: It has " + browser.getText());
		}

	}
}