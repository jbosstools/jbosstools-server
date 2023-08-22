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

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.rsp.api.dao.Status;

public class DeleteServerAction extends AbstractTreeAction {
	private static final String ERROR_DELETING_SERVER = "Error deleting server";
	private static final String ERROR_DELETING_SERVERS = "Error deleting servers";

	public DeleteServerAction(ISelectionProvider provider) {
		super(provider, "Delete Server");
	}

	@Override
	protected boolean isVisible(Object[] o) {
		return safeMultiItemClass(o, ServerStateWrapper.class);
	}

	@Override
	protected boolean isEnabled(Object[] o) {
		return safeMultiItemClass(o, ServerStateWrapper.class);
	}

	public void run() {
		actionPerformed(getSelectionArray(getSelection()));
	}

	protected void actionPerformed(Object[] selected) {
		ArrayList<ServerStateWrapper> arr = new ArrayList<>();
		for (int i = 0; i < selected.length; i++) {
			if (selected[i] instanceof ServerStateWrapper) {
				arr.add((ServerStateWrapper) selected[i]);
			}
		}
		if (arr.size() > 0) {
			MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(),
					SWT.ICON_WARNING | SWT.YES | SWT.NO);

			messageBox.setText("Delete these servers?");
			messageBox.setMessage("Are you sure you want to delete the " + arr.size() + " selected server"
					+ (arr.size() != 1 ? "s" : ""));
			int buttonID = messageBox.open();
			if (buttonID == SWT.YES) {
				new Thread("Deleting RSP Servers") {
					public void run() {
						ArrayList<Status> fails = new ArrayList<>();
						for (int i = 0; i < arr.size(); i++) {
							ServerStateWrapper sel = arr.get(i);
							RspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
							try {
								Status stat = client.getServerProxy().deleteServer(sel.getServerState().getServer())
										.get();
								String serverType = sel.getServerState().getServer().getType().getId();
//								TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_REMOVE,
//										serverType, stat);
								if (!stat.isOK()) {
									fails.add(stat);
								}
							} catch (InterruptedException | ExecutionException ex) {
								// TelemetryService.instance().send(TelemetryService.TELEMETRY_SERVER_REMOVE,
								// ex);
								apiError(ex, arr.size() > 1 ? ERROR_DELETING_SERVERS : ERROR_DELETING_SERVER);
							}
						}
						if (fails.size() > 0) {
							String result = fails.stream().map(Status::getMessage).collect(Collectors.joining(","));
							showError(result, arr.size() > 1 ? ERROR_DELETING_SERVERS : ERROR_DELETING_SERVER);
						}
					}
				}.start();
			}
		}
	}

}
