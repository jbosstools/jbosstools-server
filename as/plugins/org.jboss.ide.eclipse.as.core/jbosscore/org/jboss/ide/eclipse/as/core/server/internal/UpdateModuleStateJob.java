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

public class UpdateModuleStateJob extends Job {
	private IServer server;
	private IServerModuleStateVerifier verifier;
	public UpdateModuleStateJob(IServer server) {
		super("Update Module States"); //$NON-NLS-1$
		this.server = server;
		JBossExtendedProperties properties = (JBossExtendedProperties)server.loadAdapter(JBossExtendedProperties.class, null);
		this.verifier = properties.getModuleStateVerifier();
	}

	
	
	public UpdateModuleStateJob(IServer server, IServerModuleStateVerifier verifier) {
		super("Update Module States"); //$NON-NLS-1$
		this.server = server;
		this.verifier = verifier;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if( verifier == null )
			return Status.CANCEL_STATUS;
		
		IModule[] modules = server.getModules();
		monitor.beginTask("Verifying Module State", modules.length * 1000); //$NON-NLS-1$
		for( int i = 0; i < modules.length; i++ ) {
			IModule[] temp = new IModule[]{modules[i]};
			boolean started = verifier.isModuleStarted(server, temp, new SubProgressMonitor(monitor, 1000));
			int state = started ? IServer.STATE_STARTED : IServer.STATE_STOPPED;
			((Server)server).setModuleState(temp, state);
		}
		return Status.OK_STATUS;
	}

}
