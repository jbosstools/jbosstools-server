/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
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
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.internal.ServerUIPreferences;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.ide.eclipse.as.ui.console.ShowConsoleServerStateListener;
import org.jboss.ide.eclipse.as.ui.dialogs.ModifyDeploymentScannerIntervalDialog.DeploymentScannerUIServerStartedListener;
import org.jboss.ide.eclipse.as.ui.dialogs.ServerAlreadyStartedDialog.ServerAlreadyStartedHandler;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.XPathRuntimeListener;
import org.jboss.ide.eclipse.as.ui.wizards.JBInitialSelectionProvider;
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

	private static JBInitialSelectionProvider selectionProvider;
	private static DeploymentScannerUIServerStartedListener as7ScannerAssist;
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		selectionProvider = new JBInitialSelectionProvider();
		as7ScannerAssist = new DeploymentScannerUIServerStartedListener();
		Preferences prefs = getPluginPreferences();

		if( !prefs.getBoolean(IPreferenceKeys.ENABLED_DECORATORS)) {
			IDecoratorManager manager = WorkbenchPlugin.getDefault().getDecoratorManager();
			manager.setEnabled("org.jboss.tools.as.wst.server.ui.navigatorDecorator", true); //$NON-NLS-1$
			manager.setEnabled("org.jboss.ide.eclipse.as.ui.extensions.xml.decorator", true); //$NON-NLS-1$
			prefs.setValue(IPreferenceKeys.ENABLED_DECORATORS, true);
		}
		
		if( !prefs.getBoolean(IPreferenceKeys.DISABLE_SHOW_SERVER_VIEW)) {
			ServerUIPreferences.getInstance().setShowOnActivity(false);
			prefs.setValue(IPreferenceKeys.DISABLE_SHOW_SERVER_VIEW, true);
		}
		savePluginPreferences();
		UnitedServerListenerManager.getDefault().addListener(ShowConsoleServerStateListener.getDefault());
		UnitedServerListenerManager.getDefault().addListener(as7ScannerAssist);
		ServerCore.addServerLifecycleListener(selectionProvider);
		ServerCore.addRuntimeLifecycleListener(XPathRuntimeListener.getDefault()); 
		ExtensionManager.getDefault().setAlreadyStartedHandler(new ServerAlreadyStartedHandler());
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		ExtensionManager.getDefault().setAlreadyStartedHandler(null);
		ServerCore.removeRuntimeLifecycleListener(XPathRuntimeListener.getDefault()); 
		ServerCore.removeServerLifecycleListener(selectionProvider);
		UnitedServerListenerManager.getDefault().removeListener(ShowConsoleServerStateListener.getDefault());
		UnitedServerListenerManager.getDefault().removeListener(as7ScannerAssist);
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
		log(status);
	}

	public static void log(IStatus status) {
		JBossServerUIPlugin.getDefault().getLog().log(status);
	}
	
}
