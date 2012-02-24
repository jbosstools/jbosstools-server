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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.AbstractStartLaunchConfigurator;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;

public class LocalJBoss7StartConfigurator extends AbstractStartLaunchConfigurator {

	public LocalJBoss7StartConfigurator(IServer server) throws CoreException {
		super(server);
	}
	
	private JBoss7LaunchConfigProperties properties = null;
	
	@Override
	protected JBoss7LaunchConfigProperties getProperties() {
		if( properties == null )
			properties = createProperties();
		return properties;
	}
	@Override
	protected JBoss7LaunchConfigProperties createProperties() {
		return new JBoss7LaunchConfigProperties();
	}


	
	@Override
	protected void doOverrides(ILaunchConfigurationWorkingCopy launchConfig, JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException {
		getProperties().setHost(getHost(jbossServer, jbossRuntime), launchConfig);
		getProperties().setServerHome(getServerHome(jbossRuntime), jbossRuntime, launchConfig);
		getProperties().setJreContainer(getJreContainerPath(jbossRuntime), launchConfig);
		getProperties().setEndorsedDir(getEndorsedDir(jbossRuntime), launchConfig);
		getProperties().setJavaLibPath(getJavaLibraryPath(jbossRuntime), launchConfig);
		getProperties().setWorkingDirectory(getWorkingDirectory(jbossServer, jbossRuntime), launchConfig);
		getProperties().setClasspathProvider(getClasspathProvider(), launchConfig);
		getProperties().setClasspath(getClasspath(jbossServer, jbossRuntime, getProperties().getClasspath(launchConfig)), launchConfig);
		getProperties().setUseDefaultClassPath(isUseDefaultClasspath(), launchConfig);
		getProperties().setServerId(getServerId(jbossServer), launchConfig);
		getProperties().setModulesFolder(getModulesFolder(jbossServer, jbossRuntime), launchConfig);
		getProperties().setConfigurationFile(getServerConfigFile(jbossServer, jbossRuntime), launchConfig);
		getProperties().setBootLogFile(getBootLogPath(jbossRuntime), launchConfig);
		getProperties().setLoggingConfigFile(getLoggingConfigPath(jbossRuntime), launchConfig);
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
	
	protected String getModulesFolder(JBossServer server, IJBossServerRuntime runtime)  throws CoreException {
		return runtime.getRuntime().getLocation().append(IJBossRuntimeConstants.MODULES).toString();
	}
	
	protected String getServerConfigFile(JBossServer server, IJBossServerRuntime runtime)  throws CoreException {
		LocalJBoss7ServerRuntime rt = (LocalJBoss7ServerRuntime)runtime.getRuntime().loadAdapter(LocalJBoss7ServerRuntime.class, null);
		return rt.getConfigurationFile();
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
		if( LaunchCommandPreferences.listensOnAllHosts(server.getServer()))
			return "0.0.0.0"; //$NON-NLS-1$
		return server.getHost(); 
	}

	@Override
	protected String getDefaultProgramArguments(JBossServer server, IJBossServerRuntime runtime) {
		return runtime.getDefaultRunArgs();
	}

	@Override
	protected String getServerHome(IJBossServerRuntime runtime) {
		return runtime.getRuntime().getLocation().toString();
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

	@Override
	protected String getJavaLibraryPath(IJBossServerRuntime runtime) {
		// Intentionally empty
		return null;
	}
	
	protected String getBootLogPath(IJBossServerRuntime runtime) {
		IPath serverHome = runtime.getRuntime().getLocation();
		IJBossRuntimeResourceConstants c = new IJBossRuntimeResourceConstants() {};
		IPath bootLog = serverHome.append(c.AS7_STANDALONE).append(c.FOLDER_LOG).append(c.AS7_BOOT_LOG);
		return bootLog.toString();
	}
	
	protected String getLoggingConfigPath(IJBossServerRuntime runtime) {
		IJBossRuntimeResourceConstants c = new IJBossRuntimeResourceConstants() {};
		IPath serverHome = runtime.getRuntime().getLocation();
		IPath logConfigPath = serverHome.append(c.AS7_STANDALONE).append(c.CONFIGURATION).append(c.LOGGING_PROPERTIES);
		try {
			return logConfigPath.toFile().toURI().toURL().toString();
		} catch (MalformedURLException murle) {
			return null;
		}
	}


}