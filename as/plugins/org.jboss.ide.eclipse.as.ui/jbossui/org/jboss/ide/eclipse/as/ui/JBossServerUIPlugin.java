/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui;

import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class JBossServerUIPlugin extends AbstractUIPlugin implements IStartup {
	private static JBossServerUIPlugin plugin;
	private ResourceBundle resourceBundle;

	// UI plugin id
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.ui";

	/**
	 * The constructor.
	 */
	public JBossServerUIPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.jboss.ide.eclipse.as.ui.ServerUiPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static JBossServerUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void earlyStartup() {
		JBossServerCorePlugin.getDefault();
	}
	

	
	public ServerViewProvider[] getEnabledViewProviders(IServer server) {
		ServerViewProvider[] serverViewExtensions = getAllServerViewProviders();
		ArrayList list = new ArrayList();
		for( int i = 0; i < serverViewExtensions.length; i++ ) {
			if( serverViewExtensions[i].isEnabled() && serverViewExtensions[i].supports(server)) {
				list.add(serverViewExtensions[i]);
			}
		}
		return (ServerViewProvider[]) list.toArray(new ServerViewProvider[list.size()]);
	}
	
	public ServerViewProvider[] getAllServerViewProviders() {
		return ExtensionManager.getDefault().getAllServerViewProviders();
	}
	
	public static void log(String message, Exception e) {
		IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, message, e);
		JBossServerUIPlugin.getDefault().getLog().log(status);
	}
	
}
