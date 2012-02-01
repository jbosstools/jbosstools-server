package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public interface ILaunchConfigConfigurator {

	public abstract void configure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException;

}