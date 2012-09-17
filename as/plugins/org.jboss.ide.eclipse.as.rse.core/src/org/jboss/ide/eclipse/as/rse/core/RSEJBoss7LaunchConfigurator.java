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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.internal.Base;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7Server;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * @author André Dietisheim
 */
public class RSEJBoss7LaunchConfigurator implements ILaunchConfigConfigurator {

	private IJBossServer jbossServer;
	private IJBossServerRuntime jbossRuntime;

	public RSEJBoss7LaunchConfigurator(IServer server) throws CoreException {
		this.jbossServer = ServerConverter.checkedGetJBossServer(server);
		this.jbossRuntime = jbossServer.getRuntime();
	}

	@Override
	public void configure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {

		boolean detectStartupCommand = RSELaunchConfigProperties.isDetectStartupCommand(launchConfig, true);
		String currentStartupCmd = RSELaunchConfigProperties.getStartupCommand(launchConfig);
		String defaultStartup = getLaunchCommand((JBossServer)jbossServer, jbossRuntime);
		if( detectStartupCommand || !isSet(currentStartupCmd)) {
			RSELaunchConfigProperties.setStartupCommand(defaultStartup, launchConfig);
		}
		RSELaunchConfigProperties.setDefaultStartupCommand(defaultStartup, launchConfig);
		
		boolean detectShutdownCommand = RSELaunchConfigProperties.isDetectShutdownCommand(launchConfig, true);
		String currentShutdownCmd = RSELaunchConfigProperties.getShutdownCommand(launchConfig);
		String defaultShutdownCommand = getShutdownCommand((JBossServer)jbossServer, jbossRuntime);
		if( detectShutdownCommand || !isSet(currentShutdownCmd)) {
			RSELaunchConfigProperties.setShutdownCommand(defaultShutdownCommand, launchConfig);
		}
		RSELaunchConfigProperties.setDefaultShutdownCommand(defaultShutdownCommand, launchConfig);
	}

	protected String getShutdownCommand(JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException {
		String rseHome = RSEUtils.getRSEHomeDir(jbossServer.getServer());
		IPath p = new Path(rseHome).append(IJBossRuntimeResourceConstants.BIN);
		String ret = p.toString() + "/" + getManagementScript(jbossServer);
		
		boolean exposeManagement = LaunchCommandPreferences.exposesManagement(jbossServer.getServer());
		if( exposeManagement ) {
			String host = jbossServer.getServer().getHost();
			int defPort = IJBossToolingConstants.AS7_MANAGEMENT_PORT_DEFAULT_PORT;
			int port = (jbossServer instanceof JBoss7Server) ? 
					((JBoss7Server)jbossServer).getManagementPort() : defPort;
			ret += " --controller=" + host + ":" + port;
		}
		ret += " --connect command=:shutdown";
		return ret;
	}
	
	protected String getManagementScript(JBossServer server) {
		IServerType type = server.getServer().getServerType();
		if( type.getId().equals(IJBossToolingConstants.SERVER_AS_71) || type.getId().equals(IJBossToolingConstants.SERVER_EAP_60)) {
			return IJBossRuntimeResourceConstants.AS_71_MANAGEMENT_SCRIPT;
		}
		return IJBossRuntimeResourceConstants.AS_70_MANAGEMENT_SCRIPT;
	}
	
	protected String getArgsOverrideHost(IServer server, String preArgs) {
		// Overrides
		String host = server.getHost();
		if( LaunchCommandPreferences.listensOnAllHosts(jbossServer.getServer())) {
			host = "0.0.0.0";
		}
		
		return ArgsUtil.setArg(preArgs,
					IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
					null, host);
	}
	
	protected String getArgsOverrideConfigFile(IServer server, String preArgs) {
		String rseConfigFile = ((Base)jbossServer.getServer()).getAttribute(
				RSEUtils.RSE_SERVER_CONFIG, LocalJBoss7ServerRuntime.CONFIG_FILE_DEFAULT);
		String programArguments = ArgsUtil.setArg(preArgs, null,
				IJBossRuntimeConstants.JB7_SERVER_CONFIG_ARG, rseConfigFile
				);
		return programArguments;
	}

	protected String getArgsOverrideExposedManagement(IServer server, String preArgs) {
		boolean overrides = LaunchCommandPreferences.exposesManagement(server);
		if( overrides ) {
			String newVal = overrides ? server.getHost() : null;
			String vmArguments = ArgsUtil.setArg(preArgs, null,
					IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JB7_EXPOSE_MANAGEMENT, newVal );
			return vmArguments;
		}
		return preArgs;
	}
	
	
	protected String getLaunchCommand(JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException {
		String programArguments = getDefaultProgramArguments(jbossServer, jbossRuntime);
		programArguments = getArgsOverrideHost(jbossServer.getServer(), programArguments);
		programArguments = getArgsOverrideConfigFile(jbossServer.getServer(), programArguments);
		
		String vmArguments = getDefaultVMArguments(jbossServer, jbossRuntime);
		vmArguments = getArgsOverrideExposedManagement(jbossServer.getServer(), vmArguments);
		
		String jar = getJar(jbossServer, jbossRuntime);

		String command = "java "
				+ vmArguments
				+ " -jar " + jar + " "
				+ IJBossRuntimeConstants.SPACE + programArguments 
				+ "&";
		return command;

	}
	
	protected String getDefaultVMArguments(JBossServer server, IJBossServerRuntime runtime) {
		String rseHomeDir = RSEUtils.getRSEHomeDir(server.getServer());
		return runtime.getDefaultRunVMArgs(new Path(rseHomeDir));
	}

	protected String getDefaultProgramArguments(JBossServer server, IJBossServerRuntime runtime) {
		String rseHomeDir = RSEUtils.getRSEHomeDir(server.getServer());
		return runtime.getDefaultRunArgs(new Path(rseHomeDir));
	}
	
	protected String getJar(JBossServer server, IJBossServerRuntime runtime) {
		String rseHome = RSEUtils.getRSEHomeDir(server.getServer());
		return new Path(rseHome).append(IJBossRuntimeResourceConstants.JBOSS7_MODULES_JAR).toString();
	}
	
	protected String getMainType() {
		return IJBossRuntimeResourceConstants.JBOSS7_MODULES_JAR;
	}
	
	private boolean isSet(String value) {
		return value != null
				&& value.length() > 0;
	}
}
