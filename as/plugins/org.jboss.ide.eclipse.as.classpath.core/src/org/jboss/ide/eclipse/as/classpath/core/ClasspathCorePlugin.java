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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ClasspathCorePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.classpath.core"; //$NON-NLS-1$

	// The shared instance
	private static ClasspathCorePlugin plugin;
	
	private static Map<RuntimeKey, IClasspathEntry[]> runtimeClasspaths;
	
	private IRuntimeLifecycleListener listener = new IRuntimeLifecycleListener() {
		
		public void runtimeRemoved(IRuntime runtime) {
			removeRuntimeClasspath(runtime);
		}
		
		public void runtimeChanged(IRuntime runtime) {
			removeRuntimeClasspath(runtime);
		}
		
		public void runtimeAdded(IRuntime runtime) {
			
		}
		
		private void removeRuntimeClasspath(IRuntime runtime) {
			if (runtime == null) {
				return;
			}
			RuntimeKey key = getRuntimeKey(runtime);
			if (key != null) {
				runtimeClasspaths.remove(key);
			}
		}
	};
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
		runtimeClasspaths = new HashMap<RuntimeKey, IClasspathEntry[]>();
		ServerCore.addRuntimeLifecycleListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		runtimeClasspaths = null;
		ServerCore.removeRuntimeLifecycleListener(listener);
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

	public static Map<RuntimeKey, IClasspathEntry[]> getRuntimeClasspaths() {
		return runtimeClasspaths;
	}
	
	public static RuntimeKey getRuntimeKey(IRuntime runtime) {
		if( runtime == null ) 
			return null;

		IJBossServerRuntime jbsrt = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		IPath loc = runtime.getLocation();
		String rtID  = runtime.getRuntimeType().getId();
		IPath configPath = jbsrt == null ? null : jbsrt.getConfigurationFullPath();
		return new RuntimeKey(loc, configPath, rtID);
	}
}
