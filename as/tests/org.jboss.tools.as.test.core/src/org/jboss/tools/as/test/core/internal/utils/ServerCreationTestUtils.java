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
package org.jboss.tools.as.test.core.internal.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
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

import junit.framework.Assert;

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
	private static final String eap_server_6_1_jar = "eap6.1.0.mf.jboss-as-server.jar";
	
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
		asSystemJar.put(TEST_SERVER_TYPE_EAP_65, eap_server_6_1_jar);
		asSystemJar.put(TEST_SERVER_TYPE_JPP_60, "jpp6.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(TEST_SERVER_TYPE_JPP_61, "jpp6.1.0.mf.jboss-as-server.jar");
		asSystemJar.put(TEST_SERVER_TYPE_WONKA_1, eap_server_6_1_jar);
		asSystemJar.put(TEST_SERVER_TYPE_GATEIN_34, "gatein3.4.0.mf.jboss-as7-integration.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_32, "3.2.8" + twiddle_suffix);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_40, "4.0.5" + twiddle_suffix);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_42, "4.2.3" + twiddle_suffix);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_50, "5.0.0" + twiddle_suffix);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_51, "5.1.0" + twiddle_suffix);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_60, "6.0.0" + twiddle_suffix);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_70, "7.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_71,  "7.1.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_80, "wf8.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_90,  "wf9.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_100, "wf10.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_43, "eap4.3" + twiddle_suffix);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_50, "eap5.1" + twiddle_suffix);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_60, "eap6.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_61, "eap6.1.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_70, "eap7.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_110, "wf11.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_120, "wf12.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_130, "wf13.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_140, "wf14.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_150, "wf15.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_160, "wf16.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_170, "wf17.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_180, "wf18.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_190, "wf19.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_200, "wf20.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_210, "wf21.0.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_71, "eap7.1.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_72, "eap7.2.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_73, "eap7.3.0.mf.jboss-as-server.jar");
		asSystemJar.put(IJBossToolingConstants.SERVER_WILDFLY_220, "wf21.0.0.mf.jboss-as-server.jar");
		// AUTOGEN_SERVER_ADAPTER_CHUNK
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
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_90, IJBossToolingConstants.WILDFLY_90);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_43, IJBossToolingConstants.EAP_43);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_50, IJBossToolingConstants.EAP_50);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_60, IJBossToolingConstants.EAP_60);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_61, IJBossToolingConstants.EAP_61);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_100, IJBossToolingConstants.WILDFLY_100);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_110, IJBossToolingConstants.WILDFLY_110);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_120, IJBossToolingConstants.WILDFLY_120);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_130, IJBossToolingConstants.WILDFLY_130);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_140, IJBossToolingConstants.WILDFLY_140);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_150, IJBossToolingConstants.WILDFLY_150);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_160, IJBossToolingConstants.WILDFLY_160);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_170, IJBossToolingConstants.WILDFLY_170);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_180, IJBossToolingConstants.WILDFLY_180);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_190, IJBossToolingConstants.WILDFLY_190);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_200, IJBossToolingConstants.WILDFLY_200);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_210, IJBossToolingConstants.WILDFLY_210);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_70, IJBossToolingConstants.EAP_70);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_71, IJBossToolingConstants.EAP_71);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_72, IJBossToolingConstants.EAP_72);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_73, IJBossToolingConstants.EAP_73);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_WILDFLY_220, IJBossToolingConstants.WILDFLY_220);
		// AUTOGEN_SERVER_ADAPTER_CHUNK
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
		} else if( serverType.startsWith(IJBossToolingConstants.EAP_SERVER_PREFIX)) {
			String version = serverType.substring(IJBossToolingConstants.EAP_SERVER_PREFIX.length());
			String major = version.substring(0, version.length() - 1);
			String minor = version.substring(version.length() - 1);
			String newVersion = major + "." + minor + ".0.GA";
			IPath loc = mockedServers.append(name);
			String manString = "JBoss-Product-Release-Name: JBoss EAP\nJBoss-Product-Release-Version: " + newVersion + "\nJBoss-Product-Console-Slot: eap";
			createAS7xProductStructure(loc, true, asSystemJar.get(serverType), "eap", manString);
			serverDir = loc;
		} else if( IJBossToolingConstants.SERVER_WILDFLY_80.equals(serverType)) {
			serverDir = createWildfly80MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if( serverType.startsWith(IJBossToolingConstants.SERVER_AS_PREFIX)) {
			// wfly 9 through 20+++
			String version = serverType.substring(IJBossToolingConstants.WF_SERVER_PREFIX.length());
			String major = version.substring(0, version.length() - 1);
			String minor = version.substring(version.length() - 1);
			String newVersion = major + "." + minor + ".0.GA";
			String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: " + newVersion + "\n";
			if( Integer.parseInt(major) >= 19 )
				serverDir = createWildfly90PlusMockServerDirectory(name, serverType, asSystemJar.get(serverType), "main", manString);
			else 
				serverDir = createWildfly90PlusMockServerDirectory(name, serverType, asSystemJar.get(serverType), manString);
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
		// NEW_SERVER_ADAPTER add mock folder structure above
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

	public static IServer createServerWithRuntime(String serverType, String name, File f, IVMInstall vmi) throws CoreException {
		if( f != null ) {
			IServerType type = ServerCore.findServerType(serverType);
			if( ServerUtil.isJBoss7(type)) {
				return createJBoss7IServer(type, f.getAbsolutePath(), name, vmi);
			}
			return createServer(serverType, f.getAbsolutePath(), "default", name, vmi);
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
//	
//	private static IPath createWildfly90MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 9.0.0.Beta2\n";
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, manString);
//	}
//
//	private static IPath createWildfly100MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 10.0.0.Final\n";
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, manString);
//	}
//	
//	private static IPath createWildfly110MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 11.0.0.Alpha1-SNAPSHOT\n"; 
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, manString);
//	}
//
//	private static IPath createWildfly120MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 12.0.0.Alpha1-SNAPSHOT\n"; 
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, manString);
//	}
//
//	private static IPath createWildfly130MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 13.0.0.xyz\n"; 
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, manString);
//	}
//
//	private static IPath createWildfly140MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 14.0.0.xyz\n"; 
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, manString);
//	}
//
//	private static IPath createWildfly150MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 15.0.0.xyz\n"; 
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, manString);
//	}
//
//	private static IPath createWildfly160MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 16.0.0.xyz\n"; 
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, manString);
//	}
//
//	private static IPath createWildfly170MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 17.0.0.xyz\n"; 
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, manString);
//	}
//
//	private static IPath createWildfly180MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 18.0.0.xyz\n"; 
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, manString);
//	}
//	private static IPath createWildfly190MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 19.0.0.xyz\n"; 
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, "main", manString);
//	}
//	private static IPath createWildfly200MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 20.0.0.xyz\n"; 
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, "main", manString);
//	}
//
//	private static IPath createWildfly210MockServerDirectory(String name, String serverTypeId, String serverJar) {
//		String manString = "JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 21.0.0.xyz\n"; 
//		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, "main", manString);
//	}


	private static IPath createWildfly90PlusMockServerDirectory(String name, String serverTypeId, String serverJar, String manifestString) {
		return createWildfly90PlusMockServerDirectory(name, serverTypeId, serverJar, "wildfly-full", manifestString);
	}
	private static IPath createWildfly90PlusMockServerDirectory(String name, String serverTypeId, String serverJar, String slot, String manifestString) {

		IPath loc = mockedServers.append(name);
		createAS7xProductStructure(loc, true, serverJar, null, null);
		IPath productDir = loc.append("modules/system/layers/base/org/jboss/as/product/" + slot + "/dir/META-INF/");
		productDir.toFile().mkdirs();
		try {
			IOUtil.setContents(new File(productDir.toFile(), "manifest.mf"), manifestString);
		} catch(IOException ioe) {
			
		}
		return loc;
	}
	
	private static IPath createAS72EAP61StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		String manString = "JBoss-Product-Release-Name: EAP\nJBoss-Product-Release-Version: 6.1.0.Alpha\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, true, serverJar, "eap", manString);
		return loc;
	}

	private static IPath createEAP70StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		String manString = "JBoss-Product-Release-Name: JBoss EAP\nJBoss-Product-Release-Version: 7.0.0.GA\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, true, serverJar, "eap", manString);
		return loc;
	}

	private static IPath createEAP71StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		String manString = "JBoss-Product-Release-Name: JBoss EAP\nJBoss-Product-Release-Version: 7.1.0.GA\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, true, serverJar, "eap", manString);
		return loc;
	}
	
	private static IPath createEAP72StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		String manString = "JBoss-Product-Release-Name: JBoss EAP\nJBoss-Product-Release-Version: 7.2.0.GA\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, true, serverJar, "eap", manString);
		return loc;
	}

	private static IPath createEAP73StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		String manString = "JBoss-Product-Release-Name: JBoss EAP\nJBoss-Product-Release-Version: 7.3.0.GA\nJBoss-Product-Console-Slot: eap";
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
		return createServer(serverType, location, configuration, name, JavaRuntime.getDefaultVMInstall());
	}
	
	private static IServer createServer(String serverType,
			String location, String configuration, String name, IVMInstall vmi) throws CoreException {
		IRuntime runtime = RuntimeUtils.createRuntime(serverRuntimeMap.get(serverType), location, configuration, vmi);
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
		return createJBoss7IServer(serverType, rtLoc, name, JavaRuntime.getDefaultVMInstall());
	}
	
	private static IServer createJBoss7IServer(IServerType serverType, String rtLoc, String name, IVMInstall vmi) throws CoreException {
		IRuntime runtime = RuntimeUtils.createRuntime(serverType.getRuntimeType().getId(), rtLoc, null, vmi);
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
