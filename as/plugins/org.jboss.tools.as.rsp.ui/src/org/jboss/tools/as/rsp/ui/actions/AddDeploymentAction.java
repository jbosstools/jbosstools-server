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
import org.jboss.tools.as.rsp.ui.RspUiActivator;
import org.jboss.tools.as.rsp.ui.dialogs.AddDeploymentDialog;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.as.rsp.ui.telemetry.TelemetryService;
import org.jboss.tools.as.rsp.ui.util.ui.UIHelper;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.ListDeploymentOptionsResponse;
import org.jboss.tools.rsp.api.dao.ServerDeployableReference;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;

public class AddDeploymentAction extends AbstractTreeAction {
	private static final String ERROR_LISTING = Messages.AddDeploymentAction_errorListingDeploymentOptions;
	private static final String ERROR_ADDING = Messages.AddDeploymentAction_errorAddingDeployment;

	public AddDeploymentAction(ISelectionProvider provider) {
		super(provider, Messages.AddDeploymentAction_addDeployment);
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
			new Thread(Messages.AddDeploymentAction_addingDeployment) {
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
			TelemetryService.logEvent(TelemetryService.TELEMETRY_DEPLOYMENT_ADD, sh.getType().getId());
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
				new Thread(Messages.AddDeploymentAction_addingDeployment) { //$NON-NLS-1$
					public void run() {
						try {
							Status stat = rspServer.addDeployable(asReference(sh, label, path, opts)).get();
							TelemetryService.logEvent(TelemetryService.TELEMETRY_DEPLOYMENT_ADD, sh.getType().getId(), stat.isOK() ? 0 : 1);
							if (!stat.isOK()) {
								statusError(stat, ERROR_ADDING);
							}
						} catch (InterruptedException e) {
							TelemetryService.logEvent(TelemetryService.TELEMETRY_DEPLOYMENT_ADD, sh.getType().getId(), 1);
							apiError(e, ERROR_ADDING);
						} catch (ExecutionException e) {
							TelemetryService.logEvent(TelemetryService.TELEMETRY_DEPLOYMENT_ADD, sh.getType().getId(), 1);
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
