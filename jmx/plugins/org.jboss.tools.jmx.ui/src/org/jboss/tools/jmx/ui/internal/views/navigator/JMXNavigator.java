/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.ui.internal.views.navigator;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.jboss.tools.jmx.ui.internal.actions.NewConnectionAction;

/**
 * The view itself
 */
public class JMXNavigator extends CommonNavigator implements ITabbedPropertySheetPageContributor {
	public static final String VIEW_ID = "org.jboss.tools.jmx.ui.internal.views.navigator.MBeanExplorer"; //$NON-NLS-1$
	public JMXNavigator() {
		super();
	}
	protected IAdaptable getInitialInput() {
		return this;
	}
	public void createPartControl(Composite aParent) {
		fillActionBars();
		super.createPartControl(aParent);
	}

	protected CommonViewer createCommonViewerObject(Composite aParent) {
		FilteredTree ft = new FilteredTree(aParent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE, 
				new PatternFilter(), true) {
			protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
				return new CommonViewer(getViewSite().getId(), parent,style);
			}
		};
		return (CommonViewer)ft.getViewer();
	}
	
	public void fillActionBars() {
	    getViewSite().getActionBars().getToolBarManager().add(new NewConnectionAction());
	    getViewSite().getActionBars().getToolBarManager().add(new Separator());
	    getViewSite().getActionBars().updateActionBars();
	}
	
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter == IPropertySheetPage.class) {
                return new TabbedPropertySheetPage(this);
        }
        return super.getAdapter(adapter);
    }
    public String getContributorId() {
        return VIEW_ID;
    }
}
