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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.rse.ui.RSEDeploymentPreferenceUI.RSEDeploymentPreferenceComposite;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;
import org.jboss.ide.eclipse.as.ui.editor.ServerModeSectionComposite.ChangeServerPropertyCommand;

public class JBoss7RSEDeploymentPrefComposite extends
		RSEDeploymentPreferenceComposite {

	private Text rseServerHome;
	private Button rseBrowse;

	public JBoss7RSEDeploymentPrefComposite(Composite parent, int style,
			IServerModeUICallback callback) {
		super(parent, style, callback);
	}

	protected void createRSEWidgets(Composite child) {
		Label serverHomeLabel = new Label(this, SWT.NONE);
		serverHomeLabel.setText("Remote Server Home: ");
		rseBrowse = new Button(this, SWT.NONE);
		rseBrowse.setText("Browse...");
		rseBrowse.setLayoutData(UIUtil.createFormData2(child, 5, null,
				0, null, 0, 100, -5));
		rseBrowse.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				browseClicked2();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				browseClicked2();
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
	protected void browseClicked2() {
		String browseVal = browseClicked3(rseServerHome.getShell());
		if (browseVal != null) {
			rseServerHome.setText(browseVal);
			serverHomeChanged();
		}
	}
}
