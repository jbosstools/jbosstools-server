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
import org.jboss.tools.as.rsp.ui.util.ui.WorkflowUiUtility;
import org.jboss.tools.rsp.api.dao.DownloadRuntimeDescription;
import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.ListDownloadRuntimeResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;

public class DownloadServerAction extends AbstractTreeAction {
	private static final String ERROR_FETCHING_DOWNLOADABLE_RUNTIMES = "Error loading list of downloadable runtimes";
	private static final String ERROR_DOWNLOADING_RUNTIME = "Error downloading runtime";

	public DownloadServerAction(ISelectionProvider provider) {
		super(provider, "Download Server");
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
						new Thread("Download Runtime Workflow: " + server.getRspType().getName()) {
							public void run() {
								// TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_DOWNLOAD_RUNTIME,
								// chosen.getId());
								initiateDownloadRuntimeWorkflow(server, client, chosen);
							}
						}.start();
					}
				});
				new Thread("Load downloadable runtimes...") {
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
						"Downloading " + chosen.getName(), "This is a workflow for downloading the selected runtime",
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