/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.wtp.runtimes.tomcat.internal.detection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RuntimeTomcatActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.wtp.runtimes.tomcat"; //$NON-NLS-1$

	// The shared instance
	private static RuntimeTomcatActivator plugin;
	
	/**
	 * The constructor
	 */
	public RuntimeTomcatActivator() {
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
	public static RuntimeTomcatActivator getDefault() {
		return plugin;
	}


	public static void logWarning(String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message);
		log(status);
	}
	
	public static void logError(Throwable e) {
		logError(e.getLocalizedMessage(), e);
	}
	
	public static void logError(String message, Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
		log(status);
	}
	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

}
