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
package org.jboss.tools.as.rsp.ui.dialogs;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.WorkflowPromptDetails;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;

public class WorkflowItemPanel extends Composite implements ModifyListener {
	public static final String WORKFLOW_ITEM_STRING_PROPERTY_LINK_URL = "workflow.item.string.property.link.url"; //$NON-NLS-1$

	private static final String COMBO_TRUE = Messages.WorkflowItemPanel_1;
	private static final String COMBO_FALSE = Messages.WorkflowItemPanel_2;

	private final WorkflowResponseItem item;
	private IWorkflowItemListener listener;
	private Map<String, Object> values;
	private Combo box;
	private Text field;

	public WorkflowItemPanel(Composite parent, int style, WorkflowResponseItem item, Map<String, Object> values,
			IWorkflowItemListener listener) {
		super(parent, style);
		this.item = item;
		this.values = values;
		this.listener = listener;
		String type = item.getItemType();
		String content = item.getContent();
		String msg = item.getLabel() + (content == null || content.isEmpty() ? "" : "\n" + content); //$NON-NLS-1$ //$NON-NLS-2$

		if (type == null || "workflow.prompt.small".equals(type)) { //$NON-NLS-1$
			handleSmall(item, msg);
		} else if ("workflow.prompt.large".equals(type)) { //$NON-NLS-1$
			handleLarge(item, msg);
		}

	}

	private void handleSmall(WorkflowResponseItem item, String msg) {
		setLayout(new GridLayout(2, true));
		String linkedUrl = null;
		if (item.getProperties() != null && item.getProperties().get(WORKFLOW_ITEM_STRING_PROPERTY_LINK_URL) != null) {
			linkedUrl = item.getProperties().get(WORKFLOW_ITEM_STRING_PROPERTY_LINK_URL);
		}
		if (msg != null && !msg.isEmpty()) {
			Label l = new Label(this, SWT.NONE);
			l.setText(msg);
		}
		handleInput(item, values);
	}

	private void handleLarge(WorkflowResponseItem item, String msg) {
		setLayout(new GridLayout(1, true));
		if (msg != null && !msg.isEmpty()) {
			createTextArea(msg);
		}
		handleInput(item, values);
	}

	private Text createTextArea(String msg) {
		Text jta = new Text(this, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		jta.setEditable(false);
		if (msg != null)
			jta.setText(msg);

		GridData gridData = new GridData();
		gridData.heightHint = 300;
		jta.setLayoutData(gridData);
		return jta;
	}

	private void handleInput(WorkflowResponseItem item, Map<String, Object> values) {
		WorkflowPromptDetails details = item.getPrompt();
		if (details != null) {
			if (details.getResponseType().equals(ServerManagementAPIConstants.ATTR_TYPE_BOOL)) {
				String[] vals = new String[] { COMBO_TRUE, COMBO_FALSE };
				box = new Combo(this, SWT.DEFAULT);
				box.setItems(vals);
				box.addModifyListener(this);
			} else if (details.getResponseType().equals(ServerManagementAPIConstants.ATTR_TYPE_INT)) {
				List<String> valid = item.getPrompt().getValidResponses();
				if (valid == null || valid.size() == 0) {
					field = item.getPrompt().isResponseSecret() ? new Text(this, SWT.PASSWORD)
							: new Text(this, SWT.DEFAULT);
					field.addModifyListener(this);
				} else {
					String[] vals = valid.toArray(new String[0]);
					box = new Combo(this, SWT.DEFAULT);
					box.setItems(vals);
					box.addModifyListener(this);
				}
			} else if (details.getResponseType().equals(ServerManagementAPIConstants.ATTR_TYPE_STRING)) {
				List<String> valid = item.getPrompt().getValidResponses();
				if (valid == null || valid.size() == 0) {
					field = item.getPrompt().isResponseSecret() ? new Text(this, SWT.PASSWORD)
							: new Text(this, SWT.DEFAULT);
					field.addModifyListener(this);
				} else {
					String[] vals = valid.toArray(new String[0]);
					box = new Combo(this, SWT.NONE);
					box.setItems(vals);
					box.addModifyListener(this);
				}
			} else if (details.getResponseType().equals(ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FILE)) {
				field = new Text(this, SWT.NONE);
				field.addModifyListener(this);
				Button button = new Button(this, SWT.PUSH);
				button.setText(Messages.AttributePanel_1);
				button.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						FileDialog fd = new FileDialog(Display.getDefault().getActiveShell());
						String ret = fd.open();
						if (ret != null) {
							field.setText(ret);
						}
					}

					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}
				});
			} else if (details.getResponseType().equals(ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FOLDER)) {
				field = new Text(this, SWT.NONE);
				field.addModifyListener(this);
				Button button = new Button(this, SWT.PUSH);
				button.setText(Messages.AttributePanel_1);
				button.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						DirectoryDialog fd = new DirectoryDialog(Display.getDefault().getActiveShell());
						String ret = fd.open();
						if (ret != null) {
							field.setText(ret);
						}
					}

					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}
				});
			}
		}
	}

	private String asString(String type, Object value) {
		if (ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
			return value == null ? "false" : Boolean.toString("true".equalsIgnoreCase(value.toString())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type)) {
			if (value instanceof Number) {
				return Integer.toString(((Number) value).intValue());
			} else {
				return Integer.toString(new Double(Double.parseDouble(value.toString())).intValue());
			}
		}
		return value.toString();
	}

	private Object asObject(String text) {
		String type = item.getPrompt().getResponseType();
		if (ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
			if (COMBO_TRUE.equals(text))
				return Boolean.TRUE;
			if (COMBO_FALSE.equals(text))
				return Boolean.FALSE;
			return Boolean.parseBoolean(text);
		}
		if (ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type)) {
			try {
				return Integer.parseInt(text);
			} catch (NumberFormatException nfe) {
				return null;
			}
		}
		return text;
	}

	private String getWidgetString() {
		if (box == null) {
			return field.getText();
		}
		int selInd = box.getSelectionIndex();
		if (selInd == -1)
			return null;
		return box.getItem(selInd);
	}

	@Override
	public void modifyText(ModifyEvent e) {
		values.put(item.getId(), asObject(getWidgetString()));
		listener.panelChanged();
	}
}
