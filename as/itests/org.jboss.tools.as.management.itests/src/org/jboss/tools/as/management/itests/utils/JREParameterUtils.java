package org.jboss.tools.as.management.itests.utils;

import java.io.File;
import java.util.HashMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.tools.as.management.itests.utils.io.CommandRunner;
import org.junit.Assert;

public class JREParameterUtils extends Assert {

	private static final String JRE7_SYSPROP = "jbosstools.test.jre.7";
	private static final String JRE8_SYSPROP = "jbosstools.test.jre.8";

	public static IExecutionEnvironment getRequiredExecEnv(String runtimeType) {
		IRuntimeType type = ServerCore.findRuntimeType(runtimeType);
		System.out.println("getRequiredExecenv for " + type.getId());
		ServerExtendedProperties o = new ExtendedServerPropertiesAdapterFactory().getExtendedProperties(type);
		IExecutionEnvironment env = ((JBossExtendedProperties)o).getDefaultExecutionEnvironment();
		return env;
	}

	public static boolean requiresJava8(String runtimeType) {
		return "JavaSE-1.8".equals(getRequiredExecEnv(runtimeType).getId());
	}
	public static boolean requiresJava7(String runtimeType) {
		return "JavaSE-1.7".equals(getRequiredExecEnv(runtimeType).getId());
	}
	public static boolean requiresJava6(String runtimeType) {
		return "JavaSE-1.6".equals(getRequiredExecEnv(runtimeType).getId());
	}
	public static String getJavaHome(String rtType, String serverHome) {
		// We have some with java8 requirements. 
		ServerBeanLoader sb = new ServerBeanLoader(new File(serverHome));
		String version = sb.getFullServerVersion();
		System.out.println("**** ____  server version is " + version);
		if( requiresJava8(rtType)) {
			return getJavaHome(serverHome, JRE8_SYSPROP, "JavaSE-1.8");
		}
		// For all older, use j7
		return getJavaHome(serverHome, JRE7_SYSPROP, "JavaSE-1.7");
	}
	
	public static String getJavaHome(String serverHome, String sysprop, String javaVersion) {
		System.out.println("Getting java version for server: " + serverHome);
		String java = System.getProperty(sysprop);
		if( java == null ) {
			fail("Launching " + serverHome + " requires a " + javaVersion + ", which has not been provided via the " + sysprop + " system property");
		}

		if( !new File(java).exists()) {
			fail("Java Home " + java + " provided by the " + sysprop + " system property does not exist.");
		}

		if( sysprop.equals(JRE7_SYSPROP)) {
			verifyJava7HomeSet();
		} else if( sysprop.equals(JRE8_SYSPROP)) {
			verifyJava8HomeSet();
		}
		
		return java;
	}
	

	public static void verifyJava7HomeSet() throws RuntimeException {
		internalJavaHomeSet(getJava7Home(), JRE7_SYSPROP, "Java 7", "1.7.");
	}
	
	public static void verifyJava8HomeSet() throws RuntimeException {
		internalJavaHomeSet(getJava8Home(), JRE8_SYSPROP, "Java 8", "1.8.");
	}
	
	public static void internalJavaHomeSet(String home, String sysprop, 
			String asString, String versionPrefix) throws RuntimeException {
		if( home == null ) {
			fail("Sysprop  " + sysprop + " must be set.");
		}

		String cmdName = (Platform.getOS().equals(Platform.OS_WIN32) ? "java.exe" : "java");
		String bin = new Path(home).append("bin").append(cmdName).toOSString();
		assertTrue(asString + " binary does not exist", new File(bin).exists());
		try {
			String[] lines = CommandRunner.call(bin, new String[] {"-version"}, 
					ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(),
					new HashMap<String,String>(), 10000, CommandRunner.SYSERR);
			
			String versionString = getVersion(lines);
			assertNotNull("Unable to verify " + asString + " version", versionString);
			
			String versionError = asString + " version string must start with \"" + versionPrefix + "\". Please verify sysprop " 
					+ sysprop + ". Current version: " + versionString;
			assertTrue(versionError,versionString.startsWith(versionPrefix));
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}
	
	public static String getJava8Home() {
		String java = System.getProperty(JRE8_SYSPROP);
		return java;
	}
	
	public static String getJava7Home() {
		String java = System.getProperty(JRE7_SYSPROP);
		return java;
	}

	
	public static String getVersion(String[] arr) {
		if( arr == null || arr.length == 0 )
			return null;
		String lineOne = arr[0];
		String fromQuote = lineOne.substring(lineOne.indexOf("\"")).trim();
		return fromQuote.substring(1, fromQuote.length() - 1);
	}
	
}
