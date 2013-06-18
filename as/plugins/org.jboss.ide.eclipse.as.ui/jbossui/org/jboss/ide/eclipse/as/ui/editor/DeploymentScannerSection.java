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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.jboss.ide.eclipse.as.core.server.IDeploymentScannerModifier;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractDeploymentScannerAdditions;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.UIUtil;

public class DeploymentScannerSection extends ServerEditorSection {

	public DeploymentScannerSection() {
	}
	private Button addScannersOnStartup, removeScannersOnShutdown;
	private SelectionListener startupListener, shutdownListener;
	protected ServerAttributeHelper helper; 
	
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		helper = new ServerAttributeHelper(server.getOriginal(), server);
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		createUI(parent);
		DeployableServer ds = (DeployableServer)ServerConverter.getDeployableServer(server.getOriginal());
		boolean add = server.getAttribute(IJBossToolingConstants.PROPERTY_ADD_DEPLOYMENT_SCANNERS, true);
		addScannersOnStartup.setSelection(new Boolean(add).booleanValue());
		addScannersOnStartup.setText("Add missing deployment scanners after server startup.");
		if( removeScannersOnShutdown != null ) {
			boolean remove = server.getAttribute(IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, false);
			removeScannersOnShutdown.setSelection(new Boolean(remove).booleanValue());
			removeScannersOnShutdown.setText("Remove added deployment scanners before shutdown.");
		}
		addListeners();
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
		if( showRemoveScannerButton() ) {
			removeScannersOnShutdown = toolkit.createButton(composite, "Remove added deployment scanners before shutdown.", SWT.CHECK);
			removeScannersOnShutdown.setLayoutData(UIUtil.createFormData2(addScannersOnStartup, 5, null, 0, 0, 5, null, 0));
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

		this.addScannersOnStartup.addSelectionListener(startupListener);
		this.removeScannersOnShutdown.addSelectionListener(shutdownListener);
	}

	public class SetStartupPropertyCommand extends ServerWorkingCopyPropertyButtonCommand {
		public SetStartupPropertyCommand(IServerWorkingCopy server) {
			super(server, "Modify deployment scanner startup preference",  
					addScannersOnStartup, addScannersOnStartup.getSelection(), 
					IJBossToolingConstants.PROPERTY_ADD_DEPLOYMENT_SCANNERS, startupListener);
		}
		public void execute() {
			super.execute();
		}
		public void undo() {
			super.undo();
		}
	}

	public class SetShutdownPropertyCommand extends ServerWorkingCopyPropertyButtonCommand {
		public SetShutdownPropertyCommand(IServerWorkingCopy server) {
			super(server, "Modify deployment scanner shutdown preference",  
					removeScannersOnShutdown, removeScannersOnShutdown.getSelection(), 
					IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, shutdownListener);
		}
	}
}
