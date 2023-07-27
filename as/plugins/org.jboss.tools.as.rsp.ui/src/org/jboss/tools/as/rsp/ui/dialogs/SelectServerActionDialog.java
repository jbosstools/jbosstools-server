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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;

public class SelectServerActionDialog extends TitleAreaDialog {

	private ListServerActionResponse actionResponse;
    private ServerStateWrapper state;
    private HashMap<String, ServerActionWorkflow> dataMap;
    private ServerActionWorkflow selected = null;
    private Table table;
    public SelectServerActionDialog(ServerStateWrapper state, ListServerActionResponse actionResponse) {
		super(Display.getDefault().getActiveShell());
		this.state = state;
		this.actionResponse = actionResponse;
		dataMap = new HashMap<>();
		for (ServerActionWorkflow descriptor : actionResponse.getWorkflows()) {
			dataMap.put(descriptor.getActionId(), descriptor);
		}
	} 

    @Override
    protected Control createContents(Composite parent) { 
    	Control c = super.createContents(parent);
        setTitle("Server Actions");
        setMessage("Please select a server action to run.");
    	return c;
    }


	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite)super.createDialogArea(parent);
		Composite main = new Composite(c, SWT.BORDER);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new FormLayout());
		createUI(main);
		return c;
	}
	

    private void createUI(Composite main) {
		// TODO Auto-generated method stub
		table = new Table(main, SWT.BORDER);
		FormData fd = new FormData();
		fd.top = new FormAttachment(0,0);
		fd.left = new FormAttachment(0,0);
		fd.right = new FormAttachment(0,600);
		fd.bottom = new FormAttachment(0,400);		
		table.setLayoutData(fd);
		
	    TableColumn column1 = new TableColumn(table, SWT.NONE);
	    column1.pack();
	    updateTable();
	    table.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = table.getSelection();
				if( items != null && items.length > 0 ) {
					String name = items[0].getText();
					selected = dataMap.get(name);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}});
	}

    private void updateTable() {
		for( ServerActionWorkflow v : actionResponse.getWorkflows()) {
    		TableItem item1 = new TableItem(table, SWT.NONE);
    	    item1.setText(new String[] { v.getActionLabel() });
		}
	}

	public ServerActionWorkflow getSelected() {
        return selected;
    }
}
