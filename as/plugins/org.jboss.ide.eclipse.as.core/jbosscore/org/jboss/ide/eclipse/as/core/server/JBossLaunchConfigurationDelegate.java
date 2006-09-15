/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
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
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerType;
import org.eclipse.wst.server.core.internal.Trace;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.server.runtime.AbstractServerRuntimeDelegate;
import org.jboss.ide.eclipse.as.core.server.runtime.IJBossServerRuntimeDelegate;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class JBossLaunchConfigurationDelegate extends
	AbstractJavaLaunchConfigurationDelegate {

	protected static final char[] INVALID_CHARS = new char[] {'\\', '/', ':', '*', '?', '"', '<', '>', '|', '\0', '@', '&'};
	public static final String PRGM_ARGS_START_SUFFIX = "_SERVER_START_"; 
	public static final String PRGM_ARGS_STOP_SUFFIX = "_SERVER_STOP_";
	public static final String PRGM_ARGS_TWIDDLE_SUFFIX = "_SERVER_TWIDDLE_"; 

	public static ILaunchConfigurationWorkingCopy setupLaunchConfiguration(JBossServer server, String action) throws CoreException {
		ILaunchConfigurationWorkingCopy config = createLaunchConfiguration(server);
		setupLaunchConfiguration(config, server, action);
		return config;
	}
		
	
	/**
	 * Will be in charge of setting defaults for a launch configuration that might 
	 * not have been filled with all values. 
	 * 
	 * @param workingCopy
	 * @param server
	 * @throws CoreException
	 */
	public static void setDefaults( ILaunchConfigurationWorkingCopy workingCopy,
										JBossServer server) throws CoreException {

		JBossServerRuntime runtime = server.getJBossRuntime();
		AbstractServerRuntimeDelegate runtimeDelegate = runtime.getVersionDelegate();
		String argsKey = IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS;
		String vmArgsKey = IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS;
		
		// START items
		String pgArgs = workingCopy.getAttribute(argsKey + PRGM_ARGS_START_SUFFIX, (String)null); 
		if( pgArgs == null ) {
			pgArgs =  runtime.getVersionDelegate().getStartArgs(server);
			workingCopy.setAttribute(argsKey + PRGM_ARGS_START_SUFFIX, pgArgs);
		}

		String vmArgs = workingCopy.getAttribute(vmArgsKey + PRGM_ARGS_START_SUFFIX, (String)null);
		if( vmArgs == null ) {
			vmArgs = runtime.getVersionDelegate().getVMArgs(server);
			workingCopy.setAttribute(vmArgsKey + PRGM_ARGS_START_SUFFIX, vmArgs);
		}

		String stopArgs = workingCopy.getAttribute(argsKey + PRGM_ARGS_STOP_SUFFIX, (String)null); 
		if( stopArgs == null ) {
			stopArgs =  runtime.getVersionDelegate().getStopArgs(server);
			workingCopy.setAttribute(argsKey + PRGM_ARGS_STOP_SUFFIX, stopArgs);
		}

		String stopVMArgs = workingCopy.getAttribute(vmArgsKey + PRGM_ARGS_STOP_SUFFIX, (String)null);
		if( stopVMArgs == null ) {
			stopVMArgs = runtime.getVersionDelegate().getVMArgs(server);
			workingCopy.setAttribute(vmArgsKey + PRGM_ARGS_STOP_SUFFIX, stopVMArgs);
		}
		
		
		String twiddleVMArgs = workingCopy.getAttribute(vmArgsKey + PRGM_ARGS_TWIDDLE_SUFFIX, (String)null);
		if( twiddleVMArgs == null ) {
			twiddleVMArgs = "";
			workingCopy.setAttribute(vmArgsKey + PRGM_ARGS_TWIDDLE_SUFFIX, twiddleVMArgs);
		}
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgs);
		
		String twiddleArgs = workingCopy.getAttribute(argsKey + PRGM_ARGS_TWIDDLE_SUFFIX, (String)null);
		if( twiddleArgs == null ) {
	 		int jndiPort = server.getDescriptorModel().getJNDIPort();
			String host = server.getServer().getHost();
			twiddleArgs = "-s " + host + ":" + jndiPort +  " -a jmx/rmi/RMIAdaptor ";
			workingCopy.setAttribute(argsKey + PRGM_ARGS_TWIDDLE_SUFFIX, twiddleArgs);
		}
	}
	
	
	public static void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy, JBossServer server, String action) throws CoreException {

		JBossServerRuntime runtime = server.getJBossRuntime();
		AbstractServerRuntimeDelegate runtimeDelegate = runtime.getVersionDelegate();
		
		ServerAttributeHelper helper = server.getAttributeHelper();
		
		String argsKey = IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS;
		String vmArgsKey = IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS;
		workingCopy.setAttribute(JBossServerBehavior.ATTR_ACTION, action);	
		
		// ensure defaults are set, at the very least
		setDefaults(workingCopy, server);

		if( JBossServerBehavior.ACTION_STARTING.equals(action)) {
			String suffix = PRGM_ARGS_START_SUFFIX;
			try {
				String pgArgs = workingCopy.getAttribute(argsKey + suffix, (String)null); 
				workingCopy.setAttribute(argsKey,pgArgs);
				

				String vmArgs = workingCopy.getAttribute(vmArgsKey + suffix, (String)null);
				workingCopy.setAttribute(vmArgsKey, vmArgs);
				
				workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, 
							runtime.getVersionDelegate().getStartMainType());
		        workingCopy.setAttribute(
		                IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
		                helper.getServerHome() + Path.SEPARATOR + "bin");

				
		        boolean defaultCPVal = workingCopy.getAttribute(JBossServerBehavior.LAUNCH_CONFIG_DEFAULT_CLASSPATH, true);
		        if( defaultCPVal ) {
					List classpath = runtimeDelegate.getRuntimeClasspath(server, IJBossServerRuntimeDelegate.ACTION_START);
					workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
					workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
		        }

			} catch( Exception e ) {
				e.printStackTrace();
			}

		} else if( JBossServerBehavior.ACTION_STOPPING.equals(action)) {
			String suffix = PRGM_ARGS_STOP_SUFFIX;
			
			String args = workingCopy.getAttribute(argsKey + suffix, (String)null);
			workingCopy.setAttribute(argsKey,args);
			
			String vmArgs = workingCopy.getAttribute(vmArgsKey + suffix, (String)null);
			workingCopy.setAttribute(vmArgsKey,vmArgs);
			
			List cp = server.getJBossRuntime().getVersionDelegate().getRuntimeClasspath(server, IJBossServerRuntimeDelegate.ACTION_SHUTDOWN);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, cp);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, 
					helper.getServerHome());
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, runtimeDelegate.getStopMainType());
			
		} else if( JBossServerBehavior.ACTION_TWIDDLE.equals(action)) {
			String suffix = PRGM_ARGS_TWIDDLE_SUFFIX;

			String vmArgs = workingCopy.getAttribute(vmArgsKey + suffix, (String)null);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgs);
			
			String args = workingCopy.getAttribute(argsKey + suffix, (String)null);
			workingCopy.setAttribute(argsKey, args);

			
			List classpath = runtimeDelegate.getRuntimeClasspath(server, IJBossServerRuntimeDelegate.ACTION_TWIDDLE);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, runtimeDelegate.getTwiddleMainType());
			workingCopy.setAttribute(Server.ATTR_SERVER_ID, server.getServer().getId());
			workingCopy.setAttribute(JBossServerBehavior.ATTR_ACTION, JBossServerBehavior.ACTION_TWIDDLE);
			
	        workingCopy.setAttribute(
	                IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
	                server.getAttributeHelper().getServerHome() + Path.SEPARATOR + "bin");

		}


	
	}
	
	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(JBossServer server) throws CoreException {
		ILaunchConfigurationType launchConfigType = 
			((ServerType) server.getServer().getServerType()).getLaunchConfigurationType();
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
					if (server.getServer().getId().equals(serverId)) {
						ILaunchConfigurationWorkingCopy wc = launchConfigs[i].getWorkingCopy();
						return wc;
					}
				} catch (CoreException e) {
					Trace.trace(Trace.SEVERE, "Error configuring launch", e);
				}
			}
		}
		
		// create a new launch configuration
		String launchName = getValidLaunchConfigurationName(server.getServer().getName());
		launchName = launchManager.generateUniqueLaunchConfigurationNameFrom(launchName); 
		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, launchName);
		wc.setAttribute("server-id", server.getServer().getId());
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
	

	
	
	
	//private IProcess[] processes;
	
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		String action = configuration.getAttribute(JBossServerBehavior.ATTR_ACTION, 
				JBossServerBehavior.ACTION_STARTING);	
		
		/* temp */
		String programArgs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
		String vmArgs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
				
		if( action.equals(JBossServerBehavior.ACTION_STARTING)) {
			launchServerStart(configuration, mode, launch,  monitor);
		} else if( action.equals(JBossServerBehavior.ACTION_STOPPING)) {
			launchServerStop(configuration, mode, launch,  monitor);
		} else if( action.equals(JBossServerBehavior.ACTION_TWIDDLE)) {
			IProcess[] processes;
			processes = launchConfiguration(configuration, launch, monitor, mode);
			ServerProcessModelEntity model = getJBossServer(configuration).getProcessModel();
			model.add(processes, ServerProcessModel.TWIDDLE_PROCESSES, configuration);
		}

	}
	
	
	
	private void launchServerStop(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProcess[] processes;
		JBossServerBehavior jbossServerBehavior = getServerBehavior(configuration);
		jbossServerBehavior.serverStopping();
		processes = launchConfiguration(configuration, launch, monitor, mode);
		// Add a process listener
		ServerProcessModelEntity model = getJBossServer(configuration).getProcessModel();
		model.add(processes, ServerProcessModel.STOP_PROCESSES, configuration);
	}



	private void launchServerStart(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProcess[] processes;
		JBossServerBehavior jbossServerBehavior = getServerBehavior(configuration);
		jbossServerBehavior.serverStarting();
		processes = launchConfiguration(configuration, launch, monitor, mode);
		// Add a process listener
		ServerProcessModelEntity model = getJBossServer(configuration).getProcessModel();
		//model.clearAll();
		model.add(processes, ServerProcessModel.START_PROCESSES, configuration);
	}



	private JBossServerBehavior getServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		JBossServerBehavior jbossServerBehavior = (JBossServerBehavior) server.getAdapter(JBossServerBehavior.class);
		return jbossServerBehavior;

	}
	
	private JBossServer getJBossServer(ILaunchConfiguration configuration) throws CoreException {
		// Get access to some important structures
		IServer server = ServerUtil.getServer(configuration);
		if( server == null ) return null;
		JBossServer jbServer = (JBossServer)server.getAdapter(JBossServer.class);
		if (jbServer == null) {
			jbServer = (JBossServer) server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		}
		return jbServer;
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
