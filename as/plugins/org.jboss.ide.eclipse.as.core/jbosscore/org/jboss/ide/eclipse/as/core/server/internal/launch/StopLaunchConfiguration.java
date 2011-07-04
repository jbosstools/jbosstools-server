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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBoss6Server;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;


@Deprecated
public class StopLaunchConfiguration extends AbstractJBossLaunchConfigType {
	
	public static final String STOP_LAUNCH_TYPE = "org.jboss.ide.eclipse.as.core.server.stopLaunchConfiguration"; //$NON-NLS-1$
	public static final String STOP_MAIN_TYPE = IJBossRuntimeConstants.SHUTDOWN_MAIN_TYPE;
	public static final String STOP_JAR_LOC = IJBossRuntimeResourceConstants.BIN + File.separator + IJBossRuntimeResourceConstants.SHUTDOWN_JAR;
	
	/* Returns whether termination was normal */
	@Deprecated
	public static boolean stop(IServer server) {
		try {
			ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration(server);
			ILaunch launch = wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
			IProcess stopProcess = launch.getProcesses()[0];
			while( !stopProcess.isTerminated()) {
				try {
					Thread.yield();
					Thread.sleep(100);
				} catch(InterruptedException ie) {
				}
			}
			return stopProcess.getExitValue() == 0 ? true : false;
		} catch( CoreException ce ) {
			// report it from here
			IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					Messages.UnexpectedServerStopError, ce);
			JBossServerCorePlugin.getDefault().getLog().log(s);
			return false;
		}
	}
	
	private static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IServer server) throws CoreException {
		JBossServer jbs = ServerConverter.checkedGetJBossServer(server);
		IJBossServerRuntime jbrt = RuntimeUtils.checkedGetJBossServerRuntime(server);
		IPath serverHome = ServerUtil.getServerHomePath(jbs);
		
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigType = launchManager.getLaunchConfigurationType(STOP_LAUNCH_TYPE);
		
		String launchName = StopLaunchConfiguration.class.getName();
		launchName = launchManager.generateUniqueLaunchConfigurationNameFrom(launchName); 
		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, launchName);
		JBossLaunchConfigProperties.setServerId(server.getId(), wc);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getDefaultArgs(jbs));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, STOP_MAIN_TYPE);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, serverHome.append(IJBossRuntimeResourceConstants.BIN).toOSString());
		ArrayList<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		LaunchConfigUtils.addCPEntry(serverHome, STOP_JAR_LOC, classpath);
		LaunchConfigUtils.addJREEntry(jbrt.getVM(), classpath);
		List<String> runtimeClassPaths = LaunchConfigUtils.toStrings(classpath);
		String cpKey = IJavaLaunchConfigurationConstants.ATTR_CLASSPATH;
		wc.setAttribute(cpKey, runtimeClassPaths);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);

		return wc;
	}

	/**
	 * moved to {@link JBossServerBehavior#getDefaultStopArguments()}
	 */
	@Deprecated
	public static String getDefaultArgs(JBossServer jbs) {
		String runtimeTypeId = jbs.getRuntime().getRuntime().getRuntimeType().getId();
		String serverUrl;
		if (runtimeTypeId.equals(IJBossToolingConstants.AS_60)){
			IJBoss6Server server6 = (IJBoss6Server)jbs.getServer().loadAdapter(IJBoss6Server.class, new NullProgressMonitor());
			serverUrl = "service:jmx:rmi:///jndi/rmi://" + jbs.getHost() + ":" + server6.getJMXRMIPort() + "/jmxrmi"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			serverUrl = jbs.getHost() + ":" + jbs.getJNDIPort(); //$NON-NLS-1$
		}
		String args = IJBossRuntimeConstants.SHUTDOWN_STOP_ARG 
				+ IJBossRuntimeConstants.SPACE;
		args += IJBossRuntimeConstants.SHUTDOWN_SERVER_ARG 
				+ IJBossRuntimeConstants.SPACE 
				+ serverUrl 
				+ IJBossRuntimeConstants.SPACE;
		if( jbs.getUsername() != null && !jbs.getUsername().equals(""))  //$NON-NLS-1$
			args += IJBossRuntimeConstants.SHUTDOWN_USER_ARG 
			+ IJBossRuntimeConstants.SPACE + jbs.getUsername() + IJBossRuntimeConstants.SPACE;
		if( jbs.getPassword() != null && !jbs.getPassword().equals(""))  //$NON-NLS-1$
			args += IJBossRuntimeConstants.SHUTDOWN_PASS_ARG 
			+ IJBossRuntimeConstants.SPACE + jbs.getPassword() + IJBossRuntimeConstants.SPACE;
		return args;
	}
}
