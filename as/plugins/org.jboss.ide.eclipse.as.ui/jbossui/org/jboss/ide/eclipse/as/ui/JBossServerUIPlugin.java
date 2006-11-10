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
import java.util.Arrays;
import java.util.Comparator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
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
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		try {
			String value = Platform.getResourceString(getDefault().getBundle(), key);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void earlyStartup() {
		JBossServerCore.getDefault();
	}
	

	
	private ServerViewProvider[] serverViewExtensions;
	public ServerViewProvider[] getEnabledViewProviders() {
		getAllServerViewProviders();
		ArrayList list = new ArrayList();
		for( int i = 0; i < serverViewExtensions.length; i++ ) {
			if( serverViewExtensions[i].isEnabled()) {
				list.add(serverViewExtensions[i]);
			}
		}
		ServerViewProvider[] providers = new ServerViewProvider[list.size()];
		list.toArray(providers);
		
		Arrays.sort(providers, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				if( arg0 instanceof ServerViewProvider && arg1 instanceof ServerViewProvider) {
					return ((ServerViewProvider)arg0).getWeight() - ((ServerViewProvider)arg1).getWeight();
				}
				return 0;
			}
		});

		return providers;
	}
	
	public ServerViewProvider[] getAllServerViewProviders() {
		if( serverViewExtensions == null ) {
			loadAllServerViewProviders();
		}
		Arrays.sort(serverViewExtensions, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				if( arg0 instanceof ServerViewProvider && arg1 instanceof ServerViewProvider) {
					return ((ServerViewProvider)arg0).getWeight() - ((ServerViewProvider)arg1).getWeight();
				}
				return 0;
			}
		});
		return serverViewExtensions;
	}
	private void loadAllServerViewProviders() {

		// Create the extensions from the registry
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(JBossServerUIPlugin.PLUGIN_ID, "ServerViewExtension");
		serverViewExtensions = new ServerViewProvider[elements.length];
		for( int i = 0; i < elements.length; i++ ) {
			serverViewExtensions[i] = new ServerViewProvider(elements[i]);
			//serverViewExtensions[i].setEnabled(true);
		}
	}
	
	
	
}
