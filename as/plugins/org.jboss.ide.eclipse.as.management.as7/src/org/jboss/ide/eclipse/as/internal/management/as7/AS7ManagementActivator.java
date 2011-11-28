package org.jboss.ide.eclipse.as.internal.management.as7;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class AS7ManagementActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.management.as7"; //$NON-NLS-1$

	// The shared instance
	private static AS7ManagementActivator plugin;
	
	/**
	 * The constructor
	 */
	public AS7ManagementActivator() {
	}

	public static BundleContext getContext() {
		return plugin.getBundle().getBundleContext();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static AS7ManagementActivator getDefault() {
		return plugin;
	}

}
