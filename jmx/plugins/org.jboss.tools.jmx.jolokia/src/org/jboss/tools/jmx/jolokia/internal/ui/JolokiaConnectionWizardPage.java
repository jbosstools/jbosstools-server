/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.jolokia.internal.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.MultipleInputDialog;
import org.eclipse.debug.internal.ui.preferences.DebugPreferencesMessages;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.foundation.ui.util.FormDataUtility;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.jolokia.JolokiaConnectionWrapper;
import org.jboss.tools.jmx.jolokia.internal.JolokiaConnectionProvider;
import org.jboss.tools.jmx.ui.IEditableConnectionWizardPage;

public class JolokiaConnectionWizardPage extends WizardPage implements IEditableConnectionWizardPage {
	// Used on add headers dialog
	private static final String KEY_LABEL = "Header key: ";
	private static final String VAL_LABEL = "Header value: ";
	
	
	private JolokiaConnectionWrapper initial;
	private Text nameText, urlText;
	private String name, url, getOrPost;
	private boolean ignoreSSL;
	private TableViewer viewer;
	private Table table;
	private Button addHeaderBtn, removeHeaderBtn, ignoreSSLErrorBtn;
	private Button getBtn, postBtn;
	private HashMap<String, String> headers;

	public JolokiaConnectionWizardPage() {
		super("Jolokia JMX Connection");
		if (initial == null) {
			headers = new HashMap<String, String>();
		} else {
			headers = new HashMap<String,String>(((JolokiaConnectionWrapper)initial).getHeaders());
		}
	}


	private void preCreateControl() {
		if (initial == null)
			setTitle("New Jolokia JMX Connection");
		else
			setTitle("Edit Jolokia JMX Connection");
		setImageDescriptor(JolokiaSharedImages.getDefault().descriptor(JolokiaSharedImages.JOLOKIA_BAN));
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		preCreateControl();
		Control c = fillControl(parent);
		
		
		if( initial != null ) {
			name = initial.getId();
			nameText.setText(name);
			url = initial.getUrl();
			urlText.setText(url);
			ignoreSSL = initial.isIgnoreSSLErrors();
			ignoreSSLErrorBtn.setSelection(ignoreSSL);
			getOrPost = initial.getType();
			Map<String,String> fromInitial = initial.getHeaders();
			headers.putAll(fromInitial);
			viewer.refresh();
		} else {
			getOrPost = "POST";
		}
		boolean post = "POST".equals(getOrPost);
		postBtn.setSelection(post);
		getBtn.setSelection(!post);
		
		addListeners();

		setControl(c);
	}

