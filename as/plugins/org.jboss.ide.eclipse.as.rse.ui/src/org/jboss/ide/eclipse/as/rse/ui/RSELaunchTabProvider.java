/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.as.rse.core.RSELaunchConfigProperties;
import org.jboss.ide.eclipse.as.ui.IJBossLaunchTabProvider;
import org.jboss.ide.eclipse.as.ui.UIUtil;

/**
 * @author Rob Stryker
 * @author André Dietisheim
 *
 */
public class RSELaunchTabProvider implements IJBossLaunchTabProvider {

	public ILaunchConfigurationTab[] createTabs() {
		return new ILaunchConfigurationTab[]{
				new RSERemoteLaunchTab()
		};
	}

	
	public static class RSERemoteLaunchTab extends AbstractLaunchConfigurationTab {
		
		private Text startText,stopText;
		private Button autoStartArgs, autoStopArgs;
		private ILaunchConfiguration initialConfig;
		
		public void createControl(Composite parent) {
			createUI(parent);
		}
		
		public void createUI(Composite parent) {
			Composite main = new Composite(parent, SWT.NONE);
			main.setLayout(new FormLayout());
			main.setFont(parent.getFont());

			setControl(main);
			
			// begin start group
			Group startGroup = SWTFactory.createGroup(main, RSEUIMessages.RSE_START_COMMAND, 2, 1, GridData.FILL_HORIZONTAL);
			startGroup.setLayout(new GridLayout(1,true));
			autoStartArgs = new Button(startGroup, SWT.CHECK);
			autoStartArgs.setText(RSEUIMessages.RSE_AUTOMATICALLY_CALCULATE);
			startText = new Text(startGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
			GridData gd = new GridData(GridData.FILL_BOTH);
			gd.heightHint = 120;
			gd.widthHint = 100;
			startText.setLayoutData(gd);

			
			// begin stop group
			Group stopGroup = SWTFactory.createGroup(main, RSEUIMessages.RSE_STOP_COMMAND, 2, 1, GridData.FILL_HORIZONTAL);
			stopGroup.setLayout(new GridLayout(1, true));
			
			autoStopArgs = new Button(stopGroup, SWT.CHECK);
			autoStopArgs.setText(RSEUIMessages.RSE_AUTOMATICALLY_CALCULATE);
			stopText = new Text(stopGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
			gd = new GridData(GridData.FILL_BOTH);
			gd.heightHint = 75;
			gd.widthHint = 100;
			stopText.setLayoutData(gd);
			
			// Set the layout data of the two main widgets
			stopGroup.setLayoutData(UIUtil.createFormData(100, SWT.DEFAULT, 70, 0, 100, -5, 0, 5, 100, -5));
			startGroup.setLayoutData(UIUtil.createFormData(100, SWT.DEFAULT, 0, 5, stopGroup, -5, 0, 5, 100, -5));
		}
		
		protected void addListeners() {
			autoStartArgs.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					startText.setEditable(!autoStartArgs.getSelection());
					startText.setEnabled(!autoStartArgs.getSelection());
					if( autoStartArgs.getSelection()) {
						String command = null;
						try {
							command = RSELaunchConfigProperties.getDefaultStartupCommand(initialConfig, "");
							startText.setText(command);
						} catch(CoreException ce) {
							// This can only happen if loading properties from a launch config is f'd, 
							// in which case it's a big eclipse issue
							RSEUIPlugin.getLog().log(new Status(IStatus.ERROR, RSEUIPlugin.PLUGIN_ID, "Error loading details from launch configuration", ce));
						}
					}
					persistInWorkingCopy(((ILaunchConfigurationWorkingCopy)initialConfig), true);
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}});
			autoStopArgs.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					stopText.setEditable(!autoStopArgs.getSelection());
					stopText.setEnabled(!autoStopArgs.getSelection());
					if( autoStopArgs.getSelection()) {
						String command = null;
						try {
							command = RSELaunchConfigProperties.getDefaultShutdownCommand(initialConfig, "");
							stopText.setText(command);
						} catch(CoreException ce) {
							// This can only happen if loading properties from a launch config is f'd, 
							// in which case it's a big eclipse issue
							RSEUIPlugin.getLog().log(new Status(IStatus.ERROR, RSEUIPlugin.PLUGIN_ID, "Error loading details from launch configuration", ce));
						}
					}
					persistInWorkingCopy(((ILaunchConfigurationWorkingCopy)initialConfig), true);
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}});
			
			ModifyListener textListener = new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					persistInWorkingCopy(((ILaunchConfigurationWorkingCopy)initialConfig), true);
				}};
			startText.addModifyListener(textListener);
			stopText.addModifyListener(textListener);
		}

		public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		}
		
		public void initializeFrom(ILaunchConfiguration configuration) {
			this.initialConfig = configuration;
			
			try {
				String startCommand = RSELaunchConfigProperties.getStartupCommand(configuration);
				startText.setText(startCommand == null ? "" : startCommand);
				boolean detectStartCommand = RSELaunchConfigProperties.isDetectStartupCommand(configuration, true);
				autoStartArgs.setSelection(detectStartCommand);
				startText.setEditable(!detectStartCommand);
				startText.setEnabled(!detectStartCommand);
				
				String stopCommand = RSELaunchConfigProperties.getShutdownCommand(configuration);
				stopText.setText(stopCommand == null ? "" : stopCommand);
				boolean detectStopCommand = RSELaunchConfigProperties.isDetectShutdownCommand(configuration, true);
				autoStopArgs.setSelection(detectStopCommand);
				stopText.setEditable(!detectStopCommand);
				stopText.setEnabled(!detectStopCommand);
				
			} catch( CoreException ce) {
				// This can only happen if loading properties from a launch config is f'd, 
				// in which case it's a big eclipse issue
				RSEUIPlugin.getLog().log(new Status(IStatus.ERROR, RSEUIPlugin.PLUGIN_ID, "Error loading details from launch configuration", ce));
			}
			addListeners();
		}
		public void performApply(ILaunchConfigurationWorkingCopy configuration) {
			persistInWorkingCopy(configuration, false);
		}
		
		protected void persistInWorkingCopy(ILaunchConfigurationWorkingCopy configuration, boolean updateButtons) {
			RSELaunchConfigProperties.setStartupCommand(startText.getText(), configuration);
			RSELaunchConfigProperties.setShutdownCommand(stopText.getText(), configuration);
			RSELaunchConfigProperties.setDetectStartupCommand(autoStartArgs.getSelection(), configuration);
			RSELaunchConfigProperties.setDetectShutdownCommand(autoStopArgs.getSelection(), configuration);
			if( updateButtons)
				getLaunchConfigurationDialog().updateButtons();
		}
		public String getName() {
			return RSEUIMessages.RSE_REMOTE_LAUNCH;
		}
		
	}
}
