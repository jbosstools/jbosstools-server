/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ssh.ui.editor;

import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.ssh.Messages;
import org.jboss.ide.eclipse.as.ssh.server.ISSHDeploymentConstants;
import org.jboss.ide.eclipse.as.ssh.server.SSHPublishUtil;
import org.jboss.ide.eclipse.as.ssh.server.SSHServerBehaviourDelegate.SSHPublishMethod;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentEditorTab;
import org.jboss.ide.eclipse.as.ui.editor.ModuleDeploymentPage;

public class SSHDeploymentModuleTab implements IDeploymentEditorTab {
	private ModuleDeploymentPage page;

	public SSHDeploymentModuleTab() {
	}

	public String getTabName() {
		return Messages.SSHDeploymentSectionTitle;
	}

	public void setDeploymentPage(ModuleDeploymentPage page) {
		this.page = page;
	}

	public void setDeploymentPrefs(DeploymentPreferences prefs) {
		// DO Nothing
	}

	public Control createControl(Composite parent) {
		Composite random = new Composite(parent, SWT.NONE);
		GridData randomData = new GridData(GridData.FILL_BOTH);
		random.setLayoutData(randomData);
		random.setLayout(new FormLayout());

		Composite defaultComposite = createDefaultComposite(random);
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.top = new FormAttachment(0, 5);
		fd.right = new FormAttachment(100, -5);
		defaultComposite.setLayoutData(fd);
		return random;
	}

	private Text userText, passText, deployText, hostsFileText;
	private ModifyListener userListener, passListener, deployListener, hostsListener;
	private Button zipDeployWTPProjects, browseHostsFileButton;
	private SelectionListener zipListener, browseHostsButtonListener;

