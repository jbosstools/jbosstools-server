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
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;

public class StartServerAction extends AbstractTreeAction {
	public static final String ERROR_STARTING_SERVER = "Error starting server";
    public StartServerAction(ISelectionProvider provider) {
		super(provider, "Start Server (run)");
	}

    @Override
    protected boolean isVisible(Object[] o) {
        return safeSingleItemClass(o, ServerStateWrapper.class);
    }

    @Override
    protected boolean isEnabled(Object[] o) {
        if( o != null && o.length > 0 && o[0] instanceof ServerStateWrapper) {
            int state = ((ServerStateWrapper)o[0]).getServerState().getState();
            return state == ServerManagementAPIConstants.STATE_STOPPED || state == ServerManagementAPIConstants.STATE_UNKNOWN;
        }
        return false;
    }

    @Override
    protected void singleSelectionActionPerformed(Object selected) {
        if (selected instanceof ServerStateWrapper) {
            final ServerStateWrapper sel = (ServerStateWrapper) selected;
            final RspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
            new Thread("Starting server: " + sel.getServerState().getServer().getId()) {
                public void run() {
                    startServerRunModeInternal(sel, client);
                }
            }.start();
        }
    }
    public static void startServerRunModeInternal(ServerStateWrapper sel, RspClientLauncher client) {
        String mode = "run";
        ServerAttributes sa = new ServerAttributes(sel.getServerState().getServer().getType().getId(),
                sel.getServerState().getServer().getId(), new HashMap<String,Object>());
        LaunchParameters params = new LaunchParameters(sa, mode);

        try {
            StartServerResponse stat = client.getServerProxy().startServerAsync(params).get();
            String serverType = sel.getServerState().getServer().getType().getId();
            Status statObj = stat == null ? null : stat.getStatus();
//            TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_START, statObj, serverType,
//                    new String[]{"debug"}, new String[]{Boolean.toString(false)});
            if( !stat.getStatus().isOK()) {
                statusError(stat.getStatus(), ERROR_STARTING_SERVER);
            }
        } catch (InterruptedException | ExecutionException ex) {
//            TelemetryService.instance().send(TelemetryService.TELEMETRY_SERVER_START, ex);
            apiError(ex, ERROR_STARTING_SERVER);
        }
    }
}
