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
package org.jboss.ide.eclipse.archives.core;


import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

import de.schlichtherle.io.ArchiveDetector;
import de.schlichtherle.key.KeyManager;

/**
 * The activator class controls the plug-in life cycle
 * @author rstryker
 *
 */
public class ArchivesCorePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = ArchivesCore.PLUGIN_ID;

	// The shared instance
	private static ArchivesCorePlugin plugin;
	private static BundleContext context;
	
	/**
	 * The constructor
	 */
	public ArchivesCorePlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.context = context;
		
		// Load the workspace version of ArchivesCore
		new WorkspaceArchivesCore();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new WorkspaceChangeListener());
		
		// JBIDE-17700  workaround
		ClassLoader bundleLoader = getBundleClassLoader();
		ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(bundleLoader);
			KeyManager.getInstance();
		} finally {
			Thread.currentThread().setContextClassLoader(originalTCCL);
		}
	}

	public ClassLoader getBundleClassLoader() {
		// JBIDE-17700  workaround
		Bundle bundle = context.getBundle();
		BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
		ClassLoader bundleLoader = bundleWiring.getClassLoader();
		return bundleLoader;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
	public static ArchivesCorePlugin getDefault() {
		return plugin;
	}

}
