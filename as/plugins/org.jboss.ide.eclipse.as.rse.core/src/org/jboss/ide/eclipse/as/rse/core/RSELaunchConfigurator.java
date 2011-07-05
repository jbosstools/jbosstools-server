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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.ILaunchConfigConfigurator;

/**
 * @author André Dietisheim
 */
public class RSELaunchConfigurator implements ILaunchConfigConfigurator {

	private String defaultLaunchCommand;
	private String defaultStopCommand;

	public RSELaunchConfigurator(String defaultLaunchCommand, String defaultStopCommand) throws CoreException {
		this.defaultLaunchCommand = defaultLaunchCommand;
		this.defaultStopCommand = defaultStopCommand;
	}

	@Override
	public void configure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		RSELaunchConfigProperties.setDefaultStartupCommand(defaultLaunchCommand, launchConfig);

		boolean detectStartupCommand = RSELaunchConfigProperties.isDetectStartupCommand(launchConfig, true);
		String currentStartupCmd = RSELaunchConfigProperties.getStartupCommand(launchConfig);
		if( detectStartupCommand || !isSet(currentStartupCmd)) {
			RSELaunchConfigProperties.setStartupCommand(defaultLaunchCommand, launchConfig);
		}

		RSELaunchConfigProperties.setDefaultShutdownCommand(defaultStopCommand, launchConfig);

		boolean detectShutdownCommand = RSELaunchConfigProperties.isDetectShutdownCommand(launchConfig, true);
		String currentStopCmd = RSELaunchConfigProperties.getShutdownCommand(launchConfig);
		if( detectShutdownCommand || isSet(currentStopCmd)) {
			RSELaunchConfigProperties.setShutdownCommand(defaultStopCommand, launchConfig);
		}
	}
		
	private boolean isSet(String value) {
		return value != null
				&& value.length() > 0;
	}
}
