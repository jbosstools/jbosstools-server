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
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IPollResultListener;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
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
		boolean removeScanners = getServer().getAttribute(IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, false);
		if( removeScanners ) {
			JBossExtendedProperties properties = (JBossExtendedProperties)getServer().loadAdapter(JBossExtendedProperties.class, null);
			if( properties != null ) {
				properties.getDeploymentScannerModifier().removeAddedDeploymentScanners(getServer());
			}		
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
	
	@Override
	public void onServerStarted() {
		// The server is started. Let's add our deployment scanners
		IServer s = getActualBehavior().getServer();
		JBossExtendedProperties properties = (JBossExtendedProperties)s.loadAdapter(JBossExtendedProperties.class, null);
		if( properties != null ) {
			Job scannerJob = null;
			boolean addScanner = s.getAttribute(IJBossToolingConstants.PROPERTY_ADD_DEPLOYMENT_SCANNERS, true);
			if( addScanner )
				scannerJob = properties.getDeploymentScannerModifier().getUpdateDeploymentScannerJob(s);
			
			IServerModuleStateVerifier verifier = properties.getModuleStateVerifier();
			Job moduleStateJob = null;
			if( verifier != null ) {
				moduleStateJob = new UpdateModuleStateJob(s, verifier);
			}
			
			Job rootJob = chainJobs(scannerJob, moduleStateJob);
			if( rootJob != null )
				rootJob.schedule();
		}
	}
	
	// TODO move to common, job utils
	/**
	 * Chain two jobs together, and return the root job which should be scheduled. 
	 * @param job1 The job to launch first, or possible null 
	 * @param job2 The job to launch second, or possible null
	 * @return The job to launch... either job1 or, if null, job2
	 */
	private Job chainJobs(final Job job1, final Job job2) {
		if( job1 == null )
			return job2;
		if( job2 == null )
			return job1;
		JobChangeAdapter listener = new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				job1.removeJobChangeListener(this);
				if (job2 != null && event.getResult() != null 
						&& event.getResult().getSeverity() != IStatus.ERROR
						&& event.getResult().getSeverity() != IStatus.CANCEL)
					job2.schedule();
			}
		};
		job1.addJobChangeListener(listener);
		return job1;
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
