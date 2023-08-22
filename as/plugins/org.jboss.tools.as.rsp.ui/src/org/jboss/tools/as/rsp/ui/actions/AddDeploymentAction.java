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
import org.jboss.tools.as.rsp.ui.dialogs.AddDeploymentDialog;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.as.rsp.ui.util.ui.UIHelper;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.ListDeploymentOptionsResponse;
import org.jboss.tools.rsp.api.dao.ServerDeployableReference;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.Status;

public class AddDeploymentAction extends AbstractTreeAction {
	private static final String ERROR_LISTING = "Error listing deployment options";
	private static final String ERROR_ADDING = "Error adding deployment";

	public AddDeploymentAction(ISelectionProvider provider) {
		super(provider, "Add Deployment");
	}

	@Override
	protected boolean isVisible(Object[] o) {
		return safeSingleItemClass(o, ServerStateWrapper.class);
	}

	@Override
	protected boolean isEnabled(Object[] o) {
		return safeSingleItemClass(o, ServerStateWrapper.class);
	}

	@Override
	protected void singleSelectionActionPerformed(Object selected) {
		if (selected instanceof ServerStateWrapper) {
			ServerStateWrapper wrap = (ServerStateWrapper) selected;
			IRsp rsp = wrap.getRsp();
			ServerHandle sh = wrap.getServerState().getServer();
			RSPServer rspServer = RspCore.getDefault().getClient(rsp).getServerProxy();
			new Thread("Adding Deployment") {
				public void run() {
					actionPerformedInternal(rspServer, sh);
				}
			}.start();
		}
	}

	protected void actionPerformedInternal(RSPServer rspServer, ServerHandle sh) {
		ListDeploymentOptionsResponse options;
		try {
			options = rspServer.listDeploymentOptions(sh).get();
		} catch (InterruptedException | ExecutionException interruptedException) {
			// TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_DEPLOYMENT_ADD,
			// sh.getType().getId(), interruptedException);
			apiError(interruptedException, ERROR_LISTING);
			return;
		}

		if (options == null || !options.getStatus().isOK()) {
			statusError(options == null ? null : options.getStatus(), ERROR_LISTING);
			return;
		}

		final Attributes attr = options.getAttributes();
		Map<String, Object> opts = new HashMap<>();
		UIHelper.executeInUI(() -> {
			Attributes attr2 = attr;
			if (attr2 == null || attr2.getAttributes() == null) {
				attr2 = new Attributes();
			}
			AddDeploymentDialog dialog = new AddDeploymentDialog(attr2, opts);
			int ret = dialog.open();
			String label = dialog.getLabel();
			String path = dialog.getPath();
			if (ret == Window.OK) {
				new Thread("Adding Deployment") {
					public void run() {
						try {
							Status stat = rspServer.addDeployable(asReference(sh, label, path, opts)).get();
							// TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_DEPLOYMENT_ADD,
							// sh.getType().getId(), stat);
							if (!stat.isOK()) {
								statusError(stat, ERROR_ADDING);
							}
						} catch (InterruptedException e) {
							// TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_DEPLOYMENT_ADD,
							// sh.getType().getId(), e);
							apiError(e, ERROR_ADDING);
						} catch (ExecutionException e) {
							// TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_DEPLOYMENT_ADD,
							// sh.getType().getId(), e);
							apiError(e, ERROR_ADDING);
						}
					}
				}.start();
			}
		});
	}

	private ServerDeployableReference asReference(ServerHandle sh, String label, String path,
			Map<String, Object> options) {
		DeployableReference ref = new DeployableReference(label, path);
		ref.setOptions(options);
		ServerDeployableReference sdr = new ServerDeployableReference(sh, ref);
		return sdr;
	}
}
