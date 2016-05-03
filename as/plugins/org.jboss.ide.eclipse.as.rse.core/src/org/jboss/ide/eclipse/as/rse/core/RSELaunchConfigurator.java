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
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * @author André Dietisheim
 */
public class RSELaunchConfigurator implements ILaunchConfigConfigurator {

	private IServer server;
	public RSELaunchConfigurator(IServer server) throws CoreException {
		this.server = server;
	}

	
	private  String getDefaultStopCommand(IServer server) throws CoreException {
		String rseHome = RSEUtils.getRSEHomeDir(server, false);

		String stop = new Path(rseHome)
				.append(IJBossRuntimeResourceConstants.BIN)
				.append(IJBossRuntimeResourceConstants.SHUTDOWN_SH).toString()
				+ IJBossRuntimeConstants.SPACE;

		// Pull args from single utility method
		JBossServer jbs = (JBossServer)ServerConverter.getJBossServer(server);
		String args = jbs.getExtendedProperties().getDefaultLaunchArguments().getDefaultStopArgs();
		stop += args;
		return stop;
	}

	private String getDefaultLaunchCommand(ILaunchConfiguration config) throws CoreException {
		String serverId = new JBossLaunchConfigProperties().getServerId(config);
		IJBossServer jbossServer = ServerConverter.checkedFindJBossServer(serverId);
		String rseHome = jbossServer.getServer().getAttribute(RSEUtils.RSE_SERVER_HOME_DIR, "");
		String basedir = jbossServer.getServer().getAttribute(RSEUtils.RSE_BASE_DIR, "");
		// initialize startup command to something reasonable
		String currentArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		String currentVMArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$

		if( !"".equals(basedir))
			currentArgs = ArgsUtil.setArg(currentArgs, null,
					IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_SERVER_BASE_URL, basedir);
		
		String connectionName = RSEUtils.getRSEConnectionName(jbossServer.getServer());
		IHost host = RSEFrameworkUtils.findHost(connectionName);
		String remoteSafe = RSEUtils.pathToRemoteSystem(host, rseHome, IJBossRuntimeResourceConstants.SERVER);

		// Use new flag which forces dir instead (safer)
		currentArgs = ArgsUtil.setArg(currentArgs, null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_SERVER_BASE_DIR,
				remoteSafe);


		currentArgs = ArgsUtil.setArg(currentArgs, null,
				IJBossRuntimeConstants.STARTUP_ARG_CONFIG_LONG,
				RSEUtils.getRSEConfigName(jbossServer.getServer()));

		currentArgs = ArgsUtil.setArg(currentArgs, 
				IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT, null,
				jbossServer.getServer().getHost());

		currentVMArgs = ArgsUtil.setArg(currentVMArgs, null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.ENDORSED_DIRS,
				RSEUtils.pathToRemoteSystem(host, rseHome, IJBossRuntimeResourceConstants.LIB + Path.SEPARATOR + 
						IJBossRuntimeResourceConstants.ENDORSED), true);

		String libPath = new Path(rseHome).append(IJBossRuntimeResourceConstants.BIN)
				.append(IJBossRuntimeResourceConstants.NATIVE).toString();
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
	
	@Override
	public void configure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		String defaultLaunchCommand = getDefaultLaunchCommand(launchConfig);
		String defaultStopCommand = getDefaultStopCommand(server);
		RSELaunchConfigProperties propertyUtil = new RSELaunchConfigProperties();

		propertyUtil.setDefaultStartupCommand(defaultLaunchCommand, launchConfig);

		boolean detectStartupCommand = propertyUtil.isDetectStartupCommand(launchConfig, true);
		String currentStartupCmd = propertyUtil.getStartupCommand(launchConfig);
		if( detectStartupCommand || !isSet(currentStartupCmd)) {
			propertyUtil.setStartupCommand(defaultLaunchCommand, launchConfig);
		}

		propertyUtil.setDefaultShutdownCommand(defaultStopCommand, launchConfig);

		boolean detectShutdownCommand = propertyUtil.isDetectShutdownCommand(launchConfig, true);
		String currentStopCmd = propertyUtil.getShutdownCommand(launchConfig);
		if( detectShutdownCommand || !isSet(currentStopCmd)) {
			propertyUtil.setShutdownCommand(defaultStopCommand, launchConfig);
		}
	}
		
	private boolean isSet(String value) {
		return value != null;//  && value.length() > 0;
	}
}
