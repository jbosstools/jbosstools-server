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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;

public class WorkflowDialog extends TitleAreaDialog implements IWorkflowItemListener {
	private Map<String, Object> attributeValues;
	private WorkflowResponseItem[] items;
	private WorkflowItemsPanel panel;

	private String titleString;
	private String msgString;

	public WorkflowDialog(String title, String msg, WorkflowResponseItem[] items) {
		super(Display.getDefault().getActiveShell());
		this.items = items;
		this.attributeValues = new HashMap<String, Object>();
		this.titleString = title;
		this.msgString = msg;
	}

	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		Composite main = new Composite(c, SWT.BORDER);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new GridLayout(1, true));
		createUI(main);
		return c;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		this.getButton(IDialogConstants.OK_ID).setEnabled(isComplete());
		setTitle(titleString);
		setMessage(msgString);
		return c;
	}

	private void createUI(Composite main) {
		WorkflowItemsPanel itemsPanel = new WorkflowItemsPanel(main, SWT.NONE, items, null, attributeValues, this);
	}

	public Map<String, Object> getAttributes() {
		return attributeValues;
	}

	private boolean isComplete() {
		for (int i = 0; i < items.length; i++) {
			if (items[i].getPrompt() == null) {
				continue;
			}
			String type = items[i].getPrompt().getResponseType();
			if (type.equals(ServerManagementAPIConstants.ATTR_TYPE_NONE))
				continue;
			String id = items[i].getId();
			if (attributeValues.get(id) == null)
				return false;
			// API does not state which fields are required or optional. Ugh
		}
		return true;
	}

	@Override
	public void panelChanged() {
		getButton(IDialogConstants.OK_ID).setEnabled(isComplete());
	}

}
