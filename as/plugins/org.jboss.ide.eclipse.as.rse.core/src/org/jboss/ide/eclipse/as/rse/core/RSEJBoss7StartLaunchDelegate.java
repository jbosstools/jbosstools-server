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
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
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

public class RSEJBoss7StartLaunchDelegate extends AbstractRSELaunchDelegate {
	public void actualLaunch(DelegatingStartLaunchConfiguration launchConfig,
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

	public void preLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
	}

	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
	}

	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IServer server)
			throws CoreException {
		new RSELaunchConfigurator(getDefaultLaunchCommand(workingCopy), getDefaultStopCommand(server))
				.configure(workingCopy);
		/*
		 * /usr/lib/jvm/jre/bin/java -Dprogram.name=run.sh -server -Xms1530M
		 * -Xmx1530M -XX:PermSize=425M -XX:MaxPermSize=425M
		 * -Dorg.jboss.resolver.warning=true
		 * -Dsun.rmi.dgc.client.gcInterval=3600000
		 * -Dsun.rmi.dgc.server.gcInterval=3600000
		 * -Djboss.partition.udpGroup=228.1.2.3
		 * -Djboss.webpartition.mcast_port=45577
		 * -Djboss.hapartition.mcast_port=45566
		 * -Djboss.ejb3entitypartition.mcast_port=43333
		 * -Djboss.ejb3sfsbpartition.mcast_port=45551
		 * -Djboss.jvmRoute=node-10.209.183.100 -Djboss.gossip_port=12001
		 * -Djboss.gossip_refresh=5000 -Djava.awt.headless=true
		 * -Djava.net.preferIPv4Stack=true
		 * -Djava.endorsed.dirs=/opt/jboss-eap-5.1.0.Beta/jboss-as/lib/endorsed
		 * -classpath /opt/jboss-eap-5.1.0.Beta/jboss-as/bin/run.jar
		 * org.jboss.Main -c default -b 10.209.183.100
		 */
	}

	private  String getDefaultStopCommand(IServer server) {
		try {
			return getDefaultStopCommand(server, false);
		} catch (CoreException ce) {/* ignore, INTENTIONAL */
		}
		return null;
	}

	private String getDefaultStopCommand(IServer server, boolean errorOnFail) throws CoreException {
		String rseHome = RSEUtils.getRSEHomeDir(server, errorOnFail);

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
