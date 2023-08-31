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

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.jboss.tools.as.rsp.ui.RspUiActivator;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.as.rsp.ui.util.PortFinder;
import org.jboss.tools.as.rsp.ui.util.ui.UIHelper;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;

public class StartServerDebugAction extends AbstractTreeAction {

	private static final String ERROR_STARTING_SERVER = Messages.StartServerDebugAction_0;
	public static final String DEBUG_DETAILS_HOST = "debug.details.host";
	public static final String DEBUG_DETAILS_PORT = "debug.details.port";
	public static final String DEBUG_DETAILS_TYPE = "debug.details.type";
	public static final String DEBUG_DETAILS_TYPE_JAVA = "java";

	public StartServerDebugAction(ISelectionProvider provider) {
		super(provider, Messages.StartServerDebugAction_5);
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
			new Thread(Messages.StartServerDebugAction_6 + sel.getServerState().getServer().getId()) {
				public void run() {
					startServerDebugModeInternal(sel, client);
				}
			}.start();
		}
	}

	public static void startServerDebugModeInternal(ServerStateWrapper sel, RspClientLauncher client) {
		String mode = Messages.StartServerDebugAction_7;
		ServerHandle handle = sel.getServerState().getServer();
		ServerAttributes sa = new ServerAttributes(handle.getType().getId(), sel.getServerState().getServer().getId(),
				new HashMap<String, Object>());
		LaunchParameters params = new LaunchParameters(sa, mode);
		final StartServerResponse response;
		try {
			response = client.getServerProxy().startServerAsync(params).get();
			String serverType = sel.getServerState().getServer().getType().getId();
			Status stat = response == null ? null : response.getStatus();
//            TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_START, stat, serverType,
//                    new String[]{"debug"}, new String[]{Boolean.toString(true)});
		} catch (InterruptedException | ExecutionException ex) {
			UIHelper.executeInUI(() -> apiError(ex, ERROR_STARTING_SERVER));
			return;
		}

		if (response == null || !response.getStatus().isOK()) {
			UIHelper.executeInUI(() -> statusError(response.getStatus(), ERROR_STARTING_SERVER));
		} else {
			connectDebugger(response, handle);
		}
	}

	private static void connectDebugger(StartServerResponse stat, ServerHandle handle) {
		String host = stat.getDetails().getProperties().get(DEBUG_DETAILS_HOST);
		String port = stat.getDetails().getProperties().get(DEBUG_DETAILS_PORT);
		String type = stat.getDetails().getProperties().get(DEBUG_DETAILS_TYPE);
		if (port == null || port.isEmpty())
			return;

		PortFinder.waitForServer(host, Integer.parseInt(port), 5000);
		if (DEBUG_DETAILS_TYPE_JAVA.equals(type)) {
			launchJavaDebugger(handle, port);
		}
	}

	private static void launchJavaDebugger(ServerHandle handle, String port) {
		new Job(Messages.StartServerDebugAction_8 + handle.getId()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					String name = Messages.StartServerDebugAction_9 + handle.getId();
					ILaunchConfigurationType launchConfigurationType = DebugPlugin.getDefault().getLaunchManager()
							.getLaunchConfigurationType(ID_REMOTE_JAVA_APPLICATION);
					ILaunchConfigurationWorkingCopy launchConfiguration = launchConfigurationType.newInstance(null,
							name);
					launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
					launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
							IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);
					Map<String, String> connectMap = new HashMap<>(2);
					connectMap.put("port", String.valueOf(port)); //$NON-NLS-1$
					connectMap.put("hostname", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$
					launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, connectMap);
					launchConfiguration.launch("debug", monitor); //$NON-NLS-1$
				} catch (CoreException e) {
					return StatusFactory.throwableToStatus(IStatus.ERROR, RspUiActivator.PLUGIN_ID, e);
				}
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		}.schedule();
	}
}
