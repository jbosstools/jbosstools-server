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
package org.jboss.tools.as.rsp.ui.internal.views.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.navigator.CommonNavigator;
import org.jboss.tools.as.rsp.ui.model.IRspCoreChangeListener;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;

public class RSPNavigator extends CommonNavigator implements IRspCoreChangeListener {
	public static final String VIEW_ID = "org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPNavigator"; //$NON-NLS-1$

	public RSPNavigator() {
		super();
		RspCore.getDefault().addChangeListener(this);
	}

	protected IAdaptable getInitialInput() {
		return this;
	}

	public void createPartControl(Composite aParent) {
		fillActionBars();
		super.createPartControl(aParent);
	}

	public void fillActionBars() {
//	    getViewSite().getActionBars().getToolBarManager().add(new NewConnectionAction());
//	    getViewSite().getActionBars().getToolBarManager().add(new Separator());
//	    getViewSite().getActionBars().updateActionBars();
	}

	public String getContributorId() {
		return VIEW_ID;
	}

	@Override
	public void modelChanged(Object item) {
		Display.getDefault().asyncExec(() -> {
			// System.out.println("Refreshing " + item);
			getCommonViewer().refresh(item);
		});
	}
}
