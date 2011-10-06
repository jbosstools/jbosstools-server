package org.jboss.tools.openshift.express.internal.core.behaviour;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.IJBossBehaviourDelegate;

public class ExpressBehaviourDelegate implements IJBossBehaviourDelegate {

	public ExpressBehaviourDelegate() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getBehaviourTypeId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActualBehaviour(DelegatingServerBehavior actualBehaviour) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(boolean force) {
		// TODO Auto-generated method stub

	}

	@Override
	public void publishStart(IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void publishFinish(IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServerStarting() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServerStopping() {
		// TODO Auto-generated method stub

	}

	@Override
	public IStatus canChangeState(String launchMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultStopArguments() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
