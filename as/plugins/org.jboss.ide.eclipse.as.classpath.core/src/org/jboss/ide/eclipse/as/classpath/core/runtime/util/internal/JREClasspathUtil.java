/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.util.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;

/**
 * A utility class for getting a classpath entry
 * relating to the vm/jre in use by the runtime.
 */
public class JREClasspathUtil {
	// Get a classpath container for the VM
	public static List<IClasspathEntry> getJavaClasspathEntries(String runtimeId) {
		if( runtimeId != null ) {
			IRuntime runtime = ServerCore.findRuntime(runtimeId);
			if( runtime != null ) {
				IJBossServerRuntime  jbsRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, null);
				IVMInstall vmInstall = jbsRuntime.getVM();
				if (vmInstall != null) {
					String name = vmInstall.getName();
					String typeId = vmInstall.getVMInstallType().getId();
					IClasspathEntry[] entries = new IClasspathEntry[] { 
							JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER)
								.append(typeId).append(name)) 
					};
					return Arrays.asList(entries);
				}
			}
		}
		return new ArrayList<IClasspathEntry>();
	}
}