	protected Composite createDefaultComposite(Composite parent) {

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent,
				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
						| ExpandableComposite.TITLE_BAR);
		section.setText(Messages.SSHDeploymentSectionTitle);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);

		composite.setLayout(new FormLayout());

		Label descriptionLabel = toolkit.createLabel(composite,
				Messages.SSHDeploymentDescription);
		FormData descriptionLabelData = new FormData();
		descriptionLabelData.left = new FormAttachment(0, 5);
		descriptionLabelData.top = new FormAttachment(0, 5);
		descriptionLabel.setLayoutData(descriptionLabelData);

		Control top = descriptionLabel;
		Composite inner = toolkit.createComposite(composite);
		inner.setLayout(new GridLayout(3, false));

		FormData innerData = new FormData();
		innerData.top = new FormAttachment(descriptionLabel, 5);
		innerData.left = new FormAttachment(0, 5);
		innerData.right = new FormAttachment(100, -5);
		inner.setLayoutData(innerData);
		top = inner;

		GridData textData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		textData.widthHint = 300;
		
		Label label = toolkit.createLabel(inner,
				Messages.DeployRootFolder);
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		deployText = toolkit.createText(inner, SSHPublishUtil.getDeployDir(page.getServer().getOriginal()), SWT.BORDER);
		deployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				page.execute(new SetDeployDirCommand());
			}
		};
		deployText.addModifyListener(deployListener);
		deployText.setEnabled(true);
		deployText.setLayoutData(textData);
		
		Label userLabel = toolkit.createLabel(inner,
				Messages.UserLabel);
		userLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		userText = toolkit.createText(inner, SSHPublishUtil.getUser(page.getServer().getOriginal()), SWT.BORDER);
		userListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				page.execute(new SetUserCommand());
			}
		};
		userText.addModifyListener(userListener);
		userText.setEnabled(true);
		userText.setLayoutData(textData);
		
		
		Label passLabel = toolkit.createLabel(inner,
				Messages.PassLabel);
		passLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		passText = toolkit.createText(inner, SSHPublishUtil.getPass(page.getServer().getOriginal()), SWT.BORDER);
		passListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				page.execute(new SetPasswordCommand());
			}
		};
		passText.addModifyListener(passListener);
		passText.setEnabled(true);
		passText.setLayoutData(textData);
		
		Label hostsLabel = toolkit.createLabel(inner,
				Messages.HostsLabel);
		hostsLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		Composite hostsFileComposite = new Composite(inner, SWT.NONE);
		hostsFileComposite.setLayoutData(textData);
		hostsFileComposite.setLayout(new GridLayout(2,false));
		
		hostsFileText = toolkit.createText(hostsFileComposite, SSHPublishUtil.getHostsFile(page.getServer().getOriginal()), SWT.BORDER);
		hostsListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				page.execute(new SetHostsFileCommand());
			}
		};
		hostsFileText.addModifyListener(hostsListener);
		hostsFileText.setEnabled(true);
		GridData hostsFileData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		hostsFileData.widthHint = 200;
		hostsFileData.grabExcessHorizontalSpace = true;
		hostsFileText.setLayoutData(hostsFileData);
		
		browseHostsFileButton = toolkit.createButton(hostsFileComposite, Messages.browse, SWT.PUSH);
		browseHostsButtonListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				browseForHostsSelected();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		browseHostsFileButton.addSelectionListener(browseHostsButtonListener);
		
		zipDeployWTPProjects = toolkit.createButton(composite,
				Messages.EditorZipDeployments, SWT.CHECK);
		boolean zippedPublisherAvailable = isSSHZippedPublisherAvailable(); 
		boolean value = getZipsSSHDeployments();
		zipDeployWTPProjects.setEnabled(zippedPublisherAvailable);
		zipDeployWTPProjects.setSelection(zippedPublisherAvailable && value);

		FormData zipButtonData = new FormData();
		zipButtonData.right = new FormAttachment(100, -5);
		zipButtonData.left = new FormAttachment(0, 5);
		zipButtonData.top = new FormAttachment(top, 5);
		zipDeployWTPProjects.setLayoutData(zipButtonData);

		zipListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				page.execute(new SetZipCommand());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		};
		zipDeployWTPProjects.addSelectionListener(zipListener);

		toolkit.paintBordersFor(composite);
		section.setClient(composite);
		page.getSaveStatus();
		return section;
	}

	protected boolean isSSHZippedPublisherAvailable() {
		IJBossServerPublisher[] publishers = 
			ExtensionManager.getDefault().getZippedPublishers();
		for( int i = 0; i < publishers.length; i++ ) {
			if( publishers[i].accepts(SSHPublishMethod.SSH_PUBLISH_METHOD, getServer().getServer(), null))
				return true;
		}
		return false;
	}
	
	protected void browseForHostsSelected() {
		FileDialog d = new FileDialog(new Shell());
		d.setFilterPath(page.makeGlobal(hostsFileText.getText()));
		String x = d.open();
		if (x != null) {
			hostsFileText.setText(x);
		}
	}
	
	protected ServerAttributeHelper getHelper() {
		return new ServerAttributeHelper(page.getServer().getOriginal(), page.getServer());
	}
	
	public class SetPropertyCommand extends ServerCommand {
		protected String oldDir;
		protected String newDir;
		protected Text text;
		protected ModifyListener listener;
		protected String attribute;
		public SetPropertyCommand(String label, Text text, ModifyListener listener, String attribute) {
			super(page.getServer(), label);
			this.text = text;
			this.newDir = text.getText();
			this.listener = listener;
			this.attribute = attribute;
			this.oldDir = getHelper().getAttribute(attribute, ""); //$NON-NLS-1$
		}
		public void execute() {
			getHelper().setAttribute(attribute, newDir);
			page.getSaveStatus();
		}
		public void undo() {
			text.removeModifyListener(listener);
			getHelper().setAttribute(attribute, oldDir);
			text.setText(oldDir);
			text.addModifyListener(listener);
			page.getSaveStatus();
		}
	}

	public class SetDeployDirCommand extends SetPropertyCommand {
		public SetDeployDirCommand() {
			super(Messages.EditorSetDeployCommandLabel, deployText, 
					deployListener, ISSHDeploymentConstants.DEPLOY_DIRECTORY);
		}
	}

	public class SetUserCommand extends SetPropertyCommand {
		public SetUserCommand() {
			super(Messages.EditorSetUserCommandLabel, userText, 
					userListener, ISSHDeploymentConstants.USERNAME);
		}
	}

	public class SetPasswordCommand extends SetPropertyCommand {
		public SetPasswordCommand() {
			super(Messages.EditorSetPasswordCommandLabel, passText, 
					passListener, ISSHDeploymentConstants.PASSWORD);
		}
	}
	
	public class SetHostsFileCommand extends SetPropertyCommand {
		public SetHostsFileCommand() {
			super(Messages.EditorSetPasswordCommandLabel, hostsFileText, 
					hostsListener, ISSHDeploymentConstants.HOSTS_FILE);
		}
	}

	public class SetZipCommand extends ServerCommand {
		boolean oldVal;
		boolean newVal;
		public SetZipCommand() {
			super(page.getServer(), Messages.EditorZipDeployments);
			oldVal = getHelper().getAttribute(ISSHDeploymentConstants.ZIP_DEPLOYMENTS_PREF, false);
			newVal = zipDeployWTPProjects.getSelection();
		}
		public void execute() {
			getHelper().setAttribute(ISSHDeploymentConstants.ZIP_DEPLOYMENTS_PREF, newVal);
			page.getSaveStatus();
		}
		public void undo() {
			zipDeployWTPProjects.removeSelectionListener(zipListener);
			zipDeployWTPProjects.setSelection(oldVal);
			getHelper().setAttribute(ISSHDeploymentConstants.ZIP_DEPLOYMENTS_PREF, oldVal);
			zipDeployWTPProjects.addSelectionListener(zipListener);
			page.getSaveStatus();
		}
	}
	

	
	private boolean getZipsSSHDeployments() {
		return getHelper().getAttribute(ISSHDeploymentConstants.ZIP_DEPLOYMENTS_PREF, false);
	}

	private IDeployableServer getServer() {
		return (IDeployableServer) page.getServer().loadAdapter(
				IDeployableServer.class, new NullProgressMonitor());
	}
}
