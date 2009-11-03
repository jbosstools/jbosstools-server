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
package org.jboss.ide.eclipse.as.core;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectEvent;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.ide.eclipse.as.core.server.internal.ServerListener;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class JBossServerCorePlugin extends Plugin  {
	//The shared instance.
	private static JBossServerCorePlugin plugin;
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.core"; //$NON-NLS-1$
	
	/**
	 * The constructor.
	 */
	public JBossServerCorePlugin() {
		super();
		plugin = this;
	}

	public IExtension[] getExtensions (String extensionPoint) {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint ep = reg.getExtensionPoint(extensionPoint);
		IExtension[] extensions = ep.getExtensions();
		
		return extensions;
	}
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		// Start the array of models that need to be started
		UnitedServerListenerManager.getDefault();
		UnitedServerListenerManager.getDefault().addListener(XPathModel.getDefault());
		UnitedServerListenerManager.getDefault().addListener(ServerListener.getDefault());
		FacetedProjectFramework.addListener( JBoss4xEarFacetInstallListener.getDefault(), IFacetedProjectEvent.Type.POST_INSTALL);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		UnitedServerListenerManager.getDefault().removeListener(ServerListener.getDefault());
		UnitedServerListenerManager.getDefault().removeListener(XPathModel.getDefault());
		FacetedProjectFramework.removeListener(JBoss4xEarFacetInstallListener.getDefault());
	}

	/**
	 * Returns the shared instance.
	 */
	public static JBossServerCorePlugin getDefault() {
		return plugin;
	}

	public static IPath getServerStateLocation(IServer server) {
		return ServerUtil.getServerStateLocation(server);
	}

	public static IPath getServerStateLocation(String serverID) {
		return ServerUtil.getServerStateLocation(serverID);
	}
	
	public static IPath getGlobalSettingsLocation() {
		return JBossServerCorePlugin.getDefault().getStateLocation().append(".global"); //$NON-NLS-1$
	}
	


}
