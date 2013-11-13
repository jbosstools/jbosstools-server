/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.launch.StandardLocalJBossStartLaunchDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.LocalJBossStartLaunchConfigurator;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7StartConfigurator;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;

/**
 * The default launch controller for all local launches
 */
public class LocalJBossLaunchController  extends AbstractSubsystemController implements ILaunchServerController {

	@Override
	public IStatus canStart(String launchMode) {
		return Status.OK_STATUS;
	}

	@Override
	public void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy,
			IProgressMonitor monitor) throws CoreException {
		ILaunchConfigConfigurator configurator = getConfigurator();
		if( configurator != null ) {
			configurator.configure(workingCopy);
		}
	}

	private ILaunchConfigConfigurator getConfigurator() throws CoreException {
		JBossExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getJBossExtendedProperties(getServer());
		int fs = props.getFileStructure();
		if( fs == JBossExtendedProperties.FILE_STRUCTURE_SERVER_CONFIG_DEPLOY)
			return new LocalJBossStartLaunchConfigurator(getServer());
		else if( fs == JBossExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS)
			return new LocalJBoss7StartConfigurator(getServer());
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
		ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// FOr this method we assume everything has already been set up properly
		// and we just launch with our standard local launch delegate
		// which checks things like if a server is up already, or 
		// provides profiling integration with wtp's profiling for servers
		new StandardLocalJBossStartLaunchDelegate().launch(configuration, mode, launch, monitor);
	}
}
