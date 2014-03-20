/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.wizards;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentTypeUIUtil.ICompletable;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerProfileInitializer;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel.ServerProfile;
import org.jboss.ide.eclipse.as.wtp.ui.profile.ProfileUI;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;

/**
 * 
 *
 */
public class ServerProfileWizardFragment extends WizardFragment implements ICompletable {
	
	/**
	 * Task model id for an IRuntime that is contributed by *our* fragment and not the wtp fragments
	 * 
	 * @see #getObject(String)
	 * @see #putObject(String, Object)
	 */
	public static final String TASK_CUSTOM_RUNTIME = "custom_runtime"; //$NON-NLS-1$

	
	private IWizardHandle handle;
	private ServerProfile selectedProfile;
	private ServerProfile[] profiles;
	private Label serverExplanationLabel; 
	private WizardFragment runtimeFragment;
	private boolean requiresRuntime;
	private Combo profileCombo;
	private Label profileDescriptionLabel;
	private Label requiresRuntimeLabel;
	private Button useRuntimeButton;
	private Combo runtimeCombo;
	
	private IRuntime[] runtimes;
	private String[] runtimeNames;
	private IRuntimeWorkingCopy newRuntimeWorkingCopy;
	
	public ServerProfileWizardFragment() {
		super();
	}
	public void setComplete(boolean complete) {
		super.setComplete(complete);
	}
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		
		// make modifications to parent
		setPageDetails(handle);
		initializeModel();
		setComplete(false);
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());
		
		createExplanationLabel(main);
		createBehaviourGroup(main);
		
		updateErrorMessage();
		// now init defaults in the page
		initializeWidgetDefaults();
		updateErrorMessage();
		return main;
	}
	
	protected void initializeModel() {
		newRuntimeWorkingCopy = null;
		selectedProfile = null;
		IServerAttributes server = (IServerAttributes)getTaskModel().getObject(TaskModel.TASK_SERVER);
		String serverType = server.getServerType().getId();
		IRuntimeType rtType = server.getServerType().getRuntimeType(); 
		String runtimeType = rtType == null ? null : rtType.getId();
		try {
			newRuntimeWorkingCopy = rtType.createRuntime(null, null);
		} catch(CoreException ce) {
			JBossServerUIPlugin.log(ce.getStatus());
		}
		ServerProfile[] tmpProfiles = ServerProfileModel.getDefault().getProfiles(serverType);
		// sort by visible name
		ArrayList<ServerProfile> tmpProfileList = new ArrayList<ServerProfile>(Arrays.asList(tmpProfiles));
		Collections.sort(tmpProfileList, new Comparator<ServerProfile>(){
			public int compare(ServerProfile arg0, ServerProfile arg1) {
				String n1 = arg0.getVisibleName() == null ? arg0.getId() : arg0.getVisibleName();
				String n2 = arg1.getVisibleName() == null ? arg1.getId() : arg1.getVisibleName();
				return n1.compareTo(n2);
			}
		});
		profiles =  tmpProfileList.toArray(new ServerProfile[tmpProfileList.size()]);

		if( runtimeType != null ) {
			ArrayList<IRuntime> validRuntimes = new ArrayList<IRuntime>();
			ArrayList<String> runtimeNames = new ArrayList<String>();
			IRuntime[] runtimes = ServerCore.getRuntimes();
			for( int i = 0; i < runtimes.length; i++ ) {
				if( runtimes[i].getRuntimeType() != null && runtimes[i].getRuntimeType().getId().equals(runtimeType)) {
					validRuntimes.add(runtimes[i]);
					runtimeNames.add(runtimes[i].getName());
				}
			}
			this.runtimes = validRuntimes.toArray(new IRuntime[validRuntimes.size()]);
			this.runtimeNames = (String[]) runtimeNames.toArray(new String[runtimeNames.size()]);
		}
	}
	
	protected void initializeWidgetDefaults() {
		IServerAttributes server = (IServerAttributes)getTaskModel().getObject(TaskModel.TASK_SERVER);
		String currentProfile = ServerProfileModel.getProfile(server, ServerProfileModel.DEFAULT_SERVER_PROFILE);
		int profileIndex = -1;
		for( int i = 0; i < profiles.length && profileIndex == -1; i++ ) {
			if( profiles[i].getId().equals(currentProfile)) {
				profileCombo.select(i);
				setProfile(profiles[i]);
				break;
			}
		}
	}
	
	protected void setPageDetails(IWizardHandle handle) {
		handle.setTitle(Messages.swf_Title);
		handle.setImageDescriptor (getImageDescriptor());
		IServerAttributes server = (IServerAttributes)getTaskModel().getObject(TaskModel.TASK_SERVER);
		String serverDesc = server.getServerType().getDescription();
		handle.setDescription(serverDesc);
	}
	
	public ImageDescriptor getImageDescriptor() {
		String imageKey = JBossServerUISharedImages.WIZBAN_JBOSS_LOGO;
		return JBossServerUISharedImages.getImageDescriptor(imageKey);
	}
	
	private void createExplanationLabel(Composite main) {
		serverExplanationLabel = new Label(main, SWT.WRAP);
		FormData data = new FormData();
		data.top = new FormAttachment(0,5);
		data.left = new FormAttachment(0,5);
		data.right = new FormAttachment(100,-5);
		data.width = 300;
		serverExplanationLabel.setLayoutData(data);
		serverExplanationLabel.setText(Messages.swf_Explanation);
	}

	protected void createBehaviourGroup(Composite main) {
		profileCombo = new Combo(main, SWT.READ_ONLY);
		String[] profileNames = new String[profiles.length];
		for( int i = 0; i < profiles.length; i++ ) 
			profileNames[i] = (profiles[i].getVisibleName() == null ? profiles[i].getId() : profiles[i].getVisibleName());
		profileCombo.setItems(profileNames);
		
		Label comboLabel = new Label(main, SWT.NONE);
		comboLabel.setText("Profile: ");
		FormData labelData = UIUtil.createFormData2(serverExplanationLabel, 12, null, 0, 0,5,null,0);
		comboLabel.setLayoutData(labelData);
		
		FormData groupData = UIUtil.createFormData2(serverExplanationLabel, 10, null, 0, comboLabel,5,null,0);
		profileCombo.setLayoutData(groupData);
		
		profileCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int i = profileCombo.getSelectionIndex();
				if( i != -1 ) {
					setProfile(profiles[i]);
				}
			}
		});
		
		profileDescriptionLabel = new Label(main, SWT.WRAP);
		FormData profileDescriptionLabelData = UIUtil.createFormData2(profileCombo, 5, profileCombo, 100, 0,5,100,-5);
		profileDescriptionLabelData.width = 300;
		profileDescriptionLabel.setLayoutData(profileDescriptionLabelData);

		
		requiresRuntimeLabel = new Label(main, SWT.WRAP);
		FormData requiresRuntimeLabelData = UIUtil.createFormData2(profileDescriptionLabel, 15, null, 0, 0,5,100,-5);
		requiresRuntimeLabelData.width = 300;
		requiresRuntimeLabel.setLayoutData(requiresRuntimeLabelData);
		
		
		
		IRuntime runtime = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		if( runtime == null && !runtimeForbidden()) {
			useRuntimeButton = new Button(main, SWT.CHECK);
			useRuntimeButton.setText("Assign a runtime to this server");
			FormData useRuntimeButtonData = UIUtil.createFormData2(requiresRuntimeLabel, 5, null, 0, 0,5,100,-5);
			useRuntimeButton.setLayoutData(useRuntimeButtonData);
			
			useRuntimeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					useRuntimeChanged();
				}
			});
			
			runtimeCombo = new Combo(main, SWT.READ_ONLY);
			String[] runtimeNamesWithNew = new String[runtimeNames.length+1];
			System.arraycopy(runtimeNames, 0, runtimeNamesWithNew, 0, runtimeNames.length);
			runtimeNamesWithNew[runtimeNames.length] = "New..."; //$NON-NLS-1$
			runtimeCombo.setItems(runtimeNamesWithNew);
			if( runtimeNamesWithNew.length > 0 ) {
				runtimeComboChanged();
			}
			FormData runtimeComboData = UIUtil.createFormData2(useRuntimeButton, 5, null, 0, 0,5,50,0);
			runtimeCombo.setLayoutData(runtimeComboData);

			runtimeCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					runtimeComboChanged();
				}
			});
			runtimeCombo.setEnabled(false);
		}
	}
	
	

	/**
	 * Because of the way servers are declared in extension points, 
	 * a server must mark runtime="true" or runtime="false" in the plugin.xml to indicate
	 * whether it requires a runtime or not. 
	 * Unfortunately, a server must also still provide a runtimeType, or it cannot
	 * deploy any modules at all. 
	 * 
	 * So in effect,  runtime="true" or runtime="false" functions more as a question
	 * as to whether a runtime is "required" for the server to function. 
	 * 
	 * The remaining case, though, is how to tell if a server can optionally take a runtime,
	 * or if it is forbidden. The deploy-only server and all jboss servers both 
	 * have a runtime type, and also both mark runtime="false" (not required), but, 
	 * a runtime is optional for the jboss servers, while it is forbidden for the 
	 * deploy-only server.  
	 * 
	 * This new api is the only way we can designate whether one is forbidden or optional. 
	 * @return
	 */
	protected boolean runtimeForbidden() {
		return false;
	}
	
	private void runtimeComboChanged() {
		int runtimeSelIndex = runtimeCombo.getSelectionIndex();
		IRuntime rt = null;
		IServer s = (IServer)getTaskModel().getObject(TaskModel.TASK_SERVER);
		if( runtimeSelIndex != -1 ) {
			if( runtimeSelIndex >= runtimes.length) {
				// user clicked 'new...' so we need a new runtime fragment
				getTaskModel().putObject(TASK_CUSTOM_RUNTIME, newRuntimeWorkingCopy);
				getTaskModel().putObject(TaskModel.TASK_RUNTIME, null);
				rt = newRuntimeWorkingCopy;
			} else {
				getTaskModel().putObject(TaskModel.TASK_RUNTIME, runtimes[runtimeSelIndex]);
				getTaskModel().putObject(TASK_CUSTOM_RUNTIME, null);
				rt = runtimes[runtimeSelIndex];
			}
			if( rt != null && s instanceof IServerWorkingCopy) {
				((IServerWorkingCopy)s).setRuntime(rt);
			}
		} else {
			getTaskModel().putObject(TASK_CUSTOM_RUNTIME, null);
			getTaskModel().putObject(TaskModel.TASK_RUNTIME, null);
			((IServerWorkingCopy)s).setRuntime(null);
		}
		updateErrorMessage();
	}
	
	private void setProfile(ServerProfile sp) {
		selectedProfile = sp;
		IServerWorkingCopy serverWC = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		ServerProfileModel.setProfile(serverWC, sp.getId());
		boolean requires = ServerProfileModel.getDefault().profileRequiresRuntime(serverWC.getServerType().getId(), sp.getId());
		
		// description
		profileDescriptionLabel.setText(sp.getDescription() == null ? "" : sp.getDescription());
		
		requiresRuntime = requires;
		if( !runtimeForbidden()) {
			requiresRuntimeLabel.setText("The selected profile " + (requiresRuntime ? "requires" : "does not require") + " a runtime.");
			if( requires) {
				if( useRuntimeButton != null ) {
					useRuntimeButton.setSelection(true);
					useRuntimeButton.setEnabled(false);
					useRuntimeChanged();
				}
			} else {
				useRuntimeButton.setEnabled(true);
			}
		}
		updateErrorMessage();
	}
	private void useRuntimeChanged() {
		runtimeCombo.setEnabled(useRuntimeButton.getSelection());
		if( !useRuntimeButton.getSelection()) {
			runtimeCombo.deselectAll();
		} else {
			runtimeCombo.select(0);
		}
		runtimeComboChanged();
		updateErrorMessage();
	}
	
	private void updateErrorMessage() {
		String error = getErrorString();
		if( error == null ) {
			handle.setMessage(null, IMessageProvider.NONE);
		} else {
			handle.setMessage(error, IMessageProvider.ERROR);
		}
		setComplete(handle.getMessageType() != IMessageProvider.ERROR);
		handle.update();
	}
	
	private String getErrorString() {
		if( selectedProfile == null ) {
			return "Please select a profile for your server.";
		}
		if( runtimeCombo != null && useRuntimeButton.getSelection() && runtimeCombo.getSelectionIndex() == -1) {
			return "Please select a runtime.";
		}
		return null;
	}
		
	// WST API methods
	public void enter() {
	}

	public void exit() {
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		super.performFinish(monitor);
		trackNewServerEvent(1);
	}

	private void trackNewServerEvent(int succesful) {
		IServerAttributes server = (IServerAttributes)getTaskModel().getObject(TaskModel.TASK_SERVER);
		String serverType = "UNKNOWN";
		if(server.getServerType()!=null) {
			serverType = server.getServerType().getId();
		}
		UsageEventType eventType = JBossServerUIPlugin.getDefault().getNewServerEventType();
		UsageReporter.getInstance().trackEvent(eventType.event(serverType, succesful));
		
	}

	@Override
	public void performCancel(IProgressMonitor monitor) throws CoreException {
		super.performCancel(monitor);
		trackNewServerEvent(0);
	}

	public boolean isComplete() {
		return super.isComplete();
	}

	public boolean hasComposite() {
		return true;
	}
	// get the wizard fragment for a *new* runtime 
	protected WizardFragment getRuntimeWizardFragment() {
		if( runtimeFragment == null && getTaskModel() != null) {
			IServerAttributes server = (IServerAttributes)getTaskModel().getObject(TaskModel.TASK_SERVER);
			IRuntimeType rtType = server.getServerType().getRuntimeType();
			if( rtType != null ) {
				runtimeFragment = ServerUIPlugin.getWizardFragment(rtType.getId());
			}
		}
		return runtimeFragment;
	}
	
	@Override
	public List getChildFragments() {
		List<WizardFragment> listImpl = new ArrayList<WizardFragment>();
		if( getTaskModel() == null ) 
			return listImpl;
		
		IServerAttributes server = (IServerAttributes)getTaskModel().getObject(TaskModel.TASK_SERVER);
		WizardFragment rtFrag = getRuntimeWizardFragment();
		if( runtimeCombo != null && !runtimeCombo.isDisposed()) {
			int runtimeSelIndex = runtimeCombo.getSelectionIndex();
			if( runtimeSelIndex != -1 ) {
				if( rtFrag != null && runtimes != null && runtimeSelIndex >= runtimes.length) {
					// user requests a *new* runtime
					listImpl.add(rtFrag);
					listImpl.add(TempSaveRuntimeFragment);
					listImpl.add(SaveRuntimeFragment);
				}
			}
		}
		
		// Now add a fragment for initializers for this profile/server combo
		listImpl.add(InitializationFragment);

		// Add UI fragment contributions from other plugins for this server/profile combo
		if( selectedProfile != null ) {
			WizardFragment[] contributed = getContributedFragments(
					server.getServerType().getId(), selectedProfile.getId());
			listImpl.addAll(Arrays.asList(contributed));
		}
		return listImpl;
	}

	private HashMap<String, WizardFragment[]> contributedFragments = new HashMap<String, WizardFragment[]>();
	private WizardFragment[] getContributedFragments(String serverType, String profileId) {
		String key = serverType + ":" + profileId;
		if( contributedFragments.get(key) == null ) {
			WizardFragment[] contributed = ProfileUI.getDefault().getWizardFragments(
					serverType, profileId);
			contributedFragments.put(key,  contributed);
		}
		return contributedFragments.get(key);
	}

	/**
	 * This fragment will ensure that all initializers for this
	 * server and profile combination. 
	 */
	private static final WizardFragment InitializationFragment = new WizardFragment() {
		public void performFinish(IProgressMonitor monitor) throws CoreException {
			IServer server = (IServer) getTaskModel().getObject(TaskModel.TASK_SERVER);
			String currentProfile = ServerProfileModel.getProfile(server);
			IServerProfileInitializer[] initializers = ServerProfileModel.getDefault().getInitializers(server.getServerType().getId(), currentProfile);
			
			IServerWorkingCopy wc = null;
			if( server instanceof IServerWorkingCopy) 
				wc = (IServerWorkingCopy)server;
			else
				wc = server.createWorkingCopy();

			for( int i = 0; i < initializers.length; i++ ) {
				initializers[i].initialize(wc);
			}
			server = wc.save(false, null);
			getTaskModel().putObject(TaskModel.TASK_SERVER, server.createWorkingCopy());
		}
	};
	
	
	private static final WizardFragment TempSaveRuntimeFragment = new WizardFragment() {
		public void performFinish(IProgressMonitor monitor) throws CoreException {
			IRuntime runtime = (IRuntime) getTaskModel().getObject(TASK_CUSTOM_RUNTIME);
			if (runtime != null && runtime instanceof IRuntimeWorkingCopy) {
				IRuntimeWorkingCopy workingCopy = (IRuntimeWorkingCopy) runtime;
				if (!workingCopy.isDirty())
					return;
				
				runtime = workingCopy.save(false, monitor);
				getTaskModel().putObject(TASK_CUSTOM_RUNTIME, runtime.createWorkingCopy());
			}
		}
	};
	

	private static final WizardFragment SaveRuntimeFragment = new WizardFragment() {
		public void performFinish(IProgressMonitor monitor) throws CoreException {
			IRuntime runtime = (IRuntime) getTaskModel().getObject(TASK_CUSTOM_RUNTIME);
			if (runtime != null && runtime instanceof IRuntimeWorkingCopy) {
				IRuntimeWorkingCopy workingCopy = (IRuntimeWorkingCopy) runtime;
				if (workingCopy.isDirty())
					getTaskModel().putObject(TASK_CUSTOM_RUNTIME, workingCopy.save(false, monitor));
				getTaskModel().putObject(TaskModel.TASK_RUNTIME, getTaskModel().getObject(TASK_CUSTOM_RUNTIME));
				IServer server = (IServer) getTaskModel().getObject(TaskModel.TASK_SERVER);
				IServerWorkingCopy wc = null;
				if( server instanceof IServerWorkingCopy ) {
					wc = ((IServerWorkingCopy)server);
				}
				wc.setRuntime(runtime);
				getTaskModel().putObject(TaskModel.TASK_SERVER, wc.save(false, monitor).createWorkingCopy());
			}
		}
	};
}
