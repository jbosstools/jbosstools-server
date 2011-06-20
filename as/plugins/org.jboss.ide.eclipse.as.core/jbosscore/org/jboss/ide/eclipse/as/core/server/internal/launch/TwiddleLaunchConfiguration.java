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


import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.RMIAdaptor;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SHUTDOWN_ADAPTER_ARG;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SHUTDOWN_PASS_ARG;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SHUTDOWN_SERVER_ARG;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SHUTDOWN_USER_ARG;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SPACE;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class TwiddleLaunchConfiguration extends AbstractJBossLaunchConfigType {

	public static final String TWIDDLE_LAUNCH_TYPE = "org.jboss.ide.eclipse.as.core.server.twiddleConfiguration"; //$NON-NLS-1$

	protected static final String TWIDDLE_MAIN_TYPE = IJBossRuntimeConstants.TWIDDLE_MAIN_TYPE;
	protected static final String TWIDDLE_JAR_LOC =
		IJBossRuntimeResourceConstants.BIN + File.separator + IJBossRuntimeResourceConstants.TWIDDLE_JAR;

	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IServer server, String args) throws CoreException {
		JBossServer jbs = ServerConverter.findJBossServer(server.getId());
		if (jbs == null) {
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(Messages.ServerNotFound, server.getName())));
		}
		IJBossServerRuntime jbrt = RuntimeUtils.getJBossServerRuntime(server);
		String serverHome = ServerUtil.getServerHome(jbs);
		
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
		LaunchConfigUtils.addCPEntry(TWIDDLE_JAR_LOC, serverHome, classpath);
		// Twiddle requires more classes and I'm too lazy to actually figure OUT which ones it needs.
		LaunchConfigUtils.addDirectory(serverHome, classpath, IJBossRuntimeResourceConstants.LIB);
		LaunchConfigUtils.addDirectory(serverHome, classpath, IJBossRuntimeResourceConstants.LIB + File.separator + IJBossRuntimeResourceConstants.ENDORSED);
		LaunchConfigUtils.addDirectory(serverHome, classpath, IJBossRuntimeResourceConstants.CLIENT);
		LaunchConfigUtils.addJREEntry(jbrt.getVM(), classpath);
		List<String> runtimeClassPaths = LaunchConfigUtils.toStrings(classpath);
		String cpKey = IJavaLaunchConfigurationConstants.ATTR_CLASSPATH;
		wc.setAttribute(cpKey, runtimeClassPaths);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);

		return wc;
	}
	
	public static String getDefaultArgs(IServer server) throws CoreException {
		JBossServer jbs = ServerConverter.findJBossServer(server.getId());
		if (jbs == null) {
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(Messages.ServerNotFound, server.getName())));
		}
		String twiddleArgs = SHUTDOWN_SERVER_ARG 
				+ SPACE 
				+ jbs.getHost()  + ":"  + jbs.getJNDIPort()  //$NON-NLS-1$
				+ SPACE 
				+ SHUTDOWN_ADAPTER_ARG 
				+ SPACE 
				+ RMIAdaptor 
				+ SPACE;
		if( jbs.getUsername() != null ) 
			twiddleArgs += SHUTDOWN_USER_ARG 
			+ SPACE + jbs.getUsername() 
			+ SPACE;
		if( jbs.getPassword() != null ) 
			twiddleArgs += SHUTDOWN_PASS_ARG 
			+ SPACE 
			+ jbs.getPassword() 
			+ SPACE;
		return twiddleArgs;
	}
}
