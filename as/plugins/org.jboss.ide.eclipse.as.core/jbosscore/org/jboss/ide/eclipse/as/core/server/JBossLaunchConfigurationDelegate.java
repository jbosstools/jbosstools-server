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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;

public class JBossLaunchConfigurationDelegate extends
	AbstractJavaLaunchConfigurationDelegate {
	
	private IProcess[] processes;

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		String action = configuration.getAttribute(JBossServerBehavior.ATTR_ACTION, 
				JBossServerBehavior.ACTION_STARTING);	
		
		if( action.equals(JBossServerBehavior.ACTION_STARTING)) {
			launchServerStart(configuration, mode, launch,  monitor);
		} else if( action.equals(JBossServerBehavior.ACTION_STOPPING)) {
			launchServerStop(configuration, mode, launch,  monitor);
		} else if( action.equals(JBossServerBehavior.ACTION_TWIDDLE)) {
			processes = launchConfiguration(configuration, launch, monitor, mode);
			ServerProcessModelEntity model = getJBossServer(configuration).getProcessModel();
			model.add(processes, ServerProcessModel.TWIDDLE_PROCESSES, configuration);
		}

	}
	
	
	
	private void launchServerStop(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		JBossServerBehavior jbossServerBehavior = getServerBehavior(configuration);
		jbossServerBehavior.serverStopping();
		processes = launchConfiguration(configuration, launch, monitor, mode);
		// Add a process listener
		addDebugEventListener();
		ServerProcessModelEntity model = getJBossServer(configuration).getProcessModel();
		model.add(processes, ServerProcessModel.STOP_PROCESSES, configuration);
	}



	private void launchServerStart(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		JBossServerBehavior jbossServerBehavior = getServerBehavior(configuration);
		jbossServerBehavior.serverStarting();
		processes = launchConfiguration(configuration, launch, monitor, mode);
		// Add a process listener
		addDebugEventListener();
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
	
	
	public void addDebugEventListener() {
		IDebugEventSetListener processListener = new IDebugEventSetListener() {
			public void handleDebugEvents(DebugEvent[] events) {
				IProcess process = processes[0];
				if (events != null) {
					int size = events.length;
					for (int i = 0; i < size; i++) {
						System.out.println("Debug event " + i + " is " + events[i].getKind());
						if (process.equals(events[i].getSource()) && events[i].getKind() == DebugEvent.TERMINATE) {
							DebugPlugin.getDefault().removeDebugEventListener(this);
							System.out.println("Debug Event: Stopping");
							processes = null;
							//notifyServerStopped();
						}
					}
				}
			}
		};

	}
	
}
