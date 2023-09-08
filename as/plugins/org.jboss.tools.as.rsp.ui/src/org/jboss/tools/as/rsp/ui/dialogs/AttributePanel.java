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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attribute;

public class AttributePanel {
	private static final int TEXTFIELD_MAX_SIZE = 10;
	private Attribute attr;
	private String key;
	private Map<String, Object> values;
	private Text field;
	private Button button;

	public AttributePanel(Composite parent, int style, String key, Attribute oneAttribute, Map<String, Object> values) {
		Composite wrapped = new Composite(parent, style);
		this.attr = oneAttribute;
		this.key = key;
		this.values = values;
		wrapped.setLayout(new FormLayout());
		wrapped.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Label name = new Label(wrapped, style);
		name.setText(key);
		name.setToolTipText(attr.getDescription());

		Object valueObj = values.get(key);
		String valueStr = (valueObj == null ? "" : valueObj.toString()); //$NON-NLS-1$

		if (oneAttribute.getType().equals(ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FILE)) {
			field = new Text(wrapped, SWT.BORDER);
			if (valueStr != null)
				field.setText(valueStr);
			button = new Button(wrapped, SWT.PUSH);
			button.setText(Messages.AttributePanel_1);
			button.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String s = new FileDialog(Display.getDefault().getActiveShell()).open();
					if (s != null)
						field.setText(s);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					String s = new FileDialog(Display.getDefault().getActiveShell()).open();
					if (s != null)
						field.setText(s);
				}
			});
		} else if (oneAttribute.getType().equals(ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FOLDER)) {
			field = new Text(wrapped, SWT.BORDER);
			if (valueStr != null)
				field.setText(valueStr);
			button = new Button(wrapped, SWT.PUSH);
			button.setText(Messages.AttributePanel_1);
			button.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					String s = new DirectoryDialog(Display.getDefault().getActiveShell()).open();
					if (s != null)
						field.setText(s);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					String s = new DirectoryDialog(Display.getDefault().getActiveShell()).open();
					if (s != null)
						field.setText(s);
				}
			});
		} else {
			field = oneAttribute.isSecret() ? new Text(wrapped, SWT.BORDER | SWT.PASSWORD) : new Text(wrapped, SWT.BORDER);
			if (values.get(key) != null) {
				field.setText(asString(oneAttribute.getType(), values.get(key)));
			} else if (attr.getDefaultVal() != null) {
				field.setText(asString(oneAttribute.getType(), attr.getDefaultVal()));
			}
		}
		if (field != null) {
			field.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					values.put(key, asObject(field.getText()));
				}
			});
		}

		FormData nameData = new FormData();
		nameData.top = new FormAttachment(0, 7);
		nameData.left = new FormAttachment(0, 5);
		name.setLayoutData(nameData);

		if (button != null) {
			FormData browseData = new FormData();
			browseData.top = new FormAttachment(0, 5);
			browseData.right = new FormAttachment(100, -5);
			button.setLayoutData(browseData);
		}

		FormData textData = new FormData();
		textData.top = new FormAttachment(0, 5);
		textData.left = new FormAttachment(name, 5);
		if (button != null) {
			textData.right = new FormAttachment(button, -5);
		} else {
			textData.right = new FormAttachment(100, -5);
		}
		field.setLayoutData(textData);
	}
	
	private String asString(String type, Object value) {
		if (ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
			return value == null ? "false" : Boolean.toString("true".equalsIgnoreCase(value.toString())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type)) {
			if (value instanceof Number) {
				return Integer.toString(((Number) value).intValue());
			} else {
				return Integer.toString(Double.valueOf(Double.parseDouble(value.toString())).intValue());
			}
		}
		return value.toString();
	}

	private Object asObject(String text) {
		String type = attr.getType();
		if (ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
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
}
