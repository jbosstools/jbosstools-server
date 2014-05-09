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
/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.ui.internal.views.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.jboss.tools.jmx.commons.Viewers;
import org.jboss.tools.jmx.commons.tree.HasViewer;
import org.jboss.tools.jmx.commons.tree.RefreshableUI;
import org.jboss.tools.jmx.commons.ui.UIConstants;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.JMXActivator;
import org.jboss.tools.jmx.core.tree.NodeProvider;
import org.jboss.tools.jmx.ui.JMXUIActivator;
import org.jboss.tools.jmx.ui.internal.actions.NewConnectionAction;
import org.jboss.tools.jmx.ui.internal.actions.RefreshAction;



/**
 * The view itself
 */
public class Navigator extends CommonNavigator implements ITabbedPropertySheetPageContributor {
    public static final String VIEW_ID = "org.jboss.tools.jmx.ui.internal.views.navigator.MBeanExplorer"; //$NON-NLS-1$
	public final class RefreshableUIImplementation implements RefreshableUI, HasViewer {
		public void fireRefresh(final Object node, final boolean full) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					getCommonViewer().refresh(node, full);
				}
			});
		}

		public void fireRefresh() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					Viewers.refresh(getCommonViewer());
				}
			});
		}

		public Viewer getViewer() {
			return getCommonViewer();
		}
	}

	private Text filterText;
	private QueryContribution query;
	private RefreshAction refreshAction;
	private Object[] rootNodes;
	protected RefreshableUI refreshableUI = new RefreshableUIImplementation();
	private boolean registered;

	public Navigator() {
		super();
	}

	@Override
	protected IAdaptable getInitialInput() {
		return this;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IPropertySheetPage.class) {
			return new TabbedPropertySheetPage(this);
		}
		return super.getAdapter(adapter);
	}

    public String getContributorId() {
        return VIEW_ID;
    }

    @Override
	public void createPartControl(Composite aParent) {
		fillActionBars();
		Composite newParent = new Composite(aParent, SWT.NONE);
		newParent.setLayout(new FormLayout());
		super.createPartControl(newParent);
		filterText = new Text(newParent, SWT.SINGLE | SWT.BORDER );

		// layout the two objects
		FormData fd = new FormData();
		fd.left = new FormAttachment(0,5);
		fd.right = new FormAttachment(100,-5);
		fd.top = new FormAttachment(0,5);
		filterText.setLayoutData(fd);
		Control topOfTree = filterText;

		// If tools.jar isn't available, JMX stuff won't work...
		boolean haveTools = false;
		try {
			haveTools = Class.forName("javax.tools.ToolProvider", false, getClass().getClassLoader()) != null; //$NON-NLS-1$
		} catch (ClassNotFoundException e1) {
			// haveTools = false;
		}
		if (!haveTools) {
			Link noJDKLlink = new Link(newParent, SWT.SINGLE);
			noJDKLlink.setText("You cannot use the JMX browser until you specify the location of the JDK\u2019s tools.jar file. Go to the <a>JMX Tools preferences page</a> to set it."); //$NON-NLS-1$
			noJDKLlink.setForeground(noJDKLlink.getDisplay().getSystemColor(SWT.COLOR_RED));
			fd = new FormData();
			fd.left = new FormAttachment(0,5);
			fd.right = new FormAttachment(100,-5);
			fd.top = new FormAttachment(filterText, 5);
			noJDKLlink.setLayoutData(fd);
			noJDKLlink.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(((Control)e.widget).getShell(), "org.jboss.tools.jmx.jvmmonitor.tools.ToolsPreferencePage", null, null); //$NON-NLS-1$
					if (pref != null) {
						pref.open();
					}
				}
			});
			topOfTree = noJDKLlink;
		}

		fd = new FormData();
		fd.left = new FormAttachment(0,0);
		fd.right = new FormAttachment(100,0);
		fd.top = new FormAttachment(topOfTree, 5);
		fd.bottom = new FormAttachment(100,0);
		final CommonViewer viewer = getCommonViewer();
		Tree tree = viewer.getTree();
		tree.setLayoutData(fd);

		filterText.setToolTipText("Type in a filter"); //$NON-NLS-1$
		filterText.setText("Type in a filter"); //$NON-NLS-1$

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				query = new QueryContribution(Navigator.this);
			}
		});
	}

	public Text getFilterText() {
		return filterText;
	}


	public RefreshAction getRefreshAction() {
		if (refreshAction == null) {
			// UIConstants.JMX_EXPLORER_VIEW_ID
			refreshAction = new RefreshAction(getViewSite().getId());
			refreshAction.setViewer(getCommonViewer());
		}
		return refreshAction;
	}

	public void fillActionBars() {
		// queryContribution = new QueryContribution(this);
		// getViewSite().getActionBars().getToolBarManager().add(queryContribution);
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(new NewConnectionAction());
		toolBarManager.add(getRefreshAction());
		/*
		toolBarManager.add(new ExpandAllAction() {

			@Override
			public void run() {

			}

		});
		 */
		toolBarManager.add(new Separator());
		getViewSite().getActionBars().updateActionBars();
	}

	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		return new CommonViewer(getViewSite().getId(), aParent,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL) {
		};
	}

	@SuppressWarnings("unchecked")
	public Object[] getRootNodes() {
		IConnectionWrapper[] connections = ExtensionManager.getAllConnections();
		List<NodeProvider> list = new ArrayList<NodeProvider>();
		JMXUIActivator.provideRootNodes(refreshableUI , list);
			/*
		try {
			LocalJmxNodeProvider localJmxProvider = new LocalJmxNodeProvider(this);
			localJmxProvider.provideRootNodes(list);
		} catch (Exception e) {
			JMXUIActivator.log(IStatus.WARNING, e.getMessage(), e);
		}
			 */
		list.addAll((Collection<? extends NodeProvider>) Arrays.asList(connections));
		List<NodeProvider> nodeProviders = JMXActivator.getNodeProviders();
		
		for (NodeProvider provider : nodeProviders)
			provider.provideRootNodes(list);

		rootNodes = list.toArray();
		Object[] answer = rootNodes;
		if (answer != null && answer.length == 0) {
			rootNodes = null;
		}
		if (rootNodes != null) {
			ensureDeployViewRegistered();
		}
		return answer;
	}

	protected void ensureDeployViewRegistered() {
		if (!registered) {
			registered = true;
		}
	}

	protected String getViewId() {
		return UIConstants.JMX_EXPLORER_VIEW_ID;
	}
}

