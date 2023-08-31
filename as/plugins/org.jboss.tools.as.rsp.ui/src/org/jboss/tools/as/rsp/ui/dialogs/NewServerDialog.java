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
import java.util.concurrent.ExecutionException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.as.rsp.ui.actions.AbstractTreeAction;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.telemetry.TelemetryService;
import org.jboss.tools.as.rsp.ui.util.ui.UIHelper;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerType;

public class NewServerDialog extends TitleAreaDialog implements ModifyListener {
	private static final String ERROR_CREATING_SERVER = Messages.NewServerDialog_0;

	private RspClientLauncher client;
	private ServerType serverType;
	private final Attributes optional;
	private final Attributes required;
	private AttributesPanel requiredPanel;
	private AttributesPanel optionalPanel;
	private Map<String, Object> attributeValues;
	private Text nameField;
	private String fName;

	public NewServerDialog(RspClientLauncher client, ServerType serverType, Attributes required, Attributes optional,
			Map<String, Object> values) {
		super(Display.getDefault().getActiveShell());
		this.client = client;
		this.serverType = serverType;
		this.required = required;
		this.optional = optional;
		this.attributeValues = values;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		this.getButton(IDialogConstants.OK_ID).setEnabled(false);
		setTitle(Messages.NewServerDialog_1 + this.serverType.getVisibleName());
		getShell().setText(Messages.NewServerDialog_7 + this.serverType.getVisibleName());
		setMessage(Messages.NewServerDialog_3);
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
		Composite nameComposite = new Composite(main, SWT.BORDER);
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		nameComposite.setLayout(new GridLayout(2, false));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText(Messages.NewServerDialog_4);
		this.nameField = new Text(nameComposite, SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		this.nameField.addModifyListener(this);
		if (required != null && required.getAttributes().size() > 0)
			this.requiredPanel = new AttributesPanel(main, SWT.NONE, required, Messages.NewServerDialog_5, attributeValues);
		if (optional != null && optional.getAttributes().size() > 0)
			this.optionalPanel = new AttributesPanel(main, SWT.NONE, optional, Messages.NewServerDialog_6, attributeValues);
	}

	protected void okPressed() {
		if (getOkButton().isEnabled()) {
			getOkButton().setEnabled(false);
		}
		new Thread(Messages.NewServerDialog_7) {
			public void run() {
				ServerAttributes csa = new ServerAttributes(serverType.getId(), fName, attributeValues);
				try {
					CreateServerResponse result = client.getServerProxy().createServer(csa).get();

					if (!result.getStatus().isOK()) {
						UIHelper.executeInUI(() -> {
							setMessage(result.getStatus().getMessage(), IMessageProvider.ERROR);
							getOkButton().setEnabled(true);
						});
					} else {
						UIHelper.executeInUI(() -> {
							setReturnCode(OK);
							close();
						});
					}
					TelemetryService.logEvent(TelemetryService.TELEMETRY_SERVER_CREATE, 
							serverType.getId(), result.getStatus().isOK() ? 0 : 1);
				} catch (InterruptedException | ExecutionException e) {
					TelemetryService.logEvent(TelemetryService.TELEMETRY_SERVER_CREATE, 
							serverType.getId(), 1);
					AbstractTreeAction.apiError(e, ERROR_CREATING_SERVER);
				}
			}
		}.start();

	}

	public String getName() {
		return fName;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		fName = nameField.getText();
		String nameText = nameField.getText();
		getOkButton().setEnabled(nameText != null && nameText.trim().length() > 0);
	}
}
