/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.parametized.server;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.TestConstants;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
/**
 * This test will create run the server bean loader on all runtimes that are marked as jboss runtimes, 
 * which are all astools runtime types currently. If a type is found which does not have a cmd line arg, 
 * that test will fail.  It will verify that the proper type and version for serverbean is found
 * for the given server home. 
 * 
 *  It will also make MOCK structures for all server types, including 'subtypes' like 
 *  jpp, gatein, etc. This will help verify the mocks are created with the minimal requirements
 *  that the server bean loader looks for. 
 * 
 */
@RunWith(value = Parameterized.class)
public class ServerBeanLoader3Test extends TestCase {
	@Parameters
	 public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParametersPlusAdditionalMocks());
	 }

	private HashMap<String, Pair> expected = new HashMap<String, Pair>();
	private class Pair {
		private JBossServerType type;
		private String version;
		public Pair(JBossServerType type, String version) {
			this.type = type;
			this.version = version;
		}
	}
	
	
	private String serverType;
	public ServerBeanLoader3Test(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() {
		expected.put(IJBossToolingConstants.SERVER_AS_32, new Pair(JBossServerType.AS, IJBossToolingConstants.V3_2));
		expected.put(IJBossToolingConstants.SERVER_AS_40, new Pair(JBossServerType.AS, IJBossToolingConstants.V4_0));
		expected.put(IJBossToolingConstants.SERVER_AS_42, new Pair(JBossServerType.AS, IJBossToolingConstants.V4_2));
		expected.put(IJBossToolingConstants.SERVER_AS_50, new Pair(JBossServerType.AS, IJBossToolingConstants.V5_0));
		expected.put(IJBossToolingConstants.SERVER_AS_51, new Pair(JBossServerType.AS, IJBossToolingConstants.V5_1));
		expected.put(IJBossToolingConstants.SERVER_AS_60, new Pair(JBossServerType.AS, IJBossToolingConstants.V6_0));
		expected.put(IJBossToolingConstants.SERVER_AS_70, new Pair(JBossServerType.AS7, IJBossToolingConstants.V7_0));
		expected.put(IJBossToolingConstants.SERVER_AS_71, new Pair(JBossServerType.AS7, IJBossToolingConstants.V7_1));
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_80, new Pair(JBossServerType.WILDFLY80, IJBossToolingConstants.V8_0));
		expected.put(IJBossToolingConstants.SERVER_EAP_43, new Pair(JBossServerType.EAP_STD, IJBossToolingConstants.V4_3));
		expected.put(IJBossToolingConstants.SERVER_EAP_50, new Pair(JBossServerType.EAP_STD, IJBossToolingConstants.V5_1));
		expected.put(IJBossToolingConstants.SERVER_EAP_60, new Pair(JBossServerType.EAP6, IJBossToolingConstants.V6_0));
		expected.put(IJBossToolingConstants.SERVER_EAP_61, new Pair(JBossServerType.EAP61, IJBossToolingConstants.V6_1));
		expected.put(ServerCreationTestUtils.TEST_SERVER_TYPE_GATEIN_34, new Pair(JBossServerType.AS7GateIn, IJBossToolingConstants.V3_4));
		expected.put(ServerCreationTestUtils.TEST_SERVER_TYPE_GATEIN_35, new Pair(JBossServerType.AS7GateIn, IJBossToolingConstants.V3_5));
		expected.put(ServerCreationTestUtils.TEST_SERVER_TYPE_GATEIN_36, new Pair(JBossServerType.AS7GateIn, "3.6"));
		// NEW_SERVER_ADAPTER
	}
	
	/*
	 * Test the server bean loader using runtime home flags passed in to the build
	 */
	@Test
	public void testServerBeanLoaderFromRuntimes() {
		String fLoc = TestConstants.getServerHome(serverType);
		if( fLoc == null ) {
			if( Arrays.asList(IJBossToolingConstants.ALL_JBOSS_SERVERS).contains(serverType)) {
				// This type should be tested but has no server-home, so fail
				fail("Test Suite has no server home for server type " + serverType);
			}
			// IF we're not one of the standard types, we're a test type (jpp, gate-in, etc), and this isn't a fail
			return;
		}
		Pair p = expected.get(serverType);
		inner_testServerBeanLoaderForFolder(new File(fLoc), p.type, p.version);
		if( p.type.equals(JBossServerType.EAP_STD)) {
			inner_testServerBeanLoaderForFolder(new File(fLoc).getParentFile(), JBossServerType.EAP, p.version);
		}
	}

	/*
	 * Create a mock folder and verify the mock folder matches also
	 */
	@Test
	public void testServerBeanLoaderForMocks() {
		File serverDir = (ServerCreationTestUtils.createMockServerLayout(serverType));
		if( serverDir == null || !serverDir.exists())
			fail("Creation of mock server type " + serverType + " has failed.");
		Pair p = expected.get(serverType);
		inner_testServerBeanLoaderForFolder(serverDir, p.type, p.version);
	}
	
	private void inner_testServerBeanLoaderForFolder(File serverDir, JBossServerType expectedType, String expectedVersion) {
		assertNotNull(serverType);
		IServerType itype = ServerCore.findServerType(expectedType.getServerAdapterTypeId(expectedVersion));
		if( itype == null )
			fail("Server type " + itype + " not found in the build");
		if( expected.get(serverType) == null )
			fail("Test Case needs to be updated for new adapter or mock test");
		
		ServerBeanLoader loader = new ServerBeanLoader(serverDir);
		JBossServerType type = loader.getServerType();
		assertEquals("Expected and actual server beans do not match for server type " + serverType, expectedType, type);
		String fullVersion = loader.getFullServerVersion();
		assertTrue(fullVersion + " does not begin with " + expectedVersion + " for server type " + serverType, 
				fullVersion.startsWith(expectedVersion));
		assertEquals(loader.getServerAdapterId(), itype.getId());
	}
}
