/*******************************************************************************
 * Copyright (c) 2007 - 2019 Red Hat, Inc.
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
import java.util.Collection;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.runtimes.integration.util.RuntimeMatcher;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.osgi.framework.Version;
/**
 * This test class will verify that RuntimeMatcher utility will properly return all 
 * wst.server runtime objects that match the patterns provided. It will also verify 
 * subtypes such as gatein are also properly identified and returned via this util class. 
 */
@RunWith(value = Parameterized.class)
public class ServerBeanRuntimeMatcherTest extends TestCase {
	@Parameters(name = "{0}")
	 public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParametersPlusAdditionalMocks());
	 }

	private HashMap<String, Data> expected = new HashMap<String, Data>();
	private class Data {
		private JBossServerType type;
		private String version, overrideId;
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
	
	
	private String serverType;
	public ServerBeanRuntimeMatcherTest(String serverType) {
		this.serverType = serverType;
	}
	
	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
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
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_160, new Data(JBossServerType.WILDFLY160, "16.0"));
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_170, new Data(JBossServerType.WILDFLY170, "17.0"));
		expected.put(IJBossToolingConstants.SERVER_WILDFLY_180, new Data(JBossServerType.WILDFLY180, "18.0"));
		expected.put(IJBossToolingConstants.SERVER_EAP_43, new Data(JBossServerType.EAP_STD, IJBossToolingConstants.V4_3));
		expected.put(IJBossToolingConstants.SERVER_EAP_50, new Data(JBossServerType.EAP_STD, IJBossToolingConstants.V5_1));
		expected.put(IJBossToolingConstants.SERVER_EAP_60, new Data(JBossServerType.EAP6, IJBossToolingConstants.V6_0));
		expected.put(IJBossToolingConstants.SERVER_EAP_61, new Data(JBossServerType.EAP61, IJBossToolingConstants.V6_1));
		expected.put(IJBossToolingConstants.SERVER_EAP_70, new Data(JBossServerType.EAP70, IJBossToolingConstants.V7_0));
		expected.put(IJBossToolingConstants.SERVER_EAP_71, new Data(JBossServerType.EAP71, IJBossToolingConstants.V7_1));
		expected.put(IJBossToolingConstants.SERVER_EAP_72, new Data(JBossServerType.EAP72, IJBossToolingConstants.V7_2));
		expected.put(IJBossToolingConstants.SERVER_EAP_73, new Data(JBossServerType.EAP73, IJBossToolingConstants.V7_3));
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
	public void testServerBeanRuntimeMatcherForMocks() {
		File serverDir = (ServerCreationTestUtils.createMockServerLayout(serverType));
		if( serverDir == null || !serverDir.exists())
			fail("Creation of mock server type " + serverType + " has failed.");
		Data p = expected.get(serverType);
		try {
			ServerCreationTestUtils.createServerWithRuntime(p.type.getServerAdapterTypeId(p.version), serverType, serverDir);
		} catch(CoreException ce) {
			// Ignore, let test fail
		}
		inner_testServerBeanRuntimeMatcherForMocks(serverDir, p.type, p.version, p.overrideId);
	}
	
	private void inner_testServerBeanRuntimeMatcherForMocks(File serverDir, JBossServerType expectedType, String expectedVersion,  String underlyingId) {
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
		
		RuntimeMatcher matcher = new RuntimeMatcher();
		String serverTypeId = loader.getServerAdapterId();
		IServerType stype = ServerCore.findServerType(serverTypeId);
		String rtTypeId = stype.getRuntimeType().getId();
		assertEquals(1, matcher.findExistingRuntimes(rtTypeId).length);
		assertEquals(0, matcher.findExistingRuntimes(rtTypeId + "0").length);
		
		String expectedTypeOverride = underlyingId == null ? expectedType.getId() : underlyingId;
		
		assertEquals(1, matcher.findExistingRuntimes(matcher.createPattern(rtTypeId, expectedTypeOverride)).length);
		assertEquals(0, matcher.findExistingRuntimes(matcher.createPattern(rtTypeId, expectedTypeOverride + "0")).length);
		
		assertEquals(1, matcher.findExistingRuntimes(matcher.createPattern(rtTypeId, expectedTypeOverride, expectedVersion, true, incrementMinor(expectedVersion), false)).length);
		assertEquals(0, matcher.findExistingRuntimes(matcher.createPattern(rtTypeId, expectedTypeOverride, 
				incrementMajor(expectedVersion), true, incrementMajor(expectedVersion,2), false)).length);
	}
	
	
	private String incrementMajor(String v) {
		return incrementMajor(v,1);
	}
	
	private String incrementMinor(String v) {
		return incrementMinor(v,1);
	}

	private String incrementMicro(String v) {
		return incrementMicro(v,1);
	}

	private String incrementMajor(String v, int c) {
		Version vers = new Version(v);
		Version vers2 = new Version(vers.getMajor()+c, 0, 0, "Final");
		return vers2.toString();
	}
	
	private String incrementMinor(String v, int c) {
		Version vers = new Version(v);
		Version vers2 = new Version(vers.getMajor(), vers.getMinor()+c, 0, "Final");
		return vers2.toString();
	}

	private String incrementMicro(String v, int c) {
		Version vers = new Version(v);
		Version vers2 = new Version(vers.getMajor(), vers.getMinor(), vers.getMicro() + c, "Final");
		return vers2.toString();
	}


	
}
