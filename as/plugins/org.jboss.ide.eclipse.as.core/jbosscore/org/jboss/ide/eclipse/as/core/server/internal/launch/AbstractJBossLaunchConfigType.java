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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jst.server.core.ServerProfilerDelegate;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractLocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

/**
 * @author Rob Stryker
 */
public abstract class AbstractJBossLaunchConfigType extends AbstractJavaLaunchConfigurationDelegate {
	public static final String SERVER_ID = "server-id"; //$NON-NLS-1$

	// we have no need to do anything in pre-launch check
	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		return true;
	}

	public void preLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// override me
	}

	public void postLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// override me
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		preLaunch(configuration, mode, launch, monitor);
		actualLaunch(configuration, mode, launch, monitor);
		postLaunch(configuration, mode, launch, monitor);
	}

	protected void actualLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// And off we go!
		IVMInstall vm = verifyVMInstall(configuration);
		IVMRunner runner = vm.getVMRunner(mode);

		if (runner == null && ILaunchManager.PROFILE_MODE.equals(mode)) {
			runner = vm.getVMRunner(ILaunchManager.RUN_MODE);
		}
		if (runner == null) {
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0,
					Messages.runModeNotSupported, null));
		}

		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null)
			workingDirName = workingDir.getAbsolutePath();

		// Program & VM args
		String pgmArgs = getProgramArguments(configuration);
		String vmArgs = getVMArguments(configuration);
		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);

		// VM-specific attributes
		Map<?, ?> vmAttributesMap = getVMSpecificAttributesMap(configuration);

		// Classpath
		String[] classpath = getClasspath(configuration);

		// Environment
		String[] environment = getEnvironment(configuration);

		// Create VM config
		String mainType = getMainTypeName(configuration);
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainType, classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);
		runConfig.setEnvironment(environment);

		// Bootpath
		String[] bootpath = getBootpath(configuration);
		if (bootpath != null && bootpath.length > 0)
			runConfig.setBootClassPath(bootpath);

		setDefaultSourceLocator(launch, configuration);

		if (ILaunchManager.PROFILE_MODE.equals(mode)) {
			try {
				ServerProfilerDelegate.configureProfiling(launch, vm, runConfig, monitor);
			} catch (CoreException ce) {
				IServer server = org.eclipse.wst.server.core.ServerUtil.getServer(configuration);
				JBossServerBehavior jbsb = (JBossServerBehavior) server.getAdapter(JBossServerBehavior.class);
				jbsb.stop(true);
				// genericServer.stopImpl();
				throw ce;
			}
		}
		// Launch the configuration
		runner.run(runConfig, launch, monitor);
	}
	
	@Deprecated
	public static JBossServer findJBossServer(String serverId) throws CoreException {
		return ServerConverter.findJBossServer(serverId);
	}
	
	@Deprecated
	public static IJBossServerRuntime findJBossServerRuntime(IServer server) throws CoreException {
		return RuntimeUtils.checkedGetJBossServerRuntime(server);
	}
	
	@Deprecated
	public static void addCPEntry(ArrayList<IRuntimeClasspathEntry> list, JBossServer jbs, String relative) throws CoreException {
		String serverHome = org.jboss.ide.eclipse.as.core.util.ServerUtil.getServerHome(jbs);
		LaunchConfigUtils.addCPEntry(serverHome, relative, list);
	}

	@Deprecated
	public static void addCPEntry(ArrayList<IRuntimeClasspathEntry> list, IPath path) {
		LaunchConfigUtils.addCPEntry(path, list);
	}

	@Deprecated
	public static void addJREEntry(List<IRuntimeClasspathEntry> cp, IVMInstall vmInstall) {
		LaunchConfigUtils.addJREEntry(vmInstall, cp);
	}

	@Deprecated
	public static void addToolsJar(ArrayList<IRuntimeClasspathEntry> cp, IVMInstall vmInstall) {
		LaunchConfigUtils.addToolsJar(vmInstall, cp);
	}

	@Deprecated
	public static ArrayList<String> convertClasspath(List<IRuntimeClasspathEntry> cp) {
		return (ArrayList) LaunchConfigUtils.toStrings(cp);
	}

	@Deprecated
	protected static void addDirectory(String serverHome, List<IRuntimeClasspathEntry> classpath,
			String dirName) {
		LaunchConfigUtils.addDirectory(serverHome, classpath, dirName); 
	}

	@Deprecated
	public static String getServerHome(JBossServer jbs) throws CoreException {
		return ServerUtil.getServerHome(jbs);
	}
	
	public IVMInstall getVMInstall(ILaunchConfiguration configuration) throws CoreException {
		String serverId = configuration.getAttribute(SERVER_ID, (String) null);
		JBossServer jbs = findJBossServer(serverId);
		AbstractLocalJBossServerRuntime rt = (AbstractLocalJBossServerRuntime)
				jbs.getServer().getRuntime()
						.loadAdapter(AbstractLocalJBossServerRuntime.class, new NullProgressMonitor());
		return rt.getVM();
	}

}
