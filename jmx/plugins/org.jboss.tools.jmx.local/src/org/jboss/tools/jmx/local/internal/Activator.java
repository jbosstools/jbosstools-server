/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.local.internal;


import org.jboss.tools.foundation.core.plugin.log.IPluginLog;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.foundation.ui.plugin.BaseUIPlugin;
import org.jboss.tools.foundation.ui.plugin.BaseUISharedImages;
import org.jboss.tools.jmx.jvmmonitor.core.JvmModel;
import org.osgi.framework.BundleContext;


public class Activator extends BaseUIPlugin {
	public static String PLUGIN_ID = "org.jboss.tools.jmx.local";
	
	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public static void registerNodeProviders() {
		// lets start early loading of the model
		@SuppressWarnings("unused")
		JvmModel model = JvmModel.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}
    
	/**
	 * Create your shared images instance. Clients are expected to override this
	 */
	protected BaseUISharedImages createSharedImages() {
		return new LocalVMSharedImages(getBundle());
	}
	
	/**
	 * Get the IPluginLog for this plugin. This method 
	 * helps to make logging easier, for example:
	 * 
	 *     FoundationCorePlugin.pluginLog().logError(etc)
	 *  
	 * @return IPluginLog object
	 */
	public static IPluginLog pluginLog() {
		return getDefault().pluginLogInternal();
	}

	/**
	 * Get a status factory for this plugin
	 * @return status factory
	 */
	public static StatusFactory statusFactory() {
		return getDefault().statusFactoryInternal();
	}

}
