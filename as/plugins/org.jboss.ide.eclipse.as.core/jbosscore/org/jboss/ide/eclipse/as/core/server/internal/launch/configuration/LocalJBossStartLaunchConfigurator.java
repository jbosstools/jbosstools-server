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
package org.jboss.ide.eclipse.as.core.server.internal.launch.configuration;

import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.BIN;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.NATIVE;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDefaultClasspathLaunchConfigurator;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.RunJarContainerWrapper;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class LocalJBossStartLaunchConfigurator extends AbstractStartLaunchConfigurator implements IDefaultClasspathLaunchConfigurator {

	public LocalJBossStartLaunchConfigurator(IServer server) throws CoreException {
		super(server);
	}

	@Override
	protected String getMainType() {
		return  IJBossRuntimeConstants.START_MAIN_TYPE;
	}

	@Override
	protected String getWorkingDirectory() throws CoreException {
		return ServerUtil.checkedGetServerHome(getJBossServer()) 
				+ Path.SEPARATOR 
				+ IJBossRuntimeResourceConstants.BIN;
	}
	

	@Override
	public IRuntimeClasspathEntry[] getDefaultClasspathEntries(ILaunchConfiguration config) throws CoreException {
		IVMInstall vmInstall = getJBossRuntime().getVM();
		IRuntimeClasspathEntry modulesEntry = LaunchConfigUtils.getRunJarRuntimeCPEntry(server); 
		IRuntimeClasspathEntry jreEntry = LaunchConfigUtils.getJREEntry(vmInstall);
		return new IRuntimeClasspathEntry[]{jreEntry, modulesEntry};
	}
	
	@Override
	protected List<String> getClasspath(List<String> currentClasspath) throws CoreException {
		try {
			boolean replaced = replaceRunJarContainer(getJBossServer(), currentClasspath);
			if (!replaced) {
				String runJarEntry = LaunchConfigUtils.getRunJarRuntimeCPEntry(server).getMemento();
				currentClasspath.add(runJarEntry);
			}
			IVMInstall vmInstall = getJBossRuntime().getVM();
			List<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
			LaunchConfigUtils.addJREEntry(vmInstall, classpath);
			List<String> runtimeClassPaths = LaunchConfigUtils.toStrings(classpath);
			if (runtimeClassPaths != null && runtimeClassPaths.size() == 1) {
				String jreEntry = runtimeClassPaths.get(0);
				if (!currentClasspath.contains(jreEntry)) {
					currentClasspath.add(jreEntry);
				}
			}
			return currentClasspath;
		} catch (CoreException ce) {
			return currentClasspath;
		}
	}

	private boolean replaceRunJarContainer(JBossServer server, List<String> classpath)
			throws CoreException {
		boolean replaced = false;
		for (int i = 0; i < classpath.size(); i++) {
			String classPathEntry = classpath.get(0);
			if (classPathEntry.contains(RunJarContainerWrapper.ID)) {
				replaced = true;
				classpath.set(i, LaunchConfigUtils.getRunJarRuntimeCPEntry(server.getServer()).getMemento());
			}
		}
		return replaced;
	}

	@Override
	protected String getHost(JBossServer server, IJBossServerRuntime runtime) {
		if( LaunchCommandPreferences.listensOnAllHosts(server.getServer()))
			return "0.0.0.0"; //$NON-NLS-1$
		return server.getHost(); 
	}

	@Override
	protected String getServerHome(IJBossServerRuntime runtime) {
		try {
			return runtime.getConfigLocationFullPath().toFile().toURI().toURL().toString();
		} catch (MalformedURLException murle) {
			return null;
		}
	}

	@Override
	protected String getServerConfig(IJBossServerRuntime runtime) {
		return runtime.getJBossConfiguration();
	}

	@Override
	protected String getEndorsedDir(IJBossServerRuntime runtime) {
		return runtime.getRuntime().getLocation().append(
				IJBossRuntimeResourceConstants.LIB).append(
				IJBossRuntimeResourceConstants.ENDORSED).toOSString();
	}

	@Override
	protected String getDefaultProgramArguments() {
		return getExtendedProperties().getDefaultLaunchArguments().getStartDefaultProgramArgs();
	}

	@Override
	protected String getDefaultVMArguments(IJBossServerRuntime runtime) {
		return getExtendedProperties().getDefaultLaunchArguments().getStartDefaultVMArgs();
	}

	@Override
	protected String getJavaLibraryPath(IJBossServerRuntime runtime) {
		IPath serverHome = runtime.getRuntime().getLocation();
		IPath p = serverHome.append(BIN).append(NATIVE);
		if( p.toFile().exists() ) 
			return p.toString();
		return null;
	}
}