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
import java.util.concurrent.ExecutionException;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.as.rsp.ui.telemetry.TelemetryService;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;

public class StartServerAction extends AbstractTreeAction {
	public static final String ERROR_STARTING_SERVER = Messages.StartServerAction_0;

	public StartServerAction(ISelectionProvider provider) {
		super(provider, Messages.StartServerAction_1);
	}

	@Override
	protected boolean isVisible(Object[] o) {
		return safeSingleItemClass(o, ServerStateWrapper.class);
	}

	@Override
	protected boolean isEnabled(Object[] o) {
		if (o != null && o.length > 0 && o[0] instanceof ServerStateWrapper) {
			int state = ((ServerStateWrapper) o[0]).getServerState().getState();
			return state == ServerManagementAPIConstants.STATE_STOPPED
					|| state == ServerManagementAPIConstants.STATE_UNKNOWN;
		}
		return false;
	}

	@Override
	protected void singleSelectionActionPerformed(Object selected) {
		if (selected instanceof ServerStateWrapper) {
			final ServerStateWrapper sel = (ServerStateWrapper) selected;
			final RspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
			new Thread(Messages.StartServerAction_2 + sel.getServerState().getServer().getId()) {
				public void run() {
					startServerRunModeInternal(sel, client);
				}
			}.start();
		}
	}

	public static void startServerRunModeInternal(ServerStateWrapper sel, RspClientLauncher client) {
		String mode = Messages.StartServerAction_3;
		ServerAttributes sa = new ServerAttributes(sel.getServerState().getServer().getType().getId(),
				sel.getServerState().getServer().getId(), new HashMap<String, Object>());
		LaunchParameters params = new LaunchParameters(sa, mode);

		String serverType = sel.getServerState().getServer().getType().getId();
		try {
			StartServerResponse stat = client.getServerProxy().startServerAsync(params).get();
			Status statObj = stat == null ? null : stat.getStatus();
			TelemetryService.logEvent(TelemetryService.TELEMETRY_SERVER_START, 
					serverType, statObj.isOK() ? 0 : 1);
			if (!stat.getStatus().isOK()) {
				statusError(stat.getStatus(), ERROR_STARTING_SERVER);
			}
		} catch (InterruptedException | ExecutionException ex) {
			TelemetryService.logEvent(TelemetryService.TELEMETRY_SERVER_START, 
					serverType, 1);
			apiError(ex, ERROR_STARTING_SERVER);
		}
	}
}
