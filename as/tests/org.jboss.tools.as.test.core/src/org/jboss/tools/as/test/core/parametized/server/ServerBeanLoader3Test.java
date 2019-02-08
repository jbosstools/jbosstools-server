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
	@Parameters(name = "{0}")
	 public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParametersPlusAdditionalMocks());
	 }

	protected HashMap<String, Data> expected = new HashMap<String, Data>();
	protected class Data {
		public JBossServerType type;
		public String version, overrideId;
		public Data(JBossServerType type, String version) {
			this.type = type;
			this.version = version;
			this.overrideId = null;
		}
		public Data(JBossServerType type, String version, String oid) {
			this(type, version);
			this.overrideId = oid;
		}
	}
	
	
	protected String serverType;
	public ServerBeanLoader3Test(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() {
		expected.put(IJBossToolingConstants.SERVER_AS_32, new Data(JBossServerType.AS, IJBossToolingConstants.V3_2));
		expected.put(IJBossToolingConstants.SERVER_AS_40, new Data(JBossServerType.AS, IJBossToolingConstants.V4_0));
		expected.put(IJBossToolingConstants.SERVER_AS_42, new Data(JBossServerType.AS, IJBossToolingConstants.V4_2));
		expected.put(IJBossToolingConstants.SERVER_AS_50, new Data(JBossServerType.AS, IJBossToolingConstants.V5_0));
		expected.put(IJBossToolingConstants.SERVER_AS_51, new Data(JBossServerType.AS, IJBossToolingConstants.V5_1));
		expected.put(IJBossToolingConstants.SERVER_AS_60, new Data(JBossServerType.AS, IJBossToolingConstants.V6_0));
		expected.put(IJBossToolingConstants.SERVER_AS_70, new Data(JBossServerType.AS7, IJBossToolingConstants.V7_0));
		expected.put(IJBossToolingConstants.SERVER_AS_71, new Data(JBossServerType.AS7, IJBossToolingConstants.V7_1));
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_80, new Data(JBossServerType.WILDFLY80, IJBossToolingConstants.V8_0));
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_90, new Data(JBossServerType.WILDFLY90, "9.0"));
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_100, new Data(JBossServerType.WILDFLY100, "10.0"));
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_110, new Data(JBossServerType.WILDFLY110, "11.0"));
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_120, new Data(JBossServerType.WILDFLY120, "12.0"));
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_130, new Data(JBossServerType.WILDFLY130, "13.0"));
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_140, new Data(JBossServerType.WILDFLY140, "14.0"));
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_150, new Data(JBossServerType.WILDFLY150, "15.0"));
		expected.put(IJBossToolingConstants.SERVER_EAP_43, new Data(JBossServerType.EAP_STD, IJBossToolingConstants.V4_3));
		expected.put(IJBossToolingConstants.SERVER_EAP_50, new Data(JBossServerType.EAP_STD, IJBossToolingConstants.V5_1));
		expected.put(IJBossToolingConstants.SERVER_EAP_60, new Data(JBossServerType.EAP6, IJBossToolingConstants.V6_0));
		expected.put(IJBossToolingConstants.SERVER_EAP_61, new Data(JBossServerType.EAP61, IJBossToolingConstants.V6_1));
		expected.put(IJBossToolingConstants.SERVER_EAP_70, new Data(JBossServerType.EAP70, IJBossToolingConstants.V7_0));
		expected.put(IJBossToolingConstants.SERVER_EAP_71, new Data(JBossServerType.EAP71, IJBossToolingConstants.V7_1));
		expected.put(ServerCreationTestUtils.TEST_SERVER_TYPE_EAP_65, new Data(JBossServerType.EAP61, "6.5"));
		expected.put(ServerCreationTestUtils.TEST_SERVER_TYPE_GATEIN_34, new Data(JBossServerType.AS7GateIn, IJBossToolingConstants.V3_4));
		expected.put(ServerCreationTestUtils.TEST_SERVER_TYPE_GATEIN_35, new Data(JBossServerType.AS7GateIn, IJBossToolingConstants.V3_5));
		expected.put(ServerCreationTestUtils.TEST_SERVER_TYPE_GATEIN_36, new Data(JBossServerType.AS7GateIn, "3.6"));
		expected.put(ServerCreationTestUtils.TEST_SERVER_TYPE_JPP_60, new Data(JBossServerType.JPP6, "6.0"));
		expected.put(ServerCreationTestUtils.TEST_SERVER_TYPE_JPP_61, new Data(JBossServerType.JPP61, "6.1", "JPP"));
		expected.put(ServerCreationTestUtils.TEST_SERVER_TYPE_WONKA_1, new Data(JBossServerType.UNKNOWN_AS72_PRODUCT, "1.0", "WONKA"));
		// NEW_SERVER_ADAPTER
	}

	/*
	 * Create a mock folder and verify the mock folder matches also
	 */
	@Test
	public void testServerBeanLoaderForMocks() {
		File serverDir = (ServerCreationTestUtils.createMockServerLayout(serverType));
		if( serverDir == null || !serverDir.exists())
			fail("Creation of mock server type " + serverType + " has failed.");
		Data p = expected.get(serverType);
		inner_testServerBeanLoaderForFolder(serverDir, p.type, p.version, p.overrideId);
	}
	
	protected void inner_testServerBeanLoaderForFolder(File serverDir, JBossServerType expectedType, String expectedVersion, String underlyingId) {
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
		
		String underlying = loader.getUnderlyingTypeId();
		if(underlyingId != null ) {
			assertEquals(underlyingId, underlying);
		} else {
			assertEquals(expectedType.getId(), underlying);
		}
	}
}
