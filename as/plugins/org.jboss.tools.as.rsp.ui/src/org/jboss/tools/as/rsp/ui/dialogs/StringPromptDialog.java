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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.rsp.api.dao.StringPrompt;

public class StringPromptDialog extends TitleAreaDialog {
	private IRsp rsp;
	private StringPrompt stringPrompt;
	private String fieldVal = ""; //$NON-NLS-1$

	public StringPromptDialog(IRsp rsp, StringPrompt stringPrompt) {
		super(Display.getDefault().getActiveShell());
		this.rsp = rsp;
		this.stringPrompt = stringPrompt;
	}

	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		Composite main = new Composite(c, SWT.BORDER);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new GridLayout(1, true));
		createUI(main);
		setTitle(Messages.StringPromptDialog_1 + rsp.getRspType().getName());
		return c;
	}

	private void createUI(Composite main) {
		Label l = new Label(main, SWT.WRAP);
		l.setText(stringPrompt.getPrompt());
		int textType = SWT.BORDER;
		if (stringPrompt.isSecret()) {
			textType = textType | SWT.PASSWORD;
		}
		Text t = new Text(main, textType);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fieldVal = t.getText();
			}
		});
	}

	public String getText() {
		return fieldVal;
	}
}