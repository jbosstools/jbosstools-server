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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jst.server.core.ServerProfilerDelegate;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.UserPrompter;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.JavaUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;

/**
 * This class clones much behavior from the upstream jdt class for launching standard java configs.
 * The cloning is necessary because the upstream class does not easily allow
 * the injection of wtp code related to server profiling. 
 * 
 * We also do things like check if the server is already up, 
 * verify the server has a proper structure, or handle the scenario
 * where a server is already started (show a dialog).  
 * 
 * @author Rob Stryker
 */
public abstract class AbstractJBossStartLaunchConfiguration extends AbstractJavaLaunchConfigurationDelegate {
	
	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		IStatus s = server.canStart(mode);
		Trace.trace(Trace.STRING_FINEST, "Ensuring Server can start: " + s.getMessage()); //$NON-NLS-1$
		if (!s.isOK())
			throw new CoreException(s);
		
		IControllableServerBehavior jbsBehavior = JBossServerBehaviorUtils.getControllableBehavior(server);
		if (LaunchCommandPreferences.isIgnoreLaunchCommand(configuration)) {
			Trace.trace(Trace.STRING_FINEST, "Server is marked as ignore Launch. Marking as started."); //$NON-NLS-1$
			((ControllableServerBehavior)jbsBehavior).setServerStarted();
			return false;
		}
		
		Trace.trace(Trace.STRING_FINEST, "Verifying server structure"); //$NON-NLS-1$
		JBossExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getJBossExtendedProperties(server);
		IStatus status = props.verifyServerStructure();
		if( !status.isOK() ) {
			((ControllableServerBehavior)jbsBehavior).setServerStopped();
			throw new CoreException(status);
		}
		
		Trace.trace(Trace.STRING_FINEST, "Verifying jdk is available if server requires jdk"); //$NON-NLS-1$
		boolean requiresJDK = props.requiresJDK();
		if( requiresJDK) {
			IRuntime rt = server.getRuntime();
			IJBossServerRuntime rt2 = RuntimeUtils.getJBossServerRuntime(rt);
			IVMInstall vm = rt2.getVM();
			
			if( !JavaUtils.isJDK(vm)) {
				// JBIDE-14568 do not BLOCK launch, but log error
				Trace.trace(Trace.STRING_FINEST, "The VM to launch server '" +  //$NON-NLS-1$
						server.getName() + "' does not appear to be a JDK: " + vm.getInstallLocation().getAbsolutePath()); //$NON-NLS-1$
				IStatus stat = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
						NLS.bind(Messages.launch_requiresJDK, 
								server.getName(),
								vm.getInstallLocation().getAbsolutePath()));
				ServerLogger.getDefault().log(server, stat);
			}
		}

		
		Trace.trace(Trace.STRING_FINEST, "Checking if similar server is already up on the same ports."); //$NON-NLS-1$
		IStatus startedStatus = isServerStarted(server);
		boolean started = startedStatus.isOK();
		if (started) {
			Trace.trace(Trace.STRING_FINEST, "A server is already started. Now handling the already started scenario."); //$NON-NLS-1$
			return handleAlreadyStartedScenario(server, startedStatus);
		}

		Trace.trace(Trace.STRING_FINEST, "A full launch will now proceed."); //$NON-NLS-1$
		return true;
	}

	/*
	 * A solution needs to be found here. 
	 * Should ideally use the poller that the server says is its poller,
	 * but some pollers such as timeout poller 
	 */
	protected IStatus isServerStarted(IServer server) {
		return PollThreadUtils.isServerStarted(server);
	}
	
	protected boolean handleAlreadyStartedScenario(	IServer server, IStatus startedStatus) {
		Object ret = JBossServerCorePlugin.getInstance().getPrompter().promptUser(UserPrompter.EVENT_CODE_SERVER_ALREADY_STARTED, server, startedStatus);
		if( ret instanceof Integer ) {
			int handlerResult = ((Integer)ret).intValue();
			if( handlerResult == UserPrompter.RETURN_CODE_SAS_CONTINUE_STARTUP) {
				return true;
			}
			if( handlerResult == UserPrompter.RETURN_CODE_SAS_CANCEL) {
				return false;
			}
		}
		Trace.trace(Trace.STRING_FINEST, "Either no handler available, or user selected continue. The server will be set to started automatically. "); //$NON-NLS-1$
		// force server to started mode
		IControllableServerBehavior jbsBehavior = JBossServerBehaviorUtils.getControllableBehavior(server);
		((ControllableServerBehavior)jbsBehavior).setServerStarted();
		return false;
	}
	
	@Deprecated  // Renamed
	public void preLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		beforeVMRunner(configuration, mode, launch, monitor);
	}
	
	protected void beforeVMRunner(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// override me
	}

	

	@Deprecated  // Renamed
	public void postLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		afterVMRunner(configuration, mode, launch, monitor);
	}
	protected void afterVMRunner(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// override me
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if( LaunchCommandPreferences.isIgnoreLaunchCommand(configuration)) {
			return;
		}
		preLaunch(configuration, mode, launch, monitor);
		actualLaunch(configuration, mode, launch, monitor);
		postLaunch(configuration, mode, launch, monitor);
	}

	protected void actualLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		fireVMRunner(configuration, mode, launch, monitor);
	}
	
	protected void fireVMRunner(ILaunchConfiguration configuration,
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
		Map<String, Object> vmAttributesMap = getVMSpecificAttributesMap(configuration);

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
				ServerBehaviourDelegate jbsb = (ServerBehaviourDelegate) server.getAdapter(ServerBehaviourDelegate.class);
				jbsb.stop(true);
				throw ce;
			}
		}
		// Launch the configuration
		runner.run(runConfig, launch, monitor);
	}
}
