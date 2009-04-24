/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

public class TwiddleLaunchConfiguration extends AbstractJBossLaunchConfigType {

	public static final String TWIDDLE_LAUNCH_TYPE = "org.jboss.ide.eclipse.as.core.server.twiddleConfiguration"; //$NON-NLS-1$

	protected static final String TWIDDLE_MAIN_TYPE = IJBossRuntimeConstants.TWIDDLE_MAIN_TYPE;
	protected static final String TWIDDLE_JAR_LOC =
		IJBossRuntimeResourceConstants.BIN + File.separator + IJBossRuntimeResourceConstants.TWIDDLE_JAR;

	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IServer server) throws CoreException {
		return createLaunchConfiguration(server, getDefaultArgs(server));
	}

	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IServer server, String args) throws CoreException {
		JBossServer jbs = findJBossServer(server.getId());
		IJBossServerRuntime jbrt = findJBossServerRuntime(server);
		String serverHome = getServerHome(jbs);
		
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigType = launchManager.getLaunchConfigurationType(TWIDDLE_LAUNCH_TYPE);
		
		String launchName = TwiddleLaunchConfiguration.class.getName();
		launchName = launchManager.generateUniqueLaunchConfigurationNameFrom(launchName); 
		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, launchName);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, TWIDDLE_MAIN_TYPE);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, serverHome + Path.SEPARATOR + IJBossRuntimeResourceConstants.BIN);
		wc.setAttribute(TwiddleLaunchConfiguration.SERVER_ID, server.getId());

		ArrayList<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		addCPEntry(classpath, jbs, TWIDDLE_JAR_LOC);
		// Twiddle requires more classes and I'm too lazy to actually figure OUT which ones it needs.
		addDirectory (serverHome, classpath, IJBossRuntimeResourceConstants.LIB);
		addDirectory (serverHome, classpath, IJBossRuntimeResourceConstants.LIB + File.separator + IJBossRuntimeResourceConstants.ENDORSED);
		addDirectory (serverHome, classpath, IJBossRuntimeResourceConstants.CLIENT);
		addJREEntry(classpath, jbrt.getVM());
		ArrayList<String> runtimeClassPaths = convertClasspath(classpath);
		String cpKey = IJavaLaunchConfigurationConstants.ATTR_CLASSPATH;
		wc.setAttribute(cpKey, runtimeClassPaths);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);

		return wc;
	}
	
	public static String getDefaultArgs(IServer server) throws CoreException {
		IJBossRuntimeConstants c = new IJBossRuntimeConstants() { };
		JBossServer jbs = findJBossServer(server.getId());
		String twiddleArgs = c.SHUTDOWN_SERVER_ARG + c.SPACE + jbs.getHost() + ":"  //$NON-NLS-1$
				+ jbs.getJNDIPort() +  c.SPACE + c.SHUTDOWN_ADAPTER_ARG 
				+ c.SPACE + c.RMIAdaptor + c.SPACE;
		if( jbs.getUsername() != null ) 
			twiddleArgs += c.SHUTDOWN_USER_ARG + c.SPACE + jbs.getUsername() + c.SPACE;
		if( jbs.getPassword() != null ) 
			twiddleArgs += c.SHUTDOWN_PASS_ARG + c.SPACE + jbs.getPassword() + c.SPACE;
		return twiddleArgs;
	}

}
