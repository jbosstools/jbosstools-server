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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.LaunchConfiguratorWithOverrides;

/**
 * @author André Dietisheim
 */
public class LocalStopLaunchConfigurator extends LaunchConfiguratorWithOverrides {

	public LocalStopLaunchConfigurator(IServer server) throws CoreException {
		super(server);
	}

	@Override
	protected void doOverrides(ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		// Intentionally left blank
	}

	@Override
	protected void doConfigure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		JBossLaunchConfigProperties props = new JBossLaunchConfigProperties();
		props.setProgramArguments(getDefaultProgramArguments(), launchConfig);
		props.setMainType(getMainType(), launchConfig);
		props.setWorkingDirectory(getWorkingDirectory(), launchConfig);
		props.setClasspath(getClasspath(new JBossLaunchConfigProperties().getClasspath(launchConfig)), launchConfig);
		props.setUseDefaultClassPath(isUseDefaultClasspath(), launchConfig);
		props.setServerId(getServerId(server), launchConfig);
	}

	protected boolean isUseDefaultClasspath() {
		return false;
	}

	protected String getDefaultProgramArguments() throws CoreException {
		return getJBossServer().getExtendedProperties().getDefaultLaunchArguments().getDefaultStopArgs();
	}


	protected String getMainType() {
		return IJBossRuntimeConstants.SHUTDOWN_MAIN_TYPE;
	}


	protected String getWorkingDirectory() throws CoreException {
		return ServerUtil.getServerBinDirectory(getJBossServer()).toOSString();
	}


	protected List<String> getClasspath(List<String> currentClasspath) throws CoreException {
		ArrayList<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		LaunchConfigUtils.addCPEntry(ServerUtil.getServerHomePath(getJBossServer()),  IJBossRuntimeResourceConstants.SHUTDOWN_JAR_LOC, classpath);
		LaunchConfigUtils.addJREEntry(getJBossRuntime().getVM(), classpath);
		return LaunchConfigUtils.toStrings(classpath);
	}

	
	protected JBossServer getJBossServer() {
		return (JBossServer)server.loadAdapter(JBossServer.class, new NullProgressMonitor());
	}
	private IJBossServerRuntime getJBossRuntime() throws CoreException {
		return RuntimeUtils.checkedGetJBossServerRuntime(runtime);
	}
}
