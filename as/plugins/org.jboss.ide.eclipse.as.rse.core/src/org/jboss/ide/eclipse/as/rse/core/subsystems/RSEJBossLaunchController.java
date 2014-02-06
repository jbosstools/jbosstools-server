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
package org.jboss.ide.eclipse.as.rse.core.subsystems;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.rse.core.RSEJBoss7LaunchConfigurator;
import org.jboss.ide.eclipse.as.rse.core.RSELaunchConfigurator;
import org.jboss.ide.eclipse.as.rse.core.StandardRSEJBossStartLaunchDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;

/**
 * The default launch controller for all rse launches
 */
public class RSEJBossLaunchController  extends AbstractSubsystemController implements ILaunchServerController, ILaunchConfigurationDelegate2 {

	@Override
	public IStatus canStart(String launchMode) {
		if( !"debug".equals(launchMode))
			return Status.OK_STATUS;
		return Status.CANCEL_STATUS;
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
			return new RSELaunchConfigurator(getServer());
		else if( fs == JBossExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS)
			return new RSEJBoss7LaunchConfigurator(getServer());
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
		// and we just launch with our standard rse launch delegate
		// which checks things like if a server is up already, or 
		// provides profiling integration with wtp's profiling for servers
		getLaunchDelegate().launch(configuration, mode, launch, monitor);
	}

	private StandardRSEJBossStartLaunchDelegate launchDelegate;
	private StandardRSEJBossStartLaunchDelegate getLaunchDelegate() {
		if( launchDelegate == null ) {
			launchDelegate = new StandardRSEJBossStartLaunchDelegate();
		}
		return launchDelegate;
	}
	
	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return getLaunchDelegate().getLaunch(configuration, mode);
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		return getLaunchDelegate().buildForLaunch(configuration, mode, monitor);
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		return getLaunchDelegate().finalLaunchCheck(configuration, mode, monitor);
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		return getLaunchDelegate().preLaunchCheck(configuration, mode, monitor);
	}
	
}
