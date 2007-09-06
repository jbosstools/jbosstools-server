package org.jboss.ide.eclipse.as.core.launch;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.runtime.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.JBossServer;

public class TwiddleLaunchConfiguration extends AbstractJBossLaunchConfigType {

	public static final String TWIDDLE_LAUNCH_TYPE = "org.jboss.ide.eclipse.as.core.server.twiddleConfiguration";

	protected static final String TWIDDLE_MAIN_TYPE = "org.jboss.console.twiddle.Twiddle";
	protected static final String TWIDDLE_JAR_LOC = "bin" + File.separator + "twiddle.jar";

	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IServer server) throws CoreException {
		return createLaunchConfiguration(server, getDefaultArgs(server));
	}

	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IServer server, String args) throws CoreException {
		JBossServer jbs = findJBossServer(server.getId());
		IJBossServerRuntime jbrt = findJBossServerRuntime(server);
		String serverHome = getServerHome(jbs);
		
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigType = launchManager.getLaunchConfigurationType(TWIDDLE_LAUNCH_TYPE);
		
		String launchName = TwiddleLaunchConfiguration.class.getName();
		launchName = launchManager.generateUniqueLaunchConfigurationNameFrom(launchName); 
		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, launchName);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, TWIDDLE_MAIN_TYPE);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, serverHome + Path.SEPARATOR + "bin");
		
		ArrayList classpath = new ArrayList();
		addCPEntry(classpath, jbs, TWIDDLE_JAR_LOC);
		// Twiddle requires more classes and I'm too lazy to actually figure OUT which ones it needs.
		addDirectory (serverHome, classpath, "lib");
		addDirectory (serverHome, classpath, "lib" + File.separator + "endorsed");
		addDirectory (serverHome, classpath, "client");
		ArrayList runtimeClassPaths = convertClasspath(classpath, jbrt.getVM());
		String cpKey = IJavaLaunchConfigurationConstants.ATTR_CLASSPATH;
		wc.setAttribute(cpKey, runtimeClassPaths);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);

		return wc;
	}
	
	public static String getDefaultArgs(IServer server) throws CoreException {
		JBossServer jbs = findJBossServer(server.getId());
		String twiddleArgs = "-s " + jbs.getServer().getHost() + ":" 
				+ jbs.getJNDIPort() +  " -a jmx/rmi/RMIAdaptor ";
		if( jbs.getUsername() != null ) 
			twiddleArgs += "-u " + jbs.getUsername() + " ";
		if( jbs.getPassword() != null ) 
			twiddleArgs += "-p " + jbs.getPassword() + " ";
		return twiddleArgs;
	}

}
