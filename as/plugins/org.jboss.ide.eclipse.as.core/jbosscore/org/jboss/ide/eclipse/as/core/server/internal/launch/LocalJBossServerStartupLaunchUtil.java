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
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractLocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossBehaviorDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.JBossServerStartupLaunchConfiguration.IStartLaunchSetupParticipant;
import org.jboss.ide.eclipse.as.core.server.internal.launch.JBossServerStartupLaunchConfiguration.StartLaunchDelegate;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchConfigUtils;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

/**
 * @author Rob Stryker
 * @author André Dietisheim
 */
public class LocalJBossServerStartupLaunchUtil implements StartLaunchDelegate, IStartLaunchSetupParticipant {

	public static final String DEFAULTS_SET = "jboss.defaults.been.set"; //$NON-NLS-1$
	static final String START_JAR_LOC = IJBossRuntimeResourceConstants.BIN + Path.SEPARATOR
			+ IJBossRuntimeResourceConstants.START_JAR;
	static final String START_MAIN_TYPE = IJBossRuntimeConstants.START_MAIN_TYPE;

	public void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy, IServer server) throws CoreException {
		JBossServer jbs = ServerConverter.checkedGetJBossServer(server);
		if (!workingCopy.getAttributes().containsKey(DEFAULTS_SET)) {
			forceDefaultsSet(workingCopy, jbs);
		}

		upgradeOldLaunchConfig(workingCopy, jbs);

		// Force the launch to get certain fields from the runtime
		updateMandatedFields(workingCopy, jbs);
	}

	private void upgradeOldLaunchConfig(ILaunchConfigurationWorkingCopy workingCopy, JBossServer jbs)
			throws CoreException {
		String cpProvider = workingCopy.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, (String) null);
		if (!DEFAULT_CP_PROVIDER_ID.equals(cpProvider)) {
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, DEFAULT_CP_PROVIDER_ID);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, getClasspath(jbs));
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
		}
	}

	/*
	 * Ensures that the working directory and classpath are 100% accurate.
	 * Merges proper required params into args and vm args
	 */
	protected void updateMandatedFields(ILaunchConfigurationWorkingCopy wc, JBossServer jbs)
			throws CoreException {
		String serverHome = ServerUtil.getServerHome(jbs);
		IJBossServerRuntime runtime = RuntimeUtils.getJBossServerRuntime(jbs.getServer());

		updateVMPath(runtime, wc);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
				serverHome + Path.SEPARATOR + IJBossRuntimeResourceConstants.BIN);
		updateArguments(wc, jbs, runtime);
		updateVMArgs(wc, runtime);
		updateClassPath(wc, jbs);
		wc.setAttribute(AbstractJBossLaunchConfigType.SERVER_ID, jbs.getServer().getId());
	}

	private void updateVMPath(IJBossServerRuntime runtime, ILaunchConfigurationWorkingCopy wc) {
		IVMInstall vmInstall = runtime.getVM();
		if (vmInstall != null)
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
					JavaRuntime.newJREContainerPath(vmInstall).toPortableString());
	}

	private void updateClassPath(ILaunchConfigurationWorkingCopy wc, JBossServer jbs) throws CoreException {
		List<String> newCP = updateRunJarEntry(wc, jbs);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, newCP);
	}

	private void updateVMArgs(ILaunchConfigurationWorkingCopy wc, IJBossServerRuntime runtime) throws CoreException {
		String vmArgs = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$
		updateEndorsedDir(vmArgs, runtime);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgs.trim());
	}

	private void updateArguments(ILaunchConfigurationWorkingCopy wc, JBossServer jbs, IJBossServerRuntime runtime)
			throws CoreException {
		String args = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		String host = jbs.getServer().getHost();
		args = updateHostArgument(host, args);
		args = updateRuntimeArgument(args, runtime);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args.trim());
	}

	private void updateEndorsedDir(String vmArgs, IJBossServerRuntime runtime) {
		vmArgs = ArgsUtil.setArg(vmArgs, null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.ENDORSED_DIRS,
				runtime.getRuntime().getLocation().append(
						IJBossRuntimeResourceConstants.LIB).append(
						IJBossRuntimeResourceConstants.ENDORSED).toOSString(), true);
	}

	private String updateRuntimeArgument(String args, IJBossServerRuntime runtime) {
		String config = runtime.getJBossConfiguration();
		args = ArgsUtil.setArg(args,
				IJBossRuntimeConstants.STARTUP_ARG_CONFIG_SHORT,
				IJBossRuntimeConstants.STARTUP_ARG_CONFIG_LONG, config);

		try {
			if (!runtime.getConfigLocation().equals(IConstants.SERVER)) {
				args = ArgsUtil.setArg(args, null,
						IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_SERVER_HOME_URL,
						runtime.getConfigLocationFullPath().toFile().toURL().toString());
			}
		} catch (MalformedURLException murle) {
		}
		return args;
	}

	private String updateHostArgument(String host, String args) {
		String argsHost = ArgsUtil.getValue(args,
				IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
				IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG);

		if (!host.equals(host))
			args = ArgsUtil.setArg(args,
					IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
					IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG, host);
		return args;
	}

	protected List<String> updateRunJarEntry(ILaunchConfigurationWorkingCopy wc, JBossServer jbs) throws CoreException {
		List<String> cp = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, new ArrayList<String>());
		try {
			boolean found = false;
			String[] asString = (String[]) cp.toArray(new String[cp.size()]);
			for (int i = 0; i < asString.length; i++) {
				if (asString[i].contains(RunJarContainerWrapper.ID)) {
					found = true;
					asString[i] = LaunchConfigUtils.getRunJarRuntimeCPEntry(jbs.getServer()).getMemento();
				}
			}
			List<String> result = new ArrayList<String>();
			result.addAll(Arrays.asList(asString));
			if (!found)
				result.add(LaunchConfigUtils.getRunJarRuntimeCPEntry(jbs.getServer()).getMemento());
			return result;
		} catch (CoreException ce) {
			return cp;
		}
	}

	protected void forceDefaultsSet(ILaunchConfigurationWorkingCopy wc, JBossServer jbs) throws CoreException {
		String serverHome = ServerUtil.getServerHome(jbs);
		if (serverHome == null)
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(Messages.CannotLocateServerHome, jbs.getServer().getName())));

		IJBossServerRuntime jbrt = getJBossServerRuntime(jbs);
		if (jbrt == null)
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(Messages.ServerRuntimeNotFound, jbs.getServer().getName())));

		updateVMPath(jbrt, wc);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getDefaultArgs(jbs));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, jbrt.getDefaultRunVMArgs());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, START_MAIN_TYPE);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, serverHome + Path.SEPARATOR
				+ IJBossRuntimeResourceConstants.BIN);
		wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, jbrt.getDefaultRunEnvVars());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, getClasspath(jbs));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, DEFAULT_CP_PROVIDER_ID);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);

		wc.setAttribute(DEFAULTS_SET, true);
	}

	private IJBossServerRuntime getJBossServerRuntime(JBossServer jbs) throws CoreException {
		IRuntime rt = jbs.getServer().getRuntime();
		IJBossServerRuntime jbrt = null;
		if (rt != null) {
			jbrt = (IJBossServerRuntime) rt.getAdapter(IJBossServerRuntime.class);
		}

		return jbrt;
	}

	private List<String> getClasspath(JBossServer jbs) throws CoreException {
		IJBossServerRuntime jbrt = RuntimeUtils.getJBossServerRuntime(jbs.getServer());
		ArrayList<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		classpath.add(LaunchConfigUtils.getRunJarRuntimeCPEntry(jbs.getServer()));
		LaunchConfigUtils.addJREEntry(jbrt.getVM(), classpath);

		String version = jbs.getServer().getRuntime().getRuntimeType().getVersion();
		if (version.equals(IJBossToolingConstants.AS_40))
			LaunchConfigUtils.addToolsJar(jbrt.getVM(), classpath);

		List<String> runtimeClassPaths = LaunchConfigUtils.toStrings(classpath);
		return runtimeClassPaths;
	}

	protected String getDefaultArgs(JBossServer jbs) throws CoreException {
		IJBossServerRuntime rt = RuntimeUtils.getJBossServerRuntime(jbs.getServer());
		if (rt != null) {
			return rt.getDefaultRunArgs() +
					IJBossRuntimeConstants.SPACE + IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT +
					IJBossRuntimeConstants.SPACE + jbs.getServer().getHost();
		}
		return null;
	}

	/* For "restore defaults" functionality */
	private static final String DEFAULT_CP_PROVIDER_ID = "org.jboss.ide.eclipse.as.core.server.internal.launch.serverClasspathProvider"; //$NON-NLS-1$

	public static class JBossServerDefaultClasspathProvider extends StandardClasspathProvider {
		public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration)
				throws CoreException {
			boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
					true);
			if (useDefault) {
				return defaultEntries(configuration);
			}
			return super.computeUnresolvedClasspath(configuration);
		}

		protected IRuntimeClasspathEntry[] defaultEntries(ILaunchConfiguration config) {
			try {
				String server = config.getAttribute(AbstractJBossLaunchConfigType.SERVER_ID, (String) null);
				IServer s = ServerCore.findServer(server);
				AbstractLocalJBossServerRuntime ibjsrt = (AbstractLocalJBossServerRuntime)
						s.getRuntime().loadAdapter(AbstractLocalJBossServerRuntime.class, new NullProgressMonitor());
				JBossServer jbs = (JBossServer) s.loadAdapter(JBossServer.class, new NullProgressMonitor());
				IVMInstall install = ibjsrt.getVM();
				ArrayList<IRuntimeClasspathEntry> list = new ArrayList<IRuntimeClasspathEntry>();
				LaunchConfigUtils.addJREEntry(install, list);
				list.add(LaunchConfigUtils.getRunJarRuntimeCPEntry(s));
				return (IRuntimeClasspathEntry[]) list
						.toArray(new IRuntimeClasspathEntry[list.size()]);
			} catch (CoreException ce) {
				// ignore
			}

			try {
				return super.computeUnresolvedClasspath(config);
			} catch (CoreException ce) {
				// ignore
			}
			return new IRuntimeClasspathEntry[] {};
		}
	}

	/*
	 * Actual instance methods
	 */
	public void actualLaunch(
			JBossServerStartupLaunchConfiguration launchConfig,
			ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		launchConfig.superActualLaunch(configuration, mode, launch, monitor);
	}

	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		JBossServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
		if (!jbsBehavior.canStart(mode).isOK())
			throw new CoreException(jbsBehavior.canStart(mode));
		String ignore = jbsBehavior.getServer().getAttribute(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS,
				(String) null);
		Boolean ignoreB = ignore == null ? new Boolean(false) : new Boolean(ignore);
		if (ignoreB.booleanValue()) {
			jbsBehavior.setServerStarting();
			jbsBehavior.setServerStarted();
			return false;
		}
		boolean started = WebPortPoller.onePing(jbsBehavior.getServer());
		if (started) {
			jbsBehavior.setServerStarting();
			jbsBehavior.setServerStarted();
			return false;
		}

		return true;
	}

	public void preLaunch(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			JBossServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
			jbsBehavior.setRunMode(mode);
			jbsBehavior.serverStarting();
		} catch (CoreException ce) {
			// report it
		}
	}

	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			IProcess[] processes = launch.getProcesses();
			JBossServerBehavior jbsBehavior = JBossServerBehaviorUtils.getServerBehavior(configuration);
			((LocalJBossBehaviorDelegate) (jbsBehavior.getDelegate())).setProcess(processes[0]);
		} catch (CoreException ce) {
			// report
		}
	}

}
