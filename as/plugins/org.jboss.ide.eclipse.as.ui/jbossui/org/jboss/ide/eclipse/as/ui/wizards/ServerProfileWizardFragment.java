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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentTypeUIUtil;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentTypeUIUtil.EditServerWizardBehaviourCallback;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentTypeUIUtil.ICompletable;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;
import org.jboss.ide.eclipse.as.ui.editor.ServerModeSectionComposite.ChangeServerPropertyCommand;
import org.jboss.ide.eclipse.as.ui.editor.internal.DelayedServerWorkingCopy;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerProfileInitializer;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel.ServerProfile;
import org.jboss.ide.eclipse.as.wtp.ui.profile.ProfileUI;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;

/**
 * 
 *
 */
public class ServerProfileWizardFragment extends WizardFragment implements ICompletable, PropertyChangeListener {
	
	/**
	 * Task model id for an IRuntime that is contributed by *our* fragment and not the wtp fragments
	 * 
	 * @see #getObject(String)
	 * @see #putObject(String, Object)
	 */
	public static final String TASK_CUSTOM_RUNTIME = "custom_runtime"; //$NON-NLS-1$

	
	/**
	 * Task model id for indicating the wizard has been opened for editing, not creating, a server
	 */
	public static final String EDITING_SERVER = "editing_server"; //$NON-NLS-1$

	
	/**
	 * Task model id for the callback handler
	 */
	public static final String WORKING_COPY_CALLBACK = "wc_callback_handler"; //$NON-NLS-1$
	
	
	/**
	 * This interface is provisional API and may be changed in the future
	 */
	public static interface IProfileComposite {
		public ServerProfile getSelectedProfile();
	}
	
	
	private IWizardHandle handle;
	private ServerProfile selectedProfile;
	private Label serverExplanationLabel; 
	private WizardFragment runtimeFragment;
	private boolean requiresRuntime;
	private Label requiresRuntimeLabel;
	private Button useRuntimeButton;
	private Combo runtimeCombo;
	private Button executeShellScripts;
	
	private IRuntime[] runtimes;
	private String[] runtimeNames;
	private IRuntimeWorkingCopy newRuntimeWorkingCopy;
	
	private IProfileComposite profileComposite;
	private IServerWorkingCopy swc;
	
	public ServerProfileWizardFragment() {
		super();
	}
	
	@Override
	public void setComplete(boolean complete) {
		super.setComplete(complete);
	}

	@Override
	public boolean isComplete() {
		return !hasDisposedWidgets() && super.isComplete();
	}
	
	/*
	 * Wizard fragments are re-used. This means the only way we can check if this is being used in a new wizard, or 
	 * in a previous wizard, is to check whether any widgets are now disposed.  We'll check the most common widget, 
	 * the description label. 
	 */
	protected boolean hasDisposedWidgets() {
		return serverExplanationLabel == null || serverExplanationLabel.isDisposed();
	}
	
	
	private IServerModeUICallback createCallback(final IWizardHandle handle) {
		ICompletable c =  new ICompletable() {
			public void setComplete(boolean complete) {
				ServerProfileWizardFragment.this.setComplete(complete);
			}
		};
		
		if( !isEditingServer()) {
			// persist immediately
			return DeploymentTypeUIUtil.getCallback(getTaskModel(), handle,c);
		} else {
			// persist on finish
			return DeploymentTypeUIUtil.getCallback(getTaskModel(), handle, c, false);
		}
	}
	
	
	private IServerModeUICallback getOrCreateCallback() {
		// Ensure we have a callback detailing how to set info into the wc
		Object cb = getTaskModel().getObject(WORKING_COPY_CALLBACK);
		if( cb == null ) {
			cb = createCallback(handle);
			getTaskModel().putObject(WORKING_COPY_CALLBACK, cb);
		}
		return (IServerModeUICallback)cb;
	}
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		IRuntime initialRuntime = getRuntimeFromTaskModel();
		IServerModeUICallback cb = getOrCreateCallback();
		
		
		swc = (IServerWorkingCopy)getTaskModel().getObject(TaskModel.TASK_SERVER);
		swc.addPropertyChangeListener(this);
		
		
		// make modifications to parent
		setPageDetails(handle);
		initializeModel();
		setComplete(false);
		Composite wrapper = new Composite(parent, SWT.NONE);
		wrapper.setLayout(new GridLayout(1, true));

