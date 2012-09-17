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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.view.servers.AbstractServerAction;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.launch.JBTWebLaunchableClient;

public abstract class AbstractOpenBrowserServerAction extends CommonActionProvider {

	private ICommonActionExtensionSite actionSite;
	private OpenBrowserAction openURLAction;

	
	protected abstract String getActionText();
	protected abstract boolean accepts(IServer server);
	protected abstract String getURL(IServer server) throws CoreException;
	
	protected boolean shouldAddAction() {
		ICommonViewerSite site = actionSite.getViewSite();
		IStructuredSelection selection = null;
		if (site instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
			selection = (IStructuredSelection) wsSite.getSelectionProvider()
					.getSelection();
		}
		return shouldAddForSelection(selection);
	}
	
	protected boolean shouldAddForSelection(IStructuredSelection sel) {
		IServer server = getSingleServer(sel);
		return server != null && ServerConverter.getJBossServer(server) != null; 
	}
	
	protected IServer getSingleServer(IStructuredSelection sel) {
		if( sel.size() == 1 && sel.getFirstElement() instanceof IServer )
			return (IServer)sel.getFirstElement();
		return null;
	}
	
	public void init(ICommonActionExtensionSite site) {
		super.init(site);
		this.actionSite = site;
		createActions(site);
	}
	
	protected void createActions(ICommonActionExtensionSite site) {
		ICommonViewerWorkbenchSite commonViewerWorkbenchSite = 
				CommonActionProviderUtils.getCommonViewerWorkbenchSite(site);
		if (commonViewerWorkbenchSite != null) {
			openURLAction = 
					new OpenBrowserAction(commonViewerWorkbenchSite.getSelectionProvider());
		}
	}

	public void fillContextMenu(IMenuManager menu) {
		if( shouldAddAction())
			CommonActionProviderUtils.addToShowInQuickSubMenu(openURLAction, menu, actionSite);
	}
	
	private class OpenBrowserAction extends AbstractServerAction {
		public OpenBrowserAction(ISelectionProvider sp) {
			super(sp, getActionText());
			selectionChanged(getStructuredSelection());
		}

		public boolean accept(IServer server) {
			return accepts(server);
		}
		
		public void perform(IServer server) {
			try {
				String consoleUrl = getURL(server);
				JBTWebLaunchableClient.checkedCreateInternalBrowser(
						consoleUrl, server.getName(), JBossServerUIPlugin.PLUGIN_ID, 
						JBossServerUIPlugin.getDefault().getLog());
			} catch (CoreException e) {
				IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, e.getMessage(), e);
				JBossServerUIPlugin.log(status);
				ErrorDialog.openError(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
						, Messages.ShowInAction_Error_Title
						, NLS.bind(Messages.ShowInAction_Error, getActionText()), status);
			}
		}
	}
}