	private void addListeners() {
		addHeaderBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addHeaderPressed();
			}
		});
		removeHeaderBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeHeaderPressed();
			}
		});
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				name = nameText.getText();
				validate();
			}
		});
		urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				url = urlText.getText();
				validate();
			}
		});
		ignoreSSLErrorBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ignoreSSL = ignoreSSLErrorBtn.getSelection();
				validate();
			}
		});
		
		SelectionAdapter getOrPostListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if( getBtn.getSelection()) {
					getOrPost = "GET";
				} else if( postBtn.getSelection()){
					getOrPost = "POST";
				} else {
					getOrPost = null;
				}
				validate();
			}
		};
		getBtn.addSelectionListener(getOrPostListener);
		postBtn.addSelectionListener(getOrPostListener);
	}

	private void addHeaderPressed() {
		MultipleInputDialog dialog= new MultipleInputDialog(getShell(), DebugPreferencesMessages.SimpleVariablePreferencePage_13); 
		dialog.addTextField(KEY_LABEL, "", false);
		dialog.addTextField(VAL_LABEL, "", false);
		if (dialog.open() == Window.OK) {
			String k = dialog.getStringValue(KEY_LABEL).trim();
			String v = dialog.getStringValue(VAL_LABEL).trim();
			if( !k.isEmpty() && !v.isEmpty()) {
				headers.put(k,v);
				viewer.refresh();
				validate();
			}
		}

	}
	private void removeHeaderPressed() {
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if( sel != null ) {
			String k = (String)sel.getFirstElement();
			headers.remove(k);
			viewer.refresh();
			validate();
		}
	}

	public Control fillControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FillLayout());

		Composite main = new Composite(c, SWT.NONE);
		main.setLayout(new FormLayout());

		Composite primary = new Composite(main, SWT.BORDER);
		primary.setLayout(new GridLayout(4, false));

		Label name = new Label(primary, SWT.NONE);
		nameText = new Text(primary, SWT.SINGLE | SWT.BORDER);
		name.setText("Connection Name: ");

		Label url = new Label(primary, SWT.NONE);
		urlText = new Text(primary, SWT.SINGLE | SWT.BORDER);
		url.setText("Jolokia URL: ");

		// define the TableViewer
		viewer = new TableViewer(main,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		TableViewerColumn colHeaderKey = new TableViewerColumn(viewer, SWT.NONE);
		colHeaderKey.getColumn().setWidth(200);
		colHeaderKey.getColumn().setText("Header Key");
		colHeaderKey.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return element.toString();
			}
		});

		TableViewerColumn colHeaderVal = new TableViewerColumn(viewer, SWT.NONE);
		colHeaderVal.getColumn().setWidth(200);
		colHeaderVal.getColumn().setText("Header Value");
		colHeaderVal.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return headers == null ? null : headers.get(element);
			}
		});

		// make lines and header visible
		table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider(){
			public Object[] getElements(Object inputElement) {
				Set<String> s = headers.keySet();
				ArrayList<String> k = new ArrayList<String>(s);
				Collections.sort(k);
				return (String[]) k.toArray(new String[k.size()]);
			}
		});
		viewer.setInput(headers);
		
		addHeaderBtn = new Button(main, SWT.PUSH);
		removeHeaderBtn = new Button(main, SWT.PUSH);
		addHeaderBtn.setText("Add Header...");
		removeHeaderBtn.setText("Remove Header");

		if( initial != null ) {
			nameText.setEnabled(false);
		}
		
		Label requestTypeLabel = new Label(main, SWT.NONE);
		requestTypeLabel.setText("Request Type: ");
		this.getBtn = new Button(main, SWT.RADIO);
		this.getBtn.setText("GET");
		this.postBtn = new Button(main, SWT.RADIO);
		this.postBtn.setText("POST");
		
		
		
		ignoreSSLErrorBtn = new Button(main, SWT.CHECK);
		ignoreSSLErrorBtn.setText("Do NOT verify SSL Certificates (Dangerous, for local use only!!)");
		
		
		// Layout the widgets

		nameText.setLayoutData(GridDataFactory.defaultsFor(nameText).span(3, 1).create());
		urlText.setLayoutData(GridDataFactory.defaultsFor(urlText).span(3, 1).create());
		
		FormDataUtility fdu = new FormDataUtility();
		primary.setLayoutData(fdu.createFormData(0, 5, null, 0, 0, 5, 100, -5));
		
		FormData fd2 = fdu.createFormData(primary, 10, requestTypeLabel, -5, 0, 5, 80, -5);
		fd2.height = 100;
		table.setLayoutData(fd2);
		addHeaderBtn.setLayoutData(fdu.createFormData(primary, 10, null, 0, table, 5, 100, -5));
		removeHeaderBtn.setLayoutData(fdu.createFormData(addHeaderBtn, 5, null, 0, table, 5, 100, -5));
		
		/* Radio group */
		requestTypeLabel.setLayoutData(fdu.createFormData(null, 0, ignoreSSLErrorBtn, -5, 0, 5, null, 0));
		getBtn.setLayoutData(fdu.createFormData(null, 0, ignoreSSLErrorBtn, -5, requestTypeLabel, 5, null, 0));
		postBtn.setLayoutData(fdu.createFormData(null, 0, ignoreSSLErrorBtn, -5, getBtn, 5, null, 0));
		ignoreSSLErrorBtn.setLayoutData(fdu.createFormData(null, 5, 100, -5, 0, 5, 100, -5));
		return c;
	}


	private void validate() {
		JolokiaConnectionProvider provider = (JolokiaConnectionProvider) ExtensionManager.getProvider(JolokiaConnectionProvider.PROVIDER_ID);
		IConnectionWrapper inUse = provider.findConnection(nameText.getText());
		if( inUse != null && inUse != initial) {
			setErrorMessage("Connection name already in use.");
			setPageComplete(false);
			return;
		} 
		
		if( name == null || name.isEmpty() || url == null || url.isEmpty()) {
			setErrorMessage(null);
			setPageComplete(false);
			return;
		}
		
		if( getOrPost == null ) {
			setErrorMessage(null);
			setPageComplete(false);
			return;
		}
		
		setMessage(null);
		setPageComplete(true);
	}
	

	@Override
	public IConnectionWrapper getConnection() throws CoreException {
		if( initial == null ) {
			HashMap<String,Object> map = new HashMap<String,Object>();
			map.put(JolokiaConnectionWrapper.ID, name);
			map.put(JolokiaConnectionWrapper.URL, url);
			map.put(JolokiaConnectionWrapper.HEADERS, headers);
			map.put(JolokiaConnectionWrapper.IGNORE_SSL_ERRORS, ignoreSSL);
			map.put(JolokiaConnectionWrapper.GET_OR_POST, getOrPost);
			IConnectionProvider provider = ExtensionManager.getProvider(JolokiaConnectionProvider.PROVIDER_ID);
			return provider.createConnection(map);
		}
		initial.setUrl(url);
		initial.setHeaders(headers);
		initial.setIgnoreSSLErrors(ignoreSSL);
		initial.setType(getOrPost);
		return initial;
	}
	
	@Override
	public void setInitialConnection(IConnectionWrapper wrapper) {
		initial = (JolokiaConnectionWrapper) wrapper;
	}
}
