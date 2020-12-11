/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.util.generation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class GenerateServerAdapterMain {
	public static final String CHUNK_DELIM_JAVA = "// AUTOGEN_SERVER_ADAPTER_CHUNK";
	public static final String CHUNK_DELIM_XML = "<!-- AUTOGEN_SERVER_ADAPTER_CHUNK -->";
	public static final String CHUNK_DELIM_PROPS = "# AUTOGEN_SERVER_ADAPTER_CHUNK";
	private static final boolean TYPE_WFLY = true;
	private static final boolean TYPE_EAP = false;
	
	
	public static void main(String[] args ) throws IOException {
		new GenerateServerAdapterMain().run();
	}
	
	private void run() throws IOException {
		File f = new File("").getCanonicalFile();
		File jbtServer = f.getParentFile().getParentFile().getParentFile();
		
//		String oldVersion = "73";
//		String newVersion = "74";
//		boolean type = TYPE_EAP;

		String oldVersion = "220";
		String newVersion = "230";
		boolean type = TYPE_WFLY;

		if( type == TYPE_WFLY ) {
			runWildFly(jbtServer, oldVersion, newVersion);
		} else {
			runEAP(jbtServer, oldVersion, newVersion);
		}
		
		System.out.println("Please update as/itests/pom.xml manually!!!");
		System.out.println("Changes made to: " + jbtServer);
	}

	private void runWildFly(File jbtServerRoot, String oldVersion, String newVersion) throws IOException {
		// TODO Auto-generated method stub
		String oldServerId = "org.jboss.ide.eclipse.as.wildfly." + oldVersion;
		String newServerId = "org.jboss.ide.eclipse.as.wildfly." + newVersion;
		String toolingConstantsServerKey = getServerToolingConstant(TYPE_WFLY, newVersion);
		String toolingConstantsRuntimeKey = getRuntimeToolingConstant(TYPE_WFLY, newVersion);
		
		copyFilesetData(jbtServerRoot, oldServerId, newServerId);
		addToEJB3ClasspathContainer(jbtServerRoot, toolingConstantsRuntimeKey);
		addToJBossModulesClasspathModel(jbtServerRoot, toolingConstantsRuntimeKey);
		addToClasspathCorePluginXml(jbtServerRoot, TYPE_WFLY, newVersion);
		addToXPathModel(jbtServerRoot, toolingConstantsRuntimeKey);
		addToExtendedServerPropertiesAdapterFactory(jbtServerRoot, toolingConstantsServerKey, 
				toolingConstantsRuntimeKey, TYPE_WFLY, newVersion);
		addToAsCorePluginPropertiesWildfly(jbtServerRoot, newVersion);
		addToAsCorePluginXmlWildflyImpl(jbtServerRoot, newVersion);
		addToInitialSelectionProvider(jbtServerRoot, toolingConstantsServerKey, toolingConstantsRuntimeKey);
		addToAsUiPluginXml(jbtServerRoot, newServerId);
		addToJbossServerTypeWildfly(jbtServerRoot, newVersion);
		addToToolingConstants(jbtServerRoot, TYPE_WFLY, newVersion);
		addToPluginProperties(jbtServerRoot, TYPE_WFLY, oldVersion, newVersion);
		addToEJB3SupportVerifierTest(jbtServerRoot, toolingConstantsServerKey);
		addToDriverUtil(jbtServerRoot, toolingConstantsRuntimeKey);
		addToServerBeanLoaderTest(jbtServerRoot, TYPE_WFLY, newVersion);
		addToServerBeanRuntimeMatcherTest (jbtServerRoot, TYPE_WFLY, newVersion);
		addToTestConstants(jbtServerRoot, TYPE_WFLY, newVersion);
		addToParameterUtils(jbtServerRoot, TYPE_WFLY, newVersion);
		addToServerParameterUtils(jbtServerRoot, TYPE_WFLY, newVersion);
		addToServerCreationTestUtils(jbtServerRoot, TYPE_WFLY, newVersion);
	}

	private void runEAP(File jbtServerRoot, String oldVersion, String newVersion) throws IOException {
		// TODO Auto-generated method stub
		String oldServerId = "org.jboss.ide.eclipse.as.eap." + oldVersion;
		String newServerId = "org.jboss.ide.eclipse.as.eap." + newVersion;
		String toolingConstantsServerKey = getServerToolingConstant(TYPE_EAP, newVersion);
		String toolingConstantsRuntimeKey = getRuntimeToolingConstant(TYPE_EAP, newVersion);
		
		copyFilesetData(jbtServerRoot, oldServerId, newServerId);
		addToEJB3ClasspathContainer(jbtServerRoot, toolingConstantsRuntimeKey);
		addToJBossModulesClasspathModel(jbtServerRoot, toolingConstantsRuntimeKey);
		addToClasspathCorePluginXml(jbtServerRoot, TYPE_EAP, newVersion);
		addToXPathModel(jbtServerRoot, toolingConstantsRuntimeKey);
		addToExtendedServerPropertiesAdapterFactory(jbtServerRoot, toolingConstantsServerKey, 
				toolingConstantsRuntimeKey, TYPE_EAP, newVersion);
		addToEJB3SupportVerifierTest(jbtServerRoot, toolingConstantsServerKey);
		addToParameterUtils(jbtServerRoot, TYPE_EAP, newVersion);
		addToDriverUtil(jbtServerRoot, toolingConstantsRuntimeKey);
		addToServerBeanLoaderTest(jbtServerRoot, TYPE_EAP, newVersion);
		addToAsUiPluginXml(jbtServerRoot, newServerId);
		addToServerBeanRuntimeMatcherTest (jbtServerRoot, TYPE_EAP, newVersion);
		addToTestConstants(jbtServerRoot, TYPE_EAP, newVersion);
		addToServerCreationTestUtils(jbtServerRoot, TYPE_EAP, newVersion);
		addToServerParameterUtils(jbtServerRoot, TYPE_EAP, newVersion);
		addToInitialSelectionProvider(jbtServerRoot, toolingConstantsServerKey, toolingConstantsRuntimeKey);
		addToPluginProperties(jbtServerRoot, TYPE_EAP, oldVersion, newVersion);
		addToToolingConstants(jbtServerRoot, TYPE_EAP, newVersion);
		addToAsCorePluginPropertiesEAP(jbtServerRoot, newVersion);
		addToAsCorePluginXmlEAPImpl(jbtServerRoot, newVersion);
		addToJbossServerTypeEAP(jbtServerRoot, newVersion);
	}



	private void addToServerParameterUtils(File jbtServerRoot, boolean type, String newVersion) throws IOException {
		// TODO Auto-generated method stub
		String path = "as/tests/org.jboss.tools.as.test.core/src/org/jboss/tools/as/test/core/internal/utils/ServerParameterUtils.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		
		String customDelim = CHUNK_DELIM_JAVA + "\n";
		String[] asChunks = readFileAsChunksJava(source, customDelim);

		if( type == TYPE_WFLY ) {
			String chunk1Tmp = removeFirstLine(asChunks[1]);
			chunk1Tmp += "TESTED_SERVERS.add(IJBossToolingConstants." + getServerToolingConstant(type, newVersion) + ");\n\t\t";
			asChunks[1] = chunk1Tmp;
		} else {
			String chunk1Tmp = removeFirstLine(asChunks[2]);
			chunk1Tmp += "TESTED_SERVERS.add(IJBossToolingConstants." + getServerToolingConstant(type, newVersion) + ");\n\t\t";
			asChunks[2] = chunk1Tmp;
		}
		String out = String.join(customDelim, Arrays.asList(asChunks));
		Files.write(source, out.toString().getBytes());
	}

	private void addToServerCreationTestUtils(File jbtServerRoot, boolean type, String newVersion) throws IOException {
		String path = "as/tests/org.jboss.tools.as.test.core/src/org/jboss/tools/as/test/core/internal/utils/ServerCreationTestUtils.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		
		String customDelim = CHUNK_DELIM_JAVA + "\n";
		String[] asChunks = readFileAsChunksJava(source, customDelim);
		String chunk0Tmp = asChunks[0];
		
		// Don't use the new version. Just revert to most recent one we use.
		// We never check the contents of this jar but it is copied over for legacy purposes and 
		// because we check that it exists. 
		String fname = "";
		if( type == TYPE_WFLY ) {
			String v = "21.0.0";
			fname = "wf" + v + ".mf.jboss-as-server.jar";
		} else {
			String version = "7.3.0";
			fname = "eap" + version + ".mf.jboss-as-server.jar";			
		}
		chunk0Tmp += "asSystemJar.put(IJBossToolingConstants." + getServerToolingConstant(type, newVersion) + ", \"" + fname + "\");\n\t\t";
		asChunks[0] = chunk0Tmp;

		String chunk1Tmp = asChunks[1];
		chunk1Tmp += "serverRuntimeMap.put(IJBossToolingConstants." + getServerToolingConstant(type, newVersion) + ", IJBossToolingConstants." + getRuntimeToolingConstant(type, newVersion) + ");\n\t\t";
		asChunks[1] = chunk1Tmp;

		String out = String.join(customDelim, Arrays.asList(asChunks));
		Files.write(source, out.toString().getBytes());
	}

	private void addToParameterUtils(File jbtServerRoot, boolean type, String newVersion) throws IOException {
		String path = "as/itests/org.jboss.tools.as.management.itests/src/org/jboss/tools/as/management/itests/utils/ParameterUtils.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		
		String customDelim = CHUNK_DELIM_JAVA + "\n";
		String[] asChunks = readFileAsChunksJava(source, customDelim);

		int constantInd = type == TYPE_WFLY ? 2 : 1;
		String chunkTmp = removeFirstLine(asChunks[constantInd]);
		String homeStaticVar = getJBossHomeTestKey(type, newVersion);
		chunkTmp += "public static final String " + homeStaticVar + " = TestConstants." + homeStaticVar.replaceAll("WILDFLY",  "WF") + ";\n\t";
		asChunks[constantInd] = chunkTmp;

		
		constantInd = type == TYPE_WFLY ? 5 : 4;
		String chunk3Tmp = removeFirstLine(asChunks[constantInd]);
		// serverHomeToRuntimeType.put(JBOSS_WILDFLY_210_HOME, IJBossToolingConstants.WILDFLY_210);
		String toAdd = "serverHomeToRuntimeType.put(" + homeStaticVar + ", IJBossToolingConstants." + getRuntimeToolingConstant(type, newVersion) + ");\n\t\t";
		chunk3Tmp += toAdd;
		asChunks[constantInd] = chunk3Tmp;

		if( type == TYPE_WFLY) {
			// wfly = 7
			String chunk7Tmp = removeFirstLine(asChunks[7]);
			String toAdd7 = "paths.add(" + homeStaticVar + ");\n\t\t";
			chunk7Tmp += toAdd7;
			asChunks[7] = chunk7Tmp;
		} else if( type == TYPE_EAP ) {
			// eap  = 9
			String chunk9Tmp = removeFirstLine(asChunks[9]);
			String toAdd9 = "paths.add(" + homeStaticVar + ");\n\t\t\t";
			chunk9Tmp += toAdd9;
			asChunks[9] = chunk9Tmp;
		}

		
		if( type == TYPE_WFLY) {
			// wfly = 11
			String chunk11Tmp = removeFirstLine(asChunks[11]);
			String toAdd11 = "paths.add(" + homeStaticVar + ");\n\t\t";
			chunk11Tmp += toAdd11;
			asChunks[11] = chunk11Tmp;
		} else if( type == TYPE_EAP ) {
			// eap  = 13
			String chunk13Tmp = removeFirstLine(asChunks[13]);
			String toAdd13 = "paths.add(" + homeStaticVar + ");\n\t\t\t";
			chunk13Tmp += toAdd13;
			asChunks[13] = chunk13Tmp;
		}
		
		String out = String.join(customDelim, Arrays.asList(asChunks));
		Files.write(source, out.toString().getBytes());
	}

	private String removeFirstLine(String s) {
		return s.substring(s.indexOf('\n')+1);
	}
	
	public static String getJBossHomeTestKey(boolean type, String newVersion) {
		// public static final String JBOSS_WF_210_HOME = System.getProperty("jbosstools.test.jboss.home.21.0", "C:\\apps\\jboss\\jboss-wildfly-21.0.0.GA\\");
		String shortType = (type == TYPE_WFLY ? "WF" : "EAP");
		return "JBOSS_" + shortType + "_" + newVersion + "_HOME";
	}
	
	private void addToTestConstants(File jbtServerRoot, boolean type, String newVersion) throws IOException {
		String path = "as/tests/org.jboss.tools.as.test.core/src/org/jboss/tools/as/test/core/TestConstants.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_JAVA);
		
		String defDir = "C:\\\\apps\\\\jboss\\\\";
		if( type == TYPE_WFLY ) {
			defDir += "jboss-wildfly-" + getMajorDotMinor(newVersion) + ".0.GA";
		} else if( type == TYPE_EAP ) {
			defDir += "jboss-eap-" + getMajorDotMinor(newVersion) + ".0.GA";
		}
		String homePropKeyVersion = (type == TYPE_WFLY ? getMajorDotMinor(newVersion) : getMajorDotMinor(newVersion).substring(0,3));
		String homePropKey = "jbosstools.test.jboss.home" + (type == TYPE_WFLY ? "" : ".eap") + "." + homePropKeyVersion;
		
		
		// public static final String JBOSS_WF_210_HOME = System.getProperty("jbosstools.test.jboss.home.21.0", "C:\\apps\\jboss\\jboss-wildfly-21.0.0.GA\\");
		String homeStaticVar = getJBossHomeTestKey(type, newVersion);
		String toAdd1 = "public static final String " + homeStaticVar + " = System.getProperty(\"" 
		+ homePropKey + "\", \"" + defDir + "\\\\\");";

		
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		out.append(toAdd1);
		out.append("\n\t");
		out.append(CHUNK_DELIM_JAVA);
		out.append(asChunks[1]);
		
		String nextAdd = "serverTypeToHome.put(IJBossToolingConstants." + getServerToolingConstant(type, newVersion) + ", " + homeStaticVar + ");";
		
		out.append(nextAdd);
		out.append("\n\t\t");
		out.append(CHUNK_DELIM_JAVA);
		out.append(asChunks[2]);
		
		Files.write(source, out.toString().getBytes());
	}

	private void addToServerBeanRuntimeMatcherTest (File jbtServerRoot, boolean type, String newVersion) throws IOException {
		String path = "as/tests/org.jboss.tools.as.test.core/src/org/jboss/tools/as/test/core/parametized/server/ServerBeanRuntimeMatcherTest.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_JAVA);
		String constant1 = getServerToolingConstant(type, newVersion);
		String char4 = getMajorDotMinor(newVersion);
		String jbossServerType = getJBossServerTypeString(type, newVersion);
		String toAdd = "expected.put(IJBossToolingConstants." + constant1 +
				", new Data(" + jbossServerType + ", \"" + char4 + "\"));\n\t\t";
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		out.append(toAdd);
		out.append(CHUNK_DELIM_JAVA);
		out.append(asChunks[1]);
		Files.write(source, out.toString().getBytes());
	}

	
	private void addToServerBeanLoaderTest(File jbtServerRoot, boolean type, String newVersion) throws IOException {
		String path = "as/tests/org.jboss.tools.as.test.core/src/org/jboss/tools/as/test/core/parametized/server/ServerBeanLoader3Test.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_JAVA);
		String constant1 = getServerToolingConstant(type, newVersion);
		String char4 = getMajorDotMinor(newVersion);
		String jbossServerType = getJBossServerTypeString(type, newVersion);
		String toAdd = "expected.put(IJBossToolingConstants." + constant1 +
				", new Data(" + jbossServerType + ", \"" + char4 + "\"));\n\t\t";
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		out.append(toAdd);
		out.append(CHUNK_DELIM_JAVA);
		out.append(asChunks[1]);
		Files.write(source, out.toString().getBytes());
	}
	
	private static String getJBossServerTypeString(boolean type, String version) {
		return type == TYPE_WFLY ? "JBossServerType.WILDFLY" + version : "JBossServerType.EAP" + version.substring(0,2);
		
	}
	private static String getMajorDotMinor(String majorMinor) {
		String major = majorMinor.substring(0, majorMinor.length() - 1);
		String minor = majorMinor.substring(majorMinor.length() - 1);
		String majorDotMinor = major + "." + minor;
		return majorDotMinor;
	}

	private static String getServerToolingConstant(boolean type, String version) {
		String typeString = (type == TYPE_WFLY ? "WILDFLY" : "EAP");
		return "SERVER_" + typeString + "_" + version;
	}
	
	public static String getRuntimeToolingConstant(boolean type, String version ) {
		String typeString = (type == TYPE_WFLY ? "WILDFLY" : "EAP");
		return 	typeString + "_" + version;
	}
	
	private void addToDriverUtil(File jbtServerRoot, String toolingConstantsRuntimeKey) throws IOException {
		String path = "as/plugins/org.jboss.tools.as.runtimes.integration/src/org/jboss/tools/as/runtimes/integration/internal/DriverUtility.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_JAVA);
		String toAdd = "RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants." + toolingConstantsRuntimeKey + ", new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));\n\t\t";
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		out.append(toAdd);
		out.append(CHUNK_DELIM_JAVA);
		out.append(asChunks[1]);
		Files.write(source, out.toString().getBytes());
	}

	private void addToEJB3SupportVerifierTest(File jbtServerRoot, String toolingConstantsServerKey) throws IOException {
		String path = "as/itests/org.jboss.tools.as.itests/src/org/jboss/tools/as/itests/EJB3SupportVerifierTest.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_JAVA);
		
		String toAdd = "\n\t\texpected.put(IJBossToolingConstants." + toolingConstantsServerKey + ", true);";
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		out.append(CHUNK_DELIM_JAVA);
		out.append(toAdd);
		out.append(asChunks[1]);
		Files.write(source, out.toString().getBytes());
	}


	private void addToPluginProperties(File jbtServerRoot, boolean type, String oldVersion, String newVersion) throws IOException{
		ArrayList<File> pluginPropertiesArr = new ArrayList<>();
		Files.walk(jbtServerRoot.toPath())
        	.filter(Files::isRegularFile)
        	.forEach((f)->{
        		String name = f.toFile().getName();
        		if( name.equals("plugin.properties"))
        			pluginPropertiesArr.add(f.toFile());              
        });
		addToSinglePluginProperties(pluginPropertiesArr, type, oldVersion, newVersion);
	}

	private void addToSinglePluginProperties(ArrayList<File> pluginPropertiesArr, boolean type,
			String oldVersion, String newVersion) throws IOException {
		String serverPrefix = (type == TYPE_WFLY ? IJBossToolingConstants.WF_SERVER_PREFIX : IJBossToolingConstants.EAP_SERVER_PREFIX);
		String runtimePrefix = (type == TYPE_WFLY ? IJBossToolingConstants.WF_RUNTIME_PREFIX : IJBossToolingConstants.EAP_RUNTIME_PREFIX);
		String oldVersionRuntimeString = runtimePrefix + oldVersion;
		String newVersionRuntimeString = runtimePrefix + newVersion;
		String oldVersionServerString = serverPrefix + oldVersion;
		String newVersionServerString = serverPrefix + newVersion;
		
		String oldPlusNewRuntime = oldVersionRuntimeString + "," + newVersionRuntimeString;
		String oldPlusNewServer = oldVersionServerString + "," + newVersionServerString;
		for( File f : pluginPropertiesArr ) {
			byte[] all = Files.readAllBytes(f.toPath());
			String asString = new String(all);
			asString = asString.replaceAll(oldVersionRuntimeString, oldPlusNewRuntime);
			asString = asString.replaceAll(oldVersionServerString, oldPlusNewServer);
			Files.write(f.toPath(), asString.getBytes());
		}
	}

	private void addToToolingConstants(File jbtServerRoot, boolean type, String newVersion) throws IOException {
		String path = "as/plugins/org.jboss.ide.eclipse.as.wtp.core/src/org/jboss/ide/eclipse/as/core/util/IJBossToolingConstants.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_JAVA);
		
		String char3 = newVersion;
		
		String toolingConstantsServerKey = (type == TYPE_WFLY ? "SERVER_WILDFLY_" : "SERVER_EAP_");
		String toolingConstantsRuntimeKey = (type == TYPE_WFLY ? "WILDFLY_" : "EAP_" );
		String toolingConstantsServerKeyWithVersion = toolingConstantsServerKey + newVersion; 
		String toolingConstantsRuntimeKeyWithVersion = toolingConstantsRuntimeKey + newVersion;
		String rtPrefix = (type == TYPE_WFLY ? IJBossToolingConstants.WF_RUNTIME_PREFIX : IJBossToolingConstants.EAP_RUNTIME_PREFIX);
		String serverPrefix = (type == TYPE_WFLY ? IJBossToolingConstants.WF_SERVER_PREFIX : IJBossToolingConstants.EAP_SERVER_PREFIX);
		String l1 = "public static final String " + toolingConstantsRuntimeKeyWithVersion + " = \"" + rtPrefix + char3 + "\"; //$NON-NLS-1$\n\t";
		String l2 = "public static final String " + toolingConstantsServerKeyWithVersion + " = \"" + serverPrefix + char3 + "\"; //$NON-NLS-1$";

		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		if( type == TYPE_WFLY ) {
			out.append(CHUNK_DELIM_JAVA);
			out.append("\n\t");
			out.append(l1);
			out.append(l2);
		} else {
			out.append(l1);
			out.append(l2);
			out.append("\n\t");
			out.append(CHUNK_DELIM_JAVA);
		}
		out.append(asChunks[1]);
		out.append(toolingConstantsRuntimeKeyWithVersion + ",\n\t\t\t");
		out.append(CHUNK_DELIM_JAVA);
		out.append(asChunks[2]);
		out.append(toolingConstantsServerKeyWithVersion + ",\n\t\t");
		out.append(CHUNK_DELIM_JAVA);
		out.append(asChunks[3]);
		Files.write(source, out.toString().getBytes());
	}

	private void addToJbossServerTypeWildfly(File jbtServerRoot, String majorMinor) throws IOException {
		String path = "as/plugins/org.jboss.ide.eclipse.as.wtp.core/src/org/jboss/ide/eclipse/as/core/server/bean/JBossServerType.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_JAVA);
		
		String major = majorMinor.substring(0, majorMinor.length() - 1);
		String minor = majorMinor.substring(majorMinor.length() - 1);
		String majorDotMinor = major + "." + minor;
		
		String l1 = "public static final JBossServerType WILDFLY" + majorMinor + " = new ServerBeanTypeWildfly10Plus(\"" + majorDotMinor + "\", IJBossToolingConstants.SERVER_WILDFLY_" + majorMinor + ", \"main\");";
		String l2 = "\tpublic static final JBossServerType WILDFLY" + majorMinor + "_WEB = new ServerBeanTypeWildfly10PlusWeb(\"" + majorDotMinor + "\", IJBossToolingConstants.SERVER_WILDFLY_" + majorMinor + ");";

		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		out.append(l1);
		out.append("\n");
		out.append(l2);
		out.append("\n\t");
		out.append(CHUNK_DELIM_JAVA);
		out.append("\n");
		out.append(asChunks[1]);
		out.append(CHUNK_DELIM_JAVA);
		out.append("\n");
		out.append(asChunks[2]);
		out.append("JBossServerType.WILDFLY" + majorMinor + ",JBossServerType.WILDFLY" + majorMinor + "_WEB,\n\t\t");
		out.append(CHUNK_DELIM_JAVA);
		out.append("\n");
		out.append(asChunks[3]);
		Files.write(source, out.toString().getBytes());
		
	}

	private void addToJbossServerTypeEAP(File jbtServerRoot, String majorMinor) throws IOException {
		String path = "as/plugins/org.jboss.ide.eclipse.as.wtp.core/src/org/jboss/ide/eclipse/as/core/server/bean/JBossServerType.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_JAVA);
		
		String mdm = getMajorDotMinor(majorMinor);
		String l1 = "public static final JBossServerType EAP" + majorMinor 
				+ " = new ServerBeanTypeEAP7Plus(\"" + mdm + "\", IJBossToolingConstants.SERVER_EAP_" + majorMinor + ");\n\t";

		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		out.append(l1);
		out.append(CHUNK_DELIM_JAVA);
		out.append("\n");
		out.append(asChunks[1]);
		out.append("JBossServerType.EAP" + majorMinor + ", \n\t\t");
		out.append(CHUNK_DELIM_JAVA);
		out.append("\n");
		out.append(asChunks[2]);
		out.append(CHUNK_DELIM_JAVA);
		out.append("\n");
		out.append(asChunks[3]);
		Files.write(source, out.toString().getBytes());
	}

	private void addToAsUiPluginXml(File jbtServerRoot, String newServerId) throws IOException {
		String path = "as/plugins/org.jboss.ide.eclipse.as.ui/plugin.xml";
		Path source = jbtServerRoot.toPath().resolve(path);
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_XML);
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		
		String toAdd = "<test property=\"org.eclipse.wst.server.ui.serverType\" value=\"" + newServerId + "\"></test>";
		out.append(toAdd);
		out.append("\n\t\t\t\t\t\t");
		out.append(CHUNK_DELIM_XML);
		out.append("\n");
		out.append(asChunks[1]);
		Files.write(source, out.toString().getBytes());
	}

	private void addToInitialSelectionProvider(File jbtServerRoot, String serverKey, String runtimeKey) throws IOException {
		String path = "as/plugins/org.jboss.ide.eclipse.as.ui/jbossui/org/jboss/ide/eclipse/as/ui/wizards/JBInitialSelectionProvider.java";
		Path source = jbtServerRoot.toPath().resolve(path);
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_JAVA);
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		out.append(CHUNK_DELIM_JAVA);
		out.append("\n");
		out.append("\tprivate static final String LATEST_JBT_SERVER = IJBossToolingConstants." + serverKey + ";");
		out.append("\n");
		out.append("\tprivate static final String LATEST_JBT_RUNTIME = IJBossToolingConstants." + runtimeKey+ ";");
		out.append("\n\t");
		out.append(CHUNK_DELIM_JAVA);
		out.append("\n");
		out.append(asChunks[2]);
		Files.write(source, out.toString().getBytes());
		
	}

	private void addToAsCorePluginXmlWildflyImpl(File jbtServerRoot, String newVersion) throws IOException {
		//as/tests/org.jboss.tools.as.test.core/src/org/jboss/tools/as/test/core/util/generation/asCoreWildflyPluginXml.txt
		Path source = jbtServerRoot.toPath().resolve("as/plugins/org.jboss.ide.eclipse.as.core/plugin.xml");
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_XML);
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		
		
		String char2 = newVersion.substring(0,2);
		String char3 = newVersion;
		String char4 = newVersion.substring(0,2) + "." + newVersion.substring(2);
		Path template = jbtServerRoot.toPath().resolve("as/tests/org.jboss.tools.as.test.core/src/org/jboss/tools/as/test/core/util/generation/asCoreWildflyPluginXml.txt");
		byte[] all = Files.readAllBytes(template);
		String asString = new String(all);
		asString = asString.replaceAll("AUTOGEN_SERVER_2CHAR_VERS", char2);
		asString = asString.replaceAll("AUTOGEN_SERVER_3CHAR_VERS", char3);
		asString = asString.replaceAll("AUTOGEN_SERVER_4CHAR_VERS", char4);
		//
		out.append(asString);
		out.append("\n");
		out.append(CHUNK_DELIM_XML);
		out.append(asChunks[1]);
		Files.write(source, out.toString().getBytes());
	}
	private void addToAsCorePluginXmlEAPImpl(File jbtServerRoot, String newVersion) throws IOException {
		Path source = jbtServerRoot.toPath().resolve("as/plugins/org.jboss.ide.eclipse.as.core/plugin.xml");
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_XML);
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		
		String majorDotMinor = getMajorDotMinor(newVersion);
		String majorMinor = newVersion;
		Path template = jbtServerRoot.toPath().resolve("as/tests/org.jboss.tools.as.test.core/src/org/jboss/tools/as/test/core/util/generation/asCoreEAPPluginXml.txt");
		byte[] all = Files.readAllBytes(template);
		String asString = new String(all);
		asString = asString.replaceAll("AUTOGEN_SERVER_MAJOR_DOT_MINOR", majorDotMinor);
		asString = asString.replaceAll("AUTOGEN_SERVER_MAJOR_MINOR", majorMinor);
		//
		out.append(asString);
		out.append("\n");
		out.append(CHUNK_DELIM_XML);
		out.append(asChunks[1]);
		Files.write(source, out.toString().getBytes());
	}

	private void addToAsCorePluginPropertiesWildfly(File jbtServerRoot, String newVersion) throws IOException {
		Path source = jbtServerRoot.toPath().resolve("as/plugins/org.jboss.ide.eclipse.as.core/plugin.properties");
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_PROPS);
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		
		String newVersionTrim = newVersion.substring(0,2);
		out.append("jboss.version.wildfly" + newVersionTrim + ".name=WildFly " + newVersionTrim + "\n");
		out.append("jboss.version.wildfly" + newVersionTrim + ".description=WildFly Application Server " + newVersionTrim + "\n");
		out.append("jboss.version.wildfly" + newVersionTrim + ".runtime.name=WildFly " + newVersionTrim + " Runtime\n");
		out.append(CHUNK_DELIM_PROPS + "\n");
		out.append(asChunks[1]);
		Files.write(source, out.toString().getBytes());
	}

	private void addToAsCorePluginPropertiesEAP(File jbtServerRoot, String newVersion) throws IOException {
		Path source = jbtServerRoot.toPath().resolve("as/plugins/org.jboss.ide.eclipse.as.core/plugin.properties");
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_PROPS);
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		
		String newVersionTrim = newVersion.substring(0,2);
		String majorDotMinor = newVersionTrim.charAt(0) + "." + newVersionTrim.charAt(1);
		out.append("jboss.eap.version." + newVersionTrim + ".name=Red Hat JBoss Enterprise Application Platform " + majorDotMinor + "\n");
		out.append("jboss.eap.version." + newVersionTrim + ".description=Red Hat JBoss Enterprise Application Platform (EAP) " + majorDotMinor + "\n");
		out.append("jboss.eap.version." + newVersionTrim + ".runtime.name=Red Hat JBoss Enterprise Application Platform " + newVersionTrim + " Runtime\n");
		out.append(CHUNK_DELIM_PROPS + "\n");
		out.append(asChunks[1]);
		Files.write(source, out.toString().getBytes());
	}

	private void addToExtendedServerPropertiesAdapterFactory(File jbtServerRoot, 
			String serverKey,String runtimeKey, boolean type, String version) throws IOException {
		String extendedPropsName = (type == TYPE_WFLY ? "Wildfly15PlusExtendedProperties" : "JBossEAP73ExtendedProperties");
		String newVersionString = getMajorDotMinor(version);
		
		Path source = jbtServerRoot.toPath().resolve("as/plugins/org.jboss.ide.eclipse.as.core/jbosscore/org/jboss/ide/eclipse/as/core/server/internal/ExtendedServerPropertiesAdapterFactory.java");
		String[] asChunks = readFileAsChunksJava(source, CHUNK_DELIM_JAVA);
		StringBuffer out = new StringBuffer();
		out.append(asChunks[0]);
		out.append(CHUNK_DELIM_JAVA);
		out.append("\n			");
		out.append("if( " + serverKey + ".equals(typeId) || " + runtimeKey + ".equals(typeId))\n");
		out.append("				return new " + extendedPropsName + "(adaptable, \"" + newVersionString +"\");");
		out.append(asChunks[1]);
		Files.write(source, out.toString().getBytes());
	}

	private void addToXPathModel(File jbtServerRoot, String toolingConstantsRuntimeKey) throws IOException {
		// JBossModulesDefaultClasspathModel
		Path classpathCoreSource = jbtServerRoot.toPath().resolve("as/plugins/org.jboss.ide.eclipse.as.core/jbosscore/org/jboss/ide/eclipse/as/core/extensions/descriptors/XPathModel.java");
		String[] asChunks = readFileAsChunksJava(classpathCoreSource, CHUNK_DELIM_JAVA);
		String output = asChunks[0];
		output += CHUNK_DELIM_JAVA + "\n		";
		output += "rtToPortsFile.put(IConstants." + toolingConstantsRuntimeKey + ", getURLFor(DEFAULT_PROPS_80));";
		output += asChunks[1];
		Files.write(classpathCoreSource, output.getBytes());
	}
	
	
	private void addToClasspathCorePluginXml(File jbtServerRoot, boolean wildflyOrEap, String newVersion) throws IOException {
		// JBossModulesDefaultClasspathModel
		Path classpathCoreSource = jbtServerRoot.toPath().resolve("as/plugins/org.jboss.ide.eclipse.as.classpath.core/plugin.xml");
		String[] asChunks = readFileAsChunksJava(classpathCoreSource, CHUNK_DELIM_XML);
		String classpathCoreOutput = asChunks[0];
		classpathCoreOutput += getClasspathCorePluginXmlAddition(wildflyOrEap, newVersion);
		classpathCoreOutput += asChunks[1];
		Files.write(classpathCoreSource, classpathCoreOutput.getBytes());
	}
	
	private String getClasspathCorePluginXmlAddition(boolean wildflyOrEap, String newVersion) {
		String major = newVersion.substring(0, newVersion.length() - 1);
		String minor = newVersion.substring(newVersion.length() - 1);
		String newVersionString = major + "." + minor;
		String componentId = (wildflyOrEap == TYPE_WFLY ? "org.jboss.ide.eclipse.as.runtime.wildfly.component" : "org.jboss.ide.eclipse.eap.runtime.component");

		StringBuffer sb = new StringBuffer();
		sb.append(CHUNK_DELIM_XML);
		sb.append("\n	<adapter>\n");
		sb.append("      <runtime-component\n");
		sb.append("	            id=\"" + componentId + "\" version=\"" + newVersionString + "\"/>\n");
		sb.append("      <factory class=\"org.jboss.ide.eclipse.as.classpath.core.runtime.internal.JBossClasspathProviderAdapterFactory\"/>\n");
	    sb.append("      <type class=\"org.eclipse.jst.common.project.facet.core.IClasspathProvider\"/>\n");
		sb.append("	</adapter>");
		return sb.toString();
	}

	private void addToJBossModulesClasspathModel(File jbtServerRoot, String rtId) throws IOException {
		// JBossModulesDefaultClasspathModel
		Path jbossModulesClasspathSource = jbtServerRoot.toPath().resolve("as/plugins/org.jboss.ide.eclipse.as.classpath.core/src/org/jboss/ide/eclipse/as/classpath/core/runtime/jbossmodules/internal/JBossModulesDefaultClasspathModel.java");
		String[] asChunks = readFileAsChunksJava(jbossModulesClasspathSource, CHUNK_DELIM_JAVA);
		String jbossModulesClasspathOutput = asChunks[0];
		jbossModulesClasspathOutput += "IJBossToolingConstants." + rtId + ",\n			" + CHUNK_DELIM_JAVA;
		jbossModulesClasspathOutput += asChunks[1];
		Files.write(jbossModulesClasspathSource, jbossModulesClasspathOutput.getBytes());
	}
	
	private void addToEJB3ClasspathContainer(File jbtServerRoot, String rtId) throws IOException {
		Path ejb3ClasspathSource = jbtServerRoot.toPath().resolve("as/plugins/org.jboss.ide.eclipse.as.classpath.core/src/org/jboss/ide/eclipse/as/classpath/core/ejb3/EJB3ClasspathContainer.java");
		String[] asChunks = readFileAsChunksJava(ejb3ClasspathSource, CHUNK_DELIM_JAVA);
		String ejb3ClasspathOutput = asChunks[0];
		ejb3ClasspathOutput += "case " + rtId + ":\n                " + CHUNK_DELIM_JAVA;
		ejb3ClasspathOutput += asChunks[1];
		Files.write(ejb3ClasspathSource, ejb3ClasspathOutput.getBytes());
	}
	
	private void copyFilesetData(File jbtServerRoot, String oldId, String newId) throws IOException {
		File filesetData = jbtServerRoot.toPath().resolve("as/plugins/org.jboss.ide.eclipse.archives.webtools/filesetdata/").toFile();
		File oldFile = new File(filesetData, oldId);
		File newFile = new File(filesetData, newId);
		Files.copy(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	private String[] readFileAsChunksJava(Path file, String delim) throws IOException {
		byte[] all = Files.readAllBytes(file);
		String asString = new String(all);
		String[] split = asString.split(delim);
		return split;
	}
	

}
