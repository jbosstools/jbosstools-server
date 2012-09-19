package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.wst.server.core.IServer;

public interface IJBossLaunchDelegate {
	public void actualLaunch(LaunchConfigurationDelegate launchConfig, ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException;
	
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) 
			throws CoreException;

	public void preLaunch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) 
			throws CoreException;

	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException;
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IServer server) 
			throws CoreException;

}
