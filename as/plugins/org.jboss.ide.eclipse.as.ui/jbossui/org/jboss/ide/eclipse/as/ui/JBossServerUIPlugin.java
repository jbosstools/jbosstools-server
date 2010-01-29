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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.server.ui.internal.ServerUIPreferences;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.ide.eclipse.as.ui.console.ShowConsoleServerStateListener;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class JBossServerUIPlugin extends AbstractUIPlugin implements IStartup {
	private static JBossServerUIPlugin plugin;
	private ResourceBundle resourceBundle;

	// UI plugin id
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.ui"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public JBossServerUIPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.jboss.ide.eclipse.as.ui.ServerUiPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		Preferences prefs = getPluginPreferences();

		if( !prefs.getBoolean(IPreferenceKeys.ENABLED_DECORATORS)) {
			IDecoratorManager manager = WorkbenchPlugin.getDefault().getDecoratorManager();
			manager.setEnabled("org.jboss.tools.as.wst.server.ui.navigatorDecorator", true); //$NON-NLS-1$
			manager.setEnabled("org.jboss.ide.eclipse.as.ui.extensions.xml.decorator", true); //$NON-NLS-1$
			prefs.setValue(IPreferenceKeys.ENABLED_DECORATORS, true);
		}
		
		if( !prefs.getBoolean(IPreferenceKeys.DISABLE_SHOW_SERVER_VIEW)) {
			new ServerUIPreferences().setShowOnActivity(false);
			prefs.setValue(IPreferenceKeys.DISABLE_SHOW_SERVER_VIEW, true);
		}
		savePluginPreferences();
		UnitedServerListenerManager.getDefault().addListener(ShowConsoleServerStateListener.getDefault());
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		UnitedServerListenerManager.getDefault().removeListener(ShowConsoleServerStateListener.getDefault());
		JBossServerUISharedImages.instance().cleanup();
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
	
	public static void log(String message, Exception e) {
		IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, message, e);
		JBossServerUIPlugin.getDefault().getLog().log(status);
	}
	
}
