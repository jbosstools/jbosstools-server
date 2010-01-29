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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jst.server.core.ServerProfilerDelegate;
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
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public abstract class AbstractJBossLaunchConfigType extends AbstractJavaLaunchConfigurationDelegate {
	public static final String SERVER_ID = "server-id"; //$NON-NLS-1$

	// we have no need to do anything in pre-launch check
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return true;
	}

	protected void preLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// override me
	}
	protected void postLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// override me
	}

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		preLaunch(configuration, mode, launch, monitor);
		actualLaunch(configuration, mode, launch, monitor);
		postLaunch(configuration, mode, launch, monitor);
	}
	
	protected void actualLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// And off we go!
		IVMInstall vm = verifyVMInstall(configuration);
		IVMRunner runner = vm.getVMRunner(mode);
		
		if(runner == null && ILaunchManager.PROFILE_MODE.equals(mode)){
			runner = vm.getVMRunner(ILaunchManager.RUN_MODE);
		}
		if(runner == null){
			throw new CoreException(new Status(IStatus.ERROR,JBossServerCorePlugin.PLUGIN_ID,0,Messages.runModeNotSupported,null));
		}
		
		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null)
			workingDirName = workingDir.getAbsolutePath();
		
		// Program & VM args
		String pgmArgs = getProgramArguments(configuration);
		String vmArgs = getVMArguments(configuration);
		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
		
		// VM-specific attributes
		Map vmAttributesMap = getVMSpecificAttributesMap(configuration);
		
		// Classpath
		String[] classpath = getClasspath(configuration);
		
		// Environment
		String[] environment = getEnvironment(configuration);
		
		// Create VM config
		String mainType = getMainTypeName(configuration);
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainType, classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);
		runConfig.setEnvironment(environment);

		// Bootpath
		String[] bootpath = getBootpath(configuration);
		if (bootpath != null && bootpath.length > 0)
			runConfig.setBootClassPath(bootpath);
		
		setDefaultSourceLocator(launch, configuration);
		
		if (ILaunchManager.PROFILE_MODE.equals(mode)) {
			try {
				ServerProfilerDelegate.configureProfiling(launch, vm, runConfig, monitor);
			} catch (CoreException ce) {
				IServer server = ServerUtil.getServer(configuration);
				JBossServerBehavior jbsb = (JBossServerBehavior) server.getAdapter(JBossServerBehavior.class);
				jbsb.stop(true);
				//genericServer.stopImpl();
				throw ce;
			}
		}
		// Launch the configuration
		runner.run(runConfig, launch, monitor);
	}

	
	protected static JBossServer findJBossServer(String serverId) throws CoreException {
		if( serverId == null ) 
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind(Messages.ServerNotFound, serverId)));

		IServer s = ServerCore.findServer(serverId);
		if( s == null ) 
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind(Messages.ServerNotFound, serverId)));

		JBossServer jbs = ServerConverter.getJBossServer(s);
		if( jbs == null ) 
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
			NLS.bind(Messages.ServerNotFound, serverId)));
		
		return jbs;
	}
	
	protected static IJBossServerRuntime findJBossServerRuntime(IServer server) throws CoreException {
		IRuntime rt = server.getRuntime();
		IJBossServerRuntime jbrt = null;
		if( rt != null ) 
			jbrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		if( jbrt == null ) 
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
			NLS.bind(Messages.ServerRuntimeNotFound, server.getName())));
		return jbrt;
	}
	
	protected static void addCPEntry(ArrayList<IRuntimeClasspathEntry> list, JBossServer jbs, String relative) {
		addCPEntry(list, new Path(getServerHome(jbs)).append(relative));
	}
	protected static void addCPEntry(ArrayList<IRuntimeClasspathEntry> list, IPath path) {
		list.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
	}
	
	protected static void addJREEntry(ArrayList<IRuntimeClasspathEntry> cp, IVMInstall vmInstall) {
		if (vmInstall != null) {
			try {
				cp.add(JavaRuntime.newRuntimeContainerClasspathEntry(
					new Path(JavaRuntime.JRE_CONTAINER)
						.append(vmInstall.getVMInstallType().getId()).append(vmInstall.getName()),
						IRuntimeClasspathEntry.BOOTSTRAP_CLASSES));
			} catch (CoreException e) {
				IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
						Messages.LaunchConfigJREError, e);
				JBossServerCorePlugin.getDefault().getLog().log(s);
			}
		}
	}
	
	protected static void addToolsJar(ArrayList<IRuntimeClasspathEntry> cp, IVMInstall vmInstall) {
		File f = vmInstall.getInstallLocation();
		File c1 = new File(f, IConstants.LIB);
		File c2 = new File(c1, IConstants.TOOLS_JAR);
		if( c2.exists()) 
			addCPEntry(cp, new Path(c2.getAbsolutePath()));
	}

	
	protected static ArrayList<String> convertClasspath(ArrayList<IRuntimeClasspathEntry> cp) {
		Iterator<IRuntimeClasspathEntry> cpi = cp.iterator();
		ArrayList<String> list = new ArrayList<String>();
		while (cpi.hasNext()) {
			IRuntimeClasspathEntry entry = cpi.next();
			try {
				list.add(entry.getMemento());
			} catch (Exception e) {
				// Trace.trace(Trace.SEVERE, "Could not resolve classpath entry:
				// " + entry, e);
			}
		}

		return list;
	}

	protected static void addDirectory(String serverHome, ArrayList<IRuntimeClasspathEntry> classpath,
			String dirName) {
		String libPath = serverHome + File.separator + dirName;
		File libDir = new File(libPath);
		File libs[] = libDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name != null && name.endsWith(IConstants.EXT_JAR));
			}
		});

		if (libs == null)
			return;

		for (int i = 0; i < libs.length; i++) {
			classpath.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(
					libPath + File.separator + libs[i].getName())));
		}
	}

	
	public static String getServerHome(JBossServer jbs) {
		return jbs.getServer().getRuntime().getLocation().toOSString();
	}
	
	public IVMInstall getVMInstall(ILaunchConfiguration configuration) throws CoreException {
		String serverId = configuration.getAttribute(SERVER_ID, (String)null);
		JBossServer jbs = findJBossServer(serverId);
		IJBossServerRuntime jbrt = findJBossServerRuntime(jbs.getServer());
		return jbrt.getVM();
	}

}
