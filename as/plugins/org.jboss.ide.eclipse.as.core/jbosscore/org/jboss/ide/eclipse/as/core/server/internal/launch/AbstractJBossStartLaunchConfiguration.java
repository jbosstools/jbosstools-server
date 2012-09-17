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
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IServerAlreadyStartedHandler;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;

/**
 * @author Rob Stryker
 */
public abstract class AbstractJBossStartLaunchConfiguration extends AbstractJavaLaunchConfigurationDelegate {

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		IDelegatingServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
		IStatus s = jbsBehavior.canStart(mode);

		Trace.trace(Trace.STRING_FINEST, "Ensuring Server can start: " + s.getMessage()); //$NON-NLS-1$
		if (!s.isOK())
			throw new CoreException(jbsBehavior.canStart(mode));
		if (LaunchCommandPreferences.isIgnoreLaunchCommand(jbsBehavior.getServer())) {
			Trace.trace(Trace.STRING_FINEST, "Server is marked as ignore Launch. Marking as started."); //$NON-NLS-1$
			((DelegatingServerBehavior)jbsBehavior).setServerStarting();
			((DelegatingServerBehavior)jbsBehavior).setServerStarted();
			return false;
		}
		
		JBossExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getJBossExtendedProperties(jbsBehavior.getServer());
		IStatus status = props.verifyServerStructure();
		if( !status.isOK() ) {
			((DelegatingServerBehavior)jbsBehavior).setServerStopped();
			throw new CoreException(status);
		}
		
		Trace.trace(Trace.STRING_FINEST, "Checking if similar server is already up on the same ports."); //$NON-NLS-1$
		IStatus startedStatus = isServerStarted(jbsBehavior);
		boolean started = startedStatus.isOK();
		if (started) {
			Trace.trace(Trace.STRING_FINEST, "A server is already started. Now handling the already started scenario."); //$NON-NLS-1$
			return handleAlreadyStartedScenario(jbsBehavior, startedStatus);
		}

		Trace.trace(Trace.STRING_FINEST, "A full launch will now proceed."); //$NON-NLS-1$
		return true;
	}

	/*
	 * A solution needs to be found here. 
	 * Should ideally use the poller that the server says is its poller,
	 * but some pollers such as timeout poller 
	 */
	protected IStatus isServerStarted(IDelegatingServerBehavior jbsBehavior) {
		return PollThreadUtils.isServerStarted(jbsBehavior);
	}
	
	protected boolean handleAlreadyStartedScenario(	IDelegatingServerBehavior jbsBehavior, IStatus startedStatus) {
		IServerAlreadyStartedHandler handler = ExtensionManager.getDefault().getAlreadyStartedHandler(jbsBehavior.getServer());
		if( handler != null ) {
			int handlerResult = handler.promptForBehaviour(jbsBehavior.getServer(), startedStatus);
			if( handlerResult == IServerAlreadyStartedHandler.CONTINUE_STARTUP) {
				return true;
			}
			if( handlerResult == IServerAlreadyStartedHandler.CANCEL) {
				return false;
			}
		}
		Trace.trace(Trace.STRING_FINEST, "There is no handler available to prompt the user. The server will be set to started automatically. "); //$NON-NLS-1$
		// force server to started mode
		((DelegatingServerBehavior)jbsBehavior).setServerStarted();
		return false;
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
		IServer server = LaunchConfigUtils.checkedGetServer(configuration);
		if( LaunchCommandPreferences.isIgnoreLaunchCommand(server)) {
			return;
		}
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
				DelegatingServerBehavior jbsb = (DelegatingServerBehavior) server.getAdapter(DelegatingServerBehavior.class);
				jbsb.stop(true);
				// genericServer.stopImpl();
				throw ce;
			}
		}
		// Launch the configuration
		runner.run(runConfig, launch, monitor);
	}
}
