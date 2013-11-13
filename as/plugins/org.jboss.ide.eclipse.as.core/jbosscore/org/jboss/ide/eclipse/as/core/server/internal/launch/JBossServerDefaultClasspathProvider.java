/******************************************************************************* 
 * Copyright (c) 2007, 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.StandardClasspathProvider;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractLocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;

/**
 * This class provides a default classpath for server launches. 
 * This is useful for when users request defaults to be set again.
 */
public class JBossServerDefaultClasspathProvider extends StandardClasspathProvider {
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		if (new JBossLaunchConfigProperties().isUseDefaultClasspath(configuration)) {
			return defaultEntries(configuration);
		}
		return super.computeUnresolvedClasspath(configuration);
	}

	protected IRuntimeClasspathEntry[] defaultEntries(ILaunchConfiguration config) {
		try {
			String server = new JBossLaunchConfigProperties().getServerId(config);
			IServer s = ServerCore.findServer(server);
			AbstractLocalJBossServerRuntime ibjsrt = (AbstractLocalJBossServerRuntime)
					s.getRuntime().loadAdapter(AbstractLocalJBossServerRuntime.class, new NullProgressMonitor());
			IVMInstall install = ibjsrt.getVM();
			ArrayList<IRuntimeClasspathEntry> list = new ArrayList<IRuntimeClasspathEntry>();
			LaunchConfigUtils.addJREEntry(install, list);
			list.add(LaunchConfigUtils.getRunJarRuntimeCPEntry(s));
			return (IRuntimeClasspathEntry[]) list
					.toArray(new IRuntimeClasspathEntry[list.size()]);
		} catch (CoreException ce) {
			JBossServerCorePlugin.log(ce.getStatus());
		}

		try {
			return super.computeUnresolvedClasspath(config);
		} catch (CoreException ce) {
			JBossServerCorePlugin.log(ce.getStatus());
		}
		return new IRuntimeClasspathEntry[] {};
	}
}