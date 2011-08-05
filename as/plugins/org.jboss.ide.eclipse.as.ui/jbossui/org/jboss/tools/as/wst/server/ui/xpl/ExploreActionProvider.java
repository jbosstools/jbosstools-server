/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.wst.server.ui.xpl;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.cnf.ServerActionProvider;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.actions.ExploreUtils;
public class ExploreActionProvider extends CommonActionProvider {
	private ICommonActionExtensionSite actionSite;
	private CommonViewer cv;
	public ExploreActionProvider() {
		super();
	}
	
	private Action exploreAction;
	
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		this.actionSite = aSite;
		ICommonViewerSite site = aSite.getViewSite();
		if( site instanceof ICommonViewerWorkbenchSite ) {
			StructuredViewer v = aSite.getStructuredViewer();
			if( v instanceof CommonViewer ) {
				cv = (CommonViewer)v;
				ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite)site;
				createActions(cv, wsSite.getSelectionProvider());
			}
		}
	}
	public void createActions(CommonViewer tableViewer, ISelectionProvider provider) {
		Shell shell = tableViewer.getTree().getShell();
		exploreAction = new Action() {
			public void run() {
				runExplore();
			}
		};
		exploreAction.setText(ExploreUtils.EXPLORE);
		exploreAction.setDescription(ExploreUtils.EXPLORE_DESCRIPTION);
		exploreAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.EXPLORE_IMAGE));
	}
	
	private void runExplore() {
		if( getModuleServer() != null ) 
			runExploreModuleServer();
		else
			runExploreServer();
	}
	
	public void fillContextMenu(IMenuManager menu) {
		boolean enabled = false;
		if( getModuleServer() != null )
			menu.insertBefore(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR, exploreAction);
		else
			menu.insertBefore(ServerActionProvider.SERVER_ETC_SECTION_END_SEPARATOR, exploreAction);
		if( getServer() != null ) {
			String mode = getServer().getAttribute(IDeployableServer.SERVER_MODE, LocalPublishMethod.LOCAL_PUBLISH_METHOD);
			if( LocalPublishMethod.LOCAL_PUBLISH_METHOD.equals(mode)) {
				enabled = true;
			}
		}
		exploreAction.setEnabled(enabled);
	}
	
	public void runExploreServer() {
		String deployDirectory = ExploreUtils.getDeployDirectory(getServer());
		if (deployDirectory != null && deployDirectory.length() > 0) {
			ExploreUtils.explore(deployDirectory);
		} 
	}
	
	public void runExploreModuleServer() {
		IPath path = getModuleDeployPath();
		if (path != null) {
			File file = path.toFile();
			if (file.exists()) {
				ExploreUtils.explore(file.getAbsolutePath());
			}
		}
	}
	private IPath getModuleDeployPath() {
		ModuleServer ms = getModuleServer();
		IModule[] module = ms.module;
		IDeployableServer deployableServer = ServerConverter.getDeployableServer(ms.server);
		if( deployableServer != null )
			return ExploreUtils.getDeployPath(deployableServer, module);
		return null;
	}
	
	public IServer getServer() {
		Object o = getSelection();
		if (o instanceof IServer)
			return ((IServer)o);
		if( o instanceof ModuleServer) 
			return ((ModuleServer) o).server;
		return null;
	}
	
	public ModuleServer getModuleServer() {
		Object o = getSelection();
		if( o instanceof ModuleServer) 
			return ((ModuleServer) o);
		return null;
	}
	
	protected Object getSelection() {
		ICommonViewerSite site = actionSite.getViewSite();
		IStructuredSelection selection = null;
		if (site instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
			selection = (IStructuredSelection) wsSite.getSelectionProvider()
					.getSelection();
			Object first = selection.getFirstElement();
			return first;
		}
		return null;
	}	
}
