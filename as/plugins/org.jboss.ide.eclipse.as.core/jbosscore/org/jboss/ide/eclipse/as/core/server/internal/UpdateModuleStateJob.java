/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IModuleStateController;

public class UpdateModuleStateJob extends Job {
	private IServer server;
	private IServerModuleStateVerifier verifier;
	private IModuleStateController controller;
	private boolean wait;
	private int maxWait;
	
	public UpdateModuleStateJob(IServer server) {
		this(server, (IServerModuleStateVerifier)null, false, 0);
	}

	public UpdateModuleStateJob(IServer server, IServerModuleStateVerifier verifier) {
		this(server, verifier, false, 0);
	}
	
	public UpdateModuleStateJob(IServer server, IServerModuleStateVerifier verifier, boolean wait, int maxWait) {
		super("Check module status for server " + server.getName()); //$NON-NLS-1$
		this.wait = wait;
		this.maxWait = maxWait;
		this.server = server;
		if( verifier == null ) {
			JBossExtendedProperties properties = (JBossExtendedProperties)server.loadAdapter(JBossExtendedProperties.class, null);
			this.verifier = properties.getModuleStateVerifier();
		} else {
			this.verifier = verifier;
		}
	}
	
	public UpdateModuleStateJob(IModuleStateController controller, IServer server) {
		this(controller, server, false, 0);
	}
	
	public UpdateModuleStateJob(IModuleStateController controller, IServer server, boolean wait, int maxWait) {
		super("Check module status for server " + server.getName()); //$NON-NLS-1$
		this.wait = wait;
		this.maxWait = maxWait;
		this.server = server;
		this.controller = controller;
	}
	

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if( verifier == null && controller == null)
			return Status.CANCEL_STATUS;
		if( server.getServerState() != IServer.STATE_STARTED)
			return Status.OK_STATUS;
		
		if( !wait ) {
			// Handle the quick update scenario
			return runQuick(monitor);
		} else {
			// Handle the scenario where we wait
			return runWait(monitor);
		}
	}

	private IStatus runWait(IProgressMonitor monitor) {
		boolean allStarted = false;
		long startTime = System.currentTimeMillis();
		long endTime = startTime + maxWait;
		IModule[] modules = server.getModules();
		monitor.beginTask("Checking module states for " + server.getName(), maxWait * 1000); //$NON-NLS-1$
		while( !allStarted && System.currentTimeMillis() < endTime) {
			allStarted = true;
			long thisLoopStart = System.currentTimeMillis();
			for( int i = 0; i < modules.length; i++ ) {
				IModule[] temp = new IModule[]{modules[i]};
				int state = getModuleState(temp, new SubProgressMonitor(monitor, 1000));
				boolean started = state == IServer.STATE_STARTED;
				allStarted = allStarted && started;
				((Server)server).setModuleState(temp, state);
			}
			
			// Sleep just a bit
			try {
				Thread.sleep(500);
			} catch(InterruptedException ie) {
				// ignore
			}
			long timeDif = System.currentTimeMillis() - thisLoopStart;
			monitor.worked((int)timeDif - (modules.length * 100));
		}
		return Status.OK_STATUS;
	}
	
	private IStatus runQuick(IProgressMonitor monitor) {
		IModule[] modules = server.getModules();
		monitor.beginTask("Verifying Module State", modules.length * 1000); //$NON-NLS-1$
		for( int i = 0; i < modules.length; i++ ) {
			IModule[] temp = new IModule[]{modules[i]};
			
			//boolean started = verifier.isModuleStarted(server, temp, new SubProgressMonitor(monitor, 1000));
			//int state = started ? IServer.STATE_STARTED : IServer.STATE_STOPPED;
			int state = getModuleState(temp, new SubProgressMonitor(monitor, 1000));
			((Server)server).setModuleState(temp, state);
		}
		return Status.OK_STATUS;
	}
	
	private int getModuleState(IModule[] temp, IProgressMonitor monitor) {
		if( verifier != null )
			return verifier.getModuleState(server, temp, new SubProgressMonitor(monitor, 1000));
		if( controller != null )
			return controller.getModuleState(temp, new SubProgressMonitor(monitor, 1000));
		return IServer.STATE_UNKNOWN;
	}
}
