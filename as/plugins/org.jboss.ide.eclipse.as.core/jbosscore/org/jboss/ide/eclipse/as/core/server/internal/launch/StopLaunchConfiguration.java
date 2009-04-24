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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;


public class StopLaunchConfiguration extends AbstractJBossLaunchConfigType {
	
	public static final String STOP_LAUNCH_TYPE = "org.jboss.ide.eclipse.as.core.server.stopLaunchConfiguration"; //$NON-NLS-1$
	public static final String STOP_MAIN_TYPE = IJBossRuntimeConstants.SHUTDOWN_MAIN_TYPE;
	public static final String STOP_JAR_LOC = IJBossRuntimeResourceConstants.BIN + File.separator + IJBossRuntimeResourceConstants.SHUTDOWN_JAR;
	
	/* Returns whether termination was normal */
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
	
	protected void preLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) {
	}

	
	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IServer server) throws CoreException {
		JBossServer jbs = findJBossServer(server.getId());
		IJBossServerRuntime jbrt = findJBossServerRuntime(server);
		String serverHome = getServerHome(jbs);
		
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigType = launchManager.getLaunchConfigurationType(STOP_LAUNCH_TYPE);
		
		String launchName = StopLaunchConfiguration.class.getName();
		launchName = launchManager.generateUniqueLaunchConfigurationNameFrom(launchName); 
		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, launchName);
		wc.setAttribute(SERVER_ID, server.getId());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getDefaultArgs(jbs));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, STOP_MAIN_TYPE);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, serverHome + Path.SEPARATOR + IJBossRuntimeResourceConstants.BIN);
		ArrayList<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		addCPEntry(classpath, jbs, STOP_JAR_LOC);
		addJREEntry(classpath, jbrt.getVM());
		ArrayList<String> runtimeClassPaths = convertClasspath(classpath);
		String cpKey = IJavaLaunchConfigurationConstants.ATTR_CLASSPATH;
		wc.setAttribute(cpKey, runtimeClassPaths);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);

		return wc;
	}

	public static String getDefaultArgs(JBossServer jbs) throws CoreException {
		IJBossRuntimeConstants c = new IJBossRuntimeConstants() {};
		String args = c.SHUTDOWN_STOP_ARG + c.SPACE;
		args += c.SHUTDOWN_SERVER_ARG + c.SPACE + jbs.getHost() + ":" + jbs.getJNDIPort() + c.SPACE; //$NON-NLS-1$
		if( jbs.getUsername() != null && !jbs.getUsername().equals(""))  //$NON-NLS-1$
			args += c.SHUTDOWN_USER_ARG + c.SPACE + jbs.getUsername() + c.SPACE;
		if( jbs.getPassword() != null && !jbs.getUsername().equals(""))  //$NON-NLS-1$
			args += c.SHUTDOWN_PASS_ARG + c.SPACE + jbs.getPassword() + c.SPACE;
		return args;
	}

}
