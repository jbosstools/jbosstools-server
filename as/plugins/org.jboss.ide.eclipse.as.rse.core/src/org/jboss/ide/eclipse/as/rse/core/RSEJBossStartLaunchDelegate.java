/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
 ******************************************************************************/
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.IJBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.DelegatingStartLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.core.util.ThreadUtils;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;

public class RSEJBossStartLaunchDelegate extends AbstractRSELaunchDelegate {
	public void actualLaunch(
			DelegatingStartLaunchConfiguration launchConfig,
			ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		DelegatingServerBehavior beh = JBossServerBehaviorUtils.getServerBehavior(configuration);
		beh.setServerStarting();
		if (LaunchCommandPreferences.isIgnoreLaunchCommand(beh.getServer())) {
			beh.setServerStarted();
			return;
		}
		String command = RSELaunchConfigProperties.getStartupCommand(configuration);
		executeRemoteCommand(command, beh);
		launchPingThread(beh);
	}
	
	private void launchPingThread(DeployableServerBehavior beh) {
		// TODO do it properly here
		ThreadUtils.sleepFor(30000);
		beh.setServerStarted();
	}

	/**
	 * 
	 * @deprecated
	 * This was called from {@link RSEBehaviourDelegate#stop(boolean)
	 * WTP keeps launching in launch configs and stopping in
	 * the server behavior. We should not change that and offer
	 * stopping-functionalities in launch delegates.
	 * 
	 * @param behaviour
	 */
	@Deprecated
	public static void launchStopServerCommand(DelegatingServerBehavior behaviour) {
		if (LaunchCommandPreferences.isIgnoreLaunchCommand(behaviour.getServer())) {
			behaviour.setServerStopping();
			behaviour.setServerStopped();
			return;
		}
		ILaunchConfiguration config = null;
		String command2 = "";
		try {
			config = behaviour.getServer().getLaunchConfiguration(false, new NullProgressMonitor());
			/*	
			 * 		ATTENTION: this was commented since #getDefaultStopCommand is not static any more
			 * 		String defaultCmd = getDefaultStopCommand(behaviour.getServer(), true);
			 */

			/*
			 * This was added to make it compile
			 */
			String defaultCmd = ""; 
			
			
			command2 = config == null ? defaultCmd :
					RSELaunchConfigProperties.getShutdownCommand(config, defaultCmd);
			behaviour.setServerStopping();
			ServerShellModel model = RSEHostShellModel.getInstance().getModel(behaviour.getServer());
			model.executeRemoteCommand("/", command2, new String[] {}, new NullProgressMonitor(), 10000, true);
			if (model.getStartupShell() != null && model.getStartupShell().isActive())
				model.getStartupShell().writeToShell("exit");
			behaviour.setServerStopped();
		} catch (CoreException ce) {
			behaviour.setServerStarted();
			ServerLogger.getDefault().log(behaviour.getServer(), ce.getStatus());
		}
	}

	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		// ping if up
		final DelegatingServerBehavior beh = JBossServerBehaviorUtils.getServerBehavior(configuration);
		// TODO: use configured polelr
		boolean started = WebPortPoller.onePing(beh.getServer());
		if (started) {
			beh.setServerStarting();
			beh.setServerStarted();
			return false;
		}
		return true;
	}

	public void preLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
	}

	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
	}

	public void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy, IServer server)
			throws CoreException {
		new RSELaunchConfigurator(getDefaultLaunchCommand(workingCopy), getDefaultStopCommand(server))
				.configure(workingCopy);
	}

	private  String getDefaultStopCommand(IServer server) throws CoreException {
		String rseHome = RSEUtils.getRSEHomeDir(server, false);

		String stop = new Path(rseHome)
				.append(IJBossRuntimeResourceConstants.BIN)
				.append(IJBossRuntimeResourceConstants.SHUTDOWN_SH).toString()
				+ IJBossRuntimeConstants.SPACE;

		// Pull args from single utility method
		// stop += StopLaunchConfiguration.getDefaultArgs(jbs);
		IJBossBehaviourDelegate delegate = ServerUtil.checkedGetBehaviorDelegate(server);
		stop += delegate.getDefaultStopArguments();
		return stop;
	}

	@Deprecated
	public static IServer findServer(ILaunchConfiguration config) throws CoreException {
		String serverId = config.getAttribute("server-id", (String) null);
		JBossServer jbs = ServerConverter.findJBossServer(serverId);
		if (jbs == null) {
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(Messages.ServerNotFound, serverId)));
		}
		return jbs.getServer();
	}

	private String getDefaultLaunchCommand(ILaunchConfiguration config) throws CoreException {
		String serverId = JBossLaunchConfigProperties.getServerId(config);
		JBossServer jbossServer = ServerConverter.checkedFindJBossServer(serverId);
		String rseHome = jbossServer.getServer().getAttribute(RSEUtils.RSE_SERVER_HOME_DIR, "");
		// initialize startup command to something reasonable
		String currentArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		String currentVMArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$

		currentVMArgs = ArgsUtil.setArg(currentVMArgs, null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.ENDORSED_DIRS,
				new Path(rseHome).append(
						IJBossRuntimeResourceConstants.LIB).append(
						IJBossRuntimeResourceConstants.ENDORSED).toOSString(), true);

		String libPath = new Path(rseHome).append(IJBossRuntimeResourceConstants.BIN)
				.append(IJBossRuntimeResourceConstants.NATIVE).toOSString();
		currentVMArgs = ArgsUtil.setArg(currentVMArgs, null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JAVA_LIB_PATH,
				libPath, true);

		String cmd = "java " + currentVMArgs + " -classpath " +
				new Path(rseHome).append(IJBossRuntimeResourceConstants.BIN).append(
						IJBossRuntimeResourceConstants.START_JAR).toString() + IJBossRuntimeConstants.SPACE +
				IJBossRuntimeConstants.START_MAIN_TYPE + IJBossRuntimeConstants.SPACE + currentArgs + "&";
		return cmd;
	}
}