		createExplanationLabel(wrapper);
		this.profileComposite = createProfileSection(wrapper);
		
		executeShellScripts = new Button(wrapper, SWT.CHECK);
		executeShellScripts.setText(Messages.EditorDoNotLaunch);
		executeShellScripts.setSelection(LaunchCommandPreferences.isIgnoreLaunchCommand(cb.getServer()));
		executeShellScripts.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				executeShellToggled();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		IRuntime initial = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		createRuntimeSection(wrapper);
		if( initial != null && !runtimeForbidden()) {
			addRuntimeDetailsGroup(wrapper);
		}
		setProfile(profileComposite.getSelectedProfile());
		if( !runtimeForbidden() && runtimeCombo != null && !runtimeCombo.isDisposed()) {
			if( initialRuntime == null ) {
				runtimeComboChanged();
			} else {
				if( useRuntimeButton != null && !useRuntimeButton.isDisposed())
					useRuntimeButton.setSelection(true);
				runtimeCombo.setEnabled(true);
				String initialName = initialRuntime.getName();
				String[] all = runtimeCombo.getItems();
				int ind = Arrays.asList(all).indexOf(initialName);
				runtimeCombo.select(ind == -1 ? 0 : ind);
				runtimeComboChanged();
			}
		}
		updateErrorMessage();
		return wrapper;
	}

	// Set the 'ignore launch' boolean on the server wc
	protected void executeShellToggled() {
		getOrCreateCallback().execute(new ChangeServerPropertyCommand(
				getOrCreateCallback().getServer(), IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS,
				new Boolean(executeShellScripts.getSelection()).toString(), Messages.EditorDoNotLaunchCommand));
	}
	
	/**
	 * Intended to be overridden by subclasses that wish to list details for their runtime
	 * @param parent
	 */
	protected void addRuntimeDetailsGroup(Composite parent) {
		// Do nothing
	}
	
	
	protected void initializeModel() {
		newRuntimeWorkingCopy = null;
		selectedProfile = null;
		IServerAttributes server = (IServerAttributes)getTaskModel().getObject(TaskModel.TASK_SERVER);
		IRuntimeType rtType = server.getServerType().getRuntimeType(); 
		String runtimeType = RuntimeUtils.getRuntimeTypeId(server.getServerType());
		try {
			newRuntimeWorkingCopy = rtType.createRuntime(null, null);
		} catch(CoreException ce) {
			JBossServerUIPlugin.log(ce.getStatus());
		}

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
	
	protected void setPageDetails(IWizardHandle handle) {
		handle.setTitle(isEditingServer() ? Messages.swf_Title_edit : Messages.swf_Title);
		handle.setImageDescriptor (getImageDescriptor());
		IServerAttributes server = (IServerAttributes)getTaskModel().getObject(TaskModel.TASK_SERVER);
		String serverDesc = server.getServerType().getDescription();
		handle.setDescription(serverDesc);
	}
	
	private boolean isEditingServer() {
		Object o = getTaskModel().getObject(EDITING_SERVER);
		return o != null && Boolean.TRUE.equals(o);
	}
	
	public ImageDescriptor getImageDescriptor() {
		IServerAttributes server = (IServerAttributes)getTaskModel().getObject(TaskModel.TASK_SERVER);
		String type = server.getServerType().getId();
		String imageKey = JBossServerUISharedImages.WIZBAN_JBOSS_LOGO;
		if( type.startsWith(IJBossToolingConstants.WF_SERVER_PREFIX)) {
			imageKey = JBossServerUISharedImages.WIZBAN_WILDFLY_LOGO;
		} else if( RuntimeUtils.isEAP(server.getServerType().getRuntimeType())) {
			imageKey = JBossServerUISharedImages.WIZBAN_EAP_LOGO;
		} else if( type.equals(IJBossToolingConstants.SERVER_AS_70) || type.equals(IJBossToolingConstants.SERVER_AS_71)) {
			imageKey = JBossServerUISharedImages.WIZBAN_AS7_LOGO;
		}
		return JBossServerUISharedImages.getImageDescriptor(imageKey);
	}
	
	private void createExplanationLabel(Composite main) {
		if( isEditingServer() ){
			Composite wrap = new Composite(main, SWT.NONE);
			wrap.setLayout(new GridLayout(2, false));
			Label warningLabel = new Label(wrap, SWT.WRAP);
			serverExplanationLabel = new Label(wrap, SWT.WRAP);
			GridData gd = new GridData();
			gd.widthHint = 400;
			serverExplanationLabel.setLayoutData(gd);
			warningLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
			
			gd = new GridData();
			gd.widthHint = 600;
			wrap.setLayoutData(gd);
		} else {
			serverExplanationLabel = new Label(main, SWT.WRAP);
			GridData gd = new GridData();
			gd.widthHint = 600;
			serverExplanationLabel.setLayoutData(gd);
		}
		serverExplanationLabel.setText(isEditingServer() ? Messages.swf_Explanation_edit : Messages.swf_Explanation);
	}

	protected void createRuntimeSection(Composite main) {
		IRuntime runtime = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		createRuntimeSection(main, runtime != null);
	}
	
	protected void createRuntimeSection(Composite main, boolean hasInitialRuntime) {
		Composite runtimeWrap = new Composite(main, SWT.NONE);
		runtimeWrap.setLayout(new FormLayout());
		
		requiresRuntimeLabel = new Label(runtimeWrap, SWT.WRAP);
		FormData requiresRuntimeLabelData = FormDataUtility.createFormData2(0, 5, null, 0, 0,5,100,-5);
		requiresRuntimeLabelData.width = 300;
		requiresRuntimeLabel.setLayoutData(requiresRuntimeLabelData);
		
		if( !hasInitialRuntime && !runtimeForbidden()) {
			useRuntimeButton = new Button(runtimeWrap, SWT.CHECK);
			useRuntimeButton.setText("Assign a runtime to this server");
			FormData useRuntimeButtonData = FormDataUtility.createFormData2(requiresRuntimeLabel, 15, null, 0, 0,5,100,-5);
			useRuntimeButton.setLayoutData(useRuntimeButtonData);
			
			useRuntimeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					useRuntimeChanged();
				}
			});
			
			runtimeCombo = new Combo(runtimeWrap, SWT.READ_ONLY);
			String[] runtimeNamesWithNew = new String[runtimeNames.length+1];
			System.arraycopy(runtimeNames, 0, runtimeNamesWithNew, 0, runtimeNames.length);
			runtimeNamesWithNew[runtimeNames.length] = "Create new runtime (next page)"; //$NON-NLS-1$
			runtimeCombo.setItems(runtimeNamesWithNew);
			if( runtimeNamesWithNew.length > 0 ) {
				runtimeComboChanged();
			}
			FormData runtimeComboData = FormDataUtility.createFormData2(useRuntimeButton, 5, null, 0, 0,5,50,0);
			runtimeCombo.setLayoutData(runtimeComboData);

			runtimeCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					runtimeComboChanged();
				}
			});
			runtimeCombo.setEnabled(false);
		}
	}
	
	protected IProfileComposite createProfileSection(Composite main) {
		ProfileComposite pc =  new ProfileComposite(main, SWT.NONE, this);
		GridData gd = new GridData();
		gd.widthHint = 500;
		pc.setLayoutData(gd);
		return pc;
	}
	
	private static class ProfileComposite extends Composite implements IProfileComposite {
		private Combo profileCombo;
		private Label profileDescriptionLabel;
		private ServerProfile[] profiles;
		private ServerProfileWizardFragment profileFragment;
		ProfileComposite(Composite parent, int style, final ServerProfileWizardFragment profileFragment) {
			super(parent, style);
			setLayout(new FormLayout());
			this.profileFragment = profileFragment;
			initProfiles();
			
			profileCombo = new Combo(this, SWT.READ_ONLY);
			String[] profileNames = new String[profiles.length];
			for( int i = 0; i < profiles.length; i++ ) 
				profileNames[i] = (profiles[i].getVisibleName() == null ? profiles[i].getId() : profiles[i].getVisibleName());
			profileCombo.setItems(profileNames);
			
			Label comboLabel = new Label(this, SWT.NONE);
			comboLabel.setText("Profile: ");
			FormData labelData = FormDataUtility.createFormData2(0, 12, null, 0, 0,5,null,0);
			comboLabel.setLayoutData(labelData);
			
			FormData groupData = FormDataUtility.createFormData2(0, 10, null, 0, comboLabel,5,null,0);
			profileCombo.setLayoutData(groupData);
			
			profileCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int i = profileCombo.getSelectionIndex();
					if( i != -1 ) {
						profileFragment.setProfile(profiles[i]);
						profileDescriptionLabel.setText(profiles[i].getDescription() == null ? "" : profiles[i].getDescription());
					}
				}
			});
			
			profileDescriptionLabel = new Label(this, SWT.WRAP);
			FormData profileDescriptionLabelData = FormDataUtility.createFormData2(profileCombo, 5, profileCombo, 100, 0,5,100,-5);
			profileDescriptionLabelData.width = 300;
			profileDescriptionLabel.setLayoutData(profileDescriptionLabelData);
			initializeWidgetDefaults();
		}
		
		protected void initProfiles() {
			IServerAttributes server = (IServerAttributes)profileFragment.getTaskModel().getObject(TaskModel.TASK_SERVER);
			String serverType = server.getServerType().getId();
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
		}
		
		protected void initializeWidgetDefaults() {
			IServerAttributes server = (IServerAttributes)profileFragment.getTaskModel().getObject(TaskModel.TASK_SERVER);
			String currentProfile = ServerProfileModel.getProfile(server, ServerProfileModel.DEFAULT_SERVER_PROFILE);
			int profileIndex = -1;
			for( int i = 0; i < profiles.length && profileIndex == -1; i++ ) {
				if( profiles[i].getId().equals(currentProfile)) {
					profileCombo.select(i);
					profileFragment.setProfile(profiles[i]);
					profileDescriptionLabel.setText(profiles[i].getDescription() == null ? "" : profiles[i].getDescription());
					break;
				}
			}
		}

		@Override
		public ServerProfile getSelectedProfile() {
			int i = profileCombo.getSelectionIndex();
			if( i != -1 ) {
				return profiles[i];
			}
			return null;
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
	
	protected IRuntime getRuntimeFromTaskModel() {
		IRuntime rt = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		if( rt == null ) {
			rt = (IRuntime)getTaskModel().getObject(TASK_CUSTOM_RUNTIME);
			if( rt == null ) {
				IServerAttributes s = (IServerAttributes)getTaskModel().getObject(TaskModel.TASK_SERVER);
				rt = s.getRuntime();
				if( rt == null ) {
					if( s instanceof IServerWorkingCopy ) {
						IServer original = ((IServerWorkingCopy)s).getOriginal();
						if( original != null ) {
							rt = original.getRuntime();
						}
					}
				}
			}
		}
		return rt;
	}
	
	protected void runtimeComboChanged() {
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
			fireSetRuntimeCommand((IServerWorkingCopy)s, rt);
		} else {
			getTaskModel().putObject(TASK_CUSTOM_RUNTIME, null);
			getTaskModel().putObject(TaskModel.TASK_RUNTIME, null);
			fireSetRuntimeCommand((IServerWorkingCopy)s, null);
		}
		updateErrorMessage();
	}
	
	// Fire the delayed setting of the runtime
	private boolean updatingRuntime = false;
	private void fireSetRuntimeCommand(IServerWorkingCopy server, final IRuntime rt) {
		if( updatingRuntime ) {
			return;
		}
		updatingRuntime = true;
		IServerModeUICallback o = (IServerModeUICallback)getTaskModel().getObject(WORKING_COPY_CALLBACK);
		o.execute(new ServerCommand(server, "Set Runtime"){
			public void execute() {
				server.setRuntime(rt);
			}
			public void undo() {
			}
		});
		updatingRuntime = false;
	}
	
	
	protected void setProfile(ServerProfile sp) {
		selectedProfile = sp;
		final IServerWorkingCopy serverWC = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		final String spId = sp.getId();
		IServerModeUICallback o = (IServerModeUICallback)getTaskModel().getObject(WORKING_COPY_CALLBACK);
		o.execute(new ServerCommand(serverWC, "Modify Profile"){
			public void execute() {
				ServerProfileModel.setProfile(serverWC, spId);
			}
			public void undo() {
			}});
		boolean requires = ServerProfileModel.getDefault().profileRequiresRuntime(serverWC.getServerType().getId(), sp.getId());
		requiresRuntime = requires;
		if( !runtimeForbidden() && requiresRuntimeLabel != null && !requiresRuntimeLabel.isDisposed()) {
			String requiresText = null;
			if( requiresRuntime ) {
				requiresText = "\nThe selected profile requires a runtime.";
			} else {
				requiresText = "The selected profile does not require a runtime, though some features (ex: JMX) may not be available without one.";
			}
			requiresRuntimeLabel.setText(requiresText);
			if( requires) {
				if( useRuntimeButton != null && !useRuntimeButton.isDisposed()) {
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
		} else if( runtimeCombo.getSelectionIndex() == -1 ){
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
		if( runtimeCombo != null && !runtimeCombo.isDisposed() && useRuntimeButton.getSelection() && runtimeCombo.getSelectionIndex() == -1) {
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
		Object cb = getTaskModel().getObject(WORKING_COPY_CALLBACK);
		if( cb != null && cb instanceof EditServerWizardBehaviourCallback) {
			((EditServerWizardBehaviourCallback)cb).performFinish();
		}
		dispose();
	}

	@Override
	public void performCancel(IProgressMonitor monitor) throws CoreException {
		super.performCancel(monitor);
		dispose();
	}
	
	public boolean hasComposite() {
		return true;
	}
	
	// get the wizard fragment for a *new* runtime 
	protected WizardFragment getRuntimeWizardFragment() {
		if( runtimeFragment == null && getTaskModel() != null) {
			IServerAttributes server = (IServerAttributes)getTaskModel().getObject(TaskModel.TASK_SERVER);
			String rtType = RuntimeUtils.getRuntimeTypeId(server.getServerType());
			if( rtType != null ) {
				runtimeFragment = ServerUIPlugin.getWizardFragment(rtType);
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
			
			IServerModeUICallback cb = (IServerModeUICallback)getTaskModel().getObject(WORKING_COPY_CALLBACK); // TODO
			if( cb != null ) {
				IServerWorkingCopy wc2 = cb.getServer();
				if( wc2 instanceof DelayedServerWorkingCopy ) {
					((DelayedServerWorkingCopy)wc2).saveToOriginalWorkingCopy();
				}
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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if( evt.getPropertyName().equals(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS)) {
			Boolean newVal = (Boolean)evt.getNewValue();
			if( !newVal.equals(executeShellScripts.getSelection()))
				executeShellScripts.setSelection(newVal);
		}
		if( evt.getPropertyName().equals("runtime-id")) {
			runtimeComboChanged();
		}
		updateErrorMessage();
	}
	
	private void dispose() {
		if (swc != null) {
			swc.removePropertyChangeListener(this);
		}
	}
}
