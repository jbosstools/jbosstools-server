/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IPollResultListener;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public abstract class AbstractJBossBehaviourDelegate extends AbstractBehaviourDelegate {

	private PollThread pollThread = null;

	protected PollThread getPollThread() {
		return pollThread;
	}
	
	public IServer getServer() {
		return actualBehavior.getServer();
	}

	public void stop(boolean force) {
		if( LaunchCommandPreferences.isIgnoreLaunchCommand(getServer())) {
			actualBehavior.setServerStopped();
			return;
		}
		stopImpl(force);
	}
	
	protected abstract void stopImpl(boolean force);
	
	protected abstract void forceStop();

	protected abstract IStatus gracefullStop();
	
	@Override
	public IStatus canChangeState(String launchMode) {
		return Status.OK_STATUS;
	}

	@Override
	public String getDefaultStopArguments() throws CoreException {
		JBossServer jbs = (JBossServer)ServerConverter.getJBossServer(getServer());
		return jbs.getExtendedProperties().getDefaultLaunchArguments().getDefaultStopArgs();
	}

	protected void pollServer(final boolean expectedState) {
		IServerStatePoller poller = PollThreadUtils.getPoller(expectedState, getServer());
		pollServer(expectedState, poller);
	}
	
	protected void pollServer(boolean expectedState, IServerStatePoller poller) {
		stopPolling();
		this.pollThread = PollThreadUtils.pollServer(expectedState, poller, pollThread, onPollingFinished(),
				getServer());
	}

	protected IPollResultListener onPollingFinished() {
		return new IPollResultListener() {

			@Override
			public void stateNotAsserted(boolean expectedState, boolean currentState) {
				stop(true);
			}

			@Override
			public void stateAsserted(boolean expectedState, boolean currentState) {
				if (currentState == IServerStatePoller.SERVER_UP) {
					getActualBehavior().setServerStarted();
				} else {
					getActualBehavior().setServerStopped();
				}
			}
		};
	}

	protected void stopPolling() {
		cancelPolling(null);
	}

	protected void cancelPolling(String message) {
		PollThreadUtils.cancelPolling(message, this.pollThread);
		this.pollThread = null;
	}

	protected IDelegatingServerBehavior getActualBehavior() {
		return actualBehavior;
	}
	
	/* 
	 * The following 4 methods are not interface methods and should not be used anymore.
	 * They were convenience methods, but now seem only to confuse which level of delegation
	 * is in charge of doing what exactly.  
	 */
	@Deprecated
	protected void setServerStopping() {
		getActualBehavior().setServerStopping();
	}

	@Deprecated
	protected void setServerStopped() {
		getActualBehavior().setServerStopped();
	}

	@Deprecated
	protected void setServerStarted() {
		getActualBehavior().setServerStarted();
	}

	@Deprecated
	protected void setServerStarting() {
		getActualBehavior().setServerStarting();
	}
}
