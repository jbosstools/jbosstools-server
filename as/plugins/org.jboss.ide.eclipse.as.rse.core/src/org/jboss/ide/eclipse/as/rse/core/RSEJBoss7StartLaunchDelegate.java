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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.launch.DelegatingStartLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class RSEJBoss7StartLaunchDelegate extends AbstractRSELaunchDelegate {
	private PollThread pollThread;

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
	}

	private String getDefaultStopCommand(IServer server) {
		try {
			return getDefaultStopCommand(server, false);
		} catch (CoreException ce) {/* ignore, INTENTIONAL */
		}
		return null;
	}

	private String getDefaultStopCommand(IServer server, boolean errorOnFail) throws CoreException {
		return null;
	}

	private String getDefaultLaunchCommand(ILaunchConfiguration config) throws CoreException {
		/*
		 * -server -Xms64m -Xmx512m -XX:MaxPermSize=256m
		 * -Djava.net.preferIPv4Stack=true -Dorg.jboss.resolver.warning=true
		 * -Dsun.rmi.dgc.client.gcInterval=3600000
		 * -Dsun.rmi.dgc.server.gcInterval=3600000
		 * -Dorg.jboss.boot.log.file=/home
		 * /adietish/jboss-runtimes/jboss-7.0.0.CR1/standalone/log/boot.log
		 * -Dlogging
		 * .configuration=file:/home/adietish/jboss-runtimes/jboss-7.0.0
		 * .CR1/standalone/configuration/logging.properties -jar
		 * /home/adietish/jboss-runtimes/jboss-7.0.0.CR1/jboss-modules.jar
		 * (!!!!!!!!!MISSING) -mp
		 * /home/adietish/jboss-runtimes/jboss-7.0.0.CR1/modules -logmodule
		 * org.jboss.logmanager -jaxpmodule javax.xml.jaxp-provider
		 * org.jboss.as.standalone
		 * -Djboss.home.dir=/home/adietish/jboss-runtimes/jboss-7.0.0.CR1
		 */
		String serverId = JBossLaunchConfigProperties.getServerId(config);
		JBossServer jbossServer = ServerConverter.checkedFindJBossServer(serverId);
		String rseHome = RSEUtils.getRSEHomeDir(jbossServer.getServer());
		String currentArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		String currentVMArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$
		String jarArg = LaunchConfigUtils.classpathUserClassesToString(config); 

		String cmd = "java "
				+ currentVMArgs
				+ " -classpath "
				+ new Path(rseHome).append(IJBossRuntimeResourceConstants.BIN).append(
						IJBossRuntimeResourceConstants.START_JAR).toString()
				+ " -jar " + jarArg + " "
				+ IJBossRuntimeConstants.SPACE + currentArgs 
				+ "&";
		return cmd;
	}
}
