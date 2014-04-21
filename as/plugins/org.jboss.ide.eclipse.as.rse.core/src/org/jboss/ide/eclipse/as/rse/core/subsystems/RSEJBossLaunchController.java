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
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.rse.core.RSEJBoss7LaunchConfigurator;
import org.jboss.ide.eclipse.as.rse.core.RSELaunchConfigurator;
import org.jboss.ide.eclipse.as.rse.core.StandardRSEJBossStartLaunchDelegate;
import org.jboss.ide.eclipse.as.rse.core.StandardRSEStartLaunchDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerShutdownController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IShutdownControllerDelegate;

/**
 * The default launch controller for all jboss server rse launches
 */
public class RSEJBossLaunchController extends RSECommandLineLaunchController 
	implements ILaunchServerController, ILaunchConfigurationDelegate2, IShutdownControllerDelegate {

	@Override
	protected ILaunchConfigConfigurator getConfigurator() throws CoreException {
		JBossExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getJBossExtendedProperties(getServer());
		int fs = props.getFileStructure();
		if( fs == JBossExtendedProperties.FILE_STRUCTURE_SERVER_CONFIG_DEPLOY)
			return new RSELaunchConfigurator(getServer());
		else if( fs == JBossExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS)
			return new RSEJBoss7LaunchConfigurator(getServer());
		return null;
	}

	@Override
	protected StandardRSEStartLaunchDelegate getLaunchDelegate() {
		if( launchDelegate == null ) {
			launchDelegate = new StandardRSEJBossStartLaunchDelegate();
		}
		return launchDelegate;
	}


	@Override
	public IServerShutdownController getShutdownController() {
		IServerShutdownController c = new RSEJBossCommandLineShutdownController();
		c.initialize(getServer(), null, null);
		return c;
	}
}
