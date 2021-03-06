/*******************************************************************************
 * Copyright (c) 2014-2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.subsystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerProfileInitializer;
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

import junit.framework.TestCase;


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
	
	@Parameters(name = "{0}, {1}")
	public static Collection<Object[]> data() {
		String[] allServers = ServerParameterUtils.getAllJBossServerTypeParameters();
		
		String[] modes = new String[]{"local", "rse", null};
		Collection<Object[]> ret = MatrixUtils.toMatrix(new Object[][]{allServers, modes});
		
		String[] EXCLUDED = new String[] {
				IJBossToolingConstants.SERVER_AS_32,
				IJBossToolingConstants.SERVER_AS_40,
				IJBossToolingConstants.SERVER_AS_42,
				IJBossToolingConstants.SERVER_AS_50,
				IJBossToolingConstants.SERVER_AS_51,
				IJBossToolingConstants.SERVER_AS_60,
				IJBossToolingConstants.SERVER_EAP_43,
				IJBossToolingConstants.SERVER_EAP_50
			};

		ArrayList<String> toTest = new ArrayList<String>(Arrays.asList(IJBossToolingConstants.ALL_JBOSS_SERVERS));
		toTest.removeAll(Arrays.asList(EXCLUDED));
		String[] as7Servers = (String[]) toTest.toArray(new String[toTest.size()]);
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
