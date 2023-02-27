/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.as.management.itests.utils;

import java.io.File;
import java.util.HashMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.tools.as.management.itests.utils.io.CommandRunner;
import org.junit.Assert;

public class JREParameterUtils extends Assert {

	private static final String JRE8_SYSPROP = "jbosstools.test.jre.8";
	private static final String JRE11_SYSPROP = "jbosstools.test.jre.11";

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
	
	public static boolean requiresJava11(String runtimeType) {
		return "JavaSE-11".equals(getRequiredExecEnv(runtimeType).getId());
	}

	public static String getJavaHome(String serverHome) {
		// We have some with java8 requirements. 
		ServerBeanLoader sb = new ServerBeanLoader(new File(serverHome));
		String version = sb.getFullServerVersion();
		System.out.println("**** ____  server version is " + version);
		String serverType = ParameterUtils.getServerType(serverHome);
		IServerType st = ServerCore.findServerType(serverType);
		IRuntimeType rtt = st.getRuntimeType();
		String rtType = rtt.getId();
		if( requiresJava11(rtType) ) {
			return getJavaHome(serverHome, JRE11_SYSPROP, "JavaSE-11");
		}
		return getJavaHome(serverHome, JRE8_SYSPROP, "JavaSE-1.8");
	}
	
	public static String getJavaHome(String serverHome, String sysprop, String javaVersion) {
		System.out.println("Getting java version for server: " + serverHome + " using sysprop " + sysprop);
		String java = System.getProperty(sysprop);
		System.out.println("Returned java is " + java);
		if (java == null) {
			fail("Launching " + serverHome + " requires a " + javaVersion + ", which has not been provided via the "
					+ sysprop + " system property");
		}

		if (!new File(java).exists()) {
			fail("Java Home " + java + " provided by the " + sysprop + " system property does not exist.");
		}

		if (sysprop.equals(JRE8_SYSPROP)) {
			verifyJava8HomeSet();
		}
		
		if (sysprop.equals(JRE11_SYSPROP)) {
			verifyJava11HomeSet();
		}

		return java;
	}
	
	public static void verifyJava11HomeSet() {
		internalJavaHomeSet(getJava11Home(), JRE11_SYSPROP, "Java 11", "11");
	}
	
	public static void verifyJava8HomeSet() {
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
		String[] lines = null;
		try {
			lines = CommandRunner.call(bin, new String[] {"-version"}, 
					ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(),
					new HashMap<String,String>(), 10000, CommandRunner.SYSERR);
		} catch(Exception e) {
			String err = "Attempt to call '" + bin + " -version' has failed: " + e.getMessage() + "\n\nOutput: \n";
			if( lines != null ) {
				for( int i = 0; i < lines.length; i++ ) {
					if( lines[i] != null ) {
						err = err + lines[i] + "\n";
					}
				}
			}
			fail(err);
		}
		
		assertNotNull(lines);
		String versionString = getVersion(lines);
		assertNotNull("Unable to verify " + asString + " version", versionString);
		
		String versionError = asString + " version string must start with \"" + versionPrefix + "\". Please verify sysprop " 
				+ sysprop + ". Current version: " + versionString;
		assertTrue(versionError,versionString.startsWith(versionPrefix));
	}
	
	public static String getJava11Home() {
		return System.getProperty(JRE11_SYSPROP);
	}
	
	public static String getJava8Home() {
		return System.getProperty(JRE8_SYSPROP);
	}
	
	public static String getVersion(String[] arr) {
		if( arr == null || arr.length == 0 )
			return null;
		String lineOne = arr[0];
		String fromQuote = lineOne.substring(lineOne.indexOf("\"")).trim();
		return fromQuote.substring(1, fromQuote.length() - 1);
	}
	
}
