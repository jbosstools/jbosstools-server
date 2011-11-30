/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IServer;

/**
 * @author André Dietisheim
 */
public class CommonActionProviderUtils {
	
	private static final String SHOW_IN_QUICK_MENU_ID = "org.eclipse.ui.navigate.showInQuickMenu"; //$NON-NLS-1$

	public static ICommonViewerWorkbenchSite getCommonViewerWorkbenchSite(ICommonActionExtensionSite actionExtensionSite) {
		ICommonViewerWorkbenchSite wsSite = null;
		ICommonViewerSite viewSite = actionExtensionSite.getViewSite();
		if( viewSite instanceof ICommonViewerWorkbenchSite ) {
			StructuredViewer v = actionExtensionSite.getStructuredViewer();
			if( v instanceof CommonViewer ) {
				wsSite = (ICommonViewerWorkbenchSite) viewSite;
			}
		}
		return wsSite;
	}

	public static IStructuredSelection getSelection(ICommonActionExtensionSite actionExtensionSite) {
		IStructuredSelection structuredSelection = null;
		ICommonViewerWorkbenchSite workbenchSite = getCommonViewerWorkbenchSite(actionExtensionSite);
		if (workbenchSite != null) {
			ISelectionProvider selectionProvider = workbenchSite.getSelectionProvider();
			if (selectionProvider != null) {
				ISelection selection = selectionProvider.getSelection();
				if (selection instanceof IStructuredSelection) {
					structuredSelection = (IStructuredSelection) selection;
				}
			}
		}
		return structuredSelection;
	}
	
	public static boolean isServerSelected(IStructuredSelection selection) {
		return selection != null
				&& selection.getFirstElement() instanceof IServer; 
	}
	
	public static IContributionItem getShowInQuickMenu(IMenuManager menuManager) {
		IContributionItem item = null;
		if (menuManager != null) {
			item = menuManager.find(SHOW_IN_QUICK_MENU_ID);
		}
		return item;
	}
	
	public static void addToShowInQuickSubMenu(IAction action, IMenuManager menu, ICommonActionExtensionSite actionSite) {
		IStructuredSelection selection = CommonActionProviderUtils.getSelection(actionSite);
		IContributionItem menuItem = CommonActionProviderUtils.getShowInQuickMenu(menu);
		if (menuItem instanceof MenuManager
			&& CommonActionProviderUtils.isServerSelected(selection) 
			&& action != null) {
			((MenuManager) menuItem).add(action);
		}

	}
	
	public static IWorkbenchPart getWorkbenchPart(String id) {
		IWorkbenchPart part = null;
		IWorkbenchPage page = getActiveWorkbenchPage();
		if (page != null) {
			part = page.findView(id);
		}
		return part;
	}

	public static IWorkbenchPage getActiveWorkbenchPage() {
		IWorkbenchPage page = null; 
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			page = window.getActivePage();
		}
		return page;
	}

	public static IWorkbenchPart showView(String partId) throws PartInitException {
		IWorkbenchPart part = CommonActionProviderUtils.getWorkbenchPart(partId);
		if (part == null) {
			part = CommonActionProviderUtils.getActiveWorkbenchPage().showView(partId);
		}
		return part;
	}
}
