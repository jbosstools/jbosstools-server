/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor;

import java.util.ArrayList;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.ui.FormUtils;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentTypeUIUtil.ServerEditorUICallback;
import org.jboss.ide.eclipse.as.ui.wizards.ServerProfileWizardFragment;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyButtonCommand;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;

/**
 * A composite to choose a server mode (or profile) from a dropdown, and 
 * fill in properties specific to that mode/profile. 
 * 
 * Recently modified to no longer allow changing of the profile. 
 */
public class ServerModeSectionComposite extends Composite {
	private ArrayList<DeployUIAdditions> deployAdditions;
	private Label profileLabel;
	private ScrolledPageBook preferencePageBook;
	private ServerEditorUICallback callback;
	private Button listenOnAllHosts; // may be null
	private Button exposeManagement; // may be null
	private Button executeShellScripts; // may be null;
	protected Link configureProfileLink;
	
	private DeployUIAdditions currentUIAddition;
	private IManagedForm form;
	public ServerModeSectionComposite(Composite parent, int style, ServerEditorUICallback callback) {
		this(parent, style, callback, null);
	}
	
	public ServerModeSectionComposite(Composite parent, int style, ServerEditorUICallback callback, IManagedForm form) {
		super(parent, style);
		this.callback = callback;
		this.form = form;
		loadDeployTypeData();
		FormToolkit toolkit = new FormToolkit(getDisplay());
		FormUtils.adaptFormCompositeRecursively(this, toolkit);	
		setLayout(new FormLayout());
		
		Control top = null;
		
		configureProfileLink = new Link( this, SWT.NONE);
		FormData fd = FormDataUtility.createFormData2(top, 5, null, 0, 0, 5, null, 0);
		configureProfileLink.setLayoutData(fd);
		
		profileLabel = new Label(this, SWT.READ_ONLY);
		fd = FormDataUtility.createFormData2(top, 5, null, 0, configureProfileLink, 5, 0, 400);
		profileLabel.setLayoutData(fd);

	    String profName = getCurrentProfileName();
	    configureProfileLink.setText("<a href=\"\">Behavior Profile:</a>   "); //$NON-NLS-1$ 
	    configureProfileLink.addListener(SWT.Selection, createConfigureListener());
		
		profileLabel.setText(( profName == null ? "Not Found" : profName));
		top = configureProfileLink;
		
		if( showExecuteShellCheckbox()) {
			executeShellScripts = new Button(this, SWT.CHECK);
			executeShellScripts.setText(Messages.EditorDoNotLaunch);
			fd = FormDataUtility.createFormData2(top, 5, null, 0, 0, 5, null, 0);
			executeShellScripts.setLayoutData(fd);
			top = executeShellScripts;
			executeShellScripts.setSelection(LaunchCommandPreferences.isIgnoreLaunchCommand(callback.getServer()));
			executeShellScripts.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					executeShellToggled(this);
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		
		if( showListenOnAllHostsCheckbox()) {
			listenOnAllHosts = new Button(this, SWT.CHECK);
			listenOnAllHosts.setText(Messages.EditorListenOnAllHosts);
			fd = FormDataUtility.createFormData2(top == null ? 0 : top, 5, null, 0, 0, 5, null, 0);
			listenOnAllHosts.setLayoutData(fd);
			top = listenOnAllHosts;
			listenOnAllHosts.setSelection(LaunchCommandPreferences.listensOnAllHosts(callback.getServer()));
			listenOnAllHosts.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					listenOnAllHostsToggled(this);
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}}
			);
		}

		if( showExposeManagementCheckbox()) {
			exposeManagement = new Button(this, SWT.CHECK);
			exposeManagement.setText(Messages.EditorExposeManagement);
			fd = FormDataUtility.createFormData2(top == null ? 0 : top, 5, null, 0, 0, 5, null, 0);
			exposeManagement.setLayoutData(fd);
			top = exposeManagement;
			exposeManagement.setSelection(LaunchCommandPreferences.exposesManagement(callback.getServer()));
			exposeManagement.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					exposeManagementToggled(this);
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}}
			);
		}
		
		String profileId = getCurrentProfileId();
		ServerProfileModel.ServerProfile sp = ServerProfileModel.getDefault().getProfile(
				callback.getServer().getServerType().getId(), profileId);
		if( sp == null ) {
			// The current devenv does not have this profile in it. 
			String warn = "Your current environment is missing functionality\nassociated with your behavior profile.";
			Label myLabel = new Label(this, SWT.LEFT);
			myLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			myLabel.setText(warn);
			FormData fd2 = FormDataUtility.createFormData2(top, 5, null, 0, 0, 5, null, 0);
			myLabel.setLayoutData(fd2);
			top = myLabel;
		}
		
		// If I change style to SWT.H_SCROLL | SWT.V_SCROLL, it changes the color from white to grey
		// in the server editor, and the toolkit's attempt to change colors does not occur. Very strange. 
	    preferencePageBook = toolkit.createPageBook(this, SWT.NONE); 
	    
	    fd =  FormDataUtility.createFormData2(top, 5, null, 0, 0, 5, 100, -5);
	    fd.width = 100;
	    fd.height = 250;
	    preferencePageBook.setLayoutData(fd);
	    
		updateProfilePagebook();
	}
	
	protected String getCurrentProfileId() {
		IServer original = callback.getServer().getOriginal();
		IControllableServerBehavior ds = original == null ? null : JBossServerBehaviorUtils.getControllableBehavior(callback.getServer().getOriginal());
		String currentProfileId = null;
		if( ds != null ) {
			currentProfileId = ServerProfileModel.getProfile(callback.getServer());
		}
		return currentProfileId;
	}
	
	protected String getCurrentProfileName() {
		String id = getCurrentProfileId();
		String currentProfileName = id;
		if( id != null ) {
			ServerProfileModel.ServerProfile sp = ServerProfileModel.getDefault().getProfile(callback.getServer().getServerType().getId(), id);
			currentProfileName = sp == null ? id : sp.getVisibleName();
		}
		return currentProfileName;
	}
	
	
	
	protected Listener createConfigureListener() {
		return new Listener() {
			public void handleEvent(Event event) {
				configurePressed();
			}
		};
	}
	
	protected WizardFragment createRootConfigureFragment() {
		return ServerUIPlugin.getWizardFragment( callback.getServer().getServerType().getId());
	}
	
	private void configurePressed() {
		TaskModel tm = new TaskModel();
		tm.putObject(TaskModel.TASK_SERVER, callback.getServer());
		tm.putObject(ServerProfileWizardFragment.EDITING_SERVER, Boolean.TRUE); // indicating we're editing the server
		
		final boolean[] closed = new boolean[1];
		closed[0] = false;
		IServerWorkingCopy s = callback.getServer();
		IServer s2 = s.getOriginal();
		final IEditorSite site = callback.getPart().getEditorSite();
		
		TaskWizard tw = new TaskWizard("Configure Server Profile", createRootConfigureFragment(), tm) {

			@Override
			public boolean performFinish() {
				if( site instanceof MultiPageEditorSite) {
					MultiPageEditorPart mpep = ((MultiPageEditorSite)site).getMultiPageEditor();
					closed[0] = site.getPage().closeEditor(mpep, false);
				}
				return super.performFinish();
			}
			
		};
		WizardDialog wd = new WizardDialog(profileLabel.getShell(), tw);
		wd.open();
		if (closed[0] && site instanceof MultiPageEditorSite) {
			try {
				ServerUIPlugin.editServer(s2);
			} catch (Exception e) {
				if (Trace.SEVERE) {
					Trace.trace(Trace.STRING_SEVERE, "Error editing element", e);
				}
			}
		}
	}
	
	/* Return the currently selected behavior mode's ui object */
	public IDeploymentTypeUI getCurrentBehaviourUI() {
		return currentUIAddition.getUI();
	}
	
	/* Set what our default local and remote modes are */
	protected String getDefaultServerMode() {
		return ServerProfileModel.DEFAULT_SERVER_PROFILE;
	}
	protected String getDefaultLocalServerMode() {
		return getDefaultServerMode();
	}
	protected String getDefaultRemoteServerMode() {
		return "rse"; //$NON-NLS-1$
	}
	
	/* Can this server type expose management port? */
	protected boolean showExecuteShellCheckbox() {
		return true;
	}
	/* Can this server type listen on all ports via -b flag? */
	protected boolean showListenOnAllHostsCheckbox() {
		JBossExtendedProperties props = getExtendedProperties();
		return props == null ? false : props.runtimeSupportsBindingToAllInterfaces();
	}

	/* Can this server type expose management port? */
	protected boolean showExposeManagementCheckbox() {
		JBossExtendedProperties props = getExtendedProperties();
		return props == null ? false : props.runtimeSupportsExposingManagement();
	}

	protected JBossExtendedProperties getExtendedProperties() {
		IServerWorkingCopy wc = callback.getServer();
		JBossExtendedProperties props = (JBossExtendedProperties)wc
				.loadAdapter(JBossExtendedProperties.class, 
							 new NullProgressMonitor());
		return props;
	}
	
	// Set the 'ignore launch' boolean on the server wc
	protected void executeShellToggled(SelectionListener listener) {
		callback.execute(new ServerWorkingCopyPropertyButtonCommand(callback.getServer(), 
						 Messages.EditorDoNotLaunchCommand, executeShellScripts, executeShellScripts.getSelection(), IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, listener));
	}
	
	// Set the listen on all hosts boolean on the server wc
	protected void listenOnAllHostsToggled(SelectionListener listener) {
		callback.execute(new ServerWorkingCopyPropertyButtonCommand(callback.getServer(), 
				 Messages.EditorListenOnAllHostsCommand, listenOnAllHosts, listenOnAllHosts.getSelection(), IJBossToolingConstants.LISTEN_ALL_HOSTS, listener));
	}

	// Set the expose management boolean on the server wc
	protected void exposeManagementToggled(SelectionListener listener) {
		callback.execute(new ServerWorkingCopyPropertyButtonCommand(callback.getServer(), 
				 Messages.EditorExposeManagementCommand, exposeManagement, exposeManagement.getSelection(), IJBossToolingConstants.EXPOSE_MANAGEMENT_SERVICE, listener));
	}

	/* An internal wrapper class to help with instantiating the local / rse widgets */
	private class DeployUIAdditions {
		private String behaviourName;
		private String behaviourId;
		
		private IDeploymentTypeUI ui;
		private boolean registered = false;
		
		public DeployUIAdditions(String name, String id,IDeploymentTypeUI ui) {
			this.behaviourName = name;
			this.behaviourId = id;
			this.ui = ui;
		}
		public IDeploymentTypeUI getUI() {
			return ui;
		}
		public boolean isRegistered() {
			return registered;
		}
		public void createComposite(Composite parent) {
			// UI can be null
			if( ui != null ) {
				ui.fillComposite(parent, callback);
				registered = true;
			} else {
				parent.setLayout(new FillLayout());
				Composite child = new Composite(parent, SWT.None);
				child.setLayout(new FormLayout());
			}
		}
	}

	// Load the deploy type ui elements (local / rse / other)
	// TODO this needs rewrite
	private void loadDeployTypeData() {
		String currentProfileId = getCurrentProfileId();
		deployAdditions = new ArrayList<DeployUIAdditions>();
		String[] supported = currentProfileId == null ? new String[0] : new String[]{currentProfileId};
		String serverType = callback.getServer().getServerType().getId();
		for( int i = 0; i < supported.length; i++) {
			IDeploymentTypeUI ui = EditorExtensionManager.getDefault().getPublishPreferenceUI(supported[i]);
			ServerProfileModel.ServerProfile sp = ServerProfileModel.getDefault().getProfile(serverType, supported[i]);
			String name = (sp == null ? supported[i] : (sp.getVisibleName() == null ? sp.getId() : sp.getVisibleName()));
			deployAdditions.add(new DeployUIAdditions(
					name, 
					supported[i], ui));
		}
		currentUIAddition = deployAdditions.size() > 0 ? deployAdditions.get(0) : null;
	}

	/* 
	 * The deploy type has changed, and so we should swap out
	 * the deploy type widget to show the new mode's composite 
	 */
	private void updateProfilePagebook() {
		if( currentUIAddition != null ) {
			if( !currentUIAddition.isRegistered()) {
				Composite newRoot = preferencePageBook.createPage(currentUIAddition);

				currentUIAddition.createComposite(newRoot);
			}
			preferencePageBook.showPage(currentUIAddition);
			Control page = preferencePageBook.getCurrentPage();
			
			if (form != null) {
				// If we have a managed form, we can recompute the size of 
				// the local / rse composite to fit the contents. 
				// If we don't have a managed form, we should continue with the 
				// largest size possible. 
				Point point = page.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				FormData data = (FormData) preferencePageBook.getLayoutData();
				data.bottom = new FormAttachment(0, point.x-50);
				form.getForm().layout(true, true);
				form.getForm().reflow(true);
			}
		}
	}

	/* Deploy only server does not expose the various local / rse widgets */
	private boolean shouldChangeDefaultDeployType(IServerWorkingCopy server) {
		return !server.getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER);
	}
	
	/* A command which sets a key / value pair on the server */
	public static class ChangeServerPropertyCommand extends ServerCommand {
		private IServerWorkingCopy server;
		private String key;
		private String oldVal;
		private String newVal;
		public ChangeServerPropertyCommand(IServerWorkingCopy server, String key, String val, String commandName) {
			this(server, key, val, ServerProfileModel.DEFAULT_SERVER_PROFILE, commandName);
		}
		
		public ChangeServerPropertyCommand(IServerWorkingCopy server, String key, String val, String oldDefault, String commandName) {
			super(server, commandName);
			this.server = server;
			this.key = key;
			this.newVal = val;
			this.oldVal = server.getAttribute(key, oldDefault);
		}
		
		public void execute() {
			server.setAttribute(key, newVal);
		}
		public void undo() {
			server.setAttribute(key, oldVal);
		}
	}	
}
