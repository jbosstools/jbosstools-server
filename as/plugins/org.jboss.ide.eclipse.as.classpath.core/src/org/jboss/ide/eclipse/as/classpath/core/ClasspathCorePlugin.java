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
package org.jboss.ide.eclipse.as.classpath.core;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlotCache;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.RuntimeClasspathCache;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ClasspathCorePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.classpath.core"; //$NON-NLS-1$

	// The shared instance
	private static ClasspathCorePlugin plugin;
	
	/**
	 * The constructor
	 */
	public ClasspathCorePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ServerCore.addRuntimeLifecycleListener(RuntimeClasspathCache.getInstance());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		ServerCore.removeRuntimeLifecycleListener(RuntimeClasspathCache.getInstance());
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ClasspathCorePlugin getDefault() {
		return plugin;
	}

	public static void log(String msg,Throwable e) {
		ILog log = ClasspathCorePlugin.getDefault().getLog();
        IStatus status = new Status(Status.ERROR,ClasspathCorePlugin.PLUGIN_ID,msg,e);
        log.log(status);
	}

	/**
	 * Clear the cached entries for this runtime type
	 * @since 3.0
	 */
	public static void clearCachedClasspathEntries(IRuntimeType rt) {
		IRuntime[] allRuntimes = ServerCore.getRuntimes();
		for( int i = 0; i < allRuntimes.length; i++ ) {
			if( allRuntimes[i].getRuntimeType().getId().equals(rt.getId())) {
				RuntimeClasspathCache.getInstance().cacheEntries(allRuntimes[i], null);
				ModuleSlotCache.getInstance().clearRuntimeModuleSlots(allRuntimes[i]);
			}
		}
	}

	
}
