package org.jboss.ide.eclipse.archives.webtools;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.ide.eclipse.archives.webtools.modules.ArchivesModuleModelListener;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class IntegrationPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.archives.webtools"; //$NON-NLS-1$

	// The shared instance
	private static IntegrationPlugin plugin;

	/**
	 * The constructor
	 */
	public IntegrationPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ArchivesModuleModelListener.getInstance();
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
	public static IntegrationPlugin getDefault() {
		return plugin;
	}

}
