/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.core.server.controllable.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.internal.RecentlyUpdatedServerLaunches;
import org.jboss.ide.eclipse.as.core.server.internal.UpdateModuleStateJob;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.CachedPublisherProfileBehavior;

/**
 * This is the behavior class for a deploy-only server 
 * and all JBoss / WildFly / EAP adapters
 */
public class DeployableServerBehavior extends CachedPublisherProfileBehavior {
	
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		// this hack is still required. See RecentlyUpdatedServerLaunches
		RecentlyUpdatedServerLaunches.getDefault().setRecentServer(getServer());
		super.setupLaunchConfiguration(workingCopy, monitor);
	}

	
	@Override
	public void setServerStarted() {
		super.setServerStarted();
		launchPostStartupJobs();
	}
	
	protected void launchPostStartupJobs() {
		// Once the server is marked started, we want to update the deployment scanners and module publish state
		IServer s = getServer();
		JBossExtendedProperties properties = (JBossExtendedProperties)s.loadAdapter(JBossExtendedProperties.class, null);
		if( properties != null ) {
			Job scannerJob = null;
			boolean addScanners = getServer().getAttribute(IJBossToolingConstants.PROPERTY_ADD_DEPLOYMENT_SCANNERS, true);
			if( addScanners ) {
				scannerJob = properties.getDeploymentScannerModifier().getUpdateDeploymentScannerJob(s);
			}
				
			
			IServerModuleStateVerifier verifier = properties.getModuleStateVerifier();
			Job moduleStateJob = null;
			if( verifier != null ) {
				moduleStateJob = new UpdateModuleStateJob(s, verifier, true, 10000);
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
}
