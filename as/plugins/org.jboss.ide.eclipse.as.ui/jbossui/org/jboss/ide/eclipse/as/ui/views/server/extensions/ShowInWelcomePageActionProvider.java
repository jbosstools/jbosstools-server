/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import java.net.URL;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.internal.browser.ImageResource;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.actions.ServerActionMessages;
import org.jboss.ide.eclipse.as.ui.launch.JBTWebLaunchableClient;

public class ShowInWelcomePageActionProvider extends CommonActionProvider {

	private Action action;
	private ICommonActionExtensionSite actionSite;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		actionSite = aSite;
		ICommonViewerSite site = aSite.getViewSite();
		if( site instanceof ICommonViewerWorkbenchSite ) {
			StructuredViewer v = aSite.getStructuredViewer();
			if( v instanceof CommonViewer ) {
				ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite)site;
				createActions((CommonViewer)v, wsSite.getSelectionProvider());
			}
		}
	}

	public void createActions(CommonViewer tableViewer, ISelectionProvider provider) {
		action = new Action() {
			@Override
			public void run() {
				String url = getUrl();
				if(url!=null) {
					JBTWebLaunchableClient.checkedCreateInternalBrowser(url, getServer().getName(), JBossServerUIPlugin.PLUGIN_ID, JBossServerUIPlugin.getDefault().getLog());
				}
			}
		};
		action.setText(ServerActionMessages.OpenWithBrowser);
		action.setDescription(ServerActionMessages.OpenWithBrowserDescription);
		action.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_INTERNAL_BROWSER));
	}

	private String getUrl() {
		String urlString = null;
		IServer server = getServer();
		if(server!=null && server.getServerState() == IServer.STATE_STARTED) {
			ModuleServer ms = getModuleServer();
			if(ms!=null) {
				IModule[] mss = ms.getModule();
				IModule m = getWebModule(mss);
				if(m!=null) {
					IServer s = getServer();
					Object o = s.loadAdapter(IURLProvider.class, null);
					if(o instanceof IURLProvider) {
						URL url = ((IURLProvider)o).getModuleRootURL(m);
						if(url!=null) {
							urlString = url.toString();
						}
					}
				}
			} else {
				ServerExtendedProperties props = (ServerExtendedProperties)server.loadAdapter(ServerExtendedProperties.class, new NullProgressMonitor());
				urlString = props.getWelcomePageUrl();
			}
		}
		return urlString;
	}

	private IModule getWebModule(IModule[] m) {
		if(m.length>0) {
			IModule module = m[m.length-1];
			if(isWebModule(module)) {
				return module;
			} else {
				IServer s = getServer();
				IModule[] mms = s.getChildModules(m, null);
				for (IModule child : mms) {
					if(isWebModule(child)) {
						return child;
					}
				}
			}
		}
		return null;
	}

	private boolean isWebModule(IModule module) {
		IModuleType type = module.getModuleType();
		return "jst.web".equals(type.getId()); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		if( getModuleServer() != null || getServer()!=null ) {
			IContributionItem menuItem = CommonActionProviderUtils.getShowInQuickMenu(menu, true);
			if (menuItem instanceof MenuManager) {
				((MenuManager) menuItem).add(action);
				action.setEnabled(getUrl()!=null);
			}
		}
	}

	public IServer getServer() {
		Object o = getSelection();
		if (o instanceof IServer) {
			return ((IServer)o);
		}
		if( o instanceof ModuleServer) { 
			return ((ModuleServer) o).server;
		}
		return null;
	}

	public ModuleServer getModuleServer() {
		Object o = getSelection();
		if(o instanceof ModuleServer) { 
			return ((ModuleServer) o);
		}
		return null;
	}

	protected Object getSelection() {
		ICommonViewerSite site = actionSite.getViewSite();
		IStructuredSelection selection = null;
		if (site instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite)site;
			selection = (IStructuredSelection) wsSite.getSelectionProvider().getSelection();
			if( selection.size() == 1 ) {
				return selection.getFirstElement();
			}
		}
		return null;
	}	
}