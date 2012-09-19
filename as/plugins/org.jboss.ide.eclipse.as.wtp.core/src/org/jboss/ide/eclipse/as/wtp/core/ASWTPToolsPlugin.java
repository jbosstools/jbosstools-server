/******************************************************************************* 
 * Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ASWTPToolsPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.wtp.core"; //$NON-NLS-1$

	// The shared instance
	private static ASWTPToolsPlugin plugin;
	
	
	/**
	 * The constructor
	 */
	public ASWTPToolsPlugin() {
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
	public static ASWTPToolsPlugin getDefault() {
		return plugin;
	}


    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    public static void log(int severity, String message, Throwable e) {
        log(new Status(severity, PLUGIN_ID, 0, message, e));
    }
    

    public static void log(Throwable e) {
    	log(e.getMessage(), e);
    }

    public static void log(String message, Throwable e) {
        log(IStatus.ERROR, message, e);
    }

}
