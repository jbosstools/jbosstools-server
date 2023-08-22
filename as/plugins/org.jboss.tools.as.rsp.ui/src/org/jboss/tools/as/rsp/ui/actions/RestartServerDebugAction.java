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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;

public class RestartServerDebugAction extends RestartServerAction {

	public RestartServerDebugAction(ISelectionProvider provider) {
		super(provider, "Restart Server (Debug)");
	}

	protected void startServer(ServerStateWrapper sel, RspClientLauncher client) {
		StartServerDebugAction.startServerDebugModeInternal(sel, client);
	}

	protected void telemActionCalled(ServerStateWrapper sel) {
//        String typeId = sel.getServerState().getServer().getType().getId();
//        String[] keys = new String[]{"mode"};
//        String[] vals = new String[]{"debug"};
//        TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_RESTART, typeId, null, null, keys, vals);
	}

}
