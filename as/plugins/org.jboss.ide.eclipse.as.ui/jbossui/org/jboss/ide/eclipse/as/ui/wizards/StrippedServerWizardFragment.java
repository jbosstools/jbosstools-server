/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 * 
 */
public class StrippedServerWizardFragment extends WizardFragment {

	private IWizardHandle handle;

	private Label deployLabel, tmpDeployLabel, nameLabel;
	private Text deployText, tmpDeployText, nameText;
	private Button browse, tmpBrowse;
	private String name, deployLoc, tmpDeployLoc;

	public StrippedServerWizardFragment() {
	}

	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());

		nameLabel = new Label(main, SWT.NONE);
		nameText = new Text(main, SWT.BORDER);
		nameLabel.setText(Messages.serverName);

		deployLabel = new Label(main, SWT.NONE);
		deployText = new Text(main, SWT.BORDER);
		browse = new Button(main, SWT.PUSH);
		deployLabel.setText(Messages.swf_DeployDirectory);
		browse.setText(Messages.browse);

		tmpDeployLabel = new Label(main, SWT.NONE);
		tmpDeployText = new Text(main, SWT.BORDER);
		tmpBrowse = new Button(main, SWT.PUSH);
		tmpDeployLabel.setText(Messages.swf_TempDeployDirectory);
		tmpBrowse.setText(Messages.browse);

		FormData namelData = new FormData();
		namelData.top = new FormAttachment(0, 5);
		namelData.left = new FormAttachment(0, 5);
		nameLabel.setLayoutData(namelData);

		FormData nametData = new FormData();
		nametData.top = new FormAttachment(0, 5);
		nametData.left = new FormAttachment(deployLabel, 5);
		nametData.right = new FormAttachment(100, -5);
		nameText.setLayoutData(nametData);

		FormData lData = new FormData();
		lData.top = new FormAttachment(nameText, 5);
		lData.left = new FormAttachment(0, 5);
		deployLabel.setLayoutData(lData);

		FormData tData = new FormData();
		tData.top = new FormAttachment(nameText, 5);
		tData.left = new FormAttachment(deployLabel, 5);
		tData.right = new FormAttachment(browse, -5);
		deployText.setLayoutData(tData);

		FormData bData = new FormData();
		bData.right = new FormAttachment(100, -5);
		bData.top = new FormAttachment(nameText, 5);
		browse.setLayoutData(bData);

		lData = new FormData();
		lData.top = new FormAttachment(deployText, 5);
		lData.left = new FormAttachment(0, 5);
		tmpDeployLabel.setLayoutData(lData);

		tData = new FormData();
		tData.top = new FormAttachment(deployText, 5);
		tData.left = new FormAttachment(tmpDeployLabel, 5);
		tData.right = new FormAttachment(tmpBrowse, -5);
		tmpDeployText.setLayoutData(tData);

		bData = new FormData();
		bData.right = new FormAttachment(100, -5);
		bData.top = new FormAttachment(deployText, 5);
		tmpBrowse.setLayoutData(bData);

		
		ModifyListener ml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				textChanged();
			}
		};

		browse.addSelectionListener(new MySelectionListener(deployText));
		tmpBrowse.addSelectionListener(new MySelectionListener(tmpDeployText));

		tmpDeployText.addModifyListener(ml);
		deployText.addModifyListener(ml);
		nameText.addModifyListener(ml);
		nameText.setText(getDefaultNameText());
		handle.setImageDescriptor(JBossServerUISharedImages
				.getImageDescriptor(JBossServerUISharedImages.WIZBAN_JBOSS_LOGO));
		return main;
	}
	
	private class MySelectionListener implements SelectionListener {
		private Text text;
		public MySelectionListener(Text text) {
			this.text = text;
		}
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
			DirectoryDialog d = new DirectoryDialog(new Shell());
			d.setFilterPath(text.getText());
			String x = d.open();
			if (x != null)
				text.setText(x);
		}
	}

	protected void textChanged() {
		IStatus status = checkErrors();
		if (status.isOK()) {
			deployLoc = deployText.getText();
			tmpDeployLoc = tmpDeployText.getText();
			name = nameText.getText();
			handle.setMessage("", IStatus.OK); //$NON-NLS-1$
			handle.update();
		} else {
			handle.setMessage(status.getMessage(), IStatus.WARNING);
		}
	}

	protected IStatus checkErrors() {
		if (findServer(nameText.getText()) != null) {
			return new Status(IStatus.WARNING, JBossServerUIPlugin.PLUGIN_ID, IStatus.OK,
					Messages.StrippedServerWizardFragment_NameInUseStatusMessage, null);
		}
		File f = new File(deployText.getText());
		if (!f.exists() || !f.isDirectory()) {
			return new Status(IStatus.WARNING, JBossServerUIPlugin.PLUGIN_ID, IStatus.OK,
					Messages.StrippedServerWizardFragment_DeployFolderDoesNotExistStatusMessage, null);
		}
		f = new File(tmpDeployText.getText());
		if (!f.exists() || !f.isDirectory()) {
			return new Status(IStatus.WARNING, JBossServerUIPlugin.PLUGIN_ID, IStatus.OK,
					Messages.StrippedServerWizardFragment_TemporaryDeployFolderDoesNotExistStatusMessage, null);
		}
		
		// Check if a renameTo on these folders will fail
		File tmp1, dep1;
		tmp1 = dep1 = null;
		boolean success = false;
		try {
			tmp1 = File.createTempFile(JBossServerUIPlugin.PLUGIN_ID, ".txt", new File(tmpDeployText.getText())); //$NON-NLS-1$
			dep1 = new Path(deployText.getText()).append(JBossServerUIPlugin.PLUGIN_ID + ".txt").toFile(); //$NON-NLS-1$
			success = tmp1.renameTo(dep1);
		} catch(IOException ioe) {
		} finally {
			if( tmp1 != null && tmp1.exists())
				tmp1.delete();
			if( dep1 != null && dep1.exists())
				dep1.delete();
			if( !success )
				return new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, 0,
						"Unable to rename files from your temporary folder to your deploy folder. Please verify both are on the same filesystem.", null); //$NON-NLS-1$
		}
		
		
		return new Status(IStatus.OK, JBossServerUIPlugin.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
	}

	public void enter() {
		handle.setTitle(Messages.sswf_Title);
		IServer s = (IServer) getTaskModel().getObject(TaskModel.TASK_SERVER);
		IServerWorkingCopy swc;
		if (s instanceof IServerWorkingCopy)
			swc = (IServerWorkingCopy) s;
		else
			swc = s.createWorkingCopy();

		deployText.setText(swc.getAttribute(DeployableServer.DEPLOY_DIRECTORY, "")); //$NON-NLS-1$
	}

	public void exit() {
		textChanged();
		IServer s = (IServer) getTaskModel().getObject(TaskModel.TASK_SERVER);
		IServerWorkingCopy swc;
		if (s instanceof IServerWorkingCopy)
			swc = (IServerWorkingCopy) s;
		else
			swc = s.createWorkingCopy();

			swc.setName(name);
			swc.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deployLoc);
			String tempFolder = JBossServerCorePlugin.getServerStateLocation(s)
					.append(IJBossServerConstants.TEMP_DEPLOY).makeAbsolute().toString();
			swc.setAttribute(DeployableServer.TEMP_DEPLOY_DIRECTORY, tempFolder);
			getTaskModel().putObject(TaskModel.TASK_SERVER, swc);
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IServerWorkingCopy serverWC = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);

		try {
			serverWC.setServerConfiguration(null);
			serverWC.setName(name);
			serverWC.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deployLoc);
			serverWC.setAttribute(DeployableServer.TEMP_DEPLOY_DIRECTORY, tmpDeployLoc);
			getTaskModel().putObject(TaskModel.TASK_SERVER, serverWC);
		} catch (Exception ce) {
		}
	}

	public boolean isComplete() {
		return checkErrors().isOK();
	}

	public boolean hasComposite() {
		return true;
	}

	private String getDefaultNameText() {
		Object o = getTaskModel().getObject(TaskModel.TASK_SERVER);
		return ((IServerWorkingCopy) o).getName();
	}

	private IServer findServer(String name) {
		IServer[] servers = ServerCore.getServers();
		for (int i = 0; i < servers.length; i++) {
			IServer server = servers[i];
			if (name.equals(server.getName()))
				return server;
		}
		return null;
	}

}
