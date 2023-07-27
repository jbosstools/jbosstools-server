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

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.dialogs.SelectServerActionDialog;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;
import org.jboss.tools.as.rsp.ui.model.IRspCore;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.as.rsp.ui.telemetry.TelemetryService;
import org.jboss.tools.as.rsp.ui.util.ui.UIHelper;
import org.jboss.tools.as.rsp.ui.util.ui.WorkflowUiUtility;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;

public class ServerActionAction extends AbstractTreeAction {

  private static final String ERROR_LISTING_ACTIONS = "Error listing server actions";
  private static final String ERROR_EXECUTE_ACTIONS = "Error executing server action";
	public ServerActionAction(ISelectionProvider provider) {
		super(provider, "Server Actions...");
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
		new Thread("Server Actions") {
			public void run() {
		    	runInBackground(selected);
			}
		}.start();
    }
    protected void runInBackground(Object selected) {

        if( selected instanceof ServerStateWrapper) {
        	ServerStateWrapper state = (ServerStateWrapper)selected;
            RspClientLauncher client = RspCore.getDefault().getClient(state.getRsp());
            if (state.getRsp().getState() == IRspCore.IJServerState.STARTED) {
                ListServerActionResponse actionResponse = null;
                try {
                    actionResponse = client.getServerProxy().listServerActions(state.getServerState().getServer()).get();
                } catch (InterruptedException | ExecutionException ex) {
                    apiError(ex, ERROR_LISTING_ACTIONS);
                    return;
                }

            	final ListServerActionResponse response2 = actionResponse;
                UIHelper.executeInUI(() -> {
                    SelectServerActionDialog td = new SelectServerActionDialog(state,response2);
                    int ret = td.open();
                    ServerActionWorkflow chosen = td.getSelected();
//                    TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_ACTION,
//                            state.getServerState().getServer().getType().getId(), null, null,
//                            new String[]{"actionId"}, new String[]{chosen.getActionId()});
                    if( chosen != null && ret == Window.OK) {
                        new Thread("Server Action Workflow: " + chosen.getActionLabel()) {
                            public void run() {
                                initiateActionWorkflow(state, client, chosen);
                            }
                        }.start();
                    }
                });
            }
        }
    }

    private void initiateActionWorkflow(ServerStateWrapper state, RspClientLauncher client , ServerActionWorkflow chosen) {
        WorkflowResponse resp = chosen.getActionWorkflow();
        boolean done = false;
        while (!done) {
        	String title = state.getServerState().getServer().getId() + " Action: " + chosen.getActionLabel();
            Map<String, Object> toSend = WorkflowUiUtility.displayPromptsSeekWorkflowInput(title, "", resp);
            if (toSend == null) {
                return; // Give up. User canceled.
            }
            boolean isComplete = WorkflowUiUtility.workflowComplete(resp);
            if (isComplete)
                return;
            ServerActionRequest req = new ServerActionRequest();
            req.setActionId(chosen.getActionId());
            req.setServerId(state.getServerState().getServer().getId());
            req.setData(toSend);
            try {
                resp = client.getServerProxy().executeServerAction(req).get();
            } catch (InterruptedException | ExecutionException e) {
                apiError(e, ERROR_EXECUTE_ACTIONS);
            }
        }
    }
}
