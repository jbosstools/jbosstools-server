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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.util.AlphanumComparator;
import org.jboss.tools.rsp.api.dao.DownloadRuntimeDescription;
import org.jboss.tools.rsp.api.dao.ListDownloadRuntimeResponse;

public class SelectDownloadRuntimeDialog extends TitleAreaDialog {
	private ListDownloadRuntimeResponse runtimeResponse;
	private final IRsp rsp;
	private HashMap<String, DownloadRuntimeDescription> dataMap;
	private Table table;
	private DownloadRuntimeDescription selected = null;

	public SelectDownloadRuntimeDialog(IRsp rsp) {
		super(Display.getDefault().getActiveShell());
		this.rsp = rsp;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		setTitle("Download Server Runtime (Loading...)");
		return c;
	}

	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		Composite main = new Composite(c, SWT.BORDER);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new FormLayout());
		createUI(main);
//		setMessage(info.description != null ? info.description
//				: Messages.EditorCPD_DefaultDescription);
//		getShell().setText(info.shellTitle != null ? info.shellTitle
//				: Messages.EditorCPD_DefaultShellTitle);
		return c;
	}

	private void createUI(Composite main) {
		table = new Table(main, SWT.BORDER);
		FormData fd = new FormData();
		fd.top = new FormAttachment(0, 0);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(0, 600);
		fd.bottom = new FormAttachment(0, 400);
		table.setLayoutData(fd);

		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.pack();
		updateTable(getDownloadRuntimeList());
		table.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = table.getSelection();
				if (items != null && items.length > 0) {
					String name = items[0].getText();
					selected = dataMap.get(name);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}

	public void setDownloadRuntimes(ListDownloadRuntimeResponse runtimeResponse) {
		this.runtimeResponse = runtimeResponse;
		dataMap = new HashMap<>();
		for (DownloadRuntimeDescription descriptor : runtimeResponse.getRuntimes()) {
			dataMap.put(descriptor.getName(), descriptor);
		}
		Display.getDefault().asyncExec(() -> {
			setTitle("Download Server Runtime...");
			updateTable(getDownloadRuntimeList());
		});
	}

	private void updateTable(List<DownloadRuntimeDescription> downloadRuntimeList) {
		if (downloadRuntimeList != null) {
			for (DownloadRuntimeDescription v : downloadRuntimeList) {
				TableItem item1 = new TableItem(table, SWT.NONE);
				item1.setText(new String[] { v.getName() });
				Image i = getIconForValue(v);
				if (i != null)
					item1.setImage(0, i);
			}
		}
	}

	public DownloadRuntimeDescription getSelected() {
		return selected;
	}

	private java.util.List<DownloadRuntimeDescription> getDownloadRuntimeList() {
		if (dataMap == null)
			return null;
		ArrayList<DownloadRuntimeDescription> ret = new ArrayList<>(dataMap.values());
		Collections.sort(ret, (o1, o2) -> {
			return AlphanumComparator.staticCompare(o1.getName(), o2.getName());
		});
		return ret;
	}

	protected Image getIconForValue(DownloadRuntimeDescription value) {
		if (value instanceof DownloadRuntimeDescription) {
			String serverType = ((DownloadRuntimeDescription) value).getProperties().get("wtp-runtime-type");
			return serverType == null ? null : rsp.getRspType().getIcon(serverType);
		}
		return null;
	}

}
