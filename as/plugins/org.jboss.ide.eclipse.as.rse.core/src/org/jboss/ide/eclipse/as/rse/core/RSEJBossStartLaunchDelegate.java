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
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IJBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class RSEJBossStartLaunchDelegate extends AbstractRSELaunchDelegate {

	@Override
	public void actualLaunch(
			LaunchConfigurationDelegate launchConfig,
			ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		IDelegatingServerBehavior beh = JBossServerBehaviorUtils.getServerBehavior(configuration);
		((DelegatingServerBehavior)beh).setServerStarting();
		String command = RSELaunchConfigProperties.getStartupCommand(configuration);
		executeRemoteCommand(command, beh);
	}
	

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		// ping if up
		final IDelegatingServerBehavior beh = JBossServerBehaviorUtils.getServerBehavior(configuration);
		// TODO: use configured poller
		boolean started = WebPortPoller.onePing(beh.getServer());
		if (started) {
			((DelegatingServerBehavior)beh).setServerStarting();
			((DelegatingServerBehavior)beh).setServerStarted();
			return false;
		}
		return true;
	}

	@Override
	public void preLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
	}

	@Override
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

	private String getDefaultLaunchCommand(ILaunchConfiguration config) throws CoreException {
		String serverId = new JBossLaunchConfigProperties().getServerId(config);
		IJBossServer jbossServer = ServerConverter.checkedFindJBossServer(serverId);
		String rseHome = jbossServer.getServer().getAttribute(RSEUtils.RSE_SERVER_HOME_DIR, "");
		// initialize startup command to something reasonable
		String currentArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		String currentVMArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$

		// Clear old flag which used url
		currentArgs = ArgsUtil.setArg(currentArgs, null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_SERVER_BASE_URL, null);
		
		String connectionName = RSEUtils.getRSEConnectionName(jbossServer.getServer());
		IHost host = RSEUtils.findHost(connectionName);
		String remoteSafe = RSEUtils.pathToRemoteSystem(host, rseHome, IJBossRuntimeResourceConstants.SERVER);

		// Use new flag which forces dir instead (safer)
		currentArgs = ArgsUtil.setArg(currentArgs, null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_SERVER_BASE_DIR,
				remoteSafe);


		currentArgs = ArgsUtil.setArg(currentArgs, null,
				IJBossRuntimeConstants.STARTUP_ARG_CONFIG_LONG,
				RSEUtils.getRSEConfigName(jbossServer.getServer()));

		currentVMArgs = ArgsUtil.setArg(currentVMArgs, null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.ENDORSED_DIRS,
				RSEUtils.pathToRemoteSystem(host, rseHome, IJBossRuntimeResourceConstants.LIB + Path.SEPARATOR + 
						IJBossRuntimeResourceConstants.ENDORSED), true);

		String libPath = new Path(rseHome).append(IJBossRuntimeResourceConstants.BIN)
				.append(IJBossRuntimeResourceConstants.NATIVE).toOSString();
		currentVMArgs = ArgsUtil.setArg(currentVMArgs, null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JAVA_LIB_PATH,
				RSEUtils.pathToRemoteSystem(host, libPath, null), true);

		String startJar = new Path(rseHome).append(IJBossRuntimeResourceConstants.BIN).append(
				IJBossRuntimeResourceConstants.START_JAR).toString(); 
		String cmd = "java " + currentVMArgs + " -classpath " +
				 RSEUtils.pathToRemoteSystem(host, startJar, null) + IJBossRuntimeConstants.SPACE +
				IJBossRuntimeConstants.START_MAIN_TYPE + IJBossRuntimeConstants.SPACE + currentArgs + "&";
		return cmd;
	}
}
