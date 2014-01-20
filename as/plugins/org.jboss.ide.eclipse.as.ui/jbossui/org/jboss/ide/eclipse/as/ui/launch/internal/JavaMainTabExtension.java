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
package org.jboss.ide.eclipse.as.ui.launch.internal;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.as.ui.launch.JBossLaunchConfigurationTabGroup;
import org.jboss.ide.eclipse.as.ui.xpl.JavaMainTabClone;

public class JavaMainTabExtension extends JavaMainTabClone {
	public void createControl(Composite parent) {
		Composite comp = JBossLaunchConfigurationTabGroup.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		((GridLayout)comp.getLayout()).verticalSpacing = 0;
		//createProjectEditor(comp);
		//createVerticalSpacer(comp, 1);
		createMainTypeEditor(comp, LauncherMessages.JavaMainTab_Main_cla_ss__4);
		setControl(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);
	}
	public void initializeFrom(ILaunchConfiguration config) {
		//super.initializeFrom(config);
		//updateProjectFromConfig(config);
		setCurrentLaunchConfiguration(config);
		updateMainTypeFromConfig(config);
		updateStopInMainFromConfig(config);
		updateInheritedMainsFromConfig(config);
		updateExternalJars(config);		
	}
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);
		String name = fMainText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LauncherMessages.JavaMainTab_Main_type_not_specified_16); 
			return false;
		}
		return true;
	}
	protected IJavaProject getJavaProject() {
		return null;
	}

}