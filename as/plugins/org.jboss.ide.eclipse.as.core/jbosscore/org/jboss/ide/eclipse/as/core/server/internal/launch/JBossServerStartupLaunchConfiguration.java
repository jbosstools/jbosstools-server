/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.server.internal.launch;

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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;

public class JBossServerStartupLaunchConfiguration extends AbstractJBossLaunchConfigType {

	static final char[] INVALID_CHARS = new char[] {'\\', '/', ':', '*', '?', '"', '<', '>', '|', '\0', '@', '&'};
	static final String LAUNCH_TYPE = "org.jboss.ide.eclipse.as.core.server.startupConfiguration";
	static final String DEFAULTS_SET = "jboss.defaults.been.set";
	static final String START_JAR_LOC = "bin" + Path.SEPARATOR + "run.jar";
	static final String START_MAIN_TYPE = "org.jboss.Main";
	
	public static ILaunchConfigurationWorkingCopy setupLaunchConfiguration(IServer server, String action) throws CoreException {
		ILaunchConfigurationWorkingCopy config = createLaunchConfiguration(server);
		setupLaunchConfiguration(config, server);
		return config;
	}

	public static void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy, IServer server) throws CoreException {
		if(!workingCopy.getAttributes().containsKey(DEFAULTS_SET)) {
			forceDefaultsSet(workingCopy, server);
		}
		
		// Upgrade old launch configs
		JBossServer jbs = findJBossServer(server.getId());
		if( jbs == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Server " + server.getName() + " is not a proper JBoss Server"));

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
		String serverHome = getServerHome(jbs);
		if( serverHome == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Server " + jbs.getServer().getName() + " is corrupt and the server home is unable to be located."));
		
		IRuntime rt = jbs.getServer().getRuntime();
		IJBossServerRuntime jbrt = null;
		if( rt != null )
			jbrt = (IJBossServerRuntime)rt.getAdapter(IJBossServerRuntime.class);
		
		if( jbrt == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Runtime not found"));

		/* Args and vm args */
		
		String args = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
		String vmArgs = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
		String host = jbs.getServer().getHost();
		String host2 = ArgsUtil.getValue(args, "-b", "--host");
		if( !host.equals(host2))
			args = ArgsUtil.setArg(args, "-b", "--host", host);
		
		IJBossServerRuntime runtime = (IJBossServerRuntime)
			jbs.getServer().getRuntime().loadAdapter(IJBossServerRuntime.class, null);
		IVMInstall vmInstall = runtime.getVM();
		String config = runtime.getJBossConfiguration();
		args = ArgsUtil.setArg(args, "-c", "--configuration", config);

		vmArgs= ArgsUtil.setArg(vmArgs, null, "-Djava.endorsed.dirs", 
				"\"" + runtime.getRuntime().getLocation().append("lib").append("endorsed") + "\"", false);
		
		if( runtime.getRuntime().getLocation().append("bin").append("native").toFile().exists() ) 
			vmArgs = ArgsUtil.setArg(vmArgs, null, "-Djava.library.path",  
				"\"" + runtime.getRuntime().getLocation().append("bin").append("native") + "\"", false);
		
		/* Claspath */
		List<String> cp = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, new ArrayList<String>());
		List<String> newCP = fixCP(cp, jbs);
		
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, JavaRuntime.newJREContainerPath(vmInstall).toPortableString());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, serverHome + Path.SEPARATOR + "bin");
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgs);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, newCP);
		wc.setAttribute(SERVER_ID,jbs.getServer().getId());

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
		JBossServer jbs = findJBossServer(server.getId());
		if( jbs == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Server " + server.getName() + " is not a proper JBoss Server"));
		
		String serverHome = getServerHome(jbs);
		if( serverHome == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Server " + server.getName() + " is corrupt and the server home is unable to be located."));
		
		IRuntime rt = jbs.getServer().getRuntime();
		String jrePath = null;
		IJBossServerRuntime jbrt = null;
		if( rt != null ) {
			jbrt = (IJBossServerRuntime)rt.getAdapter(IJBossServerRuntime.class);
		}
		
		if( jbrt == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Runtime not found"));
			
		IVMInstall install = jbrt.getVM();
		IPath path = JavaRuntime.newJREContainerPath(install);
		jrePath = path.toPortableString();

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getDefaultArgs(jbs));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, jbrt.getDefaultRunVMArgs());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, START_MAIN_TYPE);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, serverHome + Path.SEPARATOR + "bin");
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, jrePath);
		wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, jbrt.getDefaultRunEnvVars());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, getClasspath(jbs));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, DEFAULT_CP_PROVIDER_ID);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);

		wc.setAttribute(DEFAULTS_SET, true);
	}

	protected static ArrayList<String> getClasspath(JBossServer jbs) throws CoreException {
		IJBossServerRuntime jbrt = findJBossServerRuntime(jbs.getServer());
		ArrayList<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		classpath.add(getRunJarRuntimeCPEntry(jbs));
		addJREEntry(classpath, jbrt.getVM());
		
		String version = jbs.getServer().getRuntime().getRuntimeType().getVersion();
		if( version.equals("4.0")) 
			addToolsJar(classpath, jbrt.getVM());
		
		ArrayList<String> runtimeClassPaths = convertClasspath(classpath);
		return runtimeClassPaths;

	}
	
	protected static IRuntimeClasspathEntry getRunJarRuntimeCPEntry(JBossServer jbs) throws CoreException {
			IPath containerPath = new Path(RunJarContainerWrapper.ID).append(jbs.getServer().getName());
			return JavaRuntime.newRuntimeContainerClasspathEntry(containerPath, IRuntimeClasspathEntry.USER_CLASSES);
	}
	
	protected static String getDefaultArgs(JBossServer jbs) throws CoreException {
		IJBossServerRuntime rt = findJBossServerRuntime(jbs.getServer());
		if (rt != null) {
			return rt.getDefaultRunArgs() + " -b " + jbs.getServer().getHost();
		}
		return null;
	}

	protected void preLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) {
		try {
			JBossServerBehavior jbsBehavior = getServerBehavior(configuration);
			jbsBehavior.setRunMode(mode);
			jbsBehavior.serverStarting();
		} catch( CoreException ce ) {
			// report it
		}
	}

	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) {
		try {
			IProcess[] processes = launch.getProcesses(); 
			JBossServerBehavior jbsBehavior = getServerBehavior(configuration);
			jbsBehavior.setProcess(processes[0]);
		} catch( CoreException ce ) {
			// report
		}
	}
	
	public static JBossServerBehavior getServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		JBossServerBehavior jbossServerBehavior = (JBossServerBehavior) server.getAdapter(JBossServerBehavior.class);
		return jbossServerBehavior;
	}
	
	protected static String getValidLaunchConfigurationName(String s) {
		if (s == null || s.length() == 0)
			return "1";
		int size = INVALID_CHARS.length;
		for (int i = 0; i < size; i++) {
			s = s.replace(INVALID_CHARS[i], '_');
		}
		return s;
	}

	/**
	 * Will create a launch configuration for the server 
	 * if one does not already exist. 
	 */
	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IServer server) throws CoreException {
		ILaunchConfigurationType launchConfigType = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(LAUNCH_TYPE);
		if (launchConfigType == null)
			return null;
		
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfiguration[] launchConfigs = null;
		try {
			launchConfigs = launchManager.getLaunchConfigurations(launchConfigType);
		} catch (CoreException e) {
			// ignore
		}
		
		if (launchConfigs != null) {
			int size = launchConfigs.length;
			for (int i = 0; i < size; i++) {
				try {
					String serverId = launchConfigs[i].getAttribute(SERVER_ID, (String) null);
					if (server.getId().equals(serverId)) {
						ILaunchConfigurationWorkingCopy wc = launchConfigs[i].getWorkingCopy();
						return wc;
					}
				} catch (CoreException e) {
				}
			}
		}
		
		// create a new launch configuration
		String launchName = getValidLaunchConfigurationName(server.getName());
		launchName = launchManager.generateUniqueLaunchConfigurationNameFrom(launchName); 
		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, launchName);
		wc.setAttribute(SERVER_ID, server.getId());
		return wc;
	}	
	
	/* For "restore defaults" functionality */
	private static final String DEFAULT_CP_PROVIDER_ID = "org.jboss.ide.eclipse.as.core.server.internal.launch.serverClasspathProvider";
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
				String server = config.getAttribute(SERVER_ID, (String)null);
				IServer s = ServerCore.findServer(server);
				IJBossServerRuntime ibjsrt = (IJBossServerRuntime)s.getRuntime().loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
				JBossServer jbs = (JBossServer)s.loadAdapter(JBossServer.class, new NullProgressMonitor());
				IVMInstall install = ibjsrt.getVM();
				ArrayList<IRuntimeClasspathEntry> list = new ArrayList<IRuntimeClasspathEntry>();
				addJREEntry(list, install);
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
}
