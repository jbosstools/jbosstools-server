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
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
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

	private JBossServer jbossServer;
	private IJBossServerRuntime jbossRuntime;

	public RSEJBoss7LaunchConfigurator(IServer server) throws CoreException {
		this.jbossServer = ServerConverter.checkedGetJBossServer(server);
		this.jbossRuntime = jbossServer.getRuntime();
	}

	@Override
	public void configure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {

		boolean detectStartupCommand = RSELaunchConfigProperties.isDetectStartupCommand(launchConfig, true);
		String currentStartupCmd = RSELaunchConfigProperties.getStartupCommand(launchConfig);
		if( detectStartupCommand || !isSet(currentStartupCmd)) {
			RSELaunchConfigProperties.setStartupCommand(getLaunchCommand(jbossServer, jbossRuntime), launchConfig);
		}

		boolean detectShutdownCommand = RSELaunchConfigProperties.isDetectShutdownCommand(launchConfig, true);
		String currentShutdownCmd = RSELaunchConfigProperties.getShutdownCommand(launchConfig);
		if( detectShutdownCommand || !isSet(currentShutdownCmd)) {
			RSELaunchConfigProperties.setShutdownCommand(getShutdownCommand(jbossServer, jbossRuntime), launchConfig);
		}
	}

	protected String getShutdownCommand(JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException {
		String rseHome = RSEUtils.getRSEHomeDir(jbossServer.getServer());
		IPath p = new Path(rseHome).append(IJBossRuntimeResourceConstants.BIN);
		return p.toString() + "/" + getManagementScript(jbossServer) + " --connect command=:shutdown";
	}
	
	protected String getManagementScript(JBossServer server) {
		IServerType type = server.getServer().getServerType();
		if( type.getId().equals(IJBossToolingConstants.SERVER_AS_71) || type.getId().equals(IJBossToolingConstants.SERVER_EAP_60)) {
			return IJBossRuntimeResourceConstants.AS_71_MANAGEMENT_SCRIPT;
		}
		return IJBossRuntimeResourceConstants.AS_70_MANAGEMENT_SCRIPT;
	}

	protected String getLaunchCommand(JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException {
		String programArguments = getDefaultProgramArguments(jbossServer, jbossRuntime);
		if( LaunchCommandPreferences.listensOnAllHosts(jbossServer.getServer())) {
			programArguments = ArgsUtil.setArg(programArguments,
					IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
					null, "0.0.0.0");
		}
		programArguments = ArgsUtil.setArg(programArguments, null,
				IJBossRuntimeConstants.JB7_SERVER_CONFIG_ARG,
				((LocalJBoss7ServerRuntime)jbossRuntime).getConfigurationFile());
		
		String vmArguments = getDefaultVMArguments(jbossServer, jbossRuntime);
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
		return new Path(rseHome).append(IJBossRuntimeResourceConstants.JBOSS7_MODULES_JAR).toOSString();
	}
	
	protected String getMainType() {
		return IJBossRuntimeResourceConstants.JBOSS7_MODULES_JAR;
	}
	
	private boolean isSet(String value) {
		return value != null
				&& value.length() > 0;
	}
}
