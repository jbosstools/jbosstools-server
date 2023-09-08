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

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;

public class WorkflowItemsPanel {

	private WorkflowResponseItem[] items;
	private String title;
	private Map<String, Object> values;

	public WorkflowItemsPanel(Composite parent, int style, WorkflowResponseItem[] items, String title,
			Map<String, Object> values, IWorkflowItemListener listener) {
		this.items = items;
		this.title = title;
		this.values = values;
		Group g = new Group(parent, style);
		g.setLayout(new GridLayout(1, true));
		if (title != null) {
			g.setText(title);
		}
		for (int i = 0; i < items.length; i++) {
			new WorkflowItemPanel(g, SWT.NONE, items[i], values, listener);
		}

	}
}
