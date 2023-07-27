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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.jboss.tools.as.rsp.ui.actions.AbstractTreeAction;
import org.jboss.tools.as.rsp.ui.actions.AddDeploymentAction;
import org.jboss.tools.as.rsp.ui.actions.CreateServerAction;
import org.jboss.tools.as.rsp.ui.actions.DeleteServerAction;
import org.jboss.tools.as.rsp.ui.actions.DownloadRspAction;
import org.jboss.tools.as.rsp.ui.actions.DownloadServerAction;
import org.jboss.tools.as.rsp.ui.actions.EditServerAction;
import org.jboss.tools.as.rsp.ui.actions.FullPublishServerAction;
import org.jboss.tools.as.rsp.ui.actions.IncrementalPublishServerAction;
import org.jboss.tools.as.rsp.ui.actions.RemoveDeploymentAction;
import org.jboss.tools.as.rsp.ui.actions.RestartServerAction;
import org.jboss.tools.as.rsp.ui.actions.RestartServerDebugAction;
import org.jboss.tools.as.rsp.ui.actions.ServerActionAction;
import org.jboss.tools.as.rsp.ui.actions.StartRspAction;
import org.jboss.tools.as.rsp.ui.actions.StartServerAction;
import org.jboss.tools.as.rsp.ui.actions.StartServerDebugAction;
import org.jboss.tools.as.rsp.ui.actions.StopRspAction;
import org.jboss.tools.as.rsp.ui.actions.StopServerAction;
import org.jboss.tools.as.rsp.ui.actions.TerminateServerAction;

public class RSPActionProvider extends CommonActionProvider {
	private ICommonActionExtensionSite actionSite;
	private AbstractTreeAction downloadRspAction;
	private StartRspAction startRspAction;
	private StopRspAction stopRspAction;
	private CreateServerAction createServerAction;
	private DownloadServerAction downloadServerAction;
	private DeleteServerAction deleteServerAction;
	
	private StartServerAction startServerAction;
	private RestartServerAction restartServerAction;
	private StartServerDebugAction startDebugServerAction;
	private RestartServerDebugAction restartDebugServerAction;
	
	
	private FullPublishServerAction fullPublishServerAction;
	private IncrementalPublishServerAction incrementalPublishServerAction;
	private StopServerAction stopServerAction;
	private TerminateServerAction terminateServerAction;
	
	private AddDeploymentAction addDeploymentAction;
	private RemoveDeploymentAction removeDeploymentAction;
	
	private EditServerAction editServerAction;
	private ServerActionAction serverActionAction;
	
	public RSPActionProvider() {
			super();
	}

	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		this.actionSite = aSite;
		createActions(aSite);
	}

	protected void createActions(ICommonActionExtensionSite aSite) {
		ICommonViewerSite site = aSite.getViewSite();
		if (site instanceof ICommonViewerWorkbenchSite) {
			StructuredViewer v = aSite.getStructuredViewer();
			if (v instanceof CommonViewer) {
				ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
				ISelectionProvider sp = wsSite.getSelectionProvider();
				this.downloadRspAction = new DownloadRspAction(sp);
				this.startRspAction = new StartRspAction(sp);
				this.stopRspAction = new StopRspAction(sp);
				this.createServerAction = new CreateServerAction(sp);
				this.downloadServerAction = new DownloadServerAction(sp);
				this.deleteServerAction = new DeleteServerAction(sp);
				this.fullPublishServerAction = new FullPublishServerAction(sp);
				this.incrementalPublishServerAction = new IncrementalPublishServerAction(sp);
				this.stopServerAction = new StopServerAction(sp);
				this.terminateServerAction = new TerminateServerAction(sp);
				this.startServerAction = new StartServerAction(sp);
				this.restartServerAction = new RestartServerAction(sp);
				this.startDebugServerAction = new StartServerDebugAction(sp);
				this.restartDebugServerAction = new RestartServerDebugAction(sp);
				this.addDeploymentAction = new AddDeploymentAction(sp);
				this.removeDeploymentAction = new RemoveDeploymentAction(sp);
				this.editServerAction = new EditServerAction(sp);
				this.serverActionAction = new ServerActionAction(sp);
			}
		}
	}

	public void fillContextMenu(IMenuManager menu) {
		ICommonViewerSite site = actionSite.getViewSite();
		IStructuredSelection selection = null;
		if (site instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
			selection = (IStructuredSelection) wsSite.getSelectionProvider().getSelection();
			
			optionallyAdd(menu, downloadRspAction);
			optionallyAdd(menu, startRspAction);
			optionallyAdd(menu, stopRspAction);
			menu.add(new Separator());
			optionallyAdd(menu, downloadServerAction);
			optionallyAdd(menu, createServerAction);
			menu.add(new Separator());
			optionallyAdd(menu, startServerAction);
			optionallyAdd(menu, startDebugServerAction);
			optionallyAdd(menu, restartServerAction);
			optionallyAdd(menu, restartDebugServerAction);
			optionallyAdd(menu, stopServerAction);
			optionallyAdd(menu, terminateServerAction);
			menu.add(new Separator());
			optionallyAdd(menu, deleteServerAction);
			menu.add(new Separator());
			optionallyAdd(menu, addDeploymentAction);
			optionallyAdd(menu, removeDeploymentAction);
			optionallyAdd(menu, fullPublishServerAction);
			optionallyAdd(menu, incrementalPublishServerAction);
			menu.add(new Separator());
			optionallyAdd(menu, editServerAction);
			optionallyAdd(menu, serverActionAction);
		}
	}

	private void optionallyAdd(IMenuManager menu, AbstractTreeAction action) {
		if( action.getShouldShow()) {
			menu.add(action);
		}
		action.setEnabled(action.getShouldEnable());
	}
	
	public class ExampleAction extends SelectionProviderAction {
		private IStructuredSelection previousSelection;

		public ExampleAction(ISelectionProvider sp) {
			super(sp, "Action Name");
			selectionChanged(sp.getSelection());
		}

		public void selectionChanged(IStructuredSelection sel) {
			if (sel.size() == 0 || sel.size() > 1) {
				setEnabled(false);
				return;
			}
			setEnabled(true);
			super.selectionChanged(sel);
		}


		@Override
		public void run() {
			// do nothing
		}
	}
}