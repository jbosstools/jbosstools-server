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

import java.util.concurrent.ExecutionException;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;

public class TerminateServerAction extends AbstractTreeAction {
	private static final String ERROR_TERMINATE_SERVER = "Error terminating server";

	public TerminateServerAction(ISelectionProvider provider) {
		super(provider, "Terminate Server");
	}

	@Override
	protected boolean isVisible(Object[] o) {
		return safeSingleItemClass(o, ServerStateWrapper.class);
	}

	@Override
	protected boolean isEnabled(Object[] o) {
		if (o != null && o.length > 0 && o[0] instanceof ServerStateWrapper) {
			int state = ((ServerStateWrapper) o[0]).getServerState().getState();
			return state != ServerManagementAPIConstants.STATE_STOPPED;
		}
		return false;
	}

	@Override
	protected void singleSelectionActionPerformed(Object selected) {
		if (selected instanceof ServerStateWrapper) {
			ServerStateWrapper sel = (ServerStateWrapper) selected;
			RspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
			StopServerAttributes ssa = new StopServerAttributes(sel.getServerState().getServer().getId(), true);
			try {
				Status stat = client.getServerProxy().stopServerAsync(ssa).get();
//                TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_STOP, serverType, stat, null,
//                        new String[]{"force"}, new String[]{Boolean.toString(true)});
				if (!stat.isOK()) {
					statusError(stat, ERROR_TERMINATE_SERVER);
				}
			} catch (InterruptedException | ExecutionException ex) {
//                TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_STOP, serverType, null, ex,
//                        new String[]{"force"}, new String[]{Boolean.toString(true)});
				apiError(ex, ERROR_TERMINATE_SERVER);
			}
		}
	}

	public static void terminateServer(IRsp rsp, String id) throws DebugException {
		new Thread("Terminating RSP Server: " + id) {
			public void run() {
				RspClientLauncher client = RspCore.getDefault().getClient(rsp);
				StopServerAttributes ssa = new StopServerAttributes(id, true);
				try {
					Status stat = client.getServerProxy().stopServerAsync(ssa).get();
//	                TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_STOP, serverType, stat, null,
//	                        new String[]{"force"}, new String[]{Boolean.toString(true)});
					if (!stat.isOK()) {
						statusError(stat, ERROR_TERMINATE_SERVER);
					}
				} catch (InterruptedException | ExecutionException ex) {
//	                TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_STOP, serverType, null, ex,
//	                        new String[]{"force"}, new String[]{Boolean.toString(true)});
					apiError(ex, ERROR_TERMINATE_SERVER);
				}
			}
		}.start();
	}
}
