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
package org.jboss.ide.eclipse.as.ui.wizards;


import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;
import org.jboss.ide.eclipse.as.ui.editor.ServerModeSectionComposite;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBossServerWizardFragment extends WizardFragment {
	private IWizardHandle handle;
	private Label serverExplanationLabel, 
					runtimeExplanationLabel; 
	private Label homeDirLabel, execEnvironmentLabel, installedJRELabel, configLabel;
	private Label homeValLabel, execEnvironmentValLabel, jreValLabel, configValLabel, configLocValLabel;
	
	private Group runtimeGroup;
	
	public JBossServerWizardFragment() {
		super();
	}
	
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		// make modifications to parent
		handle.setTitle(Messages.swf_Title);
		handle.setImageDescriptor (getImageDescriptor());
		IRuntime r = (IRuntime) getTaskModel()
			.getObject(TaskModel.TASK_RUNTIME);
		String version = r.getRuntimeType().getVersion();
		if( isEAP() && version.startsWith("5.")) //$NON-NLS-1$
			version = "5.x"; //$NON-NLS-1$
		String description = NLS.bind(
				isEAP() ? Messages.JBEAP_version : Messages.JBAS_version,
				version);
		handle.setDescription(description);
		
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());
		
		createExplanationLabel(main);
		createRuntimeGroup(main);
		createBehaviourGroup(main);

		return main;
	}

	protected boolean isEAP() {
		IRuntime rt = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		return RuntimeUtils.isEAP(rt);
	}
	
	public ImageDescriptor getImageDescriptor() {
		String imageKey = JBossServerUISharedImages.WIZBAN_JBOSS_LOGO;
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


	protected void createRuntimeGroup(Composite main) {
		createRuntimeGroup2(main);
		fillRuntimeGroupStandard();
		fillRuntimeGroupConfig();
	}
	
	protected void createRuntimeGroup2(Composite main) {
		runtimeGroup = new Group(main, SWT.NONE);
		runtimeGroup.setText(Messages.swf_RuntimeInformation);
		FormData groupData = UIUtil.createFormData2(serverExplanationLabel, 5, null, 0, 0,5,100,-5);
		runtimeGroup.setLayoutData(groupData);
		runtimeGroup.setLayout(new GridLayout(2, false));
	}
	
	protected void fillRuntimeGroupStandard() {
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
		execEnvironmentLabel = new Label(runtimeGroup, SWT.NONE);
		execEnvironmentLabel.setText(Messages.wf_ExecEnvironmentLabel);
		execEnvironmentValLabel= new Label(runtimeGroup, SWT.NONE);
		d = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		execEnvironmentValLabel.setLayoutData(d);
		
		installedJRELabel = new Label(runtimeGroup, SWT.NONE);
		installedJRELabel.setText(Messages.wf_JRELabel);
		jreValLabel = new Label(runtimeGroup, SWT.NONE);
		d = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		jreValLabel.setLayoutData(d);
	}
	
	protected void fillRuntimeGroupConfig() {	
		Label configLocationLabel = new Label(runtimeGroup, SWT.NONE);
		configLocationLabel.setText(Messages.swf_ConfigurationLocation);
		configLocValLabel = new Label(runtimeGroup, SWT.NONE);

		configLabel = new Label(runtimeGroup, SWT.NONE);
		configLabel.setText(Messages.wf_ConfigLabel);
		configValLabel = new Label(runtimeGroup, SWT.NONE);
		GridData d = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		configValLabel.setLayoutData(d);
	}
	
	protected void createBehaviourGroup(Composite main) {
		Group g = new Group(main, SWT.NONE);
		g.setText("Server Behaviour");
		FormData groupData = new FormData();
		groupData.left = new FormAttachment(0,5);
		groupData.right = new FormAttachment(100, -5);
		groupData.top = new FormAttachment(runtimeGroup, 5);
		groupData.bottom = new FormAttachment(100,-5);
		g.setLayoutData(groupData);
		
		g.setLayout(new FillLayout());
		new ServerModeSectionComposite(g, SWT.NONE, new NewServerWizardBehaviourCallback());
	}
	
	private class NewServerWizardBehaviourCallback implements IServerModeUICallback {
		public IRuntime getRuntime() {
			return (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		}
		public IServerWorkingCopy getServer() {
			return (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		}
		public void execute(IUndoableOperation operation) {
			try {
				operation.execute(new NullProgressMonitor(), null);
			} catch(ExecutionException  ee) {
				// TODO
			}
		}
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
		return null;
	}
		
	// WST API methods
	public void enter() {
		if(homeValLabel !=null && !homeValLabel.isDisposed()) {
			IJBossServerRuntime srt = getRuntime();
			homeValLabel.setText(srt.getRuntime().getLocation().toOSString());
			execEnvironmentValLabel.setText(srt.getExecutionEnvironment().getDescription());
			IVMInstall vm = srt.getHardVM();
			String jreVal = vm == null ? NLS.bind(Messages.rwf_DefaultJREForExecEnv, getRuntime().getExecutionEnvironment().getId()) : 
				vm.getInstallLocation().getAbsolutePath() + " (" + vm.getName() + ")";//$NON-NLS-1$ //$NON-NLS-2$
			jreValLabel.setText(jreVal);
			if( configValLabel != null && !configValLabel.isDisposed())
				configValLabel.setText(srt.getJBossConfiguration());
			if( configLocValLabel != null && !configLocValLabel.isDisposed()) 
				configLocValLabel.setText(srt.getConfigLocation());
			runtimeGroup.layout();
			updateErrorMessage();
		}
	}

	public void exit() {
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IServerWorkingCopy serverWC = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		serverWC.setRuntime((IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME));
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

	public boolean isComplete() {
		return getErrorString() == null ? true : false;
	}

	public boolean hasComposite() {
		return true;
	}
}
