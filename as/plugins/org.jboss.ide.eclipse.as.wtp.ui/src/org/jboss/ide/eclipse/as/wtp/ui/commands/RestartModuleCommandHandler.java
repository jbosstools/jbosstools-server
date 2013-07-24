/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.ide.eclipse.as.wtp.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.util.NullModuleArtifact;
import org.eclipse.wst.server.ui.internal.view.servers.RestartModuleAction;

public class RestartModuleCommandHandler extends AbstractModuleCommandHandler
		implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// go around in a circle to avoid plugin dependence on debug.ui
			IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
			IModule module = getModule(resource);
			IServer server = getServer(module, new NullModuleArtifact(module), new NullProgressMonitor());
			if( module != null && server != null ) {
				IModule[] module2 = new IModule[]{module};
				new RestartModuleAction(server, module2).run();
			}
			return null;
		} catch( CoreException ce) {
			throw new ExecutionException(ce.getMessage(), ce.getStatus().getException());
		}
	}

}
