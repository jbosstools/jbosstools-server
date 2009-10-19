package org.jboss.ide.eclipse.as.ssh.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;

public class SSHPublisher {
	protected void publishStart(IServer server, final IProgressMonitor monitor) throws CoreException {
	}
	
	protected void publishFinish(IServer server, final IProgressMonitor monitor) throws CoreException {
	}
	
	protected void publishModule(int publishType, IModule[] module, IProgressMonitor monitor) throws CoreException {
	}
}
