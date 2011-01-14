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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.as.rse.core.RSELaunchDelegate;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.launch.JBossLaunchConfigurationTabGroup.IJBossLaunchTabProvider;

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
			addListeners();
		}
		
		public void createUI(Composite parent) {
			Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL| GridData.FILL_VERTICAL);
			setControl(comp);
			comp.setLayout(new FormLayout());
			Group startGroup = new Group(comp, SWT.NONE);
			startGroup.setText(RSEUIMessages.RSE_START_COMMAND);
			FormData data = UIUtil.createFormData2(0, 5, 0, 150, 0, 5, 100, -5);
			startGroup.setLayoutData(data);
			startGroup.setLayout(new FormLayout());
			
			autoStartArgs = new Button(startGroup, SWT.CHECK);
			autoStartArgs.setText(RSEUIMessages.RSE_AUTOMATICALLY_CALCULATE);
			data = UIUtil.createFormData2(null, 0, 100, -5, 0, 5, 100, -5);
			autoStartArgs.setLayoutData(data);

			startText = new Text(startGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			data = UIUtil.createFormData2(0, 5, autoStartArgs, -5, 0, 5, 100, -5);
			startText.setLayoutData(data);
			
			// start stop group
			Group stopGroup = new Group(comp, SWT.NONE);
			stopGroup.setText(RSEUIMessages.RSE_STOP_COMMAND);
			data = UIUtil.createFormData2(startGroup, 5, startGroup, 300, 0, 5, 100, -5);
			stopGroup.setLayoutData(data);
			stopGroup.setLayout(new FormLayout());
			
			autoStopArgs = new Button(stopGroup, SWT.CHECK);
			autoStopArgs.setText(RSEUIMessages.RSE_AUTOMATICALLY_CALCULATE);
			data = UIUtil.createFormData2(null, 0, 100, -5, 0, 5, 100, -5);
			autoStopArgs.setLayoutData(data);

			
			stopText = new Text(stopGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			data = UIUtil.createFormData2(0, 5, autoStopArgs, -5, 0, 5, 100, -5);
			stopText.setLayoutData(data);
		}
		
		protected void addListeners() {
			autoStartArgs.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					startText.setEditable(!autoStartArgs.getSelection());
					startText.setEnabled(!autoStartArgs.getSelection());
					if( autoStartArgs.getSelection()) {
						String command = null;
						try {
							command = RSELaunchDelegate.getDefaultLaunchCommand(initialConfig);
							startText.setText(command);
						} catch(CoreException ce) {
							// TODO
						}
					}
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}});
			autoStopArgs.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					stopText.setEditable(!autoStopArgs.getSelection());
					stopText.setEnabled(!autoStopArgs.getSelection());
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}});
		}

		public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		}
		
		public void initializeFrom(ILaunchConfiguration configuration) {
			this.initialConfig = configuration;
			
			try {
				String startCommand = configuration.getAttribute(RSELaunchDelegate.RSE_STARTUP_COMMAND, "");
				startText.setText(startCommand);
				boolean detectStartCommand = configuration.getAttribute(RSELaunchDelegate.DETECT_STARTUP_COMMAND, true);
				autoStartArgs.setSelection(detectStartCommand);
				startText.setEditable(!detectStartCommand);
				startText.setEnabled(!detectStartCommand);
				
				String stopCommand = configuration.getAttribute(RSELaunchDelegate.RSE_SHUTDOWN_COMMAND, "");
				stopText.setText(stopCommand);
				boolean detectStopCommand = configuration.getAttribute(RSELaunchDelegate.DETECT_SHUTDOWN_COMMAND, true);
				autoStopArgs.setSelection(detectStopCommand);
				stopText.setEditable(!detectStopCommand);
				stopText.setEnabled(!detectStopCommand);
				
			} catch( CoreException ce) {
				// TODO
			}
		}
		public void performApply(ILaunchConfigurationWorkingCopy configuration) {
			configuration.setAttribute(RSELaunchDelegate.RSE_STARTUP_COMMAND, startText.getText());
			configuration.setAttribute(RSELaunchDelegate.RSE_SHUTDOWN_COMMAND, stopText.getText());
			configuration.setAttribute(RSELaunchDelegate.DETECT_STARTUP_COMMAND, autoStartArgs.getSelection());
			configuration.setAttribute(RSELaunchDelegate.DETECT_SHUTDOWN_COMMAND, autoStopArgs.getSelection());
		}
		public String getName() {
			return RSEUIMessages.RSE_REMOTE_LAUNCH;
		}
		
	}
}
