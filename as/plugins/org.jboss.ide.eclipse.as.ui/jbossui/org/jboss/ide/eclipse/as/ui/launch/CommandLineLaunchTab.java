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
package org.jboss.ide.eclipse.as.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.launch.CommandLineLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;

public class CommandLineLaunchTab extends AbstractLaunchConfigurationTab {
	
	protected Text startText,stopText;
	protected Button autoStartArgs, autoStopArgs;
	protected ILaunchConfiguration initialConfig;
	protected CommandLineLaunchConfigProperties propertyUtil;
	
	public void createControl(Composite parent) {
		createUI(parent);
	}
	
	public void createUI(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());
		main.setFont(parent.getFont());

		setControl(main);
		createGroups(main);
	}
	
	protected void createGroups(Composite main) {

		// begin start group
		Group startGroup = SWTFactory.createGroup(main, Messages.CommandLineLaunchTab_START_COMMAND, 
				2, 1, GridData.FILL_HORIZONTAL);
		startGroup.setLayout(new GridLayout(1,true));
		if( canAutoDetectCommand()) {
			autoStartArgs = new Button(startGroup, SWT.CHECK);
			autoStartArgs.setText(Messages.CommandLineLaunchTab_AUTOMATICALLY_CALCULATE);
		}
		startText = new Text(startGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = getStartCommandHeightHint();
		gd.widthHint = 100;
		startText.setLayoutData(gd);

		
		// begin stop group
		Group stopGroup = SWTFactory.createGroup(main, Messages.CommandLineLaunchTab_STOP_COMMAND, 2, 1, GridData.FILL_HORIZONTAL);
		stopGroup.setLayout(new GridLayout(1, true));
		
		if( canAutoDetectCommand()) {
			autoStopArgs = new Button(stopGroup, SWT.CHECK);
			autoStopArgs.setText(Messages.CommandLineLaunchTab_AUTOMATICALLY_CALCULATE);
		}
		stopText = new Text(stopGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = getStopCommandHeightHint();
		gd.widthHint = 100;
		stopText.setLayoutData(gd);
		
		// Set the layout data of the two main widgets
		stopGroup.setLayoutData(UIUtil.createFormData(getStopCommandHeightHint(), SWT.DEFAULT, 50, 0, 100, -5, 0, 5, 100, -5));
		startGroup.setLayoutData(UIUtil.createFormData(getStartCommandHeightHint(), SWT.DEFAULT, 0, 5, stopGroup, -5, 0, 5, 100, -5));
	}
	
	protected int getStartCommandHeightHint() {
		return 50;
	}
	protected int getStopCommandHeightHint() {
		return 50;
	}
	
	protected SelectionListener getAutoStartListener() {
		return new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				startText.setEditable(!autoStartArgs.getSelection());
				startText.setEnabled(!autoStartArgs.getSelection());
				if( autoStartArgs.getSelection()) {
					String command = null;
					try {
						command = getDefaultStartupCommand();
						startText.setText(command);
					} catch(CoreException ce) {
						// This can only happen if loading properties from a launch config is f'd, 
						// in which case it's a big eclipse issue
						JBossServerUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, "Error loading details from launch configuration", ce));
					}
				}
				persistInWorkingCopy(((ILaunchConfigurationWorkingCopy)initialConfig), true);
			}
		};
	}
	
	protected SelectionListener getAutoStopListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stopText.setEditable(!autoStopArgs.getSelection());
				stopText.setEnabled(!autoStopArgs.getSelection());
				if( autoStopArgs.getSelection()) {
					String command = null;
					try {
						command = getDefaultShutdownCommand();
						stopText.setText(command);
					} catch(CoreException ce) {
						// This can only happen if loading properties from a launch config is f'd, 
						// in which case it's a big eclipse issue
						JBossServerUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, "Error loading details from launch configuration", ce));
					}
				}
				persistInWorkingCopy(((ILaunchConfigurationWorkingCopy)initialConfig), true);
			}
		};
	}
	
	protected void addListeners() {
		if( canAutoDetectCommand()) {
			SelectionListener autoStartListener = getAutoStartListener();
			SelectionListener autoStopListener = getAutoStopListener();
			autoStartArgs.addSelectionListener(autoStartListener);
			autoStopArgs.addSelectionListener(autoStopListener);
		}
		
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
			boolean detectStartCommand, detectStopCommand;
			detectStartCommand = detectStopCommand = false;
			CommandLineLaunchConfigProperties propUtil = getPropertyUtility();
			String startCommand = propUtil.getStartupCommand(configuration);
			String stopCommand = propUtil.getShutdownCommand(configuration);
			startText.setText(startCommand == null ? "" : startCommand);
			stopText.setText(stopCommand == null ? "" : stopCommand);
			if( canAutoDetectCommand() ) {
				
				detectStartCommand = propUtil.isDetectStartupCommand(configuration, true);
				autoStartArgs.setSelection(detectStartCommand);
				detectStopCommand = propUtil.isDetectShutdownCommand(configuration, true);
				autoStopArgs.setSelection(detectStopCommand);
			}
			
			startText.setEditable(!canAutoDetectCommand() || !detectStartCommand);
			startText.setEnabled(!canAutoDetectCommand() || !detectStartCommand);
			
			stopText.setEditable(!canAutoDetectCommand() || !detectStopCommand);
			stopText.setEnabled(!canAutoDetectCommand() || !detectStopCommand);
			
		} catch( CoreException ce) {
			// This can only happen if loading properties from a launch config is f'd, 
			// in which case it's a big eclipse issue
			JBossServerUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, "Error loading details from launch configuration", ce));
		}
		addListeners();
		try {
			IServer s = ServerUtil.getServer(configuration);
			if (LaunchCommandPreferences.isIgnoreLaunchCommand(s)) {
				setErrorMessage("Your server is currently configured to ignore startup and shutdown actions.");
				startText.setEnabled(false);
				stopText.setEnabled(false);
				if( canAutoDetectCommand() ) {
					autoStartArgs.setEnabled(false);
					autoStopArgs.setEnabled(false);
				}
			}
		} catch(CoreException ce) {
			JBossServerUIPlugin.getDefault().getLog().log(ce.getStatus());
		}
		
	}
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		persistInWorkingCopy(configuration, false);
	}
	
	protected void persistInWorkingCopy(ILaunchConfigurationWorkingCopy configuration, boolean updateButtons) {
		CommandLineLaunchConfigProperties propUtil = getPropertyUtility();
		propUtil.setStartupCommand(startText.getText(), configuration);
		propUtil.setShutdownCommand(stopText.getText(), configuration);
		
		if( canAutoDetectCommand() ) {
			propUtil.setDetectStartupCommand(autoStartArgs.getSelection(), configuration);
			propUtil.setDetectShutdownCommand(autoStopArgs.getSelection(), configuration);
		}
		if( updateButtons)
			getLaunchConfigurationDialog().updateButtons();
	}
	
	public String getName() {
		return Messages.CommandLineLaunchTab_TAB_NAME;
	}
	
	
	protected boolean canAutoDetectCommand() {
		return false;
	}
	

	protected String getDefaultStartupCommand() throws CoreException {
		return getPropertyUtility().getDefaultStartupCommand(initialConfig, "");
	}

	protected String getDefaultShutdownCommand() throws CoreException {
		return getPropertyUtility().getDefaultShutdownCommand(initialConfig, "");
	}
	
	protected CommandLineLaunchConfigProperties getPropertyUtility() {
		if( propertyUtil == null )
			propertyUtil = new CommandLineLaunchConfigProperties();
		return propertyUtil;
	}
}