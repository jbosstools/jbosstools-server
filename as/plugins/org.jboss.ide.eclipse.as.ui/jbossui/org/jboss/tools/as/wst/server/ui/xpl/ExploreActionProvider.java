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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.subsystems.IExploreBehavior;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.CommonActionProviderUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
public class ExploreActionProvider extends CommonActionProvider {
	
	public final static String EXPLORE = Messages.ExploreUtils_Action_Text;
	public final static String EXPLORE_DESCRIPTION = Messages.ExploreUtils_Description;

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
		exploreAction.setText(EXPLORE);
		exploreAction.setDescription(EXPLORE_DESCRIPTION);
		exploreAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.EXPLORE_IMAGE));
	}
	 
	private void runExplore() {
		runExplore(getServer(), getModuleServer());
	}
	
	private org.jboss.ide.eclipse.as.ui.subsystems.IExploreBehavior getExploreBehavior(IServer server) {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(server);
		if( beh != null ) {
			try {
				return (IExploreBehavior)beh.getController(IExploreBehavior.SYSTEM_ID);
			} catch( CoreException ce) {
				// We should not log this. It is possible the server is in a mode where it cannot explore anything,
				// For example there's no way to explore a managed deployment.
				// In this case, the explore action is simply not enabled. 
			}
		}
		return null;
	}
	
	private void runExplore(IServer server, ModuleServer ms) {
		org.jboss.ide.eclipse.as.ui.subsystems.IExploreBehavior beh = getExploreBehavior(server);
		if( beh != null )
			beh.openExplorer(server, ms == null ? null : ms.module);
	}
	
	public void fillContextMenu(IMenuManager menu) {
		IServer server = getServer();
		if(server!=null) {
			org.jboss.ide.eclipse.as.ui.subsystems.IExploreBehavior beh = getExploreBehavior(server);
			if( beh == null || !beh.canExplore(getServer(), getModuleServer() == null ? null : getModuleServer().module))
				return;
			if( getModuleServer() != null || getServer() != null ) {
				IContributionItem menuItem = CommonActionProviderUtils.getShowInQuickMenu(menu, true);
				if (menuItem instanceof MenuManager) {
					((MenuManager) menuItem).add(exploreAction);
				}
			}
			exploreAction.setEnabled(true);
		}
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
