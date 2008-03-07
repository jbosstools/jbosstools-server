/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * @author rob.stryker <rob.stryker@redhat.com>
 * 
 */
public class RequiredCredentialsDialog extends Dialog {

	private String user, pass;
	private boolean save;
	private JBossServer jbs;
	
	public RequiredCredentialsDialog(Shell parentShell, JBossServer jbs) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.jbs = jbs;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Server Credentials Required");
	}

	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		Composite main = new Composite(c, SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new FormLayout());

		// make widgets
		Label top = new Label(main, SWT.NONE);
		Label userLabel = new Label(main, SWT.NONE);
		Label passLabel = new Label(main, SWT.NONE);
		final Text userText = new Text(main, SWT.DEFAULT);
		final Text passText = new Text(main, SWT.DEFAULT);
		userText.setEditable(true);
		passText.setEditable(true);
		final Button saveCredentials = new Button(main, SWT.CHECK);
		
		top.setLayoutData(createFormData(0,5,null,0,0,5,100,-5));
		userLabel.setLayoutData(createFormData(top, 10, null, 0, 0,5, 100, -5));
		userText.setLayoutData(createFormData(userLabel, 5, null, 0, 0,5, 100, -5));
		passLabel.setLayoutData(createFormData(userText, 10, null, 0, 0,5, 100, -5));
		passText.setLayoutData(createFormData(passLabel, 5, null, 0, 0,5, 100, -5));
		saveCredentials.setLayoutData(createFormData(passText, 10, null, 0, 0,5, 100, -5));
		
		top.setText("Your server is throwing a security exception.\nYou should make sure to open the server\neditor and save the jmx username and password there.");
		userLabel.setText(Messages.swf_Username);
		passLabel.setText(Messages.swf_Password);
		saveCredentials.setText("Save these credentials?");
		
		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				user = userText.getText();
				pass = passText.getText();
				save = saveCredentials.getSelection();
			}
		};
		SelectionListener listener2 = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				user = userText.getText();
				pass = passText.getText();
				save = saveCredentials.getSelection();
			}
		};
		userText.addModifyListener(listener);
		passText.addModifyListener(listener);
		saveCredentials.addSelectionListener(listener2);
		
		// defaults
		userText.setText(jbs.getUsername());
		userText.setSelection(0, jbs.getUsername() == null ? 0 : jbs.getUsername().length());
		passText.setText(jbs.getPassword());
		// save by default
		saveCredentials.setSelection(true);
		return c;
	}

	private FormData createFormData(Object topStart, int topOffset, Object bottomStart, int bottomOffset, 
			Object leftStart, int leftOffset, Object rightStart, int rightOffset) {
		FormData data = new FormData();

		if( topStart != null ) {
			data.top = topStart instanceof Control ? new FormAttachment((Control)topStart, topOffset) : 
				new FormAttachment(((Integer)topStart).intValue(), topOffset);
		}

		if( bottomStart != null ) {
			data.bottom = bottomStart instanceof Control ? new FormAttachment((Control)bottomStart, bottomOffset) : 
				new FormAttachment(((Integer)bottomStart).intValue(), bottomOffset);
		}

		if( leftStart != null ) {
			data.left = leftStart instanceof Control ? new FormAttachment((Control)leftStart, leftOffset) : 
				new FormAttachment(((Integer)leftStart).intValue(), leftOffset);
		}

		if( rightStart != null ) {
			data.right = rightStart instanceof Control ? new FormAttachment((Control)rightStart, rightOffset) : 
				new FormAttachment(((Integer)rightStart).intValue(), rightOffset);
		}

		return data;
	}
	
	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @return the pass
	 */
	public String getPass() {
		return pass;
	}
	
	/**
	 * @return whether to save
	 */
	public boolean getSave() {
		return save;
	}
}
