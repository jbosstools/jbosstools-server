/*******************************************************************************
 * Copyright (c) 2011 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.actions;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.eclipse.wst.server.ui.internal.view.servers.RestartModuleAction;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class ChangeTimeStampActionDelegate implements IWorkbenchWindowActionDelegate {

	protected IWorkbenchWindow window;
	String tooltip = null;
	IProject project = null;
	IModule[] module = null;
	IServer server = null;
	RestartModuleAction[] restartActions = null;
	private final static String ESB_PROJECT_FACET = "jst.jboss.esb"; //$NON-NLS-1$
	private static boolean isESBProject(IProject project) {
		IFacetedProject facetedProject = null;
		try {
			facetedProject = ProjectFacetsManager.create(project);
		} catch (CoreException e) {
			JBossServerUIPlugin.log(e.getMessage(), e);
			return false;
		}
		if (facetedProject != null && ProjectFacetsManager.isProjectFacetDefined(ESB_PROJECT_FACET)) {
			IProjectFacet projectFacet = ProjectFacetsManager.getProjectFacet(ESB_PROJECT_FACET);
			return projectFacet != null && facetedProject.hasProjectFacet(projectFacet);
		}
	return false;
}

	public void selectionChanged(IAction action, ISelection selection) {
		if(project == null && action.isEnabled()) action.setEnabled(false);
		if(tooltip != null && (selection instanceof IStructuredSelection)) {
			Object o = ((IStructuredSelection)selection).getFirstElement();
			setProject(o);
			setModuleAndServer(o);
			buildActions();
			if(project != null) {
				action.setEnabled(computeEnabled());
				action.setToolTipText(NLS.bind(ServerActionMessages.CHANGE_TIME_STAMP,project.getName()));
				return;
			}
		}
		tooltip = ServerActionMessages.CHANGE_TIME_STAMP_DEFAULT; 
		action.setToolTipText(tooltip);
	}

	protected void setProject(Object selection) {
		if(selection instanceof IResource) {
			project = ((IResource)selection).getProject();
		} else if(selection instanceof IAdaptable) {
			Object r = ((IAdaptable)selection).getAdapter(IResource.class);
			project= r instanceof IResource ? ((IResource)r).getProject() : null;
		} else if( selection instanceof ModuleServer) {
			project = ((ModuleServer)selection).module[0].getProject();
		} else 
			project = null;
	}

	void setModuleAndServer(Object selection) {
		if( selection instanceof IServer ) {
			server = (IServer)selection;
			module = null;
		} else if( selection instanceof ModuleServer ) {
			server = ((ModuleServer)selection).server;
			module = ((ModuleServer)selection).module;
		}
		if( project != null ) {
			IModule tmp = ServerUtil.getModule(project);
			module = tmp == null ? null : new IModule[]{tmp};
		}
	}
	
	void buildActions() {
		ArrayList<RestartModuleAction> actionList = new ArrayList<RestartModuleAction>();
		RestartModuleAction a;
		if( module != null ) {
			IServer[] targetServers = null;
			if( server == null ) {
				List<IServer> list = getServers(module[0]);
				targetServers = (IServer[]) list.toArray(new IServer[list.size()]);
			} else {
				targetServers = new IServer[]{server};
			}
			for( int i = 0; i < targetServers.length; i++ ) {
				a = new RestartModuleAction(targetServers[i], module);
				if( a.isEnabled()) {
					actionList.add(a);
				}
			}
		}
		restartActions = actionList.toArray(new RestartModuleAction[actionList.size()]);
	}
	
	protected boolean computeEnabled() {
		if( module == null ) return false;
		if(project == null || !project.isAccessible()) return false;
		boolean isWar = JavaEEProjectUtilities.isDynamicWebProject(project);
		boolean isEar = JavaEEProjectUtilities.isEARProject(project);
		boolean isEJB = JavaEEProjectUtilities.isEJBProject(project);
		boolean isEsb = isESBProject(project);
		boolean canRestart = isEar || isEJB || isWar || isEsb;
		if( canRestart && restartActions != null && restartActions.length > 0 )
			return true;
		return false;
	}

	public void run(IAction action) {
		for( int i = 0; i < restartActions.length; i++ ) {
			restartActions[i].run();
		}
	}

	static List<IServer> getServers(IModule module) {
		List<IServer> result = new ArrayList<IServer>();
		IServer[] servers = ServerCore.getServers();
		for (IServer s: servers) {
			IModule[] modules = s.getModules();
			for( int j = 0; j < modules.length; j++ ) {
				if( modules[j].equals(module))
					result.add(s);
			}
		}
		return result;
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	public void dispose() {
		window = null;
	}
}