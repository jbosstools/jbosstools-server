package org.jboss.ide.eclipse.as.core.server.internal.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;

public interface IStartLaunchSetupParticipant {

	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IServer server) throws CoreException;

}