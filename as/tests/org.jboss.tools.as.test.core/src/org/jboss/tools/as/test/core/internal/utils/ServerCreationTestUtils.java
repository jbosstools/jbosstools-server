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
	private static final String wildfly_8_0_jar = "wf8.0.0.mf.jboss-as-server.jar";
	private static final String twiddle_eap_4_3 = "eap4.3" + twiddle_suffix;
	private static final String twiddle_eap_5_0 = "eap5.0" + twiddle_suffix;
	private static final String twiddle_eap_5_1 = "eap5.1" + twiddle_suffix;
	private static final String eap_server_6_0_jar = "eap6.0.0.mf.jboss-as-server.jar";
	private static final String eap_server_6_1_jar = "eap6.1.0.mf.jboss-as-server.jar";
	private static final String jpp_server_6_0_jar = "jpp6.0.0.mf.jboss-as-server.jar";
	private static final String jpp_server_6_1_jar = "jpp6.1.0.mf.jboss-as-server.jar";
	private static final String gatein_3_4_0_jar = "gatein3.4.0.mf.jboss-as7-integration.jar";
	private static final String run_jar = "run.jar";
	private static final String service_xml = "service.xml";
	
	// ADDITIONAL NON_DEFAULT SERVER TYPES TO TEST
	public static final String TEST_SERVER_TYPE_GATEIN_34 = "TEST_SERVER_TYPE_GATEIN_34";
	public static final String TEST_SERVER_TYPE_GATEIN_35 = "TEST_SERVER_TYPE_GATEIN_35";
	public static final String TEST_SERVER_TYPE_GATEIN_36 = "TEST_SERVER_TYPE_GATEIN_36";
	public static final String TEST_SERVER_TYPE_JPP_60 = "TEST_SERVER_TYPE_JPP_60";
	
	/* Represents a mock structure for jpp 6.1 ER3,  NOT final!  */
	public static final String TEST_SERVER_TYPE_JPP_61 = "TEST_SERVER_TYPE_JPP_61";
	
	public static final String TEST_SERVER_TYPE_EAP_65 = "TEST_SERVER_TYPE_EAP_65";
	
	public static final String TEST_SERVER_TYPE_WONKA_1 = "TEST_SERVER_TYPE_WONKA_1_MISMATCH";
	
	
	public static final String[] TEST_SERVER_TYPES_TO_MOCK = new String[] { 
		TEST_SERVER_TYPE_GATEIN_34, TEST_SERVER_TYPE_GATEIN_35,TEST_SERVER_TYPE_GATEIN_36,
		TEST_SERVER_TYPE_JPP_60, TEST_SERVER_TYPE_JPP_61, TEST_SERVER_TYPE_EAP_65,
		TEST_SERVER_TYPE_WONKA_1
	};
	
	static {
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_32, twiddle_3_2_8);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_40, twiddle_4_0_5);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_42, twiddle_4_2_3);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_50, twiddle_5_0_0);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_51, twiddle_5_1_0);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_60, twiddle_6_0_0);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_70, as_server_7_0_jar);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_71, as_server_7_1_jar);
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_80, wildfly_8_0_jar);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_43, twiddle_eap_4_3);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_50, twiddle_eap_5_1);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_60, eap_server_6_0_jar);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_61, eap_server_6_1_jar);
		asSystemJar.put(TEST_SERVER_TYPE_EAP_65, eap_server_6_1_jar);
		asSystemJar.put(TEST_SERVER_TYPE_JPP_60, jpp_server_6_0_jar);
		asSystemJar.put(TEST_SERVER_TYPE_JPP_61, jpp_server_6_1_jar);
		asSystemJar.put(TEST_SERVER_TYPE_WONKA_1, eap_server_6_1_jar);
		asSystemJar.put(TEST_SERVER_TYPE_GATEIN_34, gatein_3_4_0_jar);
		// NEW_SERVER_ADAPTER Add the new runtime constant above this line
		
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_32, IJBossToolingConstants.AS_32);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_40, IJBossToolingConstants.AS_40);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_42, IJBossToolingConstants.AS_42);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_50, IJBossToolingConstants.AS_50);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_51, IJBossToolingConstants.AS_51);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_60, IJBossToolingConstants.AS_60);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_70, IJBossToolingConstants.AS_70);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_71, IJBossToolingConstants.AS_71);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_80, IJBossToolingConstants.WILDFLY_80);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_43, IJBossToolingConstants.EAP_43);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_50, IJBossToolingConstants.EAP_50);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_60, IJBossToolingConstants.EAP_60);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_61, IJBossToolingConstants.EAP_61);
		// NEW_SERVER_ADAPTER Add the new runtime constant above this line
		
		
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
		String name = serverType;
		IPath serverDir = null;
		if( IJBossToolingConstants.SERVER_AS_32.equals(serverType) ||
				IJBossToolingConstants.SERVER_AS_40.equals(serverType) ||
				IJBossToolingConstants.SERVER_AS_42.equals(serverType) ||
				IJBossToolingConstants.SERVER_AS_50.equals(serverType) ||
				IJBossToolingConstants.SERVER_AS_51.equals(serverType) ||
				IJBossToolingConstants.SERVER_AS_60.equals(serverType) ||
				IJBossToolingConstants.SERVER_EAP_43.equals(serverType) ||
				IJBossToolingConstants.SERVER_EAP_50.equals(serverType)) {
			name += (isEap ? "/jbossas" : "");
			serverDir = createAS6AndBelowMockServerDirectory(serverType + getRandomString(), 
					asSystemJar.get(serverType), "default");
		} else if( IJBossToolingConstants.SERVER_AS_70.equals(serverType) ||
				 IJBossToolingConstants.SERVER_AS_71.equals(serverType)) {
			serverDir = createAS7StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));			
		} else if( IJBossToolingConstants.SERVER_EAP_60.equals(serverType)) {
			serverDir = createEAP6StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if( IJBossToolingConstants.SERVER_EAP_61.equals(serverType)) {
			serverDir = createAS72EAP61StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if( IJBossToolingConstants.SERVER_WILDFLY_80.equals(serverType)) {
			serverDir = createWildfly80MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if( TEST_SERVER_TYPE_GATEIN_34.equals(serverType)) {
			serverDir = createGateIn34MockServerDirectory(name);
		} else if( TEST_SERVER_TYPE_GATEIN_35.equals(serverType)) {
			serverDir = createGateIn35MockServerDirectory(name);
		} else if( TEST_SERVER_TYPE_GATEIN_36.equals(serverType)) {
			serverDir = createGateIn36MockServerDirectory(name);
		}else if( TEST_SERVER_TYPE_JPP_60.equals(serverType)) {
			serverDir = createJPP60MockServerDirectory(name,serverType, asSystemJar.get(serverType));
		}else if( TEST_SERVER_TYPE_JPP_61.equals(serverType)) {
			serverDir = createJPP61MockServerDirectory(name,serverType, asSystemJar.get(serverType));
		} else if( TEST_SERVER_TYPE_EAP_65.equals(serverType)) {
			serverDir = createAS72EAP65StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));
		}else if( TEST_SERVER_TYPE_WONKA_1.equals(serverType)) {
			serverDir = createAS72Wonka1MockServerDirectory(name, serverType, asSystemJar.get(serverType));
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
		assertTrue("Runtime location for server type " + serverType + " (" + locFile.getAbsolutePath() + "), does not exist", locFile.exists());
		return createServerWithRuntime(serverType, name, locFile);
	}
	
	public static IServer createServerWithRuntime(String serverType, String name, File f) throws CoreException {
		if( f != null ) {
			IServerType type = ServerCore.findServerType(serverType);
			if( ServerUtil.isJBoss7(type)) {
				return createJBoss7IServer(type, f.getAbsolutePath(), name);
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

	private static IPath createGateIn34MockServerDirectory(String name) {
		IPath loc = mockedServers.append(name);
		createAS7xProductStructure(loc, false, asSystemJar.get(IJBossToolingConstants.SERVER_AS_71), null, null);
		IPath dest = loc.append("/gatein/modules/org/gatein/main/");
		dest.toFile().mkdirs();
		try {
			File serverJarLoc = BundleUtils.getFileLocation("serverMock/" + asSystemJar.get(TEST_SERVER_TYPE_GATEIN_34));
			FileUtil.fileSafeCopy(serverJarLoc, dest.append("anything.jar").toFile());
		} catch(CoreException ce) {
			FileUtil.completeDelete(loc.toFile());
		}

		return loc;
	}
	
	private static IPath createGateIn35MockServerDirectory(String name) {
		IPath loc = mockedServers.append(name);
		createAS7xProductStructure(loc, false, asSystemJar.get(IJBossToolingConstants.SERVER_AS_71), null, null);
		String GATEIN_35_PROPERTY_FILE = "gatein/extensions/gatein-wsrp-integration.ear/extension-war.war/META-INF/maven/org.gatein.integration/extension-war/pom.properties";
		IPath propFile = loc.append(GATEIN_35_PROPERTY_FILE);
		propFile.toFile().getParentFile().mkdirs();
		try {
			IOUtil.setContents(propFile.toFile(), "version=3.5.0");
		} catch(IOException ioe) {
			FileUtil.completeDelete(loc.toFile());
		}
		return loc;
	}
	private static IPath createGateIn36MockServerDirectory(String name) {
		IPath loc = mockedServers.append(name);
		createAS7xProductStructure(loc, false, asSystemJar.get(IJBossToolingConstants.SERVER_AS_71), null, null);
		String GATEIN_35_PROPERTY_FILE = "gatein/extensions/gatein-wsrp-integration.ear/extension-war.war/META-INF/maven/org.gatein.integration/extension-war/pom.properties";
		IPath propFile = loc.append(GATEIN_35_PROPERTY_FILE);
		propFile.toFile().getParentFile().mkdirs();
		try {
			IOUtil.setContents(propFile.toFile(), "version=3.6.0");
		} catch(IOException ioe) {
			FileUtil.completeDelete(loc.toFile());
		}
		return loc;
	}

	
	private static IPath createAS7StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		createAS7xProductStructure(loc, false, serverJar, null, null);
		return loc;
	}
	
	private static IPath createWildfly80MockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		createAS7xProductStructure(loc, true, serverJar, null, null);
		return loc;
	}
	
	
	private static IPath createAS72EAP61StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		String manString = "JBoss-Product-Release-Name: EAP\nJBoss-Product-Release-Version: 6.1.0.Alpha\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, true, serverJar, "eap", manString);
		return loc;
	}

	private static IPath createAS72EAP65StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		String manString = "JBoss-Product-Release-Name: EAP\nJBoss-Product-Release-Version: 6.5.0.Alpha\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, true, serverJar, "eap", manString);
		return loc;
	}
	private static IPath createAS72Wonka1MockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		String wonkaManString = "JBoss-Product-Release-Name: WONKA\nJBoss-Product-Release-Version: 1.0.0.Alpha\nJBoss-Product-Console-Slot: wonka";
		String eapManContents = "JBoss-Product-Release-Name: EAP\nJBoss-Product-Release-Version: 6.1.1.GA\nJBoss-Product-Console-Slot: eap";

		createAS7xProductStructure(loc, true, serverJar, null, null);
		try {
			createProductConfASgt7(loc, "wonka", new String[]{"notwonka"});
			
			IPath metainf = loc.append(getProductMetaInfFolderPath("eap", true));
			createProductMetaInfFolder(metainf, eapManContents);

			IPath metainf2 = loc.append(getProductMetaInfFolderPath("wonka", true, "notwonka"));
			createProductMetaInfFolder(metainf2, wonkaManString);
		} catch(CoreException ce) {
			FileUtil.completeDelete(loc.toFile());
		} catch(IOException ioe) {
			FileUtil.completeDelete(loc.toFile());
		}

		return loc;
	}
	

	private static IPath createEAP6StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		String manString = "JBoss-Product-Release-Name: EAP\nJBoss-Product-Release-Version: 6.0.0.Alpha\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, false, serverJar, "eap", manString);
		return loc;
	}
	
	private static IPath createJPP60MockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		String manString = "JBoss-Product-Release-Name: Portal Platform\nJBoss-Product-Release-Version: 6.0.0.CR01\nJBoss-Product-Console-Slot: jpp";
		createAS7xProductStructure(loc, false, serverJar, "jpp", manString);
		return loc;
	}

	private static IPath createJPP61MockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		String manString = "JBoss-Product-Release-Name: Portal Platform\nJBoss-Product-Release-Version: 6.1.0.ER03\nJBoss-Product-Console-Slot: jpp";
		createAS7xProductStructure(loc, true, serverJar, "jpp", manString);
		return loc;
	}


	
	private static void createAS7xProductStructure(IPath loc,  boolean includeLayers, String serverJar, String slot,
			String manifestContents ) {
		try {
			createStandaloneXML(loc);
			copyASSystemJar(loc, serverJar, includeLayers);
			loc.append("jboss-modules.jar").toFile().createNewFile();
			loc.append("bin").toFile().mkdirs();
			if( slot != null ) {
				createProductConfASgt7(loc, slot, null);
				createProductMetaInfFolder(loc, slot, includeLayers, manifestContents);
			}
		} catch(CoreException ce) {
			FileUtil.completeDelete(loc.toFile());
		} catch(IOException ioe) {
			FileUtil.completeDelete(loc.toFile());
		}
	}
	
	private static void copyASSystemJar(IPath loc, String serverJar, boolean includeLayers) throws CoreException {
		IPath serverJarBelongs = includeLayers ? 
				loc.append("modules/system/layers/base/org/jboss/as/server/main") :
					loc.append("modules/org/jboss/as/server/main");
		serverJarBelongs.toFile().mkdirs();
		File serverJarLoc = BundleUtils.getFileLocation("serverMock/" + serverJar);
		FileUtil.fileSafeCopy(serverJarLoc, serverJarBelongs.append("anything.jar").toFile());
	}
	private static void createProductMetaInfFolder(IPath loc, String slot, boolean includeLayers, String manifestContents) throws IOException, CoreException {
		IPath metainf = loc.append(getProductMetaInfFolderPath(slot, includeLayers));
		createProductMetaInfFolder(metainf, manifestContents);
	}
	private static void createProductMetaInfFolder(IPath metainf, String manifestContents) throws IOException, CoreException {
		metainf.toFile().mkdirs();
		IPath manifest = metainf.append("MANIFEST.MF");
		IOUtil.setContents(manifest.toFile(), manifestContents);		
	}
	
	private static String getProductMetaInfFolderPath(String slot, boolean includeLayers) {
		return getProductMetaInfFolderPath(slot, includeLayers, null);
	}
	
	private static String getProductMetaInfFolderPath(String slot, boolean includeLayers, String insideLayer) {
		if( insideLayer == null ) {
			if( !includeLayers ) {
				return "modules/org/jboss/as/product/" + slot + "/dir/META-INF";
			}
			return "modules/system/layers/base/org/jboss/as/product/" + slot + "/dir/META-INF";
		}
		// Now we know it lives inside a layer
		return "modules/system/layers/" + insideLayer + "/org/jboss/as/product/" + slot + "/dir/META-INF";
	}
	
	private static void createProductConfASgt7(IPath loc, String slot, String[] layers) throws CoreException, IOException  {
		loc.toFile().mkdirs();
		IPath productConf = loc.append("bin/product.conf");
		loc.append("bin").toFile().mkdirs();
		IOUtil.setContents(productConf.toFile(), "slot=" + slot);
		if( layers != null && layers.length > 0 ) {
			StringBuilder sb = new StringBuilder();
			sb.append("layers=");
			for( int i = 0; i < layers.length; i++ ) {
				sb.append(layers[i]);
				if( i < layers.length-1) {
					sb.append(",");
				}
			}
			IPath layersConf = loc.append("modules/layers.conf");
			loc.append("modules").toFile().mkdirs();
			IOUtil.setContents(layersConf.toFile(), sb.toString());
		}
	}
	
	private static void createStandaloneXML(IPath loc) throws IOException {
		IPath standalonexml = loc.append("standalone").append("configuration").append("standalone.xml");
		standalonexml.toFile().getParentFile().mkdirs();
		IOUtil.setContents(standalonexml.toFile(), "<server></server>");
	}
	
	
	private static IServer createServer(String serverType,
			String location, String configuration, String name) throws CoreException {
		IRuntime runtime = RuntimeUtils.createRuntime(serverRuntimeMap.get(serverType), location, configuration);
		IServerType serverType2 = ServerCore.findServerType(serverType);
		IServerWorkingCopy swc = ServerCreationUtils.createServerWorkingCopy(runtime, serverType2, name, "local");
		swc.setAttribute(Server.PROP_AUTO_PUBLISH_SETTING, Server.AUTO_PUBLISH_DISABLE);
		return swc.save(true, new NullProgressMonitor());
	}

	private static IServer createDeployOnlyServer(String deployLocation, String tempDeployLocation) throws CoreException {
		return createDeployOnlyServer(deployLocation, tempDeployLocation, "testRuntime", "testServer");
	}
	private static IServer createDeployOnlyServer(String deployLocation, String tempDeployLocation, 
			String rtName, String serverName) throws CoreException {
		IServerWorkingCopy wc = ServerCreationUtils.createDeployOnlyServerWorkingCopy(deployLocation, tempDeployLocation, rtName, serverName);
		wc.setAttribute(Server.PROP_AUTO_PUBLISH_SETTING, Server.AUTO_PUBLISH_DISABLE);
		return wc.save(true, new NullProgressMonitor());
	}
	
	private static IServer createJBoss7IServer(IServerType serverType, String rtLoc, String name) throws CoreException {
		IRuntime runtime = RuntimeUtils.createRuntime(serverType.getRuntimeType().getId(), rtLoc, null);
		IServerWorkingCopy swc = ServerCreationUtils.createServerWorkingCopy(runtime, serverType, name, "local");
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
