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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.DelegatingStartLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.RegExUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class RSEJBoss7StartLaunchDelegate extends AbstractRSELaunchDelegate {

	@Override
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

	@Override
	public void preLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
	}

	@Override
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
		* -server 
		* -Xms64m 
		* -Xmx512m 
		* -XX:MaxPermSize=256m 
		* -Djava.net.preferIPv4Stack=true 
		* -Dorg.jboss.resolver.warning=true 
		* -Dsun.rmi.dgc.client.gcInterval=3600000 
		* -Dsun.rmi.dgc.server.gcInterval=3600000 
		* -Djboss.modules.system.pkgs=org.jboss.byteman 
		* -Dorg.jboss.boot.log.file=/home/adietish/jboss-runtimes/jboss-as-web-7.0.0.Final/standalone/log/boot.log 
		* -Dlogging.configuration=file:/home/adietish/jboss-runtimes/jboss-as-web-7.0.0.Final/standalone/configuration/logging.properties 
		* -jar /home/adietish/jboss-runtimes/jboss-as-web-7.0.0.Final/jboss-modules.jar 
		* -mp /home/adietish/jboss-runtimes/jboss-as-web-7.0.0.Final/modules -logmodule org.jboss.logmanager 
		* -jaxpmodule javax.xml.jaxp-provider 
		* org.jboss.as.standalone 
		* -Djboss.home.dir=/home/adietish/jboss-runtimes/jboss-as-web-7.0.0.Final
		*/

		String serverId = JBossLaunchConfigProperties.getServerId(config);
		JBossServer jbossServer = ServerConverter.checkedFindJBossServer(serverId);
		String currentArgs = JBossLaunchConfigProperties.getProgramArguments(config);
		String rseArgs = replaceLocalPath(currentArgs, jbossServer);
		String currentVMArgs = JBossLaunchConfigProperties.getVMArguments(config);
		String rseVMArgs = replaceLocalPath(currentVMArgs, jbossServer);
		String jarArg = LaunchConfigUtils.classpathUserClassesToString(config); 
		String rseJarArg = replaceLocalPath(jarArg, jbossServer);
		
		String cmd = "java "
				+ rseVMArgs
				+ " -jar " + rseJarArg + " "
				+ IJBossRuntimeConstants.SPACE + rseArgs 
				+ "&";
		return cmd;
	}
	
	private String replaceLocalPath(String value, JBossServer jbossServer) throws CoreException {
		IPath localHome = ServerUtil.getServerHomePath(jbossServer);
		String localHomeRegex = RegExUtils.escapeRegex(localHome.toOSString());
		String rseHome = RSEUtils.getRSEHomeDir(jbossServer.getServer());
		return value.replaceAll(localHomeRegex, rseHome);
	}
}
