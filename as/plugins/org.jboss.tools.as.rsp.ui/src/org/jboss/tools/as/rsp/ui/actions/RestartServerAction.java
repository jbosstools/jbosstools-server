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
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspCoreChangeListener;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;

public class RestartServerAction extends AbstractTreeAction {
	private static final String ERROR_STOPPING_SERVER = "Error stopping server";

	public RestartServerAction(ISelectionProvider provider) {
		this(provider, "Restart Server (run)");
	}

	public RestartServerAction(ISelectionProvider provider, String name) {
		super(provider, name);
	}

	@Override
	protected boolean isVisible(Object[] o) {
		return safeSingleItemClass(o, ServerStateWrapper.class);
	}

	@Override
	protected boolean isEnabled(Object[] o) {
		if (o != null && o.length == 1 && o[0] instanceof ServerStateWrapper) {
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
			telemActionCalled(sel);

			new Thread("Stop Server: " + sel.getServerState().getServer().getId()) {
				public void run() {
					actionInternal(sel, client);
				}
			}.start();
		}
	}

	protected void telemActionCalled(ServerStateWrapper sel) {
		String typeId = sel.getServerState().getServer().getType().getId();
		String[] keys = new String[] { "mode" };
		String[] vals = new String[] { "run" };
		// TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_RESTART,
		// typeId, null, null, keys, vals);
	}

	private void actionInternal(ServerStateWrapper sel, RspClientLauncher client) {
		IRspCoreChangeListener listener = new IRspCoreChangeListener() {
			@Override
			public void modelChanged(Object item) {
				if (item instanceof IRsp) {
					IRsp r = (IRsp) item;
					ServerState state = r.getModel().findServerInRsp(r, sel.getServerState().getServer().getId());
					if (state != null) {
						if (state.getState() == ServerManagementAPIConstants.STATE_STOPPED) {
							final IRspCoreChangeListener l2 = this;
							new Thread("Restart server") {
								public void run() {
									RspCore.getDefault().removeChangeListener(l2);
									startServer(sel, client);
								}
							}.start();
						}
					}
				}
			}

		};
		RspCore.getDefault().addChangeListener(listener);
		stopServer(sel, client);
	}

	protected void startServer(ServerStateWrapper sel, RspClientLauncher client) {
		StartServerAction.startServerRunModeInternal(sel, client);
	}

	private void stopServer(ServerStateWrapper sel, RspClientLauncher client) {
		StopServerAttributes ssa = new StopServerAttributes(sel.getServerState().getServer().getId(), false);
		try {
			Status stat = client.getServerProxy().stopServerAsync(ssa).get();
			if (!stat.isOK()) {
				statusError(stat, ERROR_STOPPING_SERVER);
			}
		} catch (InterruptedException | ExecutionException ex) {
			apiError(ex, ERROR_STOPPING_SERVER);
		}
	}
}
