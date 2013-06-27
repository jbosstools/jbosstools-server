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
	public ServerBeanRuntimeMatcherTest(String serverType) {
		this.serverType = serverType;
	}
	
	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
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
	 * Create a mock folder and verify the mock folder matches also
	 */
	@Test
	public void testServerBeanRuntimeMatcherForMocks() {
		File serverDir = (ServerCreationTestUtils.createMockServerLayout(serverType));
		if( serverDir == null || !serverDir.exists())
			fail("Creation of mock server type " + serverType + " has failed.");
		Pair p = expected.get(serverType);
		try {
			ServerCreationTestUtils.createServerWithRuntime(p.type.getServerAdapterTypeId(p.version), serverType, serverDir);
		} catch(CoreException ce) {
			// Ignore, let test fail
		}
		inner_testServerBeanRuntimeMatcherForMocks(serverDir, p.type, p.version);
	}
	
	private void inner_testServerBeanRuntimeMatcherForMocks(File serverDir, JBossServerType expectedType, String expectedVersion) {
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
		assertEquals(1, matcher.findExistingRuntimes(matcher.createPattern(rtTypeId, expectedType.getId())).length);
		assertEquals(0, matcher.findExistingRuntimes(matcher.createPattern(rtTypeId, expectedType.getId() + "0")).length);
		
		assertEquals(1, matcher.findExistingRuntimes(matcher.createPattern(rtTypeId, expectedType.getId(), expectedVersion, true, incrementMinor(expectedVersion), false)).length);
		assertEquals(0, matcher.findExistingRuntimes(matcher.createPattern(rtTypeId, expectedType.getId(), 
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