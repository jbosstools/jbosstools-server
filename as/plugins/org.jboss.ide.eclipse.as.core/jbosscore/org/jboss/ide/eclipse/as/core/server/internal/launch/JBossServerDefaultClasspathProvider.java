/******************************************************************************* 
 * Copyright (c) 2007, 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.StandardClasspathProvider;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IDefaultClasspathLaunchConfigurator;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfiguratorProvider;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;

/**
 * This class provides a default classpath for server launches. 
 * This is useful for when users request defaults to be set again.
 */
public class JBossServerDefaultClasspathProvider extends StandardClasspathProvider {
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		if (new JBossLaunchConfigProperties().isUseDefaultClasspath(configuration)) {
			return defaultEntries(configuration);
		}
		return super.computeUnresolvedClasspath(configuration);
	}

	protected IRuntimeClasspathEntry[] defaultEntries(ILaunchConfiguration config) {
		try {
			String server = new JBossLaunchConfigProperties().getServerId(config);
			IServer s = ServerCore.findServer(server);
			IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(config);
			if( beh != null ) {
				ISubsystemController cont = beh.getController(IControllableServerBehavior.SYSTEM_LAUNCH);
				if( cont instanceof ILaunchConfigConfiguratorProvider ) {
					ILaunchConfigConfigurator c = ((ILaunchConfigConfiguratorProvider)cont).getLaunchConfigurator();
					if( c instanceof IDefaultClasspathLaunchConfigurator ) {
						return ((IDefaultClasspathLaunchConfigurator)c).getDefaultClasspathEntries(config);
					}
				}
			}
			return new IRuntimeClasspathEntry[0]; 
		} catch (CoreException ce) {
			JBossServerCorePlugin.log(ce.getStatus());
		}

		try {
			return super.computeUnresolvedClasspath(config);
		} catch (CoreException ce) {
			JBossServerCorePlugin.log(ce.getStatus());
		}
		return new IRuntimeClasspathEntry[] {};
	}
}