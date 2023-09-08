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
package org.jboss.tools.as.rsp.ui.util.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.help.browser.IBrowser;
import org.eclipse.help.internal.browser.BrowserManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.terminal.view.core.TerminalServiceFactory;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.jboss.tools.as.rsp.ui.Messages;
import org.jboss.tools.as.rsp.ui.RspUiActivator;
import org.jboss.tools.as.rsp.ui.dialogs.WorkflowDialog;
import org.jboss.tools.as.rsp.ui.util.CommandLineUtils;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;

/**
 * Workflow Utility Some RSP calls (like download runtimes or server actions)
 * use a workflow which requires multiple rounds of back-and-forth for a
 * multi-step process to complete.
 *
 * This utility will display all prompts in dialogs or perform other workflow
 * steps like open editors or terminals as requested.
 */
public class WorkflowUiUtility {

	public static Map<String, Object> displayPromptsSeekWorkflowInput(String workflowTitle, String workflowDescription,
			WorkflowResponse resp) {
		List<WorkflowResponseItem> items = resp.getItems();
		List<WorkflowResponseItem> prompts = new ArrayList<>();
		if (items == null) {
			return new HashMap<>();
		}
		for (WorkflowResponseItem i : items) {
			String type = i.getItemType();
			if (type == null)
				type = "workflow.prompt.small"; //$NON-NLS-1$
			if (type.equals("workflow.browser.open")) { //$NON-NLS-1$
				String urlString = i.getContent();
				UIHelper.executeInUI(() -> {
					openBrowser(urlString);
				});
			} else if (type.equals("workflow.editor.open")) { //$NON-NLS-1$
				UIHelper.executeInUI(() -> {
					if (i.getProperties().get("workflow.editor.file.path") != null) { //$NON-NLS-1$
						EditorUtil.openFileInEditor(new File(i.getProperties().get("workflow.editor.file.path"))); //$NON-NLS-1$
					} else if (i.getProperties().get("workflow.editor.file.content") != null) { //$NON-NLS-1$
						EditorUtil.createAndOpenVirtualFile(i.getId(),
								i.getProperties().get("workflow.editor.file.content")); //$NON-NLS-1$
					}
				});
			} else if (type.equals("workflow.terminal.open")) { //$NON-NLS-1$
				UIHelper.executeInUI(() -> {
					try {
						String cmd = i.getProperties().get("workflow.terminal.cmd"); //$NON-NLS-1$
						String[] asArr = CommandLineUtils.translateCommandline(cmd);
						File wd = new File(System.getProperty("user.home")); //$NON-NLS-1$

						final Map<String, Object> props = new HashMap<>();
						props.put(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR, wd);
						props.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID,
								"org.eclipse.tm.terminal.connector.local.LocalConnector"); //$NON-NLS-1$
						props.put(ITerminalsConnectorConstants.PROP_IP_HOST, "localhost"); //$NON-NLS-1$
						props.put(ITerminalsConnectorConstants.PROP_TIMEOUT, Integer.valueOf(0));
						props.put(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE, Integer.valueOf(300));
						props.put(ITerminalsConnectorConstants.PROP_ENCODING, null);
						props.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID,
								"org.eclipse.tm.terminal.connector.local.launcher.local"); //$NON-NLS-1$
						props.put(ITerminalsConnectorConstants.PROP_TITLE, cmd);
						ITerminalService service = TerminalServiceFactory.getService();
						service.openConsole(props, null);
					} catch (/* IOException | */ CommandLineUtils.CommandLineException e) {
						RspUiActivator.getDefault().log(StatusFactory.errorStatus(RspUiActivator.PLUGIN_ID,
								Messages.WorkflowUiUtility_12, e));
					}
				});
			} else if (type.equals("workflow.prompt.small") || type.equals("workflow.prompt.large")) { //$NON-NLS-1$ //$NON-NLS-2$
				prompts.add(i);
			}
		}
		if (prompts.size() > 0) {
			final Boolean[] isOk = new Boolean[1];
			final Map<String, Object> values = new HashMap<String, Object>();
			Display.getDefault().syncExec(() -> {
				WorkflowDialog wd = new WorkflowDialog(workflowTitle, workflowDescription,
						prompts.toArray(new WorkflowResponseItem[0]));
				int result = wd.open();
				isOk[0] = result == Window.OK;
				if (result == Window.OK) {
					values.putAll(wd.getAttributes());
				}
			});
			return isOk[0] != null && isOk[0] ? values : null;
		}
		// Fallback impl
		return new HashMap<String, Object>();
	}

	public static boolean workflowComplete(WorkflowResponse resp) {
		if (resp == null || resp.getStatus() == null) {
			return true;
		}
		int statusSev = resp.getStatus().getSeverity();
		if (statusSev == Status.CANCEL || statusSev == Status.ERROR) {
			return true;
		}
		if (statusSev == Status.OK) {
			return true;
		}
		return false;
	}

	private static void openBrowser(String url) {
		if (url == null || "".equals(url)) { //$NON-NLS-1$
			return;
		}
		IBrowser browser = BrowserManager.getInstance().createBrowser(true);
		try {
			browser.displayURL(url);
		} catch (Exception e) {
			RspUiActivator.getDefault()
					.log(StatusFactory.errorStatus(RspUiActivator.PLUGIN_ID, Messages.WorkflowUiUtility_15 + url, e));
		}
	}
}
