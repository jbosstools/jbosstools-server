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
package org.jboss.ide.eclipse.as.ui.views.as7.management.content;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * RefreshActionProvider
 * 
 * <p/>
 * Provides a refresh action for IContainer nodes.
 * 
 * @author Rob Cernich
 */
@SuppressWarnings("restriction")
public class RefreshActionProvider extends CommonActionProvider {

    private RefreshAction refreshAction;

    public RefreshActionProvider() {
    }

    public void init(ICommonActionExtensionSite aSite) {
        super.init(aSite);
        refreshAction = new RefreshAction(aSite.getStructuredViewer());
    }

    public void fillContextMenu(IMenuManager menu) {
        menu.add(refreshAction);
    }

    public void dispose() {
        if (refreshAction != null) {
            refreshAction.dispose();
            refreshAction = null;
        }
        super.dispose();
    }

    private class RefreshAction extends SelectionProviderAction {

        public RefreshAction(ISelectionProvider selectionProvider) {
            super(selectionProvider, WorkbenchMessages.Workbench_refresh);
        }

        public void run() {
            IStructuredSelection selection = getStructuredSelection();
            if (selection == null || selection.isEmpty()) {
                return;
            }
            IContainerNode<?> container = (IContainerNode<?>) ((IStructuredSelection) selection).getFirstElement();
            container.clearChildren();
            RefreshActionProvider.this.getActionSite().getStructuredViewer().refresh(container);
        }

    }
}
