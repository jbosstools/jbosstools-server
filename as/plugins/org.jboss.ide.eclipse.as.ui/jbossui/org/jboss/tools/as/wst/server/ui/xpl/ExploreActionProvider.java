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
import java.util.HashMap;

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
	public static interface IExploreBehavior {
		public boolean canExplore(IServer server, IModule[] module);
		public void openExplorer(IServer server, IModule[] module);
	}
	public static HashMap<String, IExploreBehavior> exploreBehaviorMap = new HashMap<String, IExploreBehavior>();
	static {
		exploreBehaviorMap.put(LocalPublishMethod.LOCAL_PUBLISH_METHOD, new IExploreBehavior() {
			public boolean canExplore(IServer server, IModule[] module) {
				if( module != null )
					return ExploreUtils.canExplore(server, module);
				return ExploreUtils.canExplore(server);
			}
			public void openExplorer(IServer server, IModule[] module) {
				if( module != null ) 
					runExploreModuleServer(server, module);
				else
					runExploreServer(server);
			}
			public void runExploreServer(IServer server) {
				String deployDirectory = ExploreUtils.getDeployDirectory(server);
				if (deployDirectory != null && deployDirectory.length() > 0) {
					ExploreUtils.explore(deployDirectory);
				} 
			}
			
			public void runExploreModuleServer(IServer server, IModule[] module) {
				IPath path = getModuleDeployPath(server, module);
				if (path != null) {
					File file = path.toFile();
					if (file.exists()) {
						ExploreUtils.explore(file.getAbsolutePath());
					}
				}
			}
			private IPath getModuleDeployPath(IServer server, IModule[] module) {
				IDeployableServer deployableServer = ServerConverter.getDeployableServer(server);
				if( deployableServer != null )
					return ExploreUtils.getDeployPath(deployableServer, module);
				return null;
			}
		});
	}

	
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
		runExplore(getServer(), getModuleServer());
	}
	
	private void runExplore(IServer server, ModuleServer ms) {
		String mode = getServer().getAttribute(IDeployableServer.SERVER_MODE, LocalPublishMethod.LOCAL_PUBLISH_METHOD);
		IExploreBehavior beh = exploreBehaviorMap.get(mode);
		beh.openExplorer(server, ms == null ? null : ms.module);
	}
	
	public void fillContextMenu(IMenuManager menu) {
		String mode = getServer().getAttribute(IDeployableServer.SERVER_MODE, LocalPublishMethod.LOCAL_PUBLISH_METHOD);
		IExploreBehavior beh = exploreBehaviorMap.get(mode);
		if( beh == null || !beh.canExplore(getServer(), getModuleServer() == null ? null : getModuleServer().module))
			return;
		if( getModuleServer() != null )
			menu.insertBefore(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR, exploreAction);
		else if( getServer() != null )
			menu.insertBefore(ServerActionProvider.SERVER_ETC_SECTION_END_SEPARATOR, exploreAction);
		exploreAction.setEnabled(true);
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
			if( selection.size() == 1 )
				return selection.getFirstElement();
		}
		return null;
	}	
}
