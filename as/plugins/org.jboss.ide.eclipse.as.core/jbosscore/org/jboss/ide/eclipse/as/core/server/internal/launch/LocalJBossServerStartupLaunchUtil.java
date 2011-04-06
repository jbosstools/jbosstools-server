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

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossBehaviorDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.JBossServerStartupLaunchConfiguration.IStartLaunchSetupParticipant;
import org.jboss.ide.eclipse.as.core.server.internal.launch.JBossServerStartupLaunchConfiguration.StartLaunchDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.RunJarContainerWrapper.RunJarContainer;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class LocalJBossServerStartupLaunchUtil implements StartLaunchDelegate, IStartLaunchSetupParticipant {

	static final String DEFAULTS_SET = "jboss.defaults.been.set"; //$NON-NLS-1$
	static final String START_JAR_LOC = IJBossRuntimeResourceConstants.BIN + Path.SEPARATOR + IJBossRuntimeResourceConstants.START_JAR;
	static final String START_MAIN_TYPE = IJBossRuntimeConstants.START_MAIN_TYPE;
	
	public void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy, IServer server) throws CoreException {
		if(!workingCopy.getAttributes().containsKey(DEFAULTS_SET)) {
			forceDefaultsSet(workingCopy, server);
		}
		
		// Upgrade old launch configs
		JBossServer jbs = AbstractJBossLaunchConfigType.findJBossServer(server.getId());
		if( jbs == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind(Messages.CannotSetUpImproperServer, server.getName())));

		String cpProvider = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, (String)null);
		if(  !DEFAULT_CP_PROVIDER_ID.equals(cpProvider)) {
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, DEFAULT_CP_PROVIDER_ID);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, getClasspath(jbs));
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
		}
		
		// Force the launch to get certain fields from the runtime
		updateMandatedFields(workingCopy, jbs);
	}

	/*
	 * Ensures that the working directory and classpath are 100% accurate.
	 * Merges proper required params into args and vm args
	 */
	
	protected static void updateMandatedFields(ILaunchConfigurationWorkingCopy wc, JBossServer jbs) throws CoreException{
		String serverHome = AbstractJBossLaunchConfigType.getServerHome(jbs);
		if( serverHome == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind(Messages.CannotLocateServerHome, jbs.getServer().getName())));
		
		IRuntime rt = jbs.getServer().getRuntime();
		IJBossServerRuntime jbrt = null;
		if( rt != null )
			jbrt = (IJBossServerRuntime)rt.getAdapter(IJBossServerRuntime.class);
		
		if( jbrt == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind(Messages.ServerRuntimeNotFound, jbs.getServer().getName())));

		/* Args and vm args */
		
		String args = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		String vmArgs = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$
		String host = jbs.getServer().getHost();
		String host2 = ArgsUtil.getValue(args, 
				IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT, 
				IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG);
		if( !host.equals(host2))
			args = ArgsUtil.setArg(args, 
					IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT, 
					IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG, host);

		IJBossServerRuntime runtime = (IJBossServerRuntime)
			jbs.getServer().getRuntime().loadAdapter(IJBossServerRuntime.class, null);
		String config = runtime.getJBossConfiguration();
		args = ArgsUtil.setArg(args, 
				IJBossRuntimeConstants.STARTUP_ARG_CONFIG_SHORT, 
				IJBossRuntimeConstants.STARTUP_ARG_CONFIG_LONG, config);		
		
		try {
			if( !runtime.getConfigLocation().equals(IConstants.SERVER)) {
				args = ArgsUtil.setArg(args, null, 
						IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_SERVER_HOME_URL,
						runtime.getConfigLocationFullPath().toFile().toURL().toString());
			}
		} catch( MalformedURLException murle) {}

		vmArgs= ArgsUtil.setArg(vmArgs, null, 
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.ENDORSED_DIRS,
				runtime.getRuntime().getLocation().append(
						IJBossRuntimeResourceConstants.LIB).append(
								IJBossRuntimeResourceConstants.ENDORSED).toOSString(), true);
		/* Claspath */
		List<String> cp = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, new ArrayList<String>());
		List<String> newCP = fixCP(cp, jbs);
		
		IVMInstall vmInstall = runtime.getVM();
		if( vmInstall != null ) 
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, JavaRuntime.newJREContainerPath(vmInstall).toPortableString());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, serverHome + Path.SEPARATOR + IJBossRuntimeResourceConstants.BIN);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args.trim());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgs.trim());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, newCP);
		wc.setAttribute(AbstractJBossLaunchConfigType.SERVER_ID,jbs.getServer().getId());

	}

	protected static List<String> fixCP(List<String> list, JBossServer jbs) {
		try {
			boolean found = false;
			String[] asString = (String[]) list.toArray(new String[list.size()]);
			for( int i = 0; i < asString.length; i++ ) {
				if( asString[i].contains(RunJarContainerWrapper.ID)) {
					found = true;
					asString[i] = getRunJarRuntimeCPEntry(jbs).getMemento();
				}
			}
			ArrayList<String> result = new ArrayList<String>();
			result.addAll(Arrays.asList(asString));
			if( !found )
				result.add(getRunJarRuntimeCPEntry(jbs).getMemento());
			return result;
		} catch( CoreException ce) {
			return list;
		}
	}
	
	protected static void forceDefaultsSet(ILaunchConfigurationWorkingCopy wc, IServer server) throws CoreException {
		JBossServer jbs = AbstractJBossLaunchConfigType.findJBossServer(server.getId());
		if( jbs == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind(Messages.CannotSetUpImproperServer, server.getName())));
		
		String serverHome = AbstractJBossLaunchConfigType.getServerHome(jbs);
		if( serverHome == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind(Messages.CannotLocateServerHome, server.getName())));
		
		IRuntime rt = jbs.getServer().getRuntime();
		IJBossServerRuntime jbrt = null;
		if( rt != null ) {
			jbrt = (IJBossServerRuntime)rt.getAdapter(IJBossServerRuntime.class);
		}
		
		if( jbrt == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
			NLS.bind(Messages.ServerRuntimeNotFound, jbs.getServer().getName())));		
			
		IVMInstall vmInstall = jbrt.getVM();
		if( vmInstall != null ) 
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, JavaRuntime.newJREContainerPath(vmInstall).toPortableString());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getDefaultArgs(jbs));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, jbrt.getDefaultRunVMArgs());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, START_MAIN_TYPE);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, serverHome + Path.SEPARATOR + IJBossRuntimeResourceConstants.BIN);
		wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, jbrt.getDefaultRunEnvVars());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, getClasspath(jbs));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, DEFAULT_CP_PROVIDER_ID);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);

		wc.setAttribute(DEFAULTS_SET, true);
	}

	protected static ArrayList<String> getClasspath(JBossServer jbs) throws CoreException {
		IJBossServerRuntime jbrt = AbstractJBossLaunchConfigType.findJBossServerRuntime(jbs.getServer());
		ArrayList<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		classpath.add(getRunJarRuntimeCPEntry(jbs));
		AbstractJBossLaunchConfigType.addJREEntry(classpath, jbrt.getVM());
		
		String version = jbs.getServer().getRuntime().getRuntimeType().getVersion();
		if( version.equals(IJBossToolingConstants.AS_40)) 
			AbstractJBossLaunchConfigType.addToolsJar(classpath, jbrt.getVM());
		
		ArrayList<String> runtimeClassPaths = AbstractJBossLaunchConfigType.convertClasspath(classpath);
		return runtimeClassPaths;

	}
	
	protected static IRuntimeClasspathEntry getRunJarRuntimeCPEntry(JBossServer jbs) throws CoreException {
			IPath containerPath = new Path(RunJarContainerWrapper.ID).append(jbs.getServer().getName());
			return JavaRuntime.newRuntimeContainerClasspathEntry(containerPath, IRuntimeClasspathEntry.USER_CLASSES);
	}
	
	protected static String getDefaultArgs(JBossServer jbs) throws CoreException {
		IJBossServerRuntime rt = AbstractJBossLaunchConfigType.findJBossServerRuntime(jbs.getServer());
		if (rt != null) {
			return rt.getDefaultRunArgs() + 
			IJBossRuntimeConstants.SPACE + IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT + 
			IJBossRuntimeConstants.SPACE + jbs.getServer().getHost();
		}
		return null;
	}

	
	public static JBossServerBehavior getServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		JBossServerBehavior jbossServerBehavior = (JBossServerBehavior) server.getAdapter(JBossServerBehavior.class);
		return jbossServerBehavior;
	}
	
	/* For "restore defaults" functionality */
	private static final String DEFAULT_CP_PROVIDER_ID = "org.jboss.ide.eclipse.as.core.server.internal.launch.serverClasspathProvider"; //$NON-NLS-1$
	public static class JBossServerDefaultClasspathProvider extends StandardClasspathProvider {
		public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
			boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
			if (useDefault) {
				return defaultEntries(configuration);
			}
			return super.computeUnresolvedClasspath(configuration);
		}
		
		protected IRuntimeClasspathEntry[] defaultEntries(ILaunchConfiguration config) {
			try {
				String server = config.getAttribute(AbstractJBossLaunchConfigType.SERVER_ID, (String)null);
				IServer s = ServerCore.findServer(server);
				IJBossServerRuntime ibjsrt = (IJBossServerRuntime)s.getRuntime().loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
				JBossServer jbs = (JBossServer)s.loadAdapter(JBossServer.class, new NullProgressMonitor());
				IVMInstall install = ibjsrt.getVM();
				ArrayList<IRuntimeClasspathEntry> list = new ArrayList<IRuntimeClasspathEntry>();
				AbstractJBossLaunchConfigType.addJREEntry(list, install);
				list.add(getRunJarRuntimeCPEntry(jbs));
				return (IRuntimeClasspathEntry[]) list
						.toArray(new IRuntimeClasspathEntry[list.size()]);
			} catch( CoreException ce) {
			}
			try {
				return super.computeUnresolvedClasspath(config);
			} catch( CoreException ce ) {}
			return new IRuntimeClasspathEntry[]{};
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
	
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		JBossServerBehavior jbsBehavior = getServerBehavior(configuration);
		if( !jbsBehavior.canStart(mode).isOK())
			throw new CoreException(jbsBehavior.canStart(mode));
		return true;
	}

	public void preLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			JBossServerBehavior jbsBehavior = getServerBehavior(configuration);
			jbsBehavior.setRunMode(mode);
			jbsBehavior.serverStarting();
		} catch( CoreException ce ) {
			// report it
		}
	}

	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			IProcess[] processes = launch.getProcesses(); 
			JBossServerBehavior jbsBehavior = getServerBehavior(configuration);
			((LocalJBossBehaviorDelegate)(jbsBehavior.getDelegate())).setProcess(processes[0]);
		} catch( CoreException ce ) {
			// report
		}
	}

}
