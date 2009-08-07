/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
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
	
	private Label deployLabel, tempDeployLabel, nameLabel;
	private Text deployText, tempDeployText, nameText;
	private Button browse, tempBrowse;
	private String name, deployLoc, tempDeployLoc;

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
		
		tempDeployLabel = new Label(main, SWT.NONE);
		tempDeployText = new Text(main, SWT.BORDER);
		tempBrowse = new Button(main, SWT.PUSH);
		tempDeployLabel.setText(Messages.swf_TempDeployDirectory);
		tempBrowse.setText(Messages.browse);
		
		FormData namelData = new FormData();
		namelData.top = new FormAttachment(0,5);
		namelData.left = new FormAttachment(0,5);
		nameLabel.setLayoutData(namelData);
		
		FormData nametData = new FormData();
		nametData.top = new FormAttachment(0,5);
		nametData.left = new FormAttachment(deployLabel,5);
		nametData.right = new FormAttachment(100,-5);
		nameText.setLayoutData(nametData);
		
		FormData lData = new FormData();
		lData.top = new FormAttachment(nameText,5);
		lData.left = new FormAttachment(0,5);
		deployLabel.setLayoutData(lData);
		
		FormData tData = new FormData();
		tData.top = new FormAttachment(nameText,5);
		tData.left = new FormAttachment(deployLabel,5);
		tData.right = new FormAttachment(browse, -5);
		deployText.setLayoutData(tData);

		FormData bData = new FormData();
		bData.right = new FormAttachment(100,-5);
		bData.top = new FormAttachment(nameText,5);
		browse.setLayoutData(bData);

		
		FormData templData = new FormData();
		templData.top = new FormAttachment(deployText,5);
		templData.left = new FormAttachment(0,5);
		tempDeployLabel.setLayoutData(templData);
		
		FormData temptData = new FormData();
		temptData.top = new FormAttachment(deployText,5);
		temptData.left = new FormAttachment(tempDeployLabel,5);
		temptData.right = new FormAttachment(tempBrowse, -5);
		tempDeployText.setLayoutData(temptData);

		FormData tempbData = new FormData();
		tempbData.right = new FormAttachment(100,-5);
		tempbData.top = new FormAttachment(deployText,5);
		tempBrowse.setLayoutData(tempbData);

		
		ModifyListener ml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				textChanged();
			}
		};
		
		browse.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(deployText.getText());
				String x = d.open();
				if( x != null ) 
					deployText.setText(x);
			} 
		});

		tempBrowse.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(tempDeployText.getText());
				String x = d.open();
				if( x != null ) 
					tempDeployText.setText(x);
			} 
		});

		
		deployText.addModifyListener(ml);
		tempDeployText.addModifyListener(ml);
		nameText.addModifyListener(ml);
		nameText.setText(getDefaultNameText());
		handle.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.WIZBAN_JBOSS_LOGO));
		return main;
	}

	protected void textChanged() {
		IStatus status = checkErrors();
		if( status.isOK() ) {
			deployLoc = deployText.getText();
			tempDeployLoc = tempDeployText.getText();
			name = nameText.getText();
			handle.setMessage("", IStatus.OK); //$NON-NLS-1$
			handle.update();
		} else {
			handle.setMessage(status.getMessage(), IStatus.WARNING);
		}
	}
	
	protected IStatus checkErrors() {
		if( findServer(nameText.getText()) != null ) {
			return new Status(IStatus.WARNING, JBossServerUIPlugin.PLUGIN_ID, IStatus.OK, Messages.StrippedServerWizardFragment_NameInUseStatusMessage, null);
		}
		File f = new File(deployText.getText());
		if( !f.exists() || !f.isDirectory() ) {
			return new Status(IStatus.WARNING, JBossServerUIPlugin.PLUGIN_ID, IStatus.OK, Messages.StrippedServerWizardFragment_DeployFolderDoesNotExistStatusMessage, null);
		}
		f = new File(tempDeployText.getText());
		if( !f.exists() || !f.isDirectory() ) {
			return new Status(IStatus.WARNING, JBossServerUIPlugin.PLUGIN_ID, IStatus.OK, Messages.StrippedServerWizardFragment_TemporaryDeployFolderDoesNotExistStatusMessage, null);
		}
		return new Status(IStatus.OK, JBossServerUIPlugin.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
	}
	
	public void enter() {
		//handle.setTitle("Create a new System Copy Server");
		handle.setTitle(Messages.sswf_Title);
		IServer s = (IServer) getTaskModel().getObject(TaskModel.TASK_SERVER);
		IServerWorkingCopy swc;
		if( s instanceof IServerWorkingCopy)
			swc = (IServerWorkingCopy)s;
		else
			swc = s.createWorkingCopy();
		
		ServerWorkingCopy swcInternal;
		if( swc instanceof ServerWorkingCopy )  {
			swcInternal = (ServerWorkingCopy)swc;
			deployText.setText(swcInternal.getAttribute(DeployableServer.DEPLOY_DIRECTORY, "")); //$NON-NLS-1$
		}
	}
	public void exit() {
		textChanged();
		IServer s = (IServer) getTaskModel().getObject(TaskModel.TASK_SERVER);
		IServerWorkingCopy swc;
		if( s instanceof IServerWorkingCopy)
			swc = (IServerWorkingCopy)s;
		else
			swc = s.createWorkingCopy();
		
		ServerWorkingCopy swcInternal;
		if( swc instanceof ServerWorkingCopy )  {
			swcInternal = (ServerWorkingCopy)swc;
			swcInternal.setName(name);
			swcInternal.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deployLoc);
			swcInternal.setAttribute(DeployableServer.TEMP_DEPLOY_DIRECTORY, tempDeployLoc);
			getTaskModel().putObject(TaskModel.TASK_SERVER, swcInternal);
		}
	}
	
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IServerWorkingCopy serverWC = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		ServerWorkingCopy serverwc2 = (serverWC instanceof ServerWorkingCopy ? ((ServerWorkingCopy)serverWC) : null);
		
		try {
			serverwc2.setServerConfiguration(null);
			serverwc2.setName(name);
			serverwc2.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deployLoc);
			getTaskModel().putObject(TaskModel.TASK_SERVER, serverwc2);
		} catch( Exception ce ) {
		}
	}

	public boolean isComplete() {
		return checkErrors().isOK();
	}

	public boolean hasComposite() {
		return true;
	}
	
	private String getDefaultNameText() {
		String base = Messages.sswf_BaseName;
		if( findServer(base) == null ) return base;
		int i = 1;
		while( ServerCore.findServer(base + " (" + i + ")") != null )  //$NON-NLS-1$ //$NON-NLS-2$
			i++;
		return base + " (" + i + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private IServer findServer(String name) {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			Server server = (Server) servers[i];
			if (name.equals(server.getName()))
				return server;
		}
		return null;
	}

	
}
