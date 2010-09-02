/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.core;

import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.launch.JBossServerStartupLaunchConfiguration;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class RSECorePlugin implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		RSECorePlugin.context = bundleContext;
		JBossServerBehavior.addDelegateMapping(RSEPublishMethod.RSE_ID, RSEBehaviourDelegate.class);
		JBossServerStartupLaunchConfiguration.addLaunchDelegateMapping(RSEPublishMethod.RSE_ID, new RSELaunchDelegate());
		JBossServerStartupLaunchConfiguration.addSetupLaunchParticipant(new RSELaunchDelegate());
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		RSECorePlugin.context = null;
	}

}
