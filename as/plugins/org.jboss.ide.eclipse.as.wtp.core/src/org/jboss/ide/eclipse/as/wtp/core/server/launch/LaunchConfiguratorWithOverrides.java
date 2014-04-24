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
package org.jboss.ide.eclipse.as.wtp.core.server.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator;

/**
 * This class is to configure the launch configuration of a server. 
 * It can initialize a set of values if the launch has never been configured before. 
 * It should also override values that are pulled either from the server
 * model, editor, or other locations, that must be verified each time. 
 * 
 * So for example, you may initially configure a launch to run with args such as 
 * -Dmy.custom.param=true.   
 * 
 * If the proper UI to change this is in the editor (or in the server's map of attributes), 
 * the user should not change this in the launch configuration, as other tools may 
 * be querying the server adapter's map for the value. Users should only change this
 * in the proper location, and so this configurator may choose to force the value 
 * to match the value in the server adapter, rather than let the user change it to 
 * whatever they want. 
 * 
 * @author André Dietisheim
 */
public abstract class LaunchConfiguratorWithOverrides implements ILaunchConfigConfigurator {

	private static final String DEFAULTS_SET = "DEFAULTS_SET"; //$NON-NLS-1$

	protected IServer server;
	protected IRuntime runtime;

	public LaunchConfiguratorWithOverrides(IServer server) throws CoreException {
		this.server = server;
		this.runtime = server.getRuntime();
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator#configure(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void configure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		if (!areDefaultsSet(launchConfig)) {
			doConfigure(launchConfig);
			setDefaultsSet(launchConfig);
		} 
		if( shouldUpdateLaunch(launchConfig)){
			doOverrides(launchConfig);
		}
	}

	/**
	 * Perform initial configuration 
	 * @param launchConfig
	 * @throws CoreException
	 */
	protected abstract void doConfigure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException;

	
	/**
	 * Perform any forced overrides based on external models / data stores. 
	 * @param launchConfig
	 * @throws CoreException
	 */
	protected abstract void doOverrides (ILaunchConfigurationWorkingCopy launchConfig) throws CoreException;

	protected String getServerId(IServer server) {
		return server.getId();
	}

	private boolean areDefaultsSet(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		boolean b = launchConfig.hasAttribute(DEFAULTS_SET);
		return b;
	}

	private void setDefaultsSet(ILaunchConfigurationWorkingCopy launchConfig) {
		launchConfig.setAttribute(DEFAULTS_SET, true);
	}


	private boolean shouldUpdateLaunch(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		return launchConfig.getAttribute(AUTOMATICALLY_UPDATE_LAUNCH, true);
	}
}