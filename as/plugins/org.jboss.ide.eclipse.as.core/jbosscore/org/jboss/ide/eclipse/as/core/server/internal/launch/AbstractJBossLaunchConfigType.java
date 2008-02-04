package org.jboss.ide.eclipse.as.core.server.internal.launch;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public abstract class AbstractJBossLaunchConfigType extends AbstractJavaLaunchConfigurationDelegate {
	public static final String SERVER_ID = "server-id";

	// we have no need to do anything in pre-launch check
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return true;
	}

	protected void preLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) {
		// override me
	}
	protected void postLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) {
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
		
		// Create VM config
		String mainType = getMainTypeName(configuration);
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainType, classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);

		// Bootpath
		String[] bootpath = getBootpath(configuration);
		if (bootpath != null && bootpath.length > 0)
			runConfig.setBootClassPath(bootpath);
		
		setDefaultSourceLocator(launch, configuration);
		
		// Launch the configuration
		runner.run(runConfig, launch, monitor);
	}

	
	protected static JBossServer findJBossServer(String serverId) throws CoreException {
		if( serverId == null ) 
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "No server specified"));

		IServer s = ServerCore.findServer(serverId);
		if( s == null ) 
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Server Not Found"));

		JBossServer jbs = ServerConverter.getJBossServer(s);
		if( jbs == null ) 
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Server Not Found"));
		
		return jbs;
	}
	
	protected static IJBossServerRuntime findJBossServerRuntime(IServer server) throws CoreException {
		IRuntime rt = server.getRuntime();
		IJBossServerRuntime jbrt = null;
		if( rt != null ) 
			jbrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		if( jbrt == null ) 
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Runtime Not Found"));
		return jbrt;
	}
	
	protected static void addCPEntry(ArrayList<IRuntimeClasspathEntry> list, JBossServer jbs, String relative) {
		list.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(getServerHome(jbs)).append(relative)));
	}
	
	@Deprecated
	protected static ArrayList<String> convertClasspath(ArrayList<IRuntimeClasspathEntry> cp, IVMInstall vmInstall) {
		addJREEntry(cp, vmInstall);
		return convertClasspath(cp);
	}

	protected static void addJREEntry(ArrayList<IRuntimeClasspathEntry> cp, IVMInstall vmInstall) {
		if (vmInstall != null) {
			try {
				cp.add(JavaRuntime.newRuntimeContainerClasspathEntry(
					new Path(JavaRuntime.JRE_CONTAINER)
						.append(vmInstall.getVMInstallType().getId()).append(vmInstall.getName()),
//						"org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType")
						IRuntimeClasspathEntry.BOOTSTRAP_CLASSES));
			} catch (CoreException e) {
				IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
						"Unexpected Exception converting launch classpath: ", e);
				JBossServerCorePlugin.getDefault().getLog().log(s);
			}
		}
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
				return (name != null && name.endsWith("jar"));
			}
		});

		if (libs == null)
			return;

		for (int i = 0; i < libs.length; i++) {
			classpath.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(
					libPath + File.separator + libs[i].getName())));
		}
	} // end method

	
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
