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

import java.io.File;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.rsp.api.dao.Attributes;

public class AddDeploymentDialog extends TitleAreaDialog {
	private LocationPanel locationPanel;
	private Attributes attributes;
	private AttributesPanel attributesPanel;
	private Map<String, Object> attributeValues;

	public AddDeploymentDialog(Attributes attr, Map<String, Object> values) {
		super(Display.getDefault().getActiveShell());
		this.attributes = attr;
		this.attributeValues = values;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		this.getButton(IDialogConstants.OK_ID).setEnabled(false);
		setTitle("Add a deployment");
		setMessage("Add a deployment to the server");
		return c;
	}

	protected Button getOkButton() {
		return this.getButton(IDialogConstants.OK_ID);
	}

	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		Composite main = new Composite(c, SWT.BORDER);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new GridLayout(1, false));
		createUI(main);
		return c;
	}

	private void createUI(Composite main) {
		this.locationPanel = new LocationPanel(main, SWT.NONE);
		if (attributes.getAttributes().size() > 0)
			this.attributesPanel = new AttributesPanel(main, SWT.NONE, attributes, "Attributes", attributeValues);
	}

	public String getLabel() {
		return locationPanel.getPath();
	}

	public String getPath() {
		return locationPanel.getPath();
	}

	public class LocationPanel extends Composite implements ModifyListener {
		String val;
		Text field;
		Button browseFile;
		Button browseFolder;

		public LocationPanel(Composite parent, int style) {
			super(parent, style);
			setLayout(new FormLayout());
			setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			Label name = new Label(this, SWT.NONE);
			name.setText("Deployment Path");

			field = new Text(this, SWT.BORDER);
			field.addModifyListener(this);

			browseFile = new Button(this, SWT.PUSH);
			browseFile.setText("File...");
			browseFile.addSelectionListener(new SelectionListener() {
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

			browseFolder = new Button(this, SWT.PUSH);
			browseFolder.setText("Folder...");
			browseFolder.addSelectionListener(new SelectionListener() {
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

			FormData nameData = new FormData();
			nameData.top = new FormAttachment(0, 7);
			nameData.left = new FormAttachment(0, 5);
			name.setLayoutData(nameData);

			FormData browseFolderData = new FormData();
			browseFolderData.top = new FormAttachment(0, 5);
			browseFolderData.right = new FormAttachment(100, -5);
			browseFolder.setLayoutData(browseFolderData);

			FormData browseFileData = new FormData();
			browseFileData.top = new FormAttachment(0, 5);
			browseFileData.right = new FormAttachment(browseFolder, -5);
			browseFile.setLayoutData(browseFileData);

			FormData textData = new FormData();
			textData.top = new FormAttachment(0, 5);
			textData.right = new FormAttachment(browseFile, -5);
			textData.left = new FormAttachment(name, 5);
			field.setLayoutData(textData);
		}

		public void validateField() {
			this.val = field.getText();
			String s = field.getText();
			getOkButton().setEnabled(s != null && new File(s).exists());
		}

		public String getPath() {
			return val;
		}

		@Override
		public void modifyText(ModifyEvent e) {
			validateField();
		}
	}

}
