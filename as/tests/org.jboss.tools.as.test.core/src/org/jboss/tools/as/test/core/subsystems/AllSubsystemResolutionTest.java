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
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentPageUIController;
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
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
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
 * This test class only ensures that a non-null result is returned
 * for all required subsystem types in both local and rse mode.
 * 
 * This specific test does NOT verify the proper instance is returned;
 * only that a non-null result is returned.
 */
@RunWith(value = Parameterized.class)
public class AllSubsystemResolutionTest extends TestCase {
	private String serverType;
	private String mode;
	private String system;
	private IServer server;
	
	/**
	 * Get a list of subsystems required for all server types, including deploy-only
	 * @return
	 */
	private static String[] getSubsystemsForAllServers() {
		String[] subsystems = new String[]{
				ILaunchServerController.SYSTEM_ID,
				IServerShutdownController.SYSTEM_ID,
				IModuleStateController.SYSTEM_ID,
				IPublishController.SYSTEM_ID,
				IFilesystemController.SYSTEM_ID,
				IModuleRestartBehaviorController.SYSTEM_ID,
				IServerDetailsController.SYSTEM_ID,
				IDeploymentOptionsController.SYSTEM_ID,
				IModuleDeployPathController.SYSTEM_ID,
				IBrowseBehavior.SYSTEM_ID,
				IExploreBehavior.SYSTEM_ID,
				IDeploymentPageUIController.SYSTEM_ID,
		};
		return subsystems;
	}
	

	private static String[] getSubsystemsForJBossServers() {
		String[] subsystems = new String[]{
				IJBossLaunchTabProvider.SYSTEM_ID,
				IPortsController.SYSTEM_ID,
		};
		return subsystems;
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		String[] allServers = ServerParameterUtils.getAllJBossServerTypeParameters();
		String[] jbossServers = ServerParameterUtils.getJBossServerTypeParameters();
		
		String[] modes = new String[]{"local", "rse", null};
		String[] subsystems = getSubsystemsForAllServers();
		Collection<Object[]> ret = MatrixUtils.toMatrix(new Object[][]{allServers, modes, subsystems});
		ret.addAll(MatrixUtils.toMatrix(new Object[][]{jbossServers, modes, getSubsystemsForJBossServers()}));
		
		String[] as7Servers = new String[]{
				IJBossToolingConstants.SERVER_AS_70,
				IJBossToolingConstants.SERVER_AS_71,
				IJBossToolingConstants.SERVER_WILDFLY_80,
				IJBossToolingConstants.SERVER_EAP_60,
				IJBossToolingConstants.SERVER_EAP_61
		};
		String[] as7Modes = new String[]{"local", "rse", null, "local.mgmt", "rse.mgmt"};
		ret.addAll(MatrixUtils.toMatrix(new Object[][]{as7Servers, as7Modes, getSubsystemsForJBossServers()}));
		return ret;
	}
	 
	public AllSubsystemResolutionTest(String serverType, String mode, String system) {
		this.serverType = serverType;
		this.mode = mode;
		this.system = system;
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
		try {
			ISubsystemController controller = beh.getController(system);
			assertNotNull(controller);
		} catch(CoreException ce) {
			String msg = serverType + " : " + mode + " : " + system + "  ::  " + ce.getMessage(); 
			System.out.println(msg);
			fail(msg);
		}
	}
}