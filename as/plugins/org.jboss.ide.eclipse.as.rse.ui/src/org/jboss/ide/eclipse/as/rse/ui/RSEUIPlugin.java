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
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.ui;

import org.eclipse.core.runtime.ILog;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel;
import org.jboss.ide.eclipse.as.rse.core.RSEPublishMethod;
import org.jboss.ide.eclipse.as.ui.console.JBASConsoleWriter;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentModuleOptionCompositeAssistant;
import org.jboss.ide.eclipse.as.ui.launch.JBoss7LaunchConfigurationTabGroup;
import org.jboss.ide.eclipse.as.ui.launch.JBossLaunchConfigurationTabGroup;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class RSEUIPlugin implements BundleActivator {
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.rse.ui";
	private static BundleContext context;
	private static RSEUIPlugin plugin;
	private JBASConsoleWriter consoleWriter;
	
	
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		
		context = bundleContext;
		plugin = this;
		DeploymentModuleOptionCompositeAssistant.addMapping(RSEPublishMethod.RSE_ID, new RSEDeploymentPageCallback());
		JBossLaunchConfigurationTabGroup.addTabProvider(RSEPublishMethod.RSE_ID, new RSELaunchTabProvider());
		JBoss7LaunchConfigurationTabGroup.addTabProvider(RSEPublishMethod.RSE_ID, new RSELaunchTabProvider());
		consoleWriter = new JBASConsoleWriter();
		RSEHostShellModel.getInstance().addHostShellListener(consoleWriter);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		RSEUIPlugin.context = null;
		plugin = null;
		RSEHostShellModel.getInstance().removeHostShellListener(consoleWriter);
	}

	public static ILog getLog() {
		return plugin.getLog();
	}
}
