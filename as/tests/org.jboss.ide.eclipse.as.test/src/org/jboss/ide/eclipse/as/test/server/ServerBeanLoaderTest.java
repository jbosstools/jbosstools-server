package org.jboss.ide.eclipse.as.test.server;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class ServerBeanLoaderTest extends TestCase {
	public void testAS32() {
		serverBeanLoaderTestAS6AndBelow("server32", IJBossToolingConstants.SERVER_AS_32, JBossServerType.AS, IJBossToolingConstants.V3_2);
	}
	public void testAS4() {
		serverBeanLoaderTestAS6AndBelow("server4", IJBossToolingConstants.SERVER_AS_40, JBossServerType.AS,IJBossToolingConstants.V4_0);
	}
	public void testAS42() {
		serverBeanLoaderTestAS6AndBelow("server42", IJBossToolingConstants.SERVER_AS_42, JBossServerType.AS,IJBossToolingConstants.V4_2);
	}
	public void testAS5() {
		serverBeanLoaderTestAS6AndBelow("server5", IJBossToolingConstants.SERVER_AS_50, JBossServerType.AS,IJBossToolingConstants.V5_0);
	}
	public void testAS6() {
		serverBeanLoaderTestAS6AndBelow("server6", IJBossToolingConstants.SERVER_AS_60, JBossServerType.AS,IJBossToolingConstants.V6_0);
	}
	public void testAS70() {
		serverBeanLoaderTestAS7Style("server70", IJBossToolingConstants.SERVER_AS_70, JBossServerType.AS7,IJBossToolingConstants.V7_0);
	}
	public void testAS71() {
		serverBeanLoaderTestAS7Style("server71", IJBossToolingConstants.SERVER_AS_71, JBossServerType.AS7,IJBossToolingConstants.V7_1);
	}
	
	public void testEAP4() {
		serverBeanLoaderTestAS6AndBelow("serverEAP43/jbossas", IJBossToolingConstants.SERVER_EAP_43, JBossServerType.EAP_STD,IJBossToolingConstants.V4_3);
	}
	public void testEAP5() {
		serverBeanLoaderTestAS6AndBelow("serverEAP5/jbossas", IJBossToolingConstants.SERVER_EAP_50, JBossServerType.EAP_STD,IJBossToolingConstants.V5_0);
	}
	public void testEAP60() {
			serverBeanLoaderTestEAP6Style("serverEap6", IJBossToolingConstants.SERVER_EAP_60, JBossServerType.EAP6,IJBossToolingConstants.V6_0);
	}
	
	private void serverBeanLoaderTestAS6AndBelow(String name, String serverTypeId, 
			JBossServerType expected, String actualVersionPrefix) {
		IPath serverDir = ServerRuntimeUtils.createAS6AndBelowMockServerDirectory(
				name, ServerRuntimeUtils.asSystemJar.get(serverTypeId), "default");
		ServerBeanLoader loader = new ServerBeanLoader(serverDir.toFile());
		JBossServerType type = loader.getServerType();
		assertTrue(type.equals(expected));
		String fullVersion = loader.getFullServerVersion();
		assertTrue(fullVersion.startsWith(actualVersionPrefix));
		assertEquals(loader.getServerAdapterId(), serverTypeId);
	}

	private void serverBeanLoaderTestAS7Style(String name, String serverTypeId, 
			JBossServerType expected, String actualVersionPrefix) {
		IPath serverDir = ServerRuntimeUtils.createAS7StyleMockServerDirectory(
				name, serverTypeId, ServerRuntimeUtils.asSystemJar.get(serverTypeId));
		ServerBeanLoader loader = new ServerBeanLoader(serverDir.toFile());
		JBossServerType type = loader.getServerType();
		assertTrue(type.equals(expected));
		String fullVersion = loader.getFullServerVersion();
		assertTrue(fullVersion.startsWith(actualVersionPrefix));
		assertEquals(loader.getServerAdapterId(), serverTypeId);
	}
	
	private void serverBeanLoaderTestEAP6Style(String name, String serverTypeId, 
			JBossServerType expected, String actualVersionPrefix) {
		IPath serverDir = ServerRuntimeUtils.createEAP6StyleMockServerDirectory(
				name, serverTypeId, ServerRuntimeUtils.asSystemJar.get(serverTypeId));
		ServerBeanLoader loader = new ServerBeanLoader(serverDir.toFile());
		JBossServerType type = loader.getServerType();
		assertTrue(type.equals(expected));
		String fullVersion = loader.getFullServerVersion();
		assertTrue(fullVersion.startsWith(actualVersionPrefix));
		assertEquals(loader.getServerAdapterId(), serverTypeId);
	}

}
