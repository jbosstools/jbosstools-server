/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;


public class StopLaunchConfiguration extends AbstractJBossLaunchConfigType {
	
	public static final String STOP_LAUNCH_TYPE = "org.jboss.ide.eclipse.as.core.server.stopLaunchConfiguration";
	public static final String STOP_MAIN_TYPE = "org.jboss.Shutdown";
	public static final String STOP_JAR_LOC = "bin" + File.separator + "shutdown.jar";
	
	/* Returns whether termination was normal */
	public static boolean stop(IServer server) {
		try {
			ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration(server);
			ILaunch launch = wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
			IProcess stopProcess = launch.getProcesses()[0];
			while( !stopProcess.isTerminated()) {}
			return stopProcess.getExitValue() == 0 ? true : false;
		} catch( CoreException ce ) {
			// report it from here
			IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					"Unexpected Exception launching stop server command: ", ce);
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
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, serverHome + Path.SEPARATOR + "bin");
		ArrayList classpath = new ArrayList();
		addCPEntry(classpath, jbs, STOP_JAR_LOC);
		ArrayList runtimeClassPaths = convertClasspath(classpath, jbrt.getVM());
		String cpKey = IJavaLaunchConfigurationConstants.ATTR_CLASSPATH;
		wc.setAttribute(cpKey, runtimeClassPaths);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);

		return wc;
	}

	public static String getDefaultArgs(JBossServer jbs) throws CoreException {
		String args = "-S ";
		if( jbs.getUsername() != null && !jbs.getUsername().equals("")) 
			args += "-u " + jbs.getUsername() + " ";
		if( jbs.getPassword() != null && !jbs.getUsername().equals("")) 
			args += "-p " + jbs.getPassword() + " ";
		return args;
	}

}
