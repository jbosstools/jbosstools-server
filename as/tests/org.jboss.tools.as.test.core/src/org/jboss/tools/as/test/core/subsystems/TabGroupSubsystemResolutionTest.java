/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.subsystems;

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.rse.ui.RSELaunchTabProvider;
import org.jboss.ide.eclipse.as.ui.subsystems.IJBossLaunchTabProvider;
import org.jboss.ide.eclipse.as.ui.subsystems.internal.JBoss7StandardTabProvider;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TabGroupSubsystemResolutionTest extends TestCase {
	private String serverType;
	private String mode;
	private IServer server;
	@Parameters(name = "{0}, {1}")
	public static Collection<Object[]> data() {
		String[] servers = ServerParameterUtils.getJBossServerTypeParameters();
		String[] modes = new String[]{"local", "rse", null};
		return MatrixUtils.toMatrix(new Object[][]{servers, modes});
	}
	 
	public TabGroupSubsystemResolutionTest(String serverType, String mode) {
		this.serverType = serverType;
		this.mode = mode;
	}
	
	@Before
	public void setUp() throws Exception {
		server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, getClass().getName() + serverType);
		IServerWorkingCopy wc = server.createWorkingCopy();
		ServerProfileModel.setProfile(wc, mode);
		server = wc.save(false, new NullProgressMonitor());
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
	
	
	@Test
	public void testResolution() throws Exception {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(server);
		assertNotNull(beh);
		ISubsystemController controller = beh.getController(IJBossLaunchTabProvider.SYSTEM_ID);
		assertNotNull(controller);
		if( mode == null || mode.equals("local")) {
			assertTrue( controller instanceof JBoss7StandardTabProvider);
		} else if( mode.equals("rse")) {
			assertTrue(controller instanceof RSELaunchTabProvider);
		}
	}
}