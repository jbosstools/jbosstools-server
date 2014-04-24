/******************************************************************************* 
* Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.ui.prompt;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.wtp.ui.Messages;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;

public class ServerAlreadyStartedDialog extends TitleAreaDialog  {
	
	protected IServer server;
	protected IStatus status;
	protected boolean launch;
	public ServerAlreadyStartedDialog(IServer server, IStatus status, Shell parentShell) {
		super(parentShell);
		this.server = server;
		this.status = status;
	}
	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		setMessage(NLS.bind(Messages.ServerAlreadyStartedDialog_Message, server.getHost()), IMessageProvider.WARNING );
		setTitle(NLS.bind(Messages.ServerAlreadyStartedDialog_Title, server.getHost()));
		getShell().setText(NLS.bind(Messages.ServerAlreadyStartedDialog_Title, server.getHost()));
		return c;
	}
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		super.createButtonsForButtonBar(parent);
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite((Composite)super.createDialogArea(parent), SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new FormLayout());
		
		Label desc = new Label(main, SWT.NONE);
		desc.setText(NLS.bind(Messages.ServerAlreadyStartedDialog_Desc, status.getMessage()));
		Button connectButton = new Button(main, SWT.RADIO);
		connectButton.setText(Messages.ServerAlreadyStartedDialog_Connect);
		Button launchButton = new Button(main, SWT.RADIO);
		launchButton.setText(Messages.ServerAlreadyStartedDialog_Launch);

		connectButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				launch = false;
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		launchButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				launch = true;
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		
		desc.setLayoutData(FormDataUtility.createFormData2(0, 5, null, 0, 0, 5, 100, -5));
		connectButton.setLayoutData(FormDataUtility.createFormData2(desc,         5, null, 0, 0, 5, 100, -5));
		launchButton.setLayoutData(FormDataUtility.createFormData2(connectButton, 5, null, 0, 0, 5, 100, -5));
		
		connectButton.setSelection(true);
		return main;
	}

}
