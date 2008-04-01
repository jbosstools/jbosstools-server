/*
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBossServerWizardFragment extends WizardFragment {
	private IWizardHandle handle;
	private String name, authUser, authPass, deployVal, deployTmpFolderVal;
	private Label nameLabel, serverExplanationLabel, 
					runtimeExplanationLabel, authenticationExplanationLabel; 
	private Label homeDirLabel, installedJRELabel, configLabel;
	private Label homeValLabel, jreValLabel, configValLabel;
	private Label usernameLabel, passLabel, deployLabel;
	
	private Composite nameComposite;
	private Group runtimeGroup, authenticationGroup, deployGroup;
	private Text nameText, userText, passText, deployText;
	private Button deployBrowseButton;
	
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());
		
		createExplanationLabel(main);
		createNameComposite(main);
		createRuntimeGroup(main);
		createAuthenticationGroup(main);
		createDeployGroup(main);
		
		// make modifications to parent
		handle.setTitle(Messages.swf_Title);
		handle.setDescription(Messages.swf_Description);
		handle.setImageDescriptor (getImageDescriptor());
		
		return main;
	}
	
	public ImageDescriptor getImageDescriptor() {
		IRuntime rt = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		String id = rt.getRuntimeType().getId();
		String imageKey = "";
		if( id.equals("org.jboss.ide.eclipse.as.runtime.32")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS32_LOGO;
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.40")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS40_LOGO;
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.42")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS42_LOGO;
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.50")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS50_LOGO;
		return JBossServerUISharedImages.getImageDescriptor(imageKey);
	}
	
	private void createExplanationLabel(Composite main) {
		serverExplanationLabel = new Label(main, SWT.NONE);
		FormData data = new FormData();
		data.top = new FormAttachment(0,5);
		data.left = new FormAttachment(0,5);
		data.right = new FormAttachment(100,-5);
		serverExplanationLabel.setLayoutData(data);
		serverExplanationLabel.setText(Messages.swf_Explanation);
	}

	private void createNameComposite(Composite main) {
		// Create our name composite
		nameComposite = new Composite(main, SWT.NONE);
		
		FormData cData = new FormData();
		cData.left = new FormAttachment(0,5);
		cData.right = new FormAttachment(100,-5);
		cData.top = new FormAttachment(serverExplanationLabel, 10);
		nameComposite.setLayoutData(cData);
		
		nameComposite.setLayout(new FormLayout());

		
		// create internal widgets
		nameLabel = new Label(nameComposite, SWT.None);
		nameLabel.setText(Messages.wf_NameLabel);
		
		nameText = new Text(nameComposite, SWT.BORDER);
		name = getDefaultNameText();
		nameText.setText(name);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				name = nameText.getText();
				updateErrorMessage();
			} 
		});
		
		// organize widgets inside composite
		FormData nameLabelData = new FormData();
		nameLabelData.left = new FormAttachment(0,0);
		nameLabel.setLayoutData(nameLabelData);
		
		FormData nameTextData = new FormData();
		nameTextData.left = new FormAttachment(0, 5);
		nameTextData.right = new FormAttachment(100, -5);
		nameTextData.top = new FormAttachment(nameLabel, 5);
		nameText.setLayoutData(nameTextData);
	}
	
	private String getDefaultNameText() {
		IRuntime rt = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		String base = Messages.swf_BaseName.replace(Messages.wf_BaseNameVersionReplacement, rt.getRuntimeType().getVersion());
		if( findServer(base) == null ) return base;
		int i = 1;
		while( ServerCore.findServer(base + " (" + i + ")") != null ) 
			i++;
		return base + " (" + i + ")";
	}
	private IServer findServer(String name) {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			if (name.trim().equals(servers[i].getName()))
				return servers[i];
		}
		return null;
	}

	private void createRuntimeGroup(Composite main) {
		
		runtimeGroup = new Group(main, SWT.NONE);
		runtimeGroup.setText(Messages.swf_RuntimeInformation);
		FormData groupData = new FormData();
		groupData.left = new FormAttachment(0,5);
		groupData.right = new FormAttachment(100, -5);
		groupData.top = new FormAttachment(nameComposite, 5);
		runtimeGroup.setLayoutData(groupData);

		runtimeGroup.setLayout(new GridLayout(2, false));
		GridData d = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		
		// explanation 2
		runtimeExplanationLabel = new Label(runtimeGroup, SWT.NONE);
		runtimeExplanationLabel.setText(Messages.swf_Explanation2);
		GridData explanationData = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		explanationData.horizontalSpan = 2;
		runtimeExplanationLabel.setLayoutData(explanationData);

		// Create our composite
		homeDirLabel = new Label(runtimeGroup, SWT.NONE);
		homeDirLabel.setText(Messages.wf_HomeDirLabel);
		homeValLabel = new Label(runtimeGroup, SWT.NONE);
		homeValLabel.setLayoutData(d);
		
		installedJRELabel = new Label(runtimeGroup, SWT.NONE);
		installedJRELabel.setText(Messages.wf_JRELabel);
		jreValLabel = new Label(runtimeGroup, SWT.NONE);
		d = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		jreValLabel.setLayoutData(d);
		
		configLabel = new Label(runtimeGroup, SWT.NONE);
		configLabel.setText(Messages.wf_ConfigLabel);
		configValLabel = new Label(runtimeGroup, SWT.NONE);
		d = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		configValLabel.setLayoutData(d);
	}
	
	protected void createAuthenticationGroup(Composite main) {
		authenticationGroup = new Group(main, SWT.NONE);
		authenticationGroup.setText(Messages.swf_AuthenticationGroup);
		FormData groupData = new FormData();
		groupData.left = new FormAttachment(0,5);
		groupData.right = new FormAttachment(100, -5);
		groupData.top = new FormAttachment(runtimeGroup, 5);
		authenticationGroup.setLayoutData(groupData);

		authenticationGroup.setLayout(new GridLayout(2, false));
		GridData d;

		authenticationExplanationLabel = new Label(authenticationGroup, SWT.NONE);
		authenticationExplanationLabel.setText("JMX Console Access");
		d = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		d.horizontalSpan = 2;
		authenticationExplanationLabel.setLayoutData(d);
		
		d = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		d.minimumWidth = 200;
		usernameLabel = new Label(authenticationGroup, SWT.NONE);
		usernameLabel.setText(Messages.swf_Username);
		userText = new Text(authenticationGroup, SWT.BORDER);
		userText.setLayoutData(d);

		d = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		d.minimumWidth = 200;
		passLabel = new Label(authenticationGroup, SWT.NONE);
		passLabel.setText(Messages.swf_Password);
		passText = new Text(authenticationGroup, SWT.BORDER);
		passText.setLayoutData(d);
		
		// listeners
		passText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				authPass = passText.getText();
			} 
		});
		userText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				authUser = userText.getText();
			} 
		});
	}
	
	protected void createDeployGroup(Composite main) {
		deployGroup = new Group(main, SWT.NONE);
		deployGroup.setText(Messages.swf_DeployGroup);
		FormData groupData = new FormData();
		groupData.left = new FormAttachment(0,5);
		groupData.right = new FormAttachment(100, -5);
		groupData.top = new FormAttachment(authenticationGroup, 5);
		deployGroup.setLayoutData(groupData);

		deployGroup.setLayout(new GridLayout(3, false));
		deployLabel = new Label(deployGroup, SWT.NONE);
		deployText = new Text(deployGroup, SWT.BORDER);
		deployLabel.setText(Messages.swf_DeployDirectory);
		
		deployBrowseButton = new Button(deployGroup, SWT.PUSH);
		deployBrowseButton.setText(Messages.browse);
		
		deployBrowseButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				File file = new File(deployText.getText());
				if (!file.exists()) {
					file = null;
				}

				File directory = getDirectory(file, deployGroup.getShell());
				if (directory == null) {
					return;
				}

				deployText.setText(directory.getAbsolutePath());
				deployVal = deployText.getText();
			}
		});
		
		deployText.setEditable(false);
	}
	
	protected File getDirectory(File startingDirectory, Shell shell) {
		DirectoryDialog fileDialog = new DirectoryDialog(shell, SWT.OPEN);
		if (startingDirectory != null) {
			fileDialog.setFilterPath(startingDirectory.getPath());
		}

		String dir = fileDialog.open();
		if (dir != null) {
			dir = dir.trim();
			if (dir.length() > 0) {
				return new File(dir);
			}
		}
		return null;
	}

	private void updateErrorMessage() {
		String error = getErrorString();
		if( error == null ) {
			handle.setMessage(null, IMessageProvider.NONE);
		} else {
			handle.setMessage(error, IMessageProvider.ERROR);
		}
	}
	
	private String getErrorString() {
		if( findServer(name) != null ) 
			return Messages.swf_NameInUse;
		
		if(deployVal==null || deployVal.trim().length()==0) {
			return "Deploy folder not specified";
		}

		if(deployTmpFolderVal==null || deployTmpFolderVal.trim().length()==0) {
			return "Temporary deploy folder not specified";
		}
				
		return null;
	}
		
	// WST API methods
	public void enter() {
		if(homeValLabel==null) 
			return;
		
		IJBossServerRuntime srt = getRuntime();
			
		homeValLabel.setText(srt.getRuntime().getLocation().toOSString());
		configValLabel.setText(srt.getJBossConfiguration());
		IVMInstall install = srt.getVM();
		jreValLabel.setText(install.getInstallLocation().getAbsolutePath() + " (" + install.getName() + ")");
		String deployFolder = srt.getRuntime().getLocation().append( "server").append(configValLabel.getText()).append("deploy").toOSString();
		deployTmpFolderVal = srt.getRuntime().getLocation().append( "server").append(configValLabel.getText()).append("tmp").append("jbosstoolsTemp").toOSString();
		deployText.setText(deployFolder);
		deployVal = deployFolder;
		runtimeGroup.layout();
		deployGroup.layout();
	}

	public void exit() {
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IServerWorkingCopy serverWC = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		serverWC.setRuntime((IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME));
		serverWC.setName(name);
		serverWC.setServerConfiguration(null); // no inside jboss folder
		JBossServer jbs = (JBossServer)serverWC.loadAdapter(JBossServer.class, new NullProgressMonitor());
		jbs.setUsername(authUser);
		jbs.setPassword(authPass);
		jbs.setDeployFolder(deployVal);
		jbs.setTempDeployFolder(deployTmpFolderVal);
		new File(deployTmpFolderVal).mkdirs();
	}

	private IJBossServerRuntime getRuntime() {
		IRuntime r = (IRuntime) getTaskModel()
				.getObject(TaskModel.TASK_RUNTIME);
		IJBossServerRuntime ajbsrt = null;
		if (r != null) {
			ajbsrt = (IJBossServerRuntime) r
					.loadAdapter(IJBossServerRuntime.class,
							new NullProgressMonitor());
		}
		return ajbsrt;
	}

	public boolean isComplete() {
		return getErrorString() == null ? true : false;
	}

	public boolean hasComposite() {
		return true;
	}
}
