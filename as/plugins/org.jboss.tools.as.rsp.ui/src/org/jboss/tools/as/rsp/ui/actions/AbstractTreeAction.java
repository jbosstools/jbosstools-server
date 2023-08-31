/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jboss.tools.as.rsp.ui.RspUiActivator;
import org.jboss.tools.rsp.api.dao.Status;

public abstract class AbstractTreeAction extends SelectionProviderAction {

	private static final String INVALID_RESPONSE = Messages.AbstractTreeAction_0;
	protected boolean shouldEnable = false;
	protected boolean shouldShow = true;

	protected AbstractTreeAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}

	public static void showError(String msg, String title) {
		RspUiActivator.getDefault().pluginLog().logError(title + ": " + msg); //$NON-NLS-1$
	}

	public static void apiError(Exception exception, String title) {
		showError(exception == null ? Messages.AbstractTreeAction_2 : exception.getMessage(), title);
	}

	public static void statusError(Status stat, String title) {
		showError(stat == null ? INVALID_RESPONSE : stat.getMessage(), title);
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		Object[] o2 = new Object[0];
		if (selection instanceof TreeSelection) {
			o2 = ((TreeSelection) selection).toArray();
		} else {
			o2 = new Object[] { selection.getFirstElement() };
		}
		this.shouldEnable = isEnabled(o2);
		this.shouldShow = isVisible(o2);
		setEnabled(this.shouldEnable);
	}

	public boolean getShouldEnable() {
		return this.isEnabled(getSelectionArray(getSelection()));
	}

	public boolean getShouldShow() {
		return this.isVisible(getSelectionArray(getSelection()));
	}

	public void run() {
		singleSelectionActionPerformed(getSingleSelection(getSelection()));
	}

	protected Object getSingleSelection(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			return ((IStructuredSelection) sel).getFirstElement();
		}
		return null;
	}

	protected Object[] getSelectionArray(ISelection sel) {
		if (sel instanceof TreeSelection) {
			return ((TreeSelection) sel).toArray();
		}
		if (sel instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection) sel).getFirstElement();
			if (o == null)
				return new Object[0];
			return new Object[] { o };
		}
		return new Object[0];

	}

	protected boolean safeSingleItemClass(Object[] arr, Class c) {
		return arr != null && arr.length == 1 && c.isInstance(arr[0]);
	}

	protected boolean safeMultiItemClass(Object[] arr, Class c) {
		if (arr != null) {
			for (int i = 0; i < arr.length; i++) {
				if (!c.isInstance(arr[i])) {
					return false;
				}
			}
		}
		return true;
	}

	protected boolean isEnabled(Object[] o2) {
		return false;
	}

	protected boolean isVisible(Object[] o) {
		return false;
	}

	protected void singleSelectionActionPerformed(Object selected) {

	}

}
