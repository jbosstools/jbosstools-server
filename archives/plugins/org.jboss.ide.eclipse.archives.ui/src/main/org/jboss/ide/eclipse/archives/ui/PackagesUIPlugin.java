/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.ui;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PackagesUIPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.archives.ui"; //$NON-NLS-1$
	private static PackagesUIPlugin plugin;

	private ArchivesUIBuildListener buildListener;
	public PackagesUIPlugin() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		buildListener = new ArchivesUIBuildListener();
		ArchivesModel.instance().addBuildListener(buildListener);
	}

	public void stop(BundleContext context) throws Exception {
		ArchivesModel.instance().removeBuildListener(buildListener);
		plugin = null;
		super.stop(context);
	}


	public static PackagesUIPlugin getDefault() {
		return plugin;
	}

	protected void initializeImageRegistry(ImageRegistry registry) {
		ArchivesSharedImages.register(registry);
	}
}
