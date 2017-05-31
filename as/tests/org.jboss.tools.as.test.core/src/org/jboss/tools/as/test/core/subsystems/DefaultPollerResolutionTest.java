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
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;
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
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerProfileInitializer;
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
public class DefaultPollerResolutionTest extends TestCase {
	private String serverType;
	private String mode;
	private IServer server;
	
	@Parameters
	public static Collection<Object[]> data() {
		String[] allServers = ServerParameterUtils.getAllJBossServerTypeParameters();
		
		String[] modes = new String[]{"local", "rse", null};
		Collection<Object[]> ret = MatrixUtils.toMatrix(new Object[][]{allServers, modes});
		
		String[] as7Servers = new String[]{
				IJBossToolingConstants.SERVER_AS_70,
				IJBossToolingConstants.SERVER_AS_71,
				IJBossToolingConstants.SERVER_WILDFLY_80,
				IJBossToolingConstants.SERVER_WILDFLY_90,
				IJBossToolingConstants.SERVER_WILDFLY_100,
				IJBossToolingConstants.SERVER_WILDFLY_110,
				IJBossToolingConstants.SERVER_EAP_60,
				IJBossToolingConstants.SERVER_EAP_61,
				IJBossToolingConstants.SERVER_EAP_70,
				IJBossToolingConstants.SERVER_EAP_71
				//NEW_SERVER_ADAPTER 
		};
		String[] as7Modes = new String[]{"local.mgmt", "rse.mgmt"};
		ret.addAll(MatrixUtils.toMatrix(new Object[][]{as7Servers, as7Modes}));
		return ret;
	}
	 
	public DefaultPollerResolutionTest(String serverType, String mode) {
		this.serverType = serverType;
		this.mode = mode;
	}
	
	@Before
	public void setUp() throws Exception {
		server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, getClass().getName() + serverType);
		IServerWorkingCopy wc = server.createWorkingCopy();
		ServerProfileModel.setProfile(wc, mode);
		IServerProfileInitializer[] initializers = ServerProfileModel.getDefault().getInitializers(wc.getServerType().getId(), mode);
		for( int i = 0; i < initializers.length; i++ ) {
			initializers[i].initialize(wc);
		}

		server = wc.save(false, new NullProgressMonitor());
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
	
	
	@Test
	public void testResolution() throws Exception {
		String currentMode = ServerProfileModel.getProfile(server);
		IServerStatePollerType[] startupTypes = ExtensionManager.getDefault().getStartupPollers(server.getServerType(), currentMode);
		IServerStatePollerType[] shutdownTypes = ExtensionManager.getDefault().getShutdownPollers(server.getServerType(), currentMode);
		
		assertNotNull(startupTypes);
		assertNotNull(shutdownTypes);
		assertTrue(startupTypes.length > 0);
		assertTrue(shutdownTypes.length > 0);
		
		
		String currentStartId = server.getAttribute(IJBossServerConstants.STARTUP_POLLER_KEY, IJBossServerConstants.DEFAULT_STARTUP_POLLER);
		String currentStopId = server.getAttribute(IJBossServerConstants.SHUTDOWN_POLLER_KEY, IJBossServerConstants.DEFAULT_SHUTDOWN_POLLER);
		
		if( ! contains(startupTypes, currentStartId)) {
			System.out.println("Dead");
		}
		
		
		assertTrue("Startup poller list for " + server.getServerType().getId() + 
				" and mode " + mode + " must contain " + currentStartId, contains(startupTypes, currentStartId));
		assertTrue("Shutdown poller list for " + server.getServerType().getId() + 
				" and mode " + mode + " must contain " + currentStopId, contains(shutdownTypes, currentStopId));
	}
	
	private boolean contains(IServerStatePollerType[] list, String needle) {
		for( int i = 0; i < list.length; i++ ) {
			if( list[i].getId().equals(needle)) {
				return true;
			}
		}
		return false;
	}
}