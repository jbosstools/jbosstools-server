/*************************************************************************************
 * Copyright (c) 2008-2009 JBoss by Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.ide.eclipse.as.wtp.ui.commands;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.PublishServerJob;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

public class FullPublishCommandHandler implements IHandler {

	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// go around in a circle to avoid plugin dependence on debug.ui
		IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
		IModule module = getModule(resource);
		Server server = (Server)getServer(module);
		if( module != null && server != null ) {
			IModule[] module2 = new IModule[]{module};
			server.setModulePublishState(module2, IServer.PUBLISH_STATE_FULL);
			ArrayList<IModule[]> allChildren = ServerModelUtilities.getDeepChildren(server, module2);
			for( int j = 0; j < allChildren.size(); j++ ) {
				server.setModulePublishState((IModule[])allChildren.get(j), IServer.PUBLISH_STATE_FULL);
			}
		}
		new PublishServerJob(server, IServer.PUBLISH_INCREMENTAL, true).schedule();
		return null;
	}
	
	public IServer getServer(IModule module) {
		IServer toRepublish = null;
		if( module != null ) {
			IServer[] servers = ServerCore.getServers();
			ArrayList<IServer> matched = new ArrayList<IServer>();
			for( int i = 0; i < servers.length; i++ ) {
				boolean found = false;
				IModule[] deployed = servers[i].getModules();
				for( int j = 0; j < deployed.length && !found; j++ ) 
					if( deployed[j].getId().equals(module.getId()))
						found = true;
				if( found )
					matched.add(servers[i]);
			}
			
			if( matched.size() == 0 || matched.size() > 1 ) {
				// TODO show a dialog to choose the server
				toRepublish = matched.get(0);
			} else 
				toRepublish = matched.get(0);
		}
		return toRepublish;
	}
	
	protected IModule getModule(IResource resource) {
		IModule module = null;
		if (resource != null) {
			IModuleArtifact[] moduleArtifacts = ServerPlugin.getModuleArtifacts(resource);
			IModuleArtifact moduleArtifact = null;
			if (moduleArtifacts != null && moduleArtifacts.length > 0)
				moduleArtifact = moduleArtifacts[0];
			
			if (moduleArtifact != null)
				module = moduleArtifact.getModule();
			
		}
		return module;
	}

	public boolean isEnabled() {
		IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
		IModule module = getModule(resource);
		return module != null;
	}

	public boolean isHandled() {
		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}

}
