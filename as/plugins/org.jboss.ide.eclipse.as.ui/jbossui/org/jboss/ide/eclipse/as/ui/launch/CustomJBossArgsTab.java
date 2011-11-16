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
package org.jboss.ide.eclipse.as.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.ui.Messages;

public class CustomJBossArgsTab extends JavaArgumentsTab {
	
	private Button syncWithRuntime;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		comp.setLayout(layout);
		comp.setFont(font);
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		Label desc = new Label(comp, SWT.NONE);
		desc.setText(Messages.ServerJavaArgsSyncDesc);
		syncWithRuntime = new Button(comp, SWT.CHECK);
		syncWithRuntime.setText(Messages.ServerJavaArgsSyncText);

		super.createControl(comp);
		setControl(comp);
	}
	
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		configuration.setAttribute(JBossServer.AUTOMATICALLY_UPDATE_LAUNCH, 
				syncWithRuntime.getSelection());
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		try {
			boolean selected = configuration.getAttribute(JBossServer.AUTOMATICALLY_UPDATE_LAUNCH, true);
			syncWithRuntime.setSelection(selected);
		} catch (CoreException e) {
			setErrorMessage(LauncherMessages.JavaArgumentsTab_Exception_occurred_reading_configuration___15 + e.getStatus().getMessage()); 
			JDIDebugUIPlugin.log(e);
		}
	}
}