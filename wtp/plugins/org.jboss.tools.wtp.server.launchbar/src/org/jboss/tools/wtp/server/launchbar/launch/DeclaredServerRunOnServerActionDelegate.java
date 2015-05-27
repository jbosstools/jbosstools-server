/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.wtp.server.launchbar.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.internal.actions.RunOnServerActionDelegate;

/**
 * A subclass of wst.servertools' RunOnServerActionDelegate
 * which is instantiated with a given server, rather than going
 * through the workflow to choose one. 
 */
public class DeclaredServerRunOnServerActionDelegate extends RunOnServerActionDelegate {
	protected IServer s2;
	public DeclaredServerRunOnServerActionDelegate(IServer s) {
		this.s2 = s;
	}
	public IServer getServer(IModule module, IModuleArtifact moduleArtifact, IProgressMonitor monitor) throws CoreException {
		IModule m = moduleArtifact.getModule();
		IModule[] parentModules = s2.getRootModules(module,
				monitor);
		if (parentModules != null && parentModules.length != 0) {
			module = (IModule) parentModules[0];
		}
		
		monitor.beginTask("Preparing launch", 200);
		IServerWorkingCopy serverWC = s2.createWorkingCopy();
		IStatus stat = s2.canModifyModules( new IModule[]{module}, new IModule[]{}, 
				new SubProgressMonitor(monitor, 50));
		if( stat.isOK() ) {
			serverWC.modifyModules(new IModule[] { module },
					new IModule[0], new SubProgressMonitor(monitor, 75));
			s2 = serverWC.save(false, new SubProgressMonitor(monitor, 75));
		}
		monitor.done();
		return s2;
	}
}