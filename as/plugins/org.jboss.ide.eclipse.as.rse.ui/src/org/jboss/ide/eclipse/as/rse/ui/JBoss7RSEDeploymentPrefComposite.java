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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;
import org.jboss.ide.eclipse.as.ui.editor.ServerModeSectionComposite.ChangeServerPropertyCommand;

public class JBoss7RSEDeploymentPrefComposite extends
		RSEDeploymentPreferenceComposite {

	private Text rseServerHome;
	private Button rseBrowse;
	
	private Text rseBaseDirText;
	private Button rseBaseDirBrowse;

	private Text rseConfigFileText;
	private Button rseConfigFileBrowse;
	private ControlDecoration serverHomeDecoration;

	public JBoss7RSEDeploymentPrefComposite(Composite parent, int style,
			IServerModeUICallback callback) {
		super(parent, style, callback);
		String error = validateWidgets(false);
		callback.setComplete(error == null);
	}

	protected void createRSEWidgets(Composite child) {
		Label serverHomeLabel = new Label(this, SWT.NONE);
		serverHomeLabel.setText(RSEUIMessages.REMOTE_SERVER_HOME_LABEL);
		rseBrowse = new Button(this, SWT.NONE);
		rseBrowse.setText(RSEUIMessages.BROWSE);
		rseBrowse.setLayoutData(UIUtil.createFormData2(child, 5, null,
				0, null, 0, 100, -5));
		rseBrowse.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				remoteHomeBrowseClicked();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				remoteHomeBrowseClicked();
			}
		});
		rseServerHome = new Text(this, SWT.SINGLE | SWT.BORDER);
		serverHomeLabel.setLayoutData(UIUtil.createFormData2(child, 7,
				null, 0, 0, 10, null, 0));
		rseServerHome.setLayoutData(UIUtil.createFormData2(child, 5,
				null, 0, serverHomeLabel, 10, rseBrowse, -5));
		rseServerHome.setText(callback.getServer().getAttribute(
				RSEUtils.RSE_SERVER_HOME_DIR, ""));
		serverHomeDecoration = new ControlDecoration(rseServerHome, SWT.CENTER);
		rseServerHome.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				serverHomeChanged();
			}
		});
		
		
		Label baseDirLabel = new Label(this, SWT.NONE);
		baseDirLabel.setText(RSEUIMessages.REMOTE_BASE_DIR_LABEL);
		rseBaseDirBrowse = new Button(this, SWT.NONE);
		rseBaseDirBrowse.setText(RSEUIMessages.BROWSE);
		rseBaseDirBrowse.setLayoutData(UIUtil.createFormData2(rseServerHome, 5, null,
				0, null, 0, 100, -5));
		rseBaseDirBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				remoteBaseDirBrowseClicked();
			}
		});
		rseBaseDirText = new Text(this, SWT.SINGLE | SWT.BORDER);
		baseDirLabel.setLayoutData(UIUtil.createFormData2(rseServerHome, 7,
				null, 0, 0, 10, null, 0));
		rseBaseDirText.setLayoutData(UIUtil.createFormData2(rseServerHome, 5,
				null, 0, baseDirLabel, 5, rseBaseDirBrowse, -5));
		rseBaseDirText.setText(callback.getServer().getAttribute(
				RSEUtils.RSE_BASE_DIR, IJBossRuntimeResourceConstants.AS7_STANDALONE));
		rseBaseDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				serverBaseDirChanged();
			}
		});

		
		
		
		

	
		Label serverConfigLabel = new Label(this, SWT.NONE);
		serverConfigLabel.setText(RSEUIMessages.REMOTE_CONFIG_FILE_LABEL);
		rseConfigFileBrowse = new Button(this, SWT.NONE);
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
		rseConfigFileText = new Text(this, SWT.SINGLE | SWT.BORDER);
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
			if( isEmpty ) {
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
