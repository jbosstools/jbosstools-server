package org.jboss.ide.eclipse.archives.ui;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PackagesUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.archives.ui";



	// The shared instance
	private static PackagesUIPlugin plugin;

	/**
	 * The constructor
	 */
	public PackagesUIPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	private ArchivesUIBuildListener buildListener;
	public void start(BundleContext context) throws Exception {
		super.start(context);
		buildListener = new ArchivesUIBuildListener();
		ArchivesModel.instance().addBuildListener(buildListener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		ArchivesModel.instance().removeBuildListener(buildListener);
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PackagesUIPlugin getDefault() {
		return plugin;
	}



	protected void initializeImageRegistry(ImageRegistry registry) {
		ArchivesSharedImages.register(registry);
	}
}
