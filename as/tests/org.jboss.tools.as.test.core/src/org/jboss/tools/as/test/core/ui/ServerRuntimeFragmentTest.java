/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.ui;

import java.util.Collection;

import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.TestCase;

/**
 * This class will test that wizard fragments are found
 * for each server and runtime type
 * 
 * @author rob
 *
 */
@RunWith(value = Parameterized.class)
public class ServerRuntimeFragmentTest extends TestCase {
	private String serverType;
	private IServer server;
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParameters());
	}
	 
	public ServerRuntimeFragmentTest(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() {
		server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, getClass().getName() + serverType);
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
	
	@Test
	public void testFragmentsResolved() {
		IServerType st = server.getServerType();
		WizardFragment fragment = ServerUIPlugin.getWizardFragment(st.getId());
		assertNotNull(fragment);
		WizardFragment rtFragment = ServerUIPlugin.getWizardFragment(st.getRuntimeType().getId());
		assertNotNull(rtFragment);
	}
}
