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
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.internal.UpdateModuleStateJob;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllerEnvironment;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerDetailsController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;

/**
 * This is the behavior class for a deploy-only server
 */
public class DeployableServerBehavior extends ControllableServerBehavior {
	
	public ISubsystemController getController(String system) throws CoreException {
		if( IServerDetailsController.SYSTEM_ID.equals(system))
			return getDetailsController();
		if( IDeploymentOptionsController.SYSTEM_ID.equals(system)) 
			return getDeploymentOptionsController();
		return super.getController(system);
	}
	
	protected IDeploymentOptionsController getDeploymentOptionsController() throws CoreException {
		return (IDeploymentOptionsController)getControllerForTarget(IDeploymentOptionsController.SYSTEM_ID);
	}

	protected ISubsystemController getControllerForTarget(String system) throws CoreException {
		String existingMode = getServer().getAttribute(IDeployableServer.SERVER_MODE, "local"); //$NON-NLS-1$
		ControllerEnvironment env = new ControllerEnvironment();
		env.addRequiredProperty(system, "target", existingMode); //$NON-NLS-1$
		return super.getController(system, env);
	}
	
	protected IServerDetailsController getDetailsController() throws CoreException {
		return (IServerDetailsController)getControllerForTarget(IServerDetailsController.SYSTEM_ID);
	}
	
	protected ILaunchServerController getLaunchController() throws CoreException {
		return (ILaunchServerController)getControllerForTarget(ILaunchServerController.SYSTEM_ID);
	}
	
	protected IPublishController getPublishController() throws CoreException {
		Object o = getSharedData(SYSTEM_PUBLISH);
		if( o != null ) {
			return (IPublishController)o;
		}
		String existingMode = getServer().getAttribute(IDeployableServer.SERVER_MODE, "local"); //$NON-NLS-1$
		ControllerEnvironment env = new ControllerEnvironment();
		env.addRequiredProperty(IDeploymentOptionsController.SYSTEM_ID, "target", existingMode); //$NON-NLS-1$ 
		env.addRequiredProperty(IFilesystemController.SYSTEM_ID, "target", existingMode); //$NON-NLS-1$ 
		return (IPublishController)getController(SYSTEM_PUBLISH, env);
	}
	

	@Override
	protected void publishStart(IProgressMonitor monitor) throws CoreException {
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
			putSharedData(SYSTEM_PUBLISH, null);
		}
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
