/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class ServerPasswordSection extends ServerEditorSection {

	private ModifyListener nameModifyListener, passModifyListener;
	private Text nameText, passText;
	private ServerAttributeHelper helper;
	
	private String passwordString;
	
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		helper = new ServerAttributeHelper(server.getOriginal(), server);
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR);
		section.setText(Messages.swf_AuthenticationGroup);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		Composite composite = toolkit.createComposite(section);

		composite.setLayout(new GridLayout(2, false));
		Label explanation = toolkit.createLabel(composite, Messages.swf_AuthorizationDescription);
		GridData d = new GridData(); d.horizontalSpan = 2;
		explanation.setLayoutData(d);
		
		Label username = toolkit.createLabel(composite, Messages.swf_Username);
		username.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		String n = ServerConverter.getJBossServer(server.getOriginal()).getUsername();
		String p = ServerConverter.getJBossServer(server.getOriginal()).getPassword();
		nameText = toolkit.createText(composite, n); 
		Label password = toolkit.createLabel(composite, Messages.swf_Password);
		password.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		passText = toolkit.createText(composite, p, SWT.SINGLE | SWT.PASSWORD);
		passwordString = p;
		
		d = new GridData(); d.grabExcessHorizontalSpace = true; d.widthHint = 100;
		nameText.setLayoutData(d);
		d = new GridData(); d.grabExcessHorizontalSpace = true; d.widthHint = 100;
		passText.setLayoutData(d);
		
		nameModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetUserCommand(server));
			}
		};
		
		passModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetPassCommand(server));
			}
		};

		nameText.addModifyListener(nameModifyListener);
		passText.addModifyListener(passModifyListener);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
	}

	
	public class SetUserCommand extends ServerWorkingCopyPropertyCommand {
		public SetUserCommand(IServerWorkingCopy server) {
			super(server, Messages.EditorChangeUsernameCommandName, nameText, nameText.getText(), 
					IJBossToolingConstants.SERVER_USERNAME, nameModifyListener);
		}
	}

	public class SetPassCommand extends ServerWorkingCopyPropertyCommand {
		public SetPassCommand(IServerWorkingCopy server) {
			super(server, Messages.EditorChangePasswordCommandName, passText, passText.getText(), 
					IJBossToolingConstants.SERVER_PASSWORD, passModifyListener);
			oldVal = passText.getText();
		}
		
		public void execute() {
			passwordString = newVal;
		}
		
		public void undo() {
			passwordString = oldVal;
			text.removeModifyListener(listener);
			text.setText(oldVal);
			text.addModifyListener(listener);
		}
	}

	/**
	 * Allow a section an opportunity to respond to a doSave request on the editor.
	 * @param monitor the progress monitor for the save operation.
	 */
	public void doSave(IProgressMonitor monitor) {
		JBossServer jbs = (JBossServer)ServerConverter.getJBossServer(server.getOriginal());
		jbs.setPassword(passwordString);
		monitor.worked(100);
	}

}
