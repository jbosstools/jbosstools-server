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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.ui.subsystems.IBrowseBehavior;
import org.jboss.ide.eclipse.as.ui.subsystems.IExploreBehavior;
import org.jboss.ide.eclipse.as.ui.subsystems.IJBossLaunchTabProvider;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IModuleStateController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerDetailsController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerShutdownController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleRestartBehaviorController;
import org.jboss.tools.as.core.server.controllable.systems.IPortsController;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


/**
 * This test will test anything to do with server profiles
 */
@RunWith(value = Parameterized.class)
public class ServerProfileTest extends TestCase {
	private String serverType;
	
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		String[] allServers = ServerParameterUtils.getAllJBossServerTypeParameters();
		return MatrixUtils.toMatrix(new Object[][]{allServers});
	}
	 
	public ServerProfileTest(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
	
	
	@Test
	public void testResolution() throws Exception {
		boolean localRequiresRuntime = ServerProfileModel.getDefault().profileRequiresRuntime(serverType, "local");
		assertTrue(localRequiresRuntime);
		boolean rseRequiresRuntime = ServerProfileModel.getDefault().profileRequiresRuntime(serverType, "rse");
		assertFalse(rseRequiresRuntime);
	}
}