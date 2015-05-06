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

import java.beans.PropertyChangeEvent;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;
import org.jboss.ide.eclipse.as.ui.editor.ServerModeSectionComposite.ChangeServerPropertyCommand;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;

public class JBoss7RSEDeploymentPrefComposite extends
		RSEDeploymentPreferenceComposite {

	private Text rseServerHome;
	private Button rseBrowse;
	
	private Text rseBaseDirText;
	private Button rseBaseDirBrowse;

	private Text rseConfigFileText;
	private Button rseConfigFileBrowse;
	private ControlDecoration serverHomeDecoration;
	private Label remoteRuntimeRequiredLabel;
	
	public JBoss7RSEDeploymentPrefComposite(Composite parent, int style,
			IServerModeUICallback callback) {
		super(parent, style, callback);
		validateWidgets(false);
		callback.getServer().addPropertyChangeListener(this);
	}
	
	private boolean isRemoteRuntimeRequired() {
		// remote runtime is required for all cases except rse+mgmt with externally-managed
		// I do not like this code. I would prefer if there was some way to accurately test this
		// by iterating through active subsystems to determine if any require a remote runtime or not. 
		
		String currentProfile = ServerProfileModel.getProfile(callback.getServer(), ServerProfileModel.DEFAULT_SERVER_PROFILE);
		if( currentProfile.equals("rse.mgmt")) {
			boolean ignoreLaunch = LaunchCommandPreferences.isIgnoreLaunchCommand(callback.getServer());
			if( ignoreLaunch )
				return false;
		}
		return true;
	}

	
	@Override
	protected void propertyChangeBody(PropertyChangeEvent evt) {
		if( evt.getPropertyName().equals(ServerProfileModel.SERVER_PROFILE_PROPERTY_KEY)
				|| evt.getPropertyName().equals(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS)) {
			updateRuntimeLabel();
			validateWidgets();
		}
		super.propertyChangeBody(evt);
	}
	
	private void updateRuntimeLabel() {
		if( isRemoteRuntimeRequired())
			remoteRuntimeRequiredLabel.setText("");
		else
			remoteRuntimeRequiredLabel.setText("Remote runtime details are optional for your current configuration.");
	}
	
	protected void createRSEWidgets(Composite child2) {
		isRemoteRuntimeRequired();
		
		Group child = new Group(child2, SWT.BORDER);
		
		// Where I belong in the parent
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		child.setLayoutData(data);
		
		child.setText("Remote Runtime Details");
		child.setLayout(new FormLayout());
		
		remoteRuntimeRequiredLabel = new Label(child, SWT.NONE);
		updateRuntimeLabel();
		remoteRuntimeRequiredLabel.setLayoutData(FormDataUtility.createFormData2(0, 5, null,0, 0, 10, 100, -5));
		
		Label serverHomeLabel = new Label(child, SWT.NONE);
		serverHomeLabel.setText(RSEUIMessages.REMOTE_SERVER_HOME_LABEL);
		rseBrowse = new Button(child, SWT.NONE);
		rseBrowse.setText(RSEUIMessages.BROWSE);
		rseBrowse.setLayoutData(FormDataUtility.createFormData2(remoteRuntimeRequiredLabel, 5, null,0, null, 0, 100, -5));
		rseBrowse.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				remoteHomeBrowseClicked();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				remoteHomeBrowseClicked();
			}
		});
		rseServerHome = new Text(child, SWT.SINGLE | SWT.BORDER);
		serverHomeLabel.setLayoutData(FormDataUtility.createFormData2(remoteRuntimeRequiredLabel, 7, null, 0, 0, 10, null, 0));
		FormData serverHomeData = FormDataUtility.createFormData2(remoteRuntimeRequiredLabel, 5, null, 0, serverHomeLabel, 10, rseBrowse, -5);
		serverHomeData.width = 150;
		rseServerHome.setLayoutData(serverHomeData);
		rseServerHome.setText(callback.getServer().getAttribute(
				RSEUtils.RSE_SERVER_HOME_DIR, ""));
		serverHomeDecoration = new ControlDecoration(rseServerHome, SWT.CENTER);
		rseServerHome.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				serverHomeChanged();
			}
		});
		
		
		Label baseDirLabel = new Label(child, SWT.NONE);
		baseDirLabel.setText(RSEUIMessages.REMOTE_BASE_DIR_LABEL);
		rseBaseDirBrowse = new Button(child, SWT.NONE);
		rseBaseDirBrowse.setText(RSEUIMessages.BROWSE);
		rseBaseDirBrowse.setLayoutData(UIUtil.createFormData2(rseServerHome, 5, null,
				0, null, 0, 100, -5));
		rseBaseDirBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				remoteBaseDirBrowseClicked();
			}
		});
		rseBaseDirText = new Text(child, SWT.SINGLE | SWT.BORDER);
		baseDirLabel.setLayoutData(UIUtil.createFormData2(rseServerHome, 7,
				null, 0, 0, 10, null, 0));
		FormData rseBaseDirTextData = UIUtil.createFormData2(rseServerHome, 5,
				null, 0, baseDirLabel, 5, rseBaseDirBrowse, -5);
		rseBaseDirTextData.width = 100;
		rseBaseDirText.setLayoutData(rseBaseDirTextData);
		rseBaseDirText.setText(callback.getServer().getAttribute(
				RSEUtils.RSE_BASE_DIR, IJBossRuntimeResourceConstants.AS7_STANDALONE));
		rseBaseDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				serverBaseDirChanged();
			}
		});

		Label serverConfigLabel = new Label(child, SWT.NONE);
		serverConfigLabel.setText(RSEUIMessages.REMOTE_CONFIG_FILE_LABEL);
		rseConfigFileBrowse = new Button(child, SWT.NONE);
		rseConfigFileBrowse.setText(RSEUIMessages.BROWSE);
		rseConfigFileBrowse.setLayoutData(UIUtil.createFormData2(rseBaseDirText, 5, null,
				0, null, 0, 100, -5));
		rseConfigFileBrowse.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				remoteConfigBrowseClicked();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				remoteConfigBrowseClicked();
			}
		});
		rseConfigFileText = new Text(child, SWT.SINGLE | SWT.BORDER);
		serverConfigLabel.setLayoutData(UIUtil.createFormData2(rseBaseDirText, 7,
				null, 0, 0, 10, null, 0));
		rseConfigFileText.setLayoutData(UIUtil.createFormData2(rseBaseDirText, 5,
				null, 0, serverConfigLabel, 5, rseConfigFileBrowse, -5));
		rseConfigFileText.setText(callback.getServer().getAttribute(
				RSEUtils.RSE_SERVER_CONFIG, LocalJBoss7ServerRuntime.CONFIG_FILE_DEFAULT));
		rseConfigFileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				configFileChanged();
			}
		});
		
		validateWidgets();
	}
	protected void serverHomeChanged() {
		if( !isUpdatingFromModelChange()) {
			String safeString = callback.getRuntime() != null ? callback.getRuntime().getLocation() != null ? 
					callback.getRuntime().getLocation().toString() : "" : "";
			callback.execute(new ChangeServerPropertyCommand(
					callback.getServer(), RSEUtils.RSE_SERVER_HOME_DIR, rseServerHome.getText(), 
					safeString, RSEUIMessages.CHANGE_REMOTE_SERVER_HOME));
		}
		validateWidgets();
	}
	
	
	protected String validateWidgets() {
		return validateWidgets(true);
	}
	protected String validateWidgets(boolean updateErrorMessage) {
		
		String errorMsg = null;
		if( serverHomeDecoration != null ) {
			boolean isEmpty = rseServerHome == null || rseServerHome.getText() == null || rseServerHome.getText().trim().isEmpty();
			if( isEmpty && isRemoteRuntimeRequired()) {
				serverHomeDecoration.setDescriptionText("Remote server home cannot be empty.");
	            Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
	            serverHomeDecoration.setImage(image);
	            serverHomeDecoration.show();
	            errorMsg = serverHomeDecoration.getDescriptionText();
			} else {
				serverHomeDecoration.hide();
			}
		}
		if( updateErrorMessage ) {
			callback.setErrorMessage(errorMsg);
		}
		callback.setComplete(errorMsg == null);
		return errorMsg;
	}

	
	protected void serverBaseDirChanged() {
		if( !isUpdatingFromModelChange()) {
			callback.execute(new ChangeServerPropertyCommand(
					callback.getServer(), RSEUtils.RSE_BASE_DIR, rseBaseDirText.getText(), 
					IJBossRuntimeResourceConstants.AS7_STANDALONE, 
					RSEUIMessages.CHANGE_REMOTE_BASE_DIR));
		}
	}

	protected void configFileChanged() {
		if( !isUpdatingFromModelChange()) {
			IRuntime rt = callback.getRuntime();
			LocalJBoss7ServerRuntime jb7srt = rt == null ? null : (LocalJBoss7ServerRuntime)rt.loadAdapter(LocalJBoss7ServerRuntime.class, null);
			String safeString = jb7srt == null ? "" : jb7srt.getConfigurationFile();
			callback.execute(new ChangeServerPropertyCommand(
					callback.getServer(), RSEUtils.RSE_SERVER_CONFIG, rseConfigFileText.getText(), 
					safeString, RSEUIMessages.CHANGE_REMOTE_CONFIG_FILE));
		}
	}
	
	protected void remoteHomeBrowseClicked() {
		String home = rseServerHome.getText() == null ? "" : rseServerHome.getText();
		String browseVal = RSEBrowseBehavior.browseClicked(rseServerHome.getShell(), combo.getHost(), home);
		if (browseVal != null) {
			rseServerHome.setText(browseVal);
			serverHomeChanged();
		}
	}
	
	
	protected void remoteBaseDirBrowseClicked() {
		String basedir = rseBaseDirText.getText() == null ? "" : rseBaseDirText.getText();
		String toOpen = null;
		if( new Path(basedir).isAbsolute()) {
			toOpen = basedir;
		} else {
			toOpen = new Path(rseServerHome.getText()).append(basedir).toString();
		}
		String browseVal = RSEBrowseBehavior.browseClicked(rseServerHome.getShell(), combo.getHost(), toOpen);
		if (browseVal != null) {
			if( browseVal.startsWith(rseServerHome.getText())) {
				browseVal = new Path(browseVal).makeRelativeTo(new Path(rseServerHome.getText())).toString();
			}
			rseBaseDirText.setText(browseVal);
			serverBaseDirChanged();
		}
	}
	
	protected void remoteConfigBrowseClicked() {
		IPath home = new Path(rseServerHome.getText());
		IPath basedir = new Path(rseBaseDirText.getText());
		IPath basedir2 = basedir.isAbsolute() ? basedir : home.append(basedir);
		
		IPath configFolder = basedir2.append(IJBossRuntimeResourceConstants.CONFIGURATION);
		IPath configFile = configFolder.append(rseConfigFileText.getText());
		String browseVal = RSEBrowseBehavior.browseClicked(rseServerHome.getShell(), combo.getHost(), configFile.toString());
		if (browseVal != null) {
			rseConfigFileText.setText(new Path(browseVal).lastSegment());
			configFileChanged();
		}
	}
}
