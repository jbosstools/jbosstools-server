package org.jboss.tools.as.test.core.internal.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Server;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerCreationUtils;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.TestConstants;

/**
 * This class is intended to assist in the creation ONLY 
 * for servers and runtimes. It's scope includes:
 * 
 *   1) Creating mock folders for each server type
 *   2) Creating IServer objects out of either mock folders or legitimate installations
 *
 */
public class ServerCreationTestUtils extends Assert {
	
	private static final IPath mockedServers = ASMatrixTests.getDefault().getStateLocation().append("mockedServers");
	private static HashMap<String, String> asSystemJar = new HashMap<String, String>();
	private static HashMap<String, String> serverRuntimeMap = new HashMap<String, String>();
	private static final String twiddle_suffix = ".mf.twiddle.jar";
	private static final String twiddle_3_2_8 = "3.2.8" + twiddle_suffix;
	private static final String twiddle_4_0_5 = "4.0.5" + twiddle_suffix;
	private static final String twiddle_4_2_3 = "4.2.3" + twiddle_suffix;
	private static final String twiddle_5_0_0 = "5.0.0" + twiddle_suffix;
	private static final String twiddle_5_0_1 = "5.0.1" + twiddle_suffix;
	private static final String twiddle_5_1_0 = "5.1.0" + twiddle_suffix;
	private static final String twiddle_6_0_0 = "6.0.0" + twiddle_suffix;
	private static final String as_server_7_0_jar = "7.0.0.mf.jboss-as-server.jar";
	private static final String as_server_7_1_jar = "7.1.0.mf.jboss-as-server.jar";
	private static final String twiddle_eap_4_3 = "eap4.3" + twiddle_suffix;
	private static final String twiddle_eap_5_0 = "eap5.0" + twiddle_suffix;
	private static final String eap_server_6_0_jar = "eap6.0.0.mf.jboss-as-server.jar";
	private static final String run_jar = "run.jar";
	private static final String service_xml = "service.xml";
	static {
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_32, twiddle_3_2_8);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_40, twiddle_4_0_5);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_42, twiddle_4_2_3);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_50, twiddle_5_0_0);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_51, twiddle_5_1_0);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_60, twiddle_6_0_0);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_70, as_server_7_0_jar);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_71, as_server_7_1_jar);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_43, twiddle_eap_4_3);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_50, twiddle_eap_5_0);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_60, eap_server_6_0_jar);

		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_32, IJBossToolingConstants.AS_32);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_40, IJBossToolingConstants.AS_40);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_42, IJBossToolingConstants.AS_42);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_50, IJBossToolingConstants.AS_50);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_51, IJBossToolingConstants.AS_51);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_60, IJBossToolingConstants.AS_60);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_70, IJBossToolingConstants.AS_70);
		//serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_71, IJBossToolingConstants.AS_71);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_43, IJBossToolingConstants.EAP_43);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_50, IJBossToolingConstants.EAP_50);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_60, IJBossToolingConstants.EAP_60);
	}

	private static IServer createDeployOnlyServer() throws CoreException {
		return createDeployOnlyServer(getDeployFolder(), getTmpDeployFolder());
	}

	/*
	 * Only for use with JBoss servers, since deploy-only has no custom layout
	 */
	public static File createMockServerLayout(String serverType) {
		boolean isEap = false;
		if(serverType.startsWith(IJBossToolingConstants.EAP_SERVER_PREFIX))
			isEap = true;
		String name = serverType + (isEap ? "/jbossas" : "");
		IPath serverDir = null;
		if( IJBossToolingConstants.SERVER_AS_32.equals(serverType) ||
				IJBossToolingConstants.SERVER_AS_40.equals(serverType) ||
				IJBossToolingConstants.SERVER_AS_42.equals(serverType) ||
				IJBossToolingConstants.SERVER_AS_50.equals(serverType) ||
				IJBossToolingConstants.SERVER_AS_51.equals(serverType) ||
				IJBossToolingConstants.SERVER_AS_60.equals(serverType) ||
				IJBossToolingConstants.SERVER_EAP_43.equals(serverType) ||
				IJBossToolingConstants.SERVER_EAP_50.equals(serverType)) {
			serverDir = createAS6AndBelowMockServerDirectory(serverType + getRandomString(), 
					asSystemJar.get(serverType), "default");
		} else if( IJBossToolingConstants.SERVER_AS_70.equals(serverType) ||
				 IJBossToolingConstants.SERVER_AS_71.equals(serverType)) {
			serverDir = createAS7StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));			
		} else if( IJBossToolingConstants.SERVER_EAP_60.equals(serverType)) {
			serverDir = createEAP6StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));
		}
		return serverDir == null ? null : serverDir.toFile();
	}
	
	
	/* Creates a mock file structure first, then creates an IServer out of it */
	public static IServer createMockServerWithRuntime(String serverType, String name) {
		try {
			if( serverType.equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER))
				return createDeployOnlyServer();
			File f = createMockServerLayout(serverType);
			return createServerWithRuntime(serverType, name, f);
		} catch(CoreException ce) {
			// Since we're in tests, I can throw a runtime exception here
			throw new RuntimeException("Unable to create IServer " + name + " of type " + serverType, ce);
		}
	}

	public static IServer createServerWithRuntime(String serverType, String name) throws CoreException {
		if( serverType.equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER))
			return createDeployOnlyServer();
		String loc = TestConstants.getServerHome(serverType);
		assertNotNull("Runtime location for server type " + serverType + " not set in test suite", loc);
		File locFile = new Path(loc).toFile();
		return createServerWithRuntime(serverType, name, locFile);
	}
	
	private static IServer createServerWithRuntime(String serverType, String name, File f) throws CoreException {
		if( f != null ) {
			IServerType type = ServerCore.findServerType(serverType);
			if( ServerUtil.isJBoss7(type)) {
				return createJBoss7IServer(f.getAbsolutePath(), name);
			}
			return createServer(serverType, f.getAbsolutePath(), "default", name);
		}
		return null;
	}
	
	private static IPath createAS6AndBelowMockServerDirectory(String name, String twiddleJar, String configurationName )  {
		IPath loc = mockedServers.append(name);
		try {
			loc.toFile().mkdirs();
			loc.append("bin").toFile().mkdirs();
			loc.append("server").toFile().mkdirs();
			loc.append("server").append(configurationName).toFile().mkdirs();
			IPath configConf = loc.append("server").append(configurationName).append("conf");
			configConf.toFile().mkdirs();
			File twiddleLoc = BundleUtils.getFileLocation("serverMock/" + twiddleJar);
			FileUtil.fileSafeCopy(twiddleLoc, loc.append("bin").append("twiddle.jar").toFile());
			File runJar = BundleUtils.getFileLocation("serverMock/run.jar");
			FileUtil.fileSafeCopy(runJar, loc.append("bin").append("run.jar").toFile());
			File serviceXml = BundleUtils.getFileLocation("serverMock/jboss-service.xml");
			FileUtil.fileSafeCopy(serviceXml, configConf.append("jboss-service.xml").toFile());
			return loc;
		} catch( CoreException ce ) {
			FileUtil.completeDelete(loc.toFile());
			return null;
		}
	}

	private static IPath createAS7StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		try {
			loc.toFile().mkdirs();
			IPath serverJarBelongs = loc.append("modules/org/jboss/as/server/main");
			serverJarBelongs.toFile().mkdirs();
			File serverJarLoc = BundleUtils.getFileLocation("serverMock/" + serverJar);
			FileUtil.fileSafeCopy(serverJarLoc, serverJarBelongs.append("anything.jar").toFile());
		} catch(CoreException ce) {
			FileUtil.completeDelete(loc.toFile());
			return null;
		}
		return loc;
	}

	private static IPath createEAP6StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		try {
			loc.toFile().mkdirs();
			IPath productConf = loc.append("bin/product.conf");
			loc.append("bin").toFile().mkdirs();
			IOUtil.setContents(productConf.toFile(), "slot=eap");
			loc.append("modules/org/jboss/as/product/eap/dir/META-INF").toFile().mkdirs();
			IPath manifest = loc.append("modules/org/jboss/as/product/eap/dir/META-INF/MANIFEST.MF");
			String manString = "JBoss-Product-Release-Name: EAP\nJBoss-Product-Release-Version: 6.0.0.Alpha\nJBoss-Product-Console-Slot: eap";
			IOUtil.setContents(manifest.toFile(), manString);
		} catch(CoreException ce) {
			FileUtil.completeDelete(loc.toFile());
			return null;
		} catch(IOException ioe) {
			FileUtil.completeDelete(loc.toFile());
			return null;
		}
		return loc;
	}
	
	private static IServer createServer(String serverType,
			String location, String configuration, String name) throws CoreException {
		IRuntime runtime = RuntimeUtils.createRuntime(serverRuntimeMap.get(serverType), location, configuration);
		IServer s = ServerCreationUtils.createServer2(runtime, serverType, name);
		IServerWorkingCopy wc = s.createWorkingCopy();
		wc.setAttribute(Server.PROP_AUTO_PUBLISH_SETTING, Server.AUTO_PUBLISH_DISABLE);
		return wc.save(true, new NullProgressMonitor());
	}

	private static IServer createDeployOnlyServer(String deployLocation, String tempDeployLocation) throws CoreException {
		return createDeployOnlyServer(deployLocation, tempDeployLocation, "testRuntime", "testServer");
	}
	private static IServer createDeployOnlyServer(String deployLocation, String tempDeployLocation, 
			String rtName, String serverName) throws CoreException {
		IServer s = ServerCreationUtils.createDeployOnlyServer(deployLocation, tempDeployLocation, rtName, serverName);
		IServerWorkingCopy wc = s.createWorkingCopy();
		wc.setAttribute(Server.PROP_AUTO_PUBLISH_SETTING, Server.AUTO_PUBLISH_DISABLE);
		return wc.save(true, new NullProgressMonitor());
	}
	private static IServer createJBoss7IServer(String rtLoc, String name) throws CoreException {
		IRuntime runtime = RuntimeUtils.createRuntime(IJBossToolingConstants.AS_70, rtLoc, null);
		IServer s = ServerCreationUtils.createServer2(runtime, IJBossToolingConstants.SERVER_AS_70, name);
		IServerWorkingCopy swc = s.createWorkingCopy();
		swc.setServerConfiguration(null);
		swc.setAttribute(Server.PROP_AUTO_PUBLISH_SETTING, Server.AUTO_PUBLISH_DISABLE);
		IServer server = swc.save(true, null);
		return server;
	}

	public static IPath getRandomAbsoluteFolder() {
		return getBaseDir().append(getRandomString());
	}
	private static String getDeployFolder() {
		return getRandomAbsoluteFolder().append("deploy").toOSString();
	}
	
	private static String getTmpDeployFolder() {
		return getRandomAbsoluteFolder().append("tmpDeploy").toOSString();
	}
	private static String getRandomString() {
		return String.valueOf(System.currentTimeMillis());
	}
	private static IPath getBaseDir() {
		return ASMatrixTests.getDefault().getStateLocation().append("testDeployments");
	}

	

	
	public static void deleteAllServersAndRuntimes() throws Exception {
		deleteAllServers();
		deleteAllRuntimes();
	}
	
	public static void deleteAllServers() throws CoreException {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			servers[i].delete();
		}
	}

	public static void deleteAllRuntimes() throws CoreException {
		IRuntime[] runtimes = ServerCore.getRuntimes();
		for( int i = 0; i < runtimes.length; i++ ) {
			runtimes[i].delete();
		}
	}

}
