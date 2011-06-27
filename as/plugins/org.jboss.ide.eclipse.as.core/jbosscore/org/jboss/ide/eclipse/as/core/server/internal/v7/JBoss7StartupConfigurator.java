/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.AbstractStartupConfigurator;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;

public class JBoss7StartupConfigurator extends AbstractStartupConfigurator {

	public JBoss7StartupConfigurator(IServer server) throws CoreException {
		super(server);
	}

	@Override
	protected String getMainType() {
		return IJBossRuntimeConstants.START7_MAIN_TYPE;
	}

	@Override
	protected String getWorkingDirectory(JBossServer server, IJBossServerRuntime runtime)  throws CoreException {
		return runtime.getRuntime().getLocation()
				.append(IJBossRuntimeResourceConstants.BIN)
				.toString();
	}
	
	@Override
	protected List<String> getClasspath(JBossServer server, IJBossServerRuntime runtime, List<String> currentClasspath) throws CoreException {
		IVMInstall vmInstall = runtime.getVM();
		List<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		classpath.add(LaunchConfigUtils.getModulesClasspathEntry(server));
		LaunchConfigUtils.addJREEntry(vmInstall, classpath);
		List<String> runtimeClassPaths = LaunchConfigUtils.toStrings(classpath);
		return runtimeClassPaths;
	}

	@Override
	protected String getHost(JBossServer server, IJBossServerRuntime runtime) {
		// not needed
		return null;
	}

	@Override
	protected String getDefaultProgramArguments(JBossServer server, IJBossServerRuntime runtime) {
		return runtime.getDefaultRunArgs();
	}

	@Override
	protected String getServerHome(IJBossServerRuntime runtime) {
		// not needed
		return null;
	}

	@Override
	protected String getServerConfig(IJBossServerRuntime runtime) {
		// not needed
		return null;
	}

	@Override
	protected String getEndorsedDir(IJBossServerRuntime runtime) {
		// not needed
		return null;
	}

	@Override
	protected String getDefaultVMArguments(IJBossServerRuntime runtime) {
		return runtime.getDefaultRunVMArgs();
	}
}