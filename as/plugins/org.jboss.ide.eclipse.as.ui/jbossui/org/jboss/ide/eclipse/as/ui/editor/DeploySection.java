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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class DeploySection extends ServerEditorSection {

	private Text deployText, tempDeployText;
	private ModifyListener deployListener, tempDeployListener;
	private ServerAttributeHelper helper;
	public DeploySection() {
	}

	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		helper = new ServerAttributeHelper(server.getOriginal(), server);
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR);
		section.setText(Messages.swf_DeployEditorHeading);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		Composite composite = toolkit.createComposite(section);

		composite.setLayout(new FormLayout());
		
		Label descriptionLabel = toolkit.createLabel(composite, Messages.swf_DeploymentDescription);
		
		Label label = toolkit.createLabel(composite, Messages.swf_DeployDirectory);
		deployText = toolkit.createText(composite, getDeployDir(), SWT.BORDER);
		deployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetDeployDirCommand());
				getSaveStatus();
			}
		};
		deployText.addModifyListener(deployListener);

		Button button = toolkit.createButton(composite, Messages.browse, SWT.PUSH);
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(makeGlobal(deployText.getText()));
				String x = d.open();
				if( x != null ) {
					deployText.setText(makeRelative(x));
				}
			} 
		});
		
		Label tempDeployLabel = toolkit.createLabel(composite, Messages.swf_TempDeployDirectory);
		tempDeployText = toolkit.createText(composite, getTempDeployDir(), SWT.BORDER);
		tempDeployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetTempDeployDirCommand());
				getSaveStatus();
			}
		};
		tempDeployText.addModifyListener(tempDeployListener);

		Button tempDeployButton = toolkit.createButton(composite, Messages.browse, SWT.PUSH);
		tempDeployButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(makeGlobal(tempDeployText.getText()));
				String x = d.open();
				if( x != null ) 
					tempDeployText.setText(makeRelative(x));
			} 
		});

		
		FormData descriptionLabelData = new FormData();
		descriptionLabelData.left = new FormAttachment(0,5);
		descriptionLabelData.top = new FormAttachment(0,5);
		descriptionLabel.setLayoutData(descriptionLabelData);

		// first row
		FormData labelData = new FormData();
		labelData.left = new FormAttachment(0,5);
		labelData.right = new FormAttachment(deployText,-5);
		labelData.top = new FormAttachment(descriptionLabel,5);
		label.setLayoutData(labelData);
		
		FormData textData = new FormData();
		textData.left = new FormAttachment(button, -305);
		textData.top = new FormAttachment(descriptionLabel,5);
		textData.right = new FormAttachment(button, -5);
		deployText.setLayoutData(textData);
		
		FormData buttonData = new FormData();
		buttonData.right = new FormAttachment(100,-5);
		buttonData.left = new FormAttachment(100, -100);
		buttonData.top = new FormAttachment(descriptionLabel,2);
		button.setLayoutData(buttonData);
		
		// second row
		FormData tempLabelData = new FormData();
		tempLabelData.left = new FormAttachment(0,5);
		tempLabelData.right = new FormAttachment(deployText, -5);
		tempLabelData.top = new FormAttachment(deployText,5);
		tempDeployLabel.setLayoutData(tempLabelData);
		
		FormData tempTextData = new FormData();
		tempTextData.left = new FormAttachment(tempDeployButton, -305);
		tempTextData.top = new FormAttachment(deployText,5);
		tempTextData.right = new FormAttachment(tempDeployButton, -5);
		tempDeployText.setLayoutData(tempTextData);
		
		FormData tempButtonData = new FormData();
		tempButtonData.right = new FormAttachment(100,-5);
		tempButtonData.left = new FormAttachment(100,-100);
		tempButtonData.top = new FormAttachment(deployText,5);
		tempDeployButton.setLayoutData(tempButtonData);
		
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
	}
	
	private String getDeployDir() {
		return helper.getAttribute(IDeployableServer.DEPLOY_DIRECTORY, "");
	}
	private String getTempDeployDir() {
		String defaultt = ServerPlugin.getInstance().getStateLocation().toFile().getAbsolutePath();
		return helper.getAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, defaultt);
	}
	
	public IStatus[] getSaveStatus() {
		String error = "";
		List<Status> status = new ArrayList<Status>();
		if(!new File(makeGlobal(deployText.getText())).exists()) {
			String msg = NLS.bind(Messages.EditorDeployDNE, deployText.getText()); 
			status.add(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, msg));
			error = msg + "\n"; 
		}
		
		if(!new File(makeGlobal(tempDeployText.getText())).exists()) {
			String msg = NLS.bind(Messages.EditorTempDeployDNE, tempDeployText.getText());
			status.add(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, msg));
			error = error + msg + "\n";
		}
		
		setErrorMessage(error.equals("") ? null : error);
		return status.size() == 0 ? null : status.toArray(new IStatus[status.size()]);
	}


	
	public class SetDeployDirCommand extends ServerCommand {
		private String oldDir;
		private String newDir;
		private Text text;
		private ModifyListener listener;
		public SetDeployDirCommand() {
			super(DeploySection.this.server, Messages.EditorSetDeployLabel);
			this.text = deployText;
			this.newDir = deployText.getText();
			this.listener = deployListener;
			this.oldDir = helper.getAttribute(IDeployableServer.DEPLOY_DIRECTORY, "");
		}
		public void execute() {
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, newDir);
		}
		public void undo() {
			text.removeModifyListener(listener);
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, oldDir);
			text.setText(oldDir);
			text.addModifyListener(listener);
		}
	}

	public class SetTempDeployDirCommand extends ServerCommand {
		private String oldDir;
		private String newDir;
		private Text text;
		private ModifyListener listener;
		public SetTempDeployDirCommand() {
			super(DeploySection.this.server, Messages.EditorSetTempDeployLabel);
			text = tempDeployText;
			newDir = tempDeployText.getText();
			listener = tempDeployListener;
			oldDir = helper.getAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, "");
		}
		public void execute() {
			helper.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, newDir);
		}
		public void undo() {
			text.removeModifyListener(listener);
			helper.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, oldDir);
			text.setText(oldDir);
			text.addModifyListener(listener);
		}
	}
	
	public void dispose() {
		// ignore
	}
	
	private String makeGlobal(String path) {
		return DeployableServer.makeGlobal(getRuntime(), new Path(path)).toString();
	}
	
	private String makeRelative(String path) {
		return DeployableServer.makeRelative(getRuntime(), new Path(path)).toString();
	}
	
	private IJBossServerRuntime getRuntime() {
		IRuntime r = server.getRuntime();
		IJBossServerRuntime ajbsrt = null;
		if (r != null) {
			ajbsrt = (IJBossServerRuntime) r
					.loadAdapter(IJBossServerRuntime.class,
							new NullProgressMonitor());
		}
		return ajbsrt;
	}
}
