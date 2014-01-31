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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.internal.RecentlyUpdatedServerLaunches;
import org.jboss.ide.eclipse.as.core.server.internal.UpdateModuleStateJob;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllerEnvironment;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IModuleStateController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerShutdownController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;

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
		// pull from our profile method
		return getController(system, null);
		
	}
	
	@Override
	protected ILaunchServerController getLaunchController() throws CoreException {
		return (ILaunchServerController)getController(ILaunchServerController.SYSTEM_ID, null);
	}
	
	@Override
	protected IModuleStateController getModuleStateController() throws CoreException {
		return (IModuleStateController)getController(IModuleStateController.SYSTEM_ID, null);
	}

	@Override
	protected IServerShutdownController getShutdownController() throws CoreException {
		return (IServerShutdownController)getController(IServerShutdownController.SYSTEM_ID, null);
	}
	
	@Override
	protected IPublishController getPublishController() throws CoreException {
		// If there's a current cache, use that one
		Object o = getSharedData(SYSTEM_PUBLISH);
		if( o != null ) {
			return (IPublishController)o;
		}
		return (IPublishController)getController(IPublishController.SYSTEM_ID, null);
	}
	
	/**
	 * Use the profile model to find a subsystem for the given profile
	 */
	@Override
	public ISubsystemController getController(String system, ControllerEnvironment env) throws CoreException {
		// Check override props
		ISubsystemController ret = getOverrideController(system, env);
		if( ret == null ) {
			// Check profile
			String profile = getServer().getAttribute(IDeployableServer.SERVER_MODE, "local"); //$NON-NLS-1$
			ret = ServerProfileModel.getDefault().getController(getServer(), profile, system, env);
		}
		if( ret == null ) {
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0, 
					"Unable to locate system " + system + " for server " + getServer().getName(), null)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ret;
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
