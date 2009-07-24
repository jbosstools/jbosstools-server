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


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
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
	private String name;
	private Label nameLabel, serverExplanationLabel, 
					runtimeExplanationLabel; 
	private Label homeDirLabel, installedJRELabel, configLabel;
	private Label homeValLabel, jreValLabel, configValLabel;
	
	private Composite nameComposite;
	private Group runtimeGroup;
	private Text nameText;
	
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());
		
		createExplanationLabel(main);
		createNameComposite(main);
		createRuntimeGroup(main);

		// make modifications to parent
		handle.setTitle(Messages.swf_Title);
		handle.setImageDescriptor (getImageDescriptor());
		IRuntime r = (IRuntime) getTaskModel()
			.getObject(TaskModel.TASK_RUNTIME);
		String version = r.getRuntimeType().getVersion();
		String description = NLS.bind(
				isEAP() ? Messages.JBEAP_version : Messages.JBAS_version,
				version);
		handle.setDescription(description);
		
		return main;
	}

	protected boolean isEAP() {
		IRuntime rt = (IRuntime) getTaskModel().getObject(
				TaskModel.TASK_RUNTIME);
		return rt.getRuntimeType().getId().startsWith("org.jboss.ide.eclipse.as.runtime.eap.");
	}
	
	public ImageDescriptor getImageDescriptor() {
		IRuntime rt = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		String id = rt.getRuntimeType().getId();
		String imageKey = "";
		if( id.equals("org.jboss.ide.eclipse.as.runtime.32")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS32_LOGO;
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.40")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS40_LOGO;
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.42")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS42_LOGO;
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.50")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS50_LOGO;
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.51")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS51_LOGO;
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.eap.43")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS_EAP_LOGO_43;
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.eap.50")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS_EAP_LOGO_50;

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
		String name = rt.getName();
		String base = null;
		if( name == null || name.equals(""))
			base = Messages.swf_BaseName.replace(Messages.wf_BaseNameVersionReplacement, rt.getRuntimeType().getVersion());
		else if( name.endsWith(org.jboss.ide.eclipse.as.core.Messages.runtime)) 
			base = name.substring(0, name.indexOf(org.jboss.ide.eclipse.as.core.Messages.runtime)) + org.jboss.ide.eclipse.as.core.Messages.server; 
		else 
			base = name + " " + org.jboss.ide.eclipse.as.core.Messages.server;
		
		if( findServer(base) == null ) return base;
		int i = 1;
		while( findServer(base + " (" + i + ")") != null ) 
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
		return null;
	}
		
	// WST API methods
	public void enter() {
		if(homeValLabel !=null && !homeValLabel.isDisposed()) {
			IJBossServerRuntime srt = getRuntime();
			name = name == null ? getDefaultNameText() : name;
			nameText.setText(name);
			homeValLabel.setText(srt.getRuntime().getLocation().toOSString());
			configValLabel.setText(srt.getJBossConfiguration());
			jreValLabel.setText(srt.getVM().getInstallLocation().getAbsolutePath() + " (" + srt.getVM().getName() + ")");
			runtimeGroup.layout();
			updateErrorMessage();
		}
	}

	public void exit() {
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IServerWorkingCopy serverWC = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		name = name == null ? getDefaultNameText() : name;
		JBossServer jbs = (JBossServer)serverWC.loadAdapter(JBossServer.class, new NullProgressMonitor());
		jbs.setUsername("admin");
		jbs.setPassword("admin");
		jbs.setDeployLocationType(isAS5() ? IDeployableServer.DEPLOY_SERVER : IDeployableServer.DEPLOY_METADATA);
		serverWC.setRuntime((IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME));
		serverWC.setName(name);
		serverWC.setServerConfiguration(null); // no inside jboss folder
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

	protected boolean isAS5() {
		return getRuntime().getRuntime().getRuntimeType().
				getVersion().equals("5.0");
	}
	
	public boolean isComplete() {
		return getErrorString() == null ? true : false;
	}

	public boolean hasComposite() {
		return true;
	}
}
