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
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFileDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.rse.ui.RSEDeploymentPreferenceUI.RSEDeploymentPreferenceComposite;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;
import org.jboss.ide.eclipse.as.ui.editor.ServerModeSectionComposite.ChangeServerPropertyCommand;

public class JBoss7RSEDeploymentPrefComposite extends
		RSEDeploymentPreferenceComposite {

	private Text rseServerHome;
	private Button rseBrowse;
	
	private Text rseConfigFileText;
	private Button rseConfigFileBrowse;

	public JBoss7RSEDeploymentPrefComposite(Composite parent, int style,
			IServerModeUICallback callback) {
		super(parent, style, callback);
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
				null, 0, serverHomeLabel, 5, rseBrowse, -5));
		rseServerHome.setText(callback.getServer().getAttribute(
				RSEUtils.RSE_SERVER_HOME_DIR, RSEUIMessages.UNSET_REMOTE_SERVER_HOME));
		rseServerHome.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				serverHomeChanged();
			}
		});

	
		Label serverConfigLabel = new Label(this, SWT.NONE);
		serverConfigLabel.setText(RSEUIMessages.REMOTE_CONFIG_FILE_LABEL);
		rseConfigFileBrowse = new Button(this, SWT.NONE);
		rseConfigFileBrowse.setText(RSEUIMessages.BROWSE);
		rseConfigFileBrowse.setLayoutData(UIUtil.createFormData2(rseServerHome, 5, null,
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
		serverConfigLabel.setLayoutData(UIUtil.createFormData2(rseServerHome, 7,
				null, 0, 0, 10, null, 0));
		rseConfigFileText.setLayoutData(UIUtil.createFormData2(rseServerHome, 5,
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
		String browseVal = RSEDeploymentPreferenceUI.browseClicked4(rseServerHome.getShell(), combo.getHost(), home);
		if (browseVal != null) {
			rseServerHome.setText(browseVal);
			serverHomeChanged();
		}
	}
	protected void remoteConfigBrowseClicked() {
		IPath home = new Path(rseServerHome.getText());
		IPath configFolder = home.append(IJBossRuntimeResourceConstants.AS7_STANDALONE)
				.append(IJBossRuntimeResourceConstants.CONFIGURATION);
		IPath configFile = configFolder.append(rseConfigFileText.getText());
		String browseVal = RSEDeploymentPreferenceUI.browseClicked4(rseServerHome.getShell(), combo.getHost(), configFile.toString());
		if (browseVal != null) {
			rseConfigFileText.setText(new Path(browseVal).lastSegment());
			configFileChanged();
		}
	}
}
