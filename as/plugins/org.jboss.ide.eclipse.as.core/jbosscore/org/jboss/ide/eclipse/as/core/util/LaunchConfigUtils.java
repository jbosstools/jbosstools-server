package org.jboss.ide.eclipse.as.core.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.RunJarContainerWrapper;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;

/**
 * @author André Dietisheim
 */
public class LaunchConfigUtils {

	/**
	 * Adds an entry for the given vm install to the given list of classpath
	 * entries.
	 * 
	 * @param vmInstall
	 *            to add
	 * @param cp
	 *            the classpath entries to add to
	 */
	public static void addJREEntry(IVMInstall vmInstall, List<IRuntimeClasspathEntry> cp) {
		if (vmInstall != null) {
			try {
				String name = vmInstall.getName();
				String installTypeId = vmInstall.getVMInstallType().getId();
				cp.add(JavaRuntime.newRuntimeContainerClasspathEntry(
						new Path(JavaRuntime.JRE_CONTAINER).append(installTypeId).append(name),
						IRuntimeClasspathEntry.BOOTSTRAP_CLASSES));
			} catch (CoreException e) {
				IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
						Messages.LaunchConfigJREError, e);
				JBossServerCorePlugin.getDefault().getLog().log(s);
			}
		}
	}

	public static void addToolsJar(IVMInstall vmInstall, List<IRuntimeClasspathEntry> cp) {
		File f = vmInstall.getInstallLocation();
		File c1 = new File(f, IConstants.LIB);
		File c2 = new File(c1, IConstants.TOOLS_JAR);
		if (c2.exists())
			addCPEntry(new Path(c2.getAbsolutePath()), cp);
	}

	public static void addCPEntry(IPath path, List<IRuntimeClasspathEntry> list) {
		list.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
	}

	public static void addCPEntry(String serverHome, String relative, ArrayList<IRuntimeClasspathEntry> list) {
		addCPEntry(new Path(serverHome), relative, list);
	}

	public static void addCPEntry(IPath serverHome, String relative, ArrayList<IRuntimeClasspathEntry> list) {
		addCPEntry(serverHome.append(relative), list);
	}

	public static List<String> toStrings(List<IRuntimeClasspathEntry> cp) throws CoreException {
		Iterator<IRuntimeClasspathEntry> cpi = cp.iterator();
		ArrayList<String> list = new ArrayList<String>();
		while (cpi.hasNext()) {
			IRuntimeClasspathEntry entry = cpi.next();
				list.add(entry.getMemento());
		}

		return list;
	}
	
	public static String classpathUserClassesToString(ILaunchConfiguration config) throws CoreException {
		StringBuilder builder = new StringBuilder();
		List<String> classpath = new JBossLaunchConfigProperties().getClasspath(config);
		for(String entry : classpath) {
			IRuntimeClasspathEntry runtimeEntry = JavaRuntime.newRuntimeClasspathEntry(entry);
			int classpathProperty = runtimeEntry.getClasspathProperty();
			if (classpathProperty == IRuntimeClasspathEntry.USER_CLASSES) {
				builder.append(runtimeEntry.getLocation());
				builder.append(IJBossRuntimeConstants.SPACE);
			}
		}
		return builder.toString();
	}
	
	public static IRuntimeClasspathEntry getRunJarRuntimeCPEntry(IServer server) throws CoreException {
		// TODO: improve/avoid server version check
//		if (server.getServerType().getId().endsWith("70")) { //$NON-NLS-1$
//			return getModulesClasspathEntry(server);
//		} else {
			IPath containerPath = new Path(RunJarContainerWrapper.ID).append(server.getName());
			return JavaRuntime.newRuntimeContainerClasspathEntry(containerPath, IRuntimeClasspathEntry.USER_CLASSES);
//		}
	}

	public static IRuntimeClasspathEntry getModulesClasspathEntry(JBossServer server) throws CoreException {
		return getModulesClasspathEntry(server.getServer());
	}

	public static IRuntimeClasspathEntry getModulesClasspathEntry(IServer server) throws CoreException {
		return JavaRuntime.newArchiveRuntimeClasspathEntry(getModulesPath(server));
	}

	public static IPath getModulesPath(IServer server) throws CoreException {
		IPath runtimeLocation = server.getRuntime().getLocation();
		return runtimeLocation.append(IJBossRuntimeResourceConstants.JBOSS7_MODULES_JAR);
	}

	public static void addDirectory(String serverHome, List<IRuntimeClasspathEntry> classpath, String dirName) {
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
	
	/**
	 * Creates a {@link ILaunchConfigurationWorkingCopy} for the given name and type.
	 * 
	 * @param name the name for the new launch configuration
	 * @param type the type of the new launch configuration
	 * @return the new launch configuration working copy
	 * @throws CoreException
	 */
	public static ILaunchConfigurationWorkingCopy createLaunchConfigurationWorkingCopy(String name, String type) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigType = launchManager.getLaunchConfigurationType(type);
		
		String launchName = launchManager.generateLaunchConfigurationName(name); 
		return launchConfigType.newInstance(null, launchName);
	}
	
	public static IServer checkedGetServer(ILaunchConfiguration launchConfig) throws CoreException {
		String serverId = new JBossLaunchConfigProperties().getServerId(launchConfig);
		return ServerConverter.findServer(serverId);
	}
}
