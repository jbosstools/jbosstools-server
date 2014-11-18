/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.internal.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;

public class RuntimeClasspathCache implements IRuntimeLifecycleListener {
	private static RuntimeClasspathCache instance = null;
	public static RuntimeClasspathCache getInstance() {
		if( instance == null )
			instance = new RuntimeClasspathCache();
		return instance;
	}
	
	
	private Map<RuntimeKey, IClasspathEntry[]> runtimeClasspaths;
	
	RuntimeClasspathCache() {
		runtimeClasspaths = new HashMap<RuntimeKey, IClasspathEntry[]>();
	}
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
	
	private Map<RuntimeKey, IClasspathEntry[]> getRuntimeClasspaths() {
		return runtimeClasspaths;
	}
	
	public IClasspathEntry[] getEntries(IRuntime rt) {
		return getRuntimeClasspaths().get(getRuntimeKey(rt));
	}
	
	public void cacheEntries(IRuntime rt, IClasspathEntry[] entries) {
		getRuntimeClasspaths().put(getRuntimeKey(rt), entries);
	}
	
	static RuntimeKey getRuntimeKey(IRuntime runtime) {
		if( runtime == null ) {
			logError(runtime);
			return null;
		}

		IJBossServerRuntime jbsrt = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		if( jbsrt == null ) {
			return null;
		}
		IPath loc = runtime.getLocation();
		String rtID  = runtime.getRuntimeType().getId();
		IPath configPath = jbsrt == null ? null : jbsrt.getConfigurationFullPath();
		return new RuntimeKey(loc, configPath, rtID);
	}

	private static void logError(IRuntime runtime) {
		// log error
		IStatus status = new Status(IStatus.WARNING, ClasspathCorePlugin.PLUGIN_ID, MessageFormat.format(Messages.ClientAllRuntimeClasspathProvider_wrong_runtime_type,
				runtime == null ? null : runtime.getName()));
		ClasspathCorePlugin.getDefault().getLog().log(status);
	}
}
