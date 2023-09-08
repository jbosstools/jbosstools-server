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
import org.jboss.tools.as.rsp.ui.telemetry.TelemetryService;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.PublishServerRequest;
import org.jboss.tools.rsp.api.dao.Status;

public class IncrementalPublishServerAction extends AbstractTreeAction {

	private static final String ERROR_PUBLISHING = Messages.IncrementalPublishServerAction_0;

	public IncrementalPublishServerAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}

	public IncrementalPublishServerAction(ISelectionProvider provider) {
		this(provider, Messages.IncrementalPublishServerAction_1);
	}

	@Override
	protected boolean isVisible(Object[] o) {
		return safeSingleItemClass(o, ServerStateWrapper.class);
	}

	@Override
	protected boolean isEnabled(Object[] o) {
		return safeSingleItemClass(o, ServerStateWrapper.class);
	}

	protected void singleSelectionActionPerformed(Object selected) {
		singleSelectionActionPerformed(selected, ServerManagementAPIConstants.PUBLISH_INCREMENTAL);
	}

	protected void singleSelectionActionPerformed(Object selected, int kind) {
		if (selected instanceof ServerStateWrapper) {
			ServerStateWrapper server = (ServerStateWrapper) selected;
			PublishServerRequest req = new PublishServerRequest();
			req.setServer(server.getServerState().getServer());
			req.setKind(kind);
			RspClientLauncher client = RspCore.getDefault().getClient(server.getRsp());
			try {
				Status stat = client.getServerProxy().publishAsync(req).get();
				TelemetryService.logEvent(TelemetryService.TELEMETRY_PUBLISH, 
						server.getServerState().getServer().getType().getId(), stat.isOK() ? 0 : 1);
				if (!stat.isOK()) {
					statusError(stat, ERROR_PUBLISHING);
				}
			} catch (InterruptedException | ExecutionException ex) {
				TelemetryService.logEvent(TelemetryService.TELEMETRY_PUBLISH, 
						server.getServerState().getServer().getType().getId(), 1);
				apiError(ex, ERROR_PUBLISHING);
			}
		}
	}
}