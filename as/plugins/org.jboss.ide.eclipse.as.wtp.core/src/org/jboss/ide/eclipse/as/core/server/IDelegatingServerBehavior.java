package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.IStatus;

public interface IDelegatingServerBehavior extends IDeployableServerBehaviour {
	public IJBossBehaviourDelegate getDelegate();
	public IStatus canStart(String launchMode);
	public IStatus canStop(String launchMode);
	public IStatus canRestart(String launchMode);
	public void setServerStopping();
	public void setServerStopped();
	public void setServerStarting();
	public void setServerStarted();
}
