package org.jboss.ide.eclipse.as.core.server.internal.launch;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.runtime.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.JBossServer;


public class StopLaunchConfiguration extends AbstractJBossLaunchConfigType {
	
	public static final String STOP_LAUNCH_TYPE = "org.jboss.ide.eclipse.as.core.server.stopLaunchConfiguration";
	public static final String STOP_MAIN_TYPE = "org.jboss.Shutdown";
	public static final String STOP_JAR_LOC = "bin" + File.separator + "shutdown.jar";
	
	public static void stop(IServer server) {
		try {
			ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration(server);
			wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		} catch( CoreException ce ) {
			// report it from here
		}
	}
	
	protected void preLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) {
	}

	
	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IServer server) throws CoreException {
		JBossServer jbs = findJBossServer(server.getId());
		IJBossServerRuntime jbrt = findJBossServerRuntime(server);
		String serverHome = getServerHome(jbs);
		
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigType = launchManager.getLaunchConfigurationType(STOP_LAUNCH_TYPE);
		
		String launchName = TwiddleLaunchConfiguration.class.getName();
		launchName = launchManager.generateUniqueLaunchConfigurationNameFrom(launchName); 
		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, launchName);
		wc.setAttribute(SERVER_ID, server.getId());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getDefaultArgs(jbs));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, STOP_MAIN_TYPE);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, serverHome + Path.SEPARATOR + "bin");
		ArrayList classpath = new ArrayList();
		addCPEntry(classpath, jbs, STOP_JAR_LOC);
		ArrayList runtimeClassPaths = convertClasspath(classpath, jbrt.getVM());
		String cpKey = IJavaLaunchConfigurationConstants.ATTR_CLASSPATH;
		wc.setAttribute(cpKey, runtimeClassPaths);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);

		return wc;
	}

	public static String getDefaultArgs(JBossServer jbs) throws CoreException {
		String args = "-S ";
		if( jbs.getUsername() != null && !jbs.getUsername().equals("")) 
			args += "-u " + jbs.getUsername() + " ";
		if( jbs.getPassword() != null && !jbs.getUsername().equals("")) 
			args += "-p " + jbs.getPassword() + " ";
		return args;
	}

}
