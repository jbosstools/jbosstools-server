package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.AbstractJBossLaunchConfigType;

public class JBossRuntimeClasspathUtil {

	private static final String JBOSS_MODULES_JAR = "jboss-modules.jar"; //$NON-NLS-1$

	public static List<String> getClasspath(IServer server, IVMInstall vmInstall) throws CoreException {
		List<IRuntimeClasspathEntry> classpath = new ArrayList<IRuntimeClasspathEntry>();
		classpath.add(getRunJarRuntimeCPEntry(server));
		AbstractJBossLaunchConfigType.addJREEntry(classpath, vmInstall);
		List<String> runtimeClassPaths = AbstractJBossLaunchConfigType.convertClasspath(classpath);
		return runtimeClassPaths;
	}

	public static IRuntimeClasspathEntry getRunJarRuntimeCPEntry(IServer server) throws CoreException {
		IPath location = server.getRuntime().getLocation();
		IClasspathEntry entry =
				JavaRuntime.newArchiveRuntimeClasspathEntry(location.append(JBOSS_MODULES_JAR)).getClasspathEntry();
		return new RuntimeClasspathEntry(entry);
	}

}
