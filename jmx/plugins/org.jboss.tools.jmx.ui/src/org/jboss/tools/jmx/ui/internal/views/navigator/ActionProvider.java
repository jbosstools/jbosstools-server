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
import java.util.Iterator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.jboss.tools.common.jdt.debug.RemoteDebugActivator;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IDebuggableConnection;
import org.jboss.tools.jmx.core.tree.Root;
import org.jboss.tools.jmx.ui.UIExtensionManager;
import org.jboss.tools.jmx.ui.UIExtensionManager.ConnectionProviderUI;
import org.jboss.tools.jmx.ui.internal.actions.ConnectDebuggerAction;
import org.jboss.tools.jmx.ui.internal.actions.DeleteConnectionAction;
import org.jboss.tools.jmx.ui.internal.actions.DisconnectDebuggerAction;
import org.jboss.tools.jmx.ui.internal.actions.DoubleClickAction;
import org.jboss.tools.jmx.ui.internal.actions.EditConnectionAction;
import org.jboss.tools.jmx.ui.internal.actions.MBeanServerConnectAction;
import org.jboss.tools.jmx.ui.internal.actions.MBeanServerDisconnectAction;
import org.jboss.tools.jmx.ui.internal.actions.NewConnectionAction;
import org.jboss.tools.jmx.ui.internal.actions.RefreshAction;


/**
 * The action provider as declared in plugin.xml as relates to Common Navigator
 */
public class ActionProvider extends CommonActionProvider {
	private DoubleClickAction doubleClickAction;
	private NewConnectionAction newConnectionAction;
	private RefreshAction refreshAction;
	private ICommonActionExtensionSite site;
	public ActionProvider() {
		super();
	}
	@Override
	public void init(ICommonActionExtensionSite site) {
		super.init(site);
		this.site = site;
		doubleClickAction = new DoubleClickAction();
		newConnectionAction = new NewConnectionAction();
		String viewId = site.getViewSite().getId();
		refreshAction = new RefreshAction(viewId);
		StructuredViewer viewer = site.getStructuredViewer();
		refreshAction.setViewer(viewer);
		viewer.addSelectionChangedListener(doubleClickAction);
	}

	public StructuredViewer getStructuredViewer() {
		return site.getStructuredViewer();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, doubleClickAction);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		menu.appendToGroup("additions", refreshAction); //$NON-NLS-1$

		Object firstSelection = getFirstSelection();
		IConnectionWrapper[] connections = getWrappersFromSelection();
		if (connections != null && connections.length > 0) {
			if (!anyConnected(connections) && allControlable(connections))
				menu.add(new MBeanServerConnectAction(connections));
			else if (allControlable(connections))
				menu.add(new MBeanServerDisconnectAction(connections));
		}
		if( connections.length == 1 ) {
			String id = connections[0].getProvider().getId();
	    		ConnectionProviderUI ui = UIExtensionManager.getConnectionProviderUI(id);
			if( ui != null && ui.isEditable() && !connections[0].isConnected())
				menu.add(new EditConnectionAction(connections[0]));

			menu.add(new DeleteConnectionAction(connections));
			//menu.add(new Separator());
		}

		if (firstSelection == null || firstSelection instanceof Root || firstSelection instanceof IConnectionWrapper) {
			menu.add(newConnectionAction);
			//menu.add(new Separator());
		}
		//menu.add(refreshAction);
		
		if( firstSelection != null && firstSelection instanceof IConnectionWrapper ) {
			if(firstSelection instanceof IDebuggableConnection) {
				IDebuggableConnection debuggable = (IDebuggableConnection)firstSelection;
				if( debuggable.debugEnabled()) {
					// Add an action to connect the debugger
					menu.add(new Separator());
					if( RemoteDebugActivator.isRemoteDebuggerConnected(debuggable.getDebugHost(), debuggable.getDebugPort())) {
						menu.add(new DisconnectDebuggerAction(debuggable));
					} else {
						menu.add(new ConnectDebuggerAction(debuggable));
					}
				}
			}
		}
	}

	protected boolean anyConnected(IConnectionWrapper[] connections) {
		for (int i = 0; i < connections.length; i++)
			if (connections[i].isConnected())
				return true;
		return false;
	}

	protected boolean allControlable(IConnectionWrapper[] connections) {
		for (int i = 0; i < connections.length; i++)
			if (!connections[i].canControl())
				return false;
		return true;
	}

    protected int getSelectionSize() {
	if( getContext() != null && getContext().getSelection() != null ) {
    		ISelection sel = getContext().getSelection();
    		if( sel instanceof IStructuredSelection ) {
    			return ((IStructuredSelection)sel).size();
    		}
    	}
	return -1;
    }

	protected IConnectionWrapper[] getWrappersFromSelection() {
		ArrayList<IConnectionWrapper> list = new ArrayList<IConnectionWrapper>();
		IStructuredSelection selection = getContextSelection();
		if (selection != null) {
			Iterator i = selection.iterator();
			Object o;
			while (i.hasNext()) {
				o = i.next();
				if (o instanceof IConnectionWrapper) {
					list.add((IConnectionWrapper) o);
				}
			}
		}
		return list.toArray(new IConnectionWrapper[list.size()]);
	}

	protected Object getFirstSelection() {
		IStructuredSelection selection = getContextSelection();
		if (selection != null) {
			return selection.getFirstElement();
		}
		return null;
	}

	protected IStructuredSelection getContextSelection() {
		IStructuredSelection answer = null;
		if (getContext() != null && getContext().getSelection() != null) {
			ISelection sel = getContext().getSelection();
			if (sel instanceof IStructuredSelection) {
				answer = (IStructuredSelection) sel;
			}
		}
		return answer;
	}
}
