package org.jboss.ide.eclipse.archives.jdt.integration;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.ide.eclipse.archives.jdt.integration.model.IArchiveLibFileSet;
import org.jboss.ide.eclipse.archives.ui.ExtensionManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ArchiveJDTIntegrationPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.archives.jdt.integration";

	// The shared instance
	private static ArchiveJDTIntegrationPlugin plugin;
	
	/**
	 * The constructor
	 */
	public ArchiveJDTIntegrationPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		addProvider();
	}

	private ILabelProvider labelProvider;
	protected void addProvider() {
		labelProvider = 
			new LabelProvider() { 
				public Image getImage(Object element) {
					if( element instanceof IArchiveLibFileSet) 
						return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_LIBRARY);
					return null;
				}
				public String getText(Object element) {
					if( element instanceof IArchiveLibFileSet)
						return ((IArchiveLibFileSet)element).getId();
					return null;
				}
		};
		ExtensionManager.addLabelProvider(labelProvider);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		ExtensionManager.removeLabelProvider(labelProvider);
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ArchiveJDTIntegrationPlugin getDefault() {
		return plugin;
	}

}
