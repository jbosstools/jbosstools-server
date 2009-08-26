package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;

public interface IJBossServerPublishMethod {
	public boolean accepts(String methodType);
	public void publishStart(DeployableServerBehavior behaviour, IProgressMonitor monitor) throws CoreException;
	public int publishFinish(DeployableServerBehavior behaviour, IProgressMonitor monitor) throws CoreException;
	public int publishModule(DeployableServerBehavior behaviour, int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException;
}
