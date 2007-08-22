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
package org.jboss.ide.eclipse.as.core.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ServerType;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;

public class JBossServerLaunchConfiguration extends AbstractJavaLaunchConfigurationDelegate {

	public static final String START = "_START_";
	public static final String STOP = "_STOP_";
	public static final String TWIDDLE = "_TWIDDLE_";
	
	public static final String CURRENT_ACTION = "org.jboss.ide.eclipse.as.core.server.currentAction";
	public static final String DEFAULT_SETTINGS = "org.jboss.ide.eclipse.as.core.server.defaultsSet";
	
	
	protected static final char[] INVALID_CHARS = new char[] {'\\', '/', ':', '*', '?', '"', '<', '>', '|', '\0', '@', '&'};
	public static final String PRGM_ARGS_START_SUFFIX = "org.jboss.ide.eclipse.as.core.server.start_suffix"; 
	public static final String PRGM_ARGS_STOP_SUFFIX = "org.jboss.ide.eclipse.as.core.server.stop_suffix";
	public static final String PRGM_ARGS_TWIDDLE_SUFFIX = "org.jboss.ide.eclipse.as.core.server.twiddle_suffix"; 
	
	
	
	public static JBossServerBehavior getServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		JBossServerBehavior jbossServerBehavior = (JBossServerBehavior) server.getAdapter(JBossServerBehavior.class);
		return jbossServerBehavior;
	}
	
	public static JBossServer getJBossServer(ILaunchConfiguration configuration) throws CoreException {
		// Get access to some important structures
		return getJBossServer(ServerUtil.getServer(configuration));
	}

	public static JBossServer getJBossServer(IServer server) {
		if( server == null ) return null;
		JBossServer jbServer = (JBossServer)server.getAdapter(JBossServer.class);
		if (jbServer == null) {
			jbServer = (JBossServer) server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		}
		return jbServer;
	}
	
	
	public static ILaunchConfigurationWorkingCopy setupLaunchConfiguration(IServer server, String action) throws CoreException {
		ILaunchConfigurationWorkingCopy config = createLaunchConfiguration(server);
		setupLaunchConfiguration(config, server, action);
		return config;
	}

	public static void ensureDefaultsSet(ILaunchConfigurationWorkingCopy workingCopy, IServer server ) {
		if( !defaultsBeenSet(workingCopy, server) ) {
			try { 
				getJBossServer(server).getLaunchDefaults().fillDefaults(workingCopy);
				workingCopy.setAttribute(DEFAULT_SETTINGS, true);
			} catch( CoreException ce ) {}
		}
	}
	
	public static void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, 
						IServer server, String action) throws CoreException {

		ensureDefaultsSet(workingCopy, server);

		// Now that defaults have all been set, set it up for the action (start, stop, twiddle etc)
		String argsKey = IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS;
		String vmArgsKey = IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS;
		workingCopy.setAttribute(CURRENT_ACTION, action);	

		String suffix;
		if( START.equals(action)) {
			suffix = PRGM_ARGS_START_SUFFIX;
		} else if( STOP.equals(action)) {
			suffix = PRGM_ARGS_STOP_SUFFIX;
		} else if( TWIDDLE.equals(action)) {
			suffix = PRGM_ARGS_TWIDDLE_SUFFIX;
		} else {
			throw new CoreException(null);
		}
		
		try {
			String pgArgs = workingCopy.getAttribute(argsKey + suffix, (String)null); 
			workingCopy.setAttribute(argsKey,pgArgs);
			

			String vmArgs = workingCopy.getAttribute(vmArgsKey + suffix, (String)null);
			workingCopy.setAttribute(vmArgsKey, vmArgs);
			
			String main = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME + suffix, (String)null);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, main);

			String workingDir = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY + suffix, (String)null);
			workingCopy.setAttribute(
	                IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workingDir);

			
			List classpath = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH + suffix, new ArrayList());
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);

			if( STOP.equals(action) || TWIDDLE.equals(action))
				addCredentials(workingCopy, getJBossServer(workingCopy));

		} catch( Exception e ) {
			e.printStackTrace();
		}

	}
		

	protected static void addCredentials(ILaunchConfigurationWorkingCopy configuration, JBossServer server) {
		try {
			String argsKey = IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS;
			String args = configuration.getAttribute(argsKey, (String)null);
			String user = ArgsUtil.getValue(args, "-u", "--user");
			String pass = ArgsUtil.getValue(args, "-p", "--password");
			if( user == null )
				args = args + " -u " + server.getUsername();
			if( pass == null ) 
				args = args + " -p " + server.getPassword();
			
			configuration.setAttribute(argsKey, args);
		} catch( CoreException ce ) {
			ce.printStackTrace();
		}
	}


	// Have the defaults been set for this launch config yet? ever?
	public static boolean defaultsBeenSet(ILaunchConfiguration workingCopy, IServer server) {
		try {
			return workingCopy.getAttribute(DEFAULT_SETTINGS, false);
		} catch( CoreException ce ) {
		}
		return false;
	}
	
	
	/**
	 * Will create a launch configuration for the server 
	 * if one does not already exist.
	 */
	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IServer server) throws CoreException {
		ILaunchConfigurationType launchConfigType = 
			((ServerType) server.getServerType()).getLaunchConfigurationType();
		if (launchConfigType == null)
			return null;
		
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfiguration[] launchConfigs = null;
		try {
			launchConfigs = launchManager.getLaunchConfigurations(launchConfigType);
		} catch (CoreException e) {
			// ignore
		}
		
		if (launchConfigs != null) {
			int size = launchConfigs.length;
			for (int i = 0; i < size; i++) {
				try {
					String serverId = launchConfigs[i].getAttribute("server-id", (String) null);
					if (server.getId().equals(serverId)) {
						ILaunchConfigurationWorkingCopy wc = launchConfigs[i].getWorkingCopy();
						return wc;
					}
				} catch (CoreException e) {
				}
			}
		}
		
		// create a new launch configuration
		String launchName = getValidLaunchConfigurationName(server.getName());
		launchName = launchManager.generateUniqueLaunchConfigurationNameFrom(launchName); 
		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, launchName);
		wc.setAttribute("server-id", server.getId());
		return wc;
	}

	protected static String getValidLaunchConfigurationName(String s) {
		if (s == null || s.length() == 0)
			return "1";
		int size = INVALID_CHARS.length;
		for (int i = 0; i < size; i++) {
			s = s.replace(INVALID_CHARS[i], '_');
		}
		return s;
	}
	

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		try {
			String action = configuration.getAttribute(CURRENT_ACTION, START);	
	
			IProcess[] processes = new IProcess[0];
			JBossServerBehavior jbossServerBehavior = getServerBehavior(configuration);
			String serverID = jbossServerBehavior.getServer().getId();
			if( action.equals(START)) {
				processes = launchConfiguration(configuration, launch, monitor, mode);
				jbossServerBehavior.setProcess(processes[0]);
				jbossServerBehavior.serverStarting();
			} else if( action.equals(STOP)) {
				processes = launchConfiguration(configuration, launch, monitor, mode);
				jbossServerBehavior.serverStopping();
			} else if( action.equals(TWIDDLE)) {
				processes = launchConfiguration(configuration, launch, monitor, mode);
			}
		} catch( Exception e ) {
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IStatus.ERROR, "Error launching twiddle", e));
		}
	}
	
	public IProcess[] launchConfiguration(ILaunchConfiguration configuration, ILaunch launch,
			IProgressMonitor monitor, String mode) throws CoreException {

		// And off we go!
		IVMInstall vm = verifyVMInstall(configuration);
		IVMRunner runner = vm.getVMRunner(mode);
		
		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null)
			workingDirName = workingDir.getAbsolutePath();
		
		// Program & VM args
		String pgmArgs = getProgramArguments(configuration);
		String vmArgs = getVMArguments(configuration);
		
		
		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
		
		// VM-specific attributes
		Map vmAttributesMap = getVMSpecificAttributesMap(configuration);
		
		// Classpath
		String[] classpath = getClasspath(configuration);
		
		// Create VM config
		String mainType = getMainTypeName(configuration);
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainType, classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);

		// Bootpath
		String[] bootpath = getBootpath(configuration);
		if (bootpath != null && bootpath.length > 0)
			runConfig.setBootClassPath(bootpath);
		
		setDefaultSourceLocator(launch, configuration);
		
		// Launch the configuration
		runner.run(runConfig, launch, monitor);
		
		return launch.getProcesses();
	}

}
