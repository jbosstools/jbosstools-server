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
package org.jboss.ide.eclipse.as.rse.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.rse.ui.RSEDeploymentPreferenceUI.RSEDeploymentPreferenceComposite;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;

public class DeployOnlyRSEPrefComposite extends
		RSEDeploymentPreferenceComposite {
	public DeployOnlyRSEPrefComposite(Composite parent, int style, IServerModeUICallback callback) {
		super(parent, style, callback);
	}
	protected void createRSEWidgets(Composite child) {
		handleDeployOnlyServer(child);
	}

	private Text deployText, tempDeployText;
	private Button deployButton, tempDeployButton;
	private ModifyListener deployListener, tempDeployListener;


	private void handleDeployOnlyServer(Composite composite) {
		Label label = new Label(this, SWT.NONE);
		label.setText(Messages.swf_DeployDirectory);
		deployText = new Text(this, SWT.BORDER);
		deployText.setText(getServer().getDeployFolder());
		deployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				callback.execute(new SetDeployDirCommand());
			}
		};
		deployText.addModifyListener(deployListener);

		deployButton = new Button(this, SWT.PUSH);
		deployButton.setText(Messages.browse);
		label.setLayoutData(UIUtil.createFormData2(composite, 7, null, 0, 0, 10, null, 0));
		deployButton.setLayoutData(UIUtil.createFormData2(composite, 5, null, 0, null, 0, 100, -5));
		deployText.setLayoutData(UIUtil.createFormData2(composite, 5, null, 0, label, 5, deployButton, -5));

		deployButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				String browseVal = browseClicked3(deployText.getShell());
				if (browseVal != null) {
					deployText.setText(browseVal);
				}
			}
		});

		Label tempDeployLabel = new Label(this, SWT.NONE);
		tempDeployLabel.setText(Messages.swf_TempDeployDirectory);
		tempDeployText = new Text(this, SWT.BORDER);
		tempDeployText.setText(getServer().getTempDeployFolder());
		tempDeployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				callback.execute(new SetTempDeployDirCommand());
			}
		};
		tempDeployText.addModifyListener(tempDeployListener);

		tempDeployButton = new Button(this, SWT.PUSH);
		tempDeployButton.setText(Messages.browse);
		
		tempDeployLabel.setLayoutData(UIUtil.createFormData2(deployText, 7, null, 0, 0, 10, null, 0));
		tempDeployButton.setLayoutData(UIUtil.createFormData2(deployText, 5, null, 0, null, 0, 100, -5));
		tempDeployText.setLayoutData(UIUtil.createFormData2(deployText, 5, null, 0, tempDeployLabel, 5, deployButton, -5));

		tempDeployButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				String browseVal = browseClicked3(tempDeployText.getShell());
				if (browseVal != null) {
					tempDeployText.setText(browseVal);
				}
			}
		});
	}
	
	private void updateDeployOnlyWidgets() {
		String newDir = callback.getServer().getAttribute(IDeployableServer.DEPLOY_DIRECTORY, "");
		String newTemp = callback.getServer().getAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, "");
		deployText.removeModifyListener(deployListener);
		deployText.setText(newDir);
		deployText.addModifyListener(deployListener);
		tempDeployText.removeModifyListener(tempDeployListener);
		tempDeployText.setText(newTemp);
		tempDeployText.addModifyListener(tempDeployListener);
	}
	public class SetDeployDirCommand extends ServerCommand {
		private String oldDir;
		private String newDir;
		private Text text;
		private ModifyListener listener;

		public SetDeployDirCommand() {
			super(callback.getServer(), Messages.EditorSetDeployLabel);
			this.text = deployText;
			this.newDir = deployText.getText();
			this.listener = deployListener;
			this.oldDir = callback.getServer().getAttribute(
					IDeployableServer.DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
		}

		public void execute() {
			callback.getServer().setAttribute(
					IDeployableServer.DEPLOY_DIRECTORY, newDir);
			updateDeployOnlyWidgets();
		}

		public void undo() {
			callback.getServer().setAttribute(
					IDeployableServer.DEPLOY_DIRECTORY, oldDir);
			updateDeployOnlyWidgets();
		}
	}

	public class SetTempDeployDirCommand extends ServerCommand {
		private String oldDir;
		private String newDir;
		private Text text;
		private ModifyListener listener;

		public SetTempDeployDirCommand() {
			super(callback.getServer(), Messages.EditorSetTempDeployLabel);
			text = tempDeployText;
			newDir = tempDeployText.getText();
			listener = tempDeployListener;
			oldDir = callback.getServer().getAttribute(
					IDeployableServer.TEMP_DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
		}

		public void execute() {
			callback.getServer().setAttribute(
					IDeployableServer.TEMP_DEPLOY_DIRECTORY, newDir);
		}

		public void undo() {
			callback.getServer().setAttribute(
					IDeployableServer.TEMP_DEPLOY_DIRECTORY, oldDir);
		}
	}
}
