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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * @author André Dietisheim
 */
public abstract class AbstractLaunchConfigurator implements ILaunchConfigConfigurator {

	private static final String DEFAULTS_SET = "DEFAULTS_SET"; //$NON-NLS-1$

	private IJBossServer jbossServer;
	private IJBossServerRuntime jbossRuntime;

	public AbstractLaunchConfigurator(IServer server) throws CoreException {
		this.jbossServer = ServerConverter.checkedGetJBossServer(server);
		this.jbossRuntime = jbossServer.getRuntime();
	}

	@Override
	public void configure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if (!areDefaultsSet(launchConfig)) {
			doConfigure(launchConfig, (JBossServer)jbossServer, jbossRuntime);
			setDefaultsSet(launchConfig);
		} 
		if( shouldUpdateLaunch(launchConfig)){
			doOverrides(launchConfig, (JBossServer)jbossServer, jbossRuntime);
		}
	}

	protected abstract void doConfigure(ILaunchConfigurationWorkingCopy launchConfig, JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException;

	protected abstract void doOverrides (ILaunchConfigurationWorkingCopy launchConfig, JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException;
	
	protected abstract String getDefaultProgramArguments(JBossServer server, IJBossServerRuntime runtime) throws CoreException;

	protected abstract String getMainType();

	protected abstract String getWorkingDirectory(JBossServer server, IJBossServerRuntime jbossRuntime)
			throws CoreException;

	protected String getServerId(JBossServer server) {
		return server.getServer().getId();
	}

	protected abstract List<String> getClasspath(JBossServer server,  IJBossServerRuntime runtime, List<String> currentClasspath) throws CoreException;

	protected boolean isUseDefaultClasspath() {
		return false;
	}

	protected IJBossServer getJbossServer() {
		return jbossServer;
	}

	protected IJBossServerRuntime getJbossRuntime() {
		return jbossRuntime;
	}

	private boolean areDefaultsSet(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		boolean b = launchConfig.hasAttribute(DEFAULTS_SET);
		return b;
	}

	private boolean shouldUpdateLaunch(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		return launchConfig.getAttribute(JBossServer.AUTOMATICALLY_UPDATE_LAUNCH, true);
	}

	private void setDefaultsSet(ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(DEFAULTS_SET, true);
	}
}