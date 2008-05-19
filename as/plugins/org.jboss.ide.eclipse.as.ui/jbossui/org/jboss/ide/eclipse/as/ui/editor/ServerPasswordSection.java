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
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
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
		
		toolkit.createLabel(composite, Messages.swf_Username);
		String n = helper.getAttribute(JBossServer.SERVER_USERNAME, "");
		String p = helper.getAttribute(JBossServer.SERVER_PASSWORD, "");
		nameText = toolkit.createText(composite, n); 
		toolkit.createLabel(composite, Messages.swf_Password);
		passText = toolkit.createText(composite, p);
		
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

	
	public class SetUserCommand extends SetVarCommand {
		public SetUserCommand(IServerWorkingCopy server) {
			super(server, "Change Username", nameText, nameText.getText(), 
					JBossServer.SERVER_USERNAME, nameModifyListener);
		}
	}

	public class SetPassCommand extends SetVarCommand {
		public SetPassCommand(IServerWorkingCopy server) {
			super(server, "Change Password", passText, passText.getText(), 
					JBossServer.SERVER_PASSWORD, passModifyListener);
		}
	}

	public class SetVarCommand extends ServerCommand {
		private String oldVal;
		private String newVal;
		private String key;
		private Text text;
		private ModifyListener listener;
		
		public SetVarCommand(IServerWorkingCopy wc, String name, 
				Text text, String newVal, String attributeKey,
				ModifyListener listener) {
			super(wc, name);
			this.text = text;
			this.key = attributeKey;
			this.newVal = newVal;
			this.listener = listener;
			this.oldVal = helper.getAttribute(attributeKey, "");
		}
		
		public void execute() {
			helper.setAttribute(key, newVal);
		}
		
		public void undo() {
			text.removeModifyListener(listener);
			helper.setAttribute(key, oldVal);
			text.setText(oldVal);
			text.addModifyListener(listener);
		}
	}
}
