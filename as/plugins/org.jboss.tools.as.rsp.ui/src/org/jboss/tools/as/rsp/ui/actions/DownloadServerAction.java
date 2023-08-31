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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.dialogs.SelectDownloadRuntimeDialog;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspCore;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.as.rsp.ui.telemetry.TelemetryService;
import org.jboss.tools.as.rsp.ui.util.ui.WorkflowUiUtility;
import org.jboss.tools.rsp.api.dao.DownloadRuntimeDescription;
import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.ListDownloadRuntimeResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;

public class DownloadServerAction extends AbstractTreeAction {
	private static final String ERROR_FETCHING_DOWNLOADABLE_RUNTIMES = org.jboss.tools.as.rsp.ui.actions.Messages.DownloadServerAction_0;
	private static final String ERROR_DOWNLOADING_RUNTIME = org.jboss.tools.as.rsp.ui.actions.Messages.DownloadServerAction_1;

	public DownloadServerAction(ISelectionProvider provider) {
		super(provider, org.jboss.tools.as.rsp.ui.actions.Messages.DownloadServerAction_2);
	}

	@Override
	protected boolean isVisible(Object[] o) {
		return safeSingleItemClass(o, IRsp.class);
	}

	@Override
	protected boolean isEnabled(Object[] o) {
		return safeSingleItemClass(o, IRsp.class) && ((IRsp) o[0]).getState() == IRspCore.IJServerState.STARTED;
	}

	@Override
	protected void singleSelectionActionPerformed(Object selected) {
		if (selected instanceof IRsp) {
			final IRsp server = (IRsp) selected;
			if (server.getState() == IRspCore.IJServerState.STARTED) {

				final RspClientLauncher client = RspCore.getDefault().getClient(server);
				final SelectDownloadRuntimeDialog td = new SelectDownloadRuntimeDialog(server);
				Display.getDefault().asyncExec(() -> {
					int ret = td.open();
					DownloadRuntimeDescription chosen = td.getSelected();
					if (chosen != null && ret == Window.OK) {
						new Thread(org.jboss.tools.as.rsp.ui.actions.Messages.DownloadServerAction_3 + server.getRspType().getName()) {
							public void run() {
								TelemetryService.logEvent(TelemetryService.TELEMETRY_DOWNLOAD_RUNTIME, chosen.getId(), 0);
								initiateDownloadRuntimeWorkflow(server, client, chosen);
							}
						}.start();
					}
				});
				new Thread(org.jboss.tools.as.rsp.ui.actions.Messages.DownloadServerAction_4) {
					public void run() {
						ListDownloadRuntimeResponse runtimeResponse = null;
						try {
							runtimeResponse = client.getServerProxy().listDownloadableRuntimes().get();
							td.setDownloadRuntimes(runtimeResponse);
						} catch (InterruptedException | ExecutionException ex) {
							apiError(ex, ERROR_FETCHING_DOWNLOADABLE_RUNTIMES);
							return;
						}

					}
				}.start();
			}
		}
	}

	private void initiateDownloadRuntimeWorkflow(IRsp server, RspClientLauncher client,
			DownloadRuntimeDescription chosen) {
		DownloadSingleRuntimeRequest req = new DownloadSingleRuntimeRequest();
		req.setDownloadRuntimeId(chosen.getId());
		WorkflowResponse resp = null;
		boolean done = false;
		do {
			try {
				resp = client.getServerProxy().downloadRuntime(req).get();
			} catch (InterruptedException | ExecutionException ex) {
				apiError(ex, ERROR_DOWNLOADING_RUNTIME);
			}
			boolean isComplete = WorkflowUiUtility.workflowComplete(resp);
			if (isComplete)
				return;

			final Map<String, Object> toSend = new HashMap<String, Object>();
			;
			final WorkflowResponse resp2 = resp;
			Display.getDefault().syncExec(() -> {
				Map<String, Object> tmp = WorkflowUiUtility.displayPromptsSeekWorkflowInput(
						org.jboss.tools.as.rsp.ui.actions.Messages.DownloadServerAction_5 + chosen.getName(), org.jboss.tools.as.rsp.ui.actions.Messages.DownloadServerAction_6,
						resp2);
				if (tmp != null)
					toSend.putAll(tmp);
			});

			if (toSend == null) {
				return; // Give up. User canceled.
			}
			DownloadSingleRuntimeRequest req2 = new DownloadSingleRuntimeRequest();
			req2.setRequestId(resp.getRequestId());
			req2.setDownloadRuntimeId(chosen.getId());
			req2.setData(toSend);
			req = req2;
		} while (!done);
	}
}