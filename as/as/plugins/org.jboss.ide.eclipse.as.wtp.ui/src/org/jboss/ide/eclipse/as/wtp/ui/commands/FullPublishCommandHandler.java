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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.PublishServerJob;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.util.NullModuleArtifact;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;
import org.jboss.ide.eclipse.as.wtp.ui.wizards.xpl.export.FullPublishToServerWizard;

public class FullPublishCommandHandler implements IHandler {

	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// go around in a circle to avoid plugin dependence on debug.ui
			IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
			IModule module = getModule(resource);
			IServer server = getServer(module, new NullModuleArtifact(module), new NullProgressMonitor());
			if( module != null && server != null ) {
				IModule[] module2 = new IModule[]{module};
				((Server)server).setModulePublishState(module2, IServer.PUBLISH_STATE_FULL);
				ArrayList<IModule[]> allChildren = ServerModelUtilities.getDeepChildren(server, module2);
				for( int j = 0; j < allChildren.size(); j++ ) {
					((Server)server).setModulePublishState((IModule[])allChildren.get(j), IServer.PUBLISH_STATE_FULL);
				}
				new PublishServerJob(server, IServer.PUBLISH_INCREMENTAL, true).schedule();
			}
			return null;
		} catch( CoreException ce) {
			throw new ExecutionException(ce.getMessage(), ce.getStatus().getException());
		}
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
		ArrayList<IModule> possibleModules = new ArrayList<IModule>();
		if (resource != null) {
			IModuleArtifact[] moduleArtifacts = ServerPlugin.getModuleArtifacts(resource);
			if (moduleArtifacts != null && moduleArtifacts.length > 0)
				for( int i = 0; i < moduleArtifacts.length; i++ ) 
					if( moduleArtifacts[i].getModule() != null && 
							!possibleModules.contains(moduleArtifacts[i].getModule()))
						possibleModules.add(moduleArtifacts[i].getModule());
			if( possibleModules.size() > 0 ) {
				module = promptForModule(possibleModules);
			}
		}
		return module;
	}

	protected IModule promptForModule(ArrayList<IModule> modules) {
		if(modules.size() == 1 )
			return modules.get(0);
		// TODO prompt
		return modules.get(0);
	}
	
	public IServer getServer(IModule module, IModuleArtifact moduleArtifact, IProgressMonitor monitor) throws CoreException {
		IServer server = ServerCore.getDefaultServer(module);
		if (server != null && !ServerUtil.containsModule(server, module, monitor)) {
			IServerWorkingCopy wc = server.createWorkingCopy();
			try {
				ServerUtil.modifyModules(wc, new IModule[] { module }, new IModule[0], monitor);
				wc.save(false, monitor);
			} catch (CoreException ce) {
				server = null;
			}
		}
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if (server == null) {
			// try the full wizard
			FullPublishToServerWizard wizard = new FullPublishToServerWizard(module, moduleArtifact);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			if (dialog.open() == Window.CANCEL) {
				if (monitor != null)
					monitor.setCanceled(true);
				return null;
			}
			
			try {
				Job.getJobManager().join("org.eclipse.wst.server.ui.family", null);
			} catch (Exception e) {
			}
			server = wizard.getServer();
			boolean preferred = wizard.isPreferredServer();
//			tasksAndClientShown = true;
//			client = wizard.getSelectedClient();
//			launchableAdapter = wizard.getLaunchableAdapter();
			
			// set preferred server if requested
			if (server != null && preferred) {
				try {
					ServerCore.setDefaultServer(module, server, monitor);
				} catch (CoreException ce) {
					String message = "Could not save server preference information.";
					ErrorDialog.openError(shell, "Server Error", message, ce.getStatus());
				}
			}
		}
		
		try {
			Job.getJobManager().join("org.eclipse.wst.server.ui.family", new NullProgressMonitor());
		} catch (Exception e) {
		}
		
		return server;
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
