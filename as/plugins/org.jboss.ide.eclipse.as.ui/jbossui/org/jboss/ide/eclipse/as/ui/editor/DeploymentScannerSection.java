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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.jboss.ide.eclipse.as.core.server.IDeploymentScannerModifier;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractDeploymentScannerAdditions;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.ui.UIUtil;

public class DeploymentScannerSection extends ServerEditorSection {
	public DeploymentScannerSection() {
	}
	private Button addScannersOnStartup, removeScannersOnShutdown;
	private Text intervalText, timeoutText;
	private ModifyListener intervalListener, timeoutListener;
	private SelectionListener startupListener, shutdownListener;
	protected ServerAttributeHelper helper; 
	
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		helper = new ServerAttributeHelper(server.getOriginal(), server);
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		createUI(parent);
		setDefaultValues();
		addListeners();
	}
	
	/**
	 * @since 2.5
	 */
	protected void setDefaultValues() {
		// set initial values
		boolean add = server.getAttribute(IJBossToolingConstants.PROPERTY_ADD_DEPLOYMENT_SCANNERS, true);
		addScannersOnStartup.setSelection(new Boolean(add).booleanValue());
		if( removeScannersOnShutdown != null ) {
			boolean remove = server.getAttribute(IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, true);
			removeScannersOnShutdown.setSelection(new Boolean(remove).booleanValue());
		}
		
		if( timeoutText != null ) {
			// using an unlikely value as default in case property isn't set. User is unlikely to have used -20 as timeout
			int s = server.getAttribute(IJBossToolingConstants.PROPERTY_SCANNER_TIMEOUT, -20);
			//int s2 = s == -20 ? DEFAULT_TIMEOUT : s; // default value of 60 seconds for timeout
			timeoutText.setText(s == -20 ? "(default)" : new Integer(s).toString());
		}
		
		if( intervalText != null ) {
			// using an unlikely value as default in case property isn't set. User is unlikely to have used -20 as interval
			int s = server.getAttribute(IJBossToolingConstants.PROPERTY_SCANNER_INTERVAL, -20);
			//int s2 = s == -20 ? DEFAULT_INTERVAL : s;  // default of 5000 ms for interval
			intervalText.setText(s == -20 ? "(default)" : new Integer(s).toString());
		}
	}
	
	protected void createUI(Composite parent) {
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.TITLE_BAR);
		section.setText("Deployment Scanners");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		Composite composite = toolkit.createComposite(section);
		composite.setLayout(new FormLayout());
		
		addScannersOnStartup = toolkit.createButton(composite, "Add missing deployment scanners after server startup.", SWT.CHECK);
		addScannersOnStartup.setLayoutData(UIUtil.createFormData2(0, 5, null, 0, 0, 5, null, 0));
		Control top = addScannersOnStartup;
		if( showRemoveScannerButton() ) {
			removeScannersOnShutdown = toolkit.createButton(composite, "Remove added deployment scanners before shutdown.", SWT.CHECK);
			removeScannersOnShutdown.setLayoutData(UIUtil.createFormData2(addScannersOnStartup, 5, null, 0, 0, 5, null, 0));
			top = removeScannersOnShutdown;
		}
		if( showIntervalText()) {
			Label intLabel = toolkit.createLabel(composite, "Override Scanner Interval (ms)");
			intLabel.setLayoutData(UIUtil.createFormData2(top, 5, null, 0, 0, 5, null, 0));
			intervalText = toolkit.createText(composite, "", SWT.CHECK);
			intervalText.setLayoutData(UIUtil.createFormData2(top, 5, null, 0, 50, 5, 80, 0));
			top = intervalText;
		}
		if( showTimeoutText()) {
			Label timeLabel = toolkit.createLabel(composite, "Override Scanner Timeout (sec)");
			timeLabel.setLayoutData(UIUtil.createFormData2(top, 5, null, 0, 0, 5, null, 0));
			timeoutText = toolkit.createText(composite, "", SWT.CHECK);
			timeoutText.setLayoutData(UIUtil.createFormData2(top, 5, null, 0, 50, 5, 80, 0));
			top = timeoutText;
		}
		
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
	}
	
	private boolean showRemoveScannerButton() {
		JBossExtendedProperties props = (JBossExtendedProperties)server.loadAdapter(JBossExtendedProperties.class, null);
		if( props != null ) {
			IDeploymentScannerModifier scanner = props.getDeploymentScannerModifier();
			if( scanner instanceof AbstractDeploymentScannerAdditions && ((AbstractDeploymentScannerAdditions)scanner).persistsScannerChanges())
				return true;
		}
		return false;
	}
	
	private boolean showTimeoutText() {
		JBossExtendedProperties props = (JBossExtendedProperties)server.loadAdapter(JBossExtendedProperties.class, null);
		if( props != null ) {
			IDeploymentScannerModifier scanner = props.getDeploymentScannerModifier();
			if( scanner instanceof AbstractDeploymentScannerAdditions && 
					((AbstractDeploymentScannerAdditions)scanner).canCustomizeTimeout())
				return true;
		}
		return false;
	}

	private boolean showIntervalText() {
		JBossExtendedProperties props = (JBossExtendedProperties)server.loadAdapter(JBossExtendedProperties.class, null);
		if( props != null ) {
			IDeploymentScannerModifier scanner = props.getDeploymentScannerModifier();
			if( scanner instanceof AbstractDeploymentScannerAdditions && ((AbstractDeploymentScannerAdditions)scanner).canCustomizeInterval())
				return true;
		}
		return false;
	}

	
	protected void addListeners() {
		startupListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				execute(new SetStartupPropertyCommand(server));
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		shutdownListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				execute(new SetShutdownPropertyCommand(server));
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		
		intervalListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetIntervalPropertyCommand(server));
			}
		};
		timeoutListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetTimeoutPropertyCommand(server));
			}
		};

		this.addScannersOnStartup.addSelectionListener(startupListener);
		if( showRemoveScannerButton())
			this.removeScannersOnShutdown.addSelectionListener(shutdownListener);
		
		if( showIntervalText())
			this.intervalText.addModifyListener(intervalListener);
		if( showTimeoutText())
			this.timeoutText.addModifyListener(timeoutListener);
	}

	public class SetStartupPropertyCommand extends ServerWorkingCopyPropertyButtonCommand {
		public SetStartupPropertyCommand(IServerWorkingCopy server) {
			super(server, "Modify deployment scanner startup preference",  
					addScannersOnStartup, addScannersOnStartup.getSelection(), 
					IJBossToolingConstants.PROPERTY_ADD_DEPLOYMENT_SCANNERS, startupListener);
		}
	}

	public class SetShutdownPropertyCommand extends ServerWorkingCopyPropertyButtonCommand {
		public SetShutdownPropertyCommand(IServerWorkingCopy server) {
			super(server, "Modify deployment scanner shutdown preference",  
					removeScannersOnShutdown, removeScannersOnShutdown.getSelection(), 
					IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, shutdownListener);
		}
	}


	private class SetIntervalPropertyCommand extends ServerWorkingCopyPropertyTextCommand {
		public SetIntervalPropertyCommand(IServerWorkingCopy server) {
			super(server, "Modify deployment scanner interval preference",  
					intervalText, intervalText.getText(), 
					IJBossToolingConstants.PROPERTY_SCANNER_INTERVAL, "(default)", intervalListener);
		}
	}

	private class SetTimeoutPropertyCommand extends ServerWorkingCopyPropertyTextCommand {
		public SetTimeoutPropertyCommand(IServerWorkingCopy server) {
			super(server, "Modify deployment scanner timeout preference",  
					timeoutText, timeoutText.getText(), 
					IJBossToolingConstants.PROPERTY_SCANNER_TIMEOUT, "(default)", timeoutListener);
		}
	}

}
