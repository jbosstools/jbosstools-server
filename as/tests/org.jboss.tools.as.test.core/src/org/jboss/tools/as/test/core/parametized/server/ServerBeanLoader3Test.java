package org.jboss.tools.as.test.core.parametized.server;

import java.io.File;
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
 * 
 * @author rob
 *
 */
@RunWith(value = Parameterized.class)
public class ServerBeanLoader3Test extends TestCase {
	@Parameters
	 public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParamterers());
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
		expected.put(IJBossToolingConstants.SERVER_EAP_43, new Pair(JBossServerType.EAP_STD, IJBossToolingConstants.V4_3));
		expected.put(IJBossToolingConstants.SERVER_EAP_50, new Pair(JBossServerType.EAP_STD, IJBossToolingConstants.V5_0));
		expected.put(IJBossToolingConstants.SERVER_EAP_60, new Pair(JBossServerType.EAP6, IJBossToolingConstants.V6_0));
	}
	
	/*
	 * Test the server bean loader using runtime home flags passed in to the build
	 */
	@Test
	public void testServerBeanLoaderFromRuntimes() {
		String fLoc = TestConstants.getServerHome(serverType);
		if( fLoc == null )
			fail("Test Suite has no server home for server type " + serverType);
		inner_testServerBeanLoaderForFolder(new File(fLoc));
	}

	/*
	 * Create a mock folder and verify the mock folder matches also
	 */
	@Test
	public void testServerBeanLoaderForMocks() {
		File serverDir = (ServerCreationTestUtils.createMockServerLayout(serverType));
		if( serverDir == null || !serverDir.exists())
			fail("Creation of mock server type " + serverType + " has failed.");
		inner_testServerBeanLoaderForFolder(serverDir);
	}
	
	private void inner_testServerBeanLoaderForFolder(File serverDir) {
		assertNotNull(serverType);
		IServerType itype = ServerCore.findServerType(serverType);
		if( itype == null )
			fail("Server type " + itype + " not found in the build");
		if( expected.get(serverType) == null )
			fail("Test Case needs to be updated for new adapter");
		
		ServerBeanLoader loader = new ServerBeanLoader(serverDir);
		JBossServerType type = loader.getServerType();
		assertTrue(type.equals(expected.get(serverType).type));
		String fullVersion = loader.getFullServerVersion();
		assertTrue(fullVersion + " does not begin with " + expected.get(serverType).version, 
				fullVersion.startsWith(expected.get(serverType).version));
		assertEquals(loader.getServerAdapterId(), serverType);
	}
}
