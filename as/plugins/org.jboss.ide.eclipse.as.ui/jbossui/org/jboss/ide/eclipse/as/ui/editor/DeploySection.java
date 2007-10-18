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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class DeploySection extends ServerEditorSection {

	public DeploySection() {
		// TODO Auto-generated constructor stub
	}

	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
	}
	
	public void createSection(Composite parent)
	{
		super.createSection(parent);
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR);
		section.setText(Messages.sswf_DeployDirectory);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		Composite composite = toolkit.createComposite(section);

		composite.setLayout(new FormLayout());
		
		Label label = toolkit.createLabel(composite, Messages.sswf_DeployDirectory);
		final Text text = toolkit.createText(composite, getDeployDir(), SWT.BORDER);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetDeployDirCommand(server, text, text.getText()));
			}
		});

		Button button = toolkit.createButton(composite, Messages.browse, SWT.PUSH);
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(text.getText());
				String x = d.open();
				if( x != null ) 
					text.setText(x);
			} 
		});

		
		FormData labelData = new FormData();
		labelData.left = new FormAttachment(0,5);
		labelData.top = new FormAttachment(0,5);
		label.setLayoutData(labelData);
		
		FormData textData = new FormData();
		textData.left = new FormAttachment(label, 5);
		textData.top = new FormAttachment(0,5);
		textData.right = new FormAttachment(button,-5);
		text.setLayoutData(textData);
		
		FormData buttonData = new FormData();
		buttonData.right = new FormAttachment(100,-5);
		buttonData.top = new FormAttachment(0,5);
		button.setLayoutData(buttonData);

		toolkit.paintBordersFor(composite);
		section.setClient(composite);
	}
	
	private String getDeployDir() {
		if( server instanceof ServerWorkingCopy ) {
			return ((ServerWorkingCopy)server).getAttribute(DeployableServer.DEPLOY_DIRECTORY, "");
		}
		return "";
	}

	
	public static class SetDeployDirCommand extends ServerCommand {
		private String oldDir;
		private String newDir;
		private Text text;
		public SetDeployDirCommand(IServerWorkingCopy wc, Text text, String newDir) {
			super(wc, "SetDeployDirCommand");
			this.text = text;
			this.newDir = newDir;
			if( wc instanceof ServerWorkingCopy ) {
				this.oldDir = ((ServerWorkingCopy)wc).getAttribute(DeployableServer.DEPLOY_DIRECTORY, "");
			}
		}
		public void execute() {
			if( server instanceof ServerWorkingCopy ) 
				((ServerWorkingCopy)server).setAttribute(DeployableServer.DEPLOY_DIRECTORY, newDir);
		}
		public void undo() {
			if( server instanceof ServerWorkingCopy ) 
				((ServerWorkingCopy)server).setAttribute(DeployableServer.DEPLOY_DIRECTORY, oldDir);
			text.setText(oldDir);
		}
	}
	
	public void dispose() {
		// ignore
	}
}
