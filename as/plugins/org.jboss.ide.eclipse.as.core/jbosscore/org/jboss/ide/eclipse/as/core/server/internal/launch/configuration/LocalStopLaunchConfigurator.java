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
package org.jboss.ide.eclipse.as.core.server.internal.launch.configuration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior.JBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

/**
 * @author André Dietisheim
 */
public class LocalStopLaunchConfigurator extends AbstractLaunchConfigurator {

	public LocalStopLaunchConfigurator(IServer server) throws CoreException {
		super(server);
	}

	@Override
	protected void doConfigure(ILaunchConfigurationWorkingCopy launchConfig, JBossServer jbossServer,
			IJBossServerRuntime jbossRuntime) throws CoreException {

		JBossLaunchConfigProperties.setProgramArguments(getDefaultProgramArguments(jbossServer, jbossRuntime), launchConfig);
		JBossLaunchConfigProperties.setMainType(getMainType(), launchConfig);
		JBossLaunchConfigProperties.setWorkingDirectory(getWorkingDirectory(jbossServer, jbossRuntime), launchConfig);
		JBossLaunchConfigProperties.setClasspath(getClasspath(jbossServer, jbossRuntime, JBossLaunchConfigProperties.getClasspath(launchConfig)), launchConfig);
		JBossLaunchConfigProperties.setUseDefaultClassPath(isUseDefaultClasspath(), launchConfig);
		JBossLaunchConfigProperties.setServerId(getServerId(jbossServer), launchConfig);
	}

	@Override
	protected String getDefaultProgramArguments(JBossServer server, IJBossServerRuntime runtime) throws CoreException {
		JBossBehaviourDelegate delegate = ServerUtil.checkedGetBehaviorDelegate(server.getServer());
		return delegate.getDefaultStopArguments();
	}

	@Override
	protected String getMainType() {
		return IJBossRuntimeConstants.SHUTDOWN_MAIN_TYPE;
	}

	@Override
	protected String getWorkingDirectory(JBossServer server, IJBossServerRuntime jbossRuntime) throws CoreException {
		return ServerUtil.getServerBinDirectory(server).toOSString();
	}

	@Override
	protected List<String> getClasspath(JBossServer server, IJBossServerRuntime runtime, List<String> currentClasspath)
			throws CoreException {
		ArrayList<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		LaunchConfigUtils.addCPEntry(ServerUtil.getServerHomePath(server),  IJBossRuntimeResourceConstants.SHUTDOWN_JAR_LOC, classpath);
		LaunchConfigUtils.addJREEntry(runtime.getVM(), classpath);
		return LaunchConfigUtils.toStrings(classpath);
	}
}
