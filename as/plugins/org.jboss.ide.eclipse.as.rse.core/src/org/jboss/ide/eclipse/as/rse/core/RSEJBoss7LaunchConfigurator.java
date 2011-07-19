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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
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
	}

	protected String getLaunchCommand(JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException {
		String programArguments = getDefaultProgramArguments(jbossServer, jbossRuntime);
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
