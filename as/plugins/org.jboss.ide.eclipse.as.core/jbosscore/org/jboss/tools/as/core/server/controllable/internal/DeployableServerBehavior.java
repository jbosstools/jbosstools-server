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
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.internal.RecentlyUpdatedServerLaunches;
import org.jboss.ide.eclipse.as.core.server.internal.UpdateModuleStateJob;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllerEnvironment;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.SubsystemModel;

/**
 * This is the behavior class for a deploy-only server
 */
public class DeployableServerBehavior extends ControllableServerBehavior {
	
	@Override
	public ISubsystemController getController(String system) throws CoreException {
		// publish caches, so we check that first
		if( SYSTEM_PUBLISH.equals(system)) {
			return getPublishController();
		}
		
		try {
			// Default to returning an implementation that matches the current server mode (local, rse, etc)
			return getControllerForTarget(system);
		} catch(CoreException ce) {
			// Ignore this. No subsystem matches that flag
		}
		// Use superclass impl to get whatever matches
		return super.getController(system);
	}

	protected ISubsystemController getControllerForTarget(String system) throws CoreException {
		return getControllerForTarget(system, new ControllerEnvironment());
	}
	
	protected ISubsystemController getControllerForTarget(String system, ControllerEnvironment env) throws CoreException {
		String existingMode = getServer().getAttribute(IDeployableServer.SERVER_MODE, "local"); //$NON-NLS-1$
		env.addRequiredProperty(system, "target", existingMode); //$NON-NLS-1$
		return super.getController(system, env);
	}
	
	@Override
	protected ILaunchServerController getLaunchController() throws CoreException {
		return (ILaunchServerController)getControllerForTarget(ILaunchServerController.SYSTEM_ID);
	}
	
	@Override
	protected IPublishController getPublishController() throws CoreException {
		// If there's a current cache, use that one
		Object o = getSharedData(SYSTEM_PUBLISH);
		if( o != null ) {
			return (IPublishController)o;
		}
		return (IPublishController)super.getController(IPublishController.SYSTEM_ID, null);
	}
	

	@Override
	protected void publishStart(IProgressMonitor monitor) throws CoreException {
		// Cache the controller during publish start
		IPublishController controller = getPublishController();
		putSharedData(SYSTEM_PUBLISH, controller);
		controller.publishStart(monitor);
	}
	
	@Override
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		IPublishController controller = getPublishController();
		try {
			controller.publishFinish(monitor);
		} finally {
			// clear the controller after publish finish
			putSharedData(SYSTEM_PUBLISH, null);
		}
	}
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		super.setupLaunchConfiguration(workingCopy, monitor);
		// this hack is still required. See RecentlyUpdatedServerLaunches
		RecentlyUpdatedServerLaunches.getDefault().setRecentServer(getServer());
	}

	
	@Override
	public void setServerStarted() {
		super.setServerStarted();
		
		// Once the server is marked started, we want to update the deployment scanners and module publish state
		IServer s = getServer();
		JBossExtendedProperties properties = (JBossExtendedProperties)s.loadAdapter(JBossExtendedProperties.class, null);
		if( properties != null ) {
			Job scannerJob = properties.getDeploymentScannerModifier().getUpdateDeploymentScannerJob(s);
			
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
}
