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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;

public class StopServerAction extends AbstractTreeAction {
	private static final String ERROR_STOPPING_SERVER = Messages.StopServerAction_0;

	public StopServerAction(ISelectionProvider provider) {
		super(provider, Messages.StopServerAction_1);
	}

	@Override
	protected boolean isVisible(Object[] o) {
		return safeSingleItemClass(o, ServerStateWrapper.class);
	}

	@Override
	protected boolean isEnabled(Object[] o) {
		if (o != null && o.length > 0 && o[0] instanceof ServerStateWrapper) {
			int state = ((ServerStateWrapper) o[0]).getServerState().getState();
			return state == ServerManagementAPIConstants.STATE_STARTED;
		}
		return false;
	}

	@Override
	protected void singleSelectionActionPerformed(Object selected) {
		if (selected instanceof ServerStateWrapper) {
			ServerStateWrapper sel = (ServerStateWrapper) selected;
			RspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
			new Thread(Messages.StopServerAction_2 + sel.getServerState().getServer().getId()) {
				public void run() {
					actionInternal(sel, client);
				}
			}.start();
		}
	}

	private void actionInternal(ServerStateWrapper sel, RspClientLauncher client) {
		StopServerAttributes ssa = new StopServerAttributes(sel.getServerState().getServer().getId(), false);
		try {
			Status stat = client.getServerProxy().stopServerAsync(ssa).get();
//            TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_STOP, stat, serverType,
//                    new String[]{"force"}, new String[]{Boolean.toString(false)});
			if (!stat.isOK()) {
				statusError(stat, ERROR_STOPPING_SERVER);
			}
		} catch (InterruptedException | ExecutionException ex) {
//            TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_STOP, serverType, null, ex,
//                    new String[]{"force"}, new String[]{Boolean.toString(false)});

			apiError(ex, ERROR_STOPPING_SERVER);
		}
	}
}
