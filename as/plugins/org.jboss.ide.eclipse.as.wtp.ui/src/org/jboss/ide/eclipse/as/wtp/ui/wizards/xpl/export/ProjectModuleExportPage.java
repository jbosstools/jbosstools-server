/******************************************************************************* 
 * Copyright (c) 2008 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.ui.wizards.xpl.export;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.jboss.ide.eclipse.as.wtp.core.util.VCFUtil;
import org.jboss.ide.eclipse.as.wtp.ui.Messages;

public class ProjectModuleExportPage extends DataModelWizardPage {
	private static boolean isWindows = SWT.getPlatform().toLowerCase().startsWith("win"); //$NON-NLS-1$
	private static final int SIZING_TEXT_FIELD_WIDTH = 305;
	protected static final String STORE_PREFIX = "JBT_EXPORT_"; //$NON-NLS-1$
	protected static final String STORE_OVERWRITE = "OVERWRITE"; //$NON-NLS-1$

	protected IStructuredSelection selection;
	private Combo resourceNameCombo;
	private Combo destinationNameCombo;
	private Button destinationBrowseButton;
	protected Button overwriteExistingFilesCheckbox;

	public ProjectModuleExportPage(IDataModel model, String pageName, IStructuredSelection selection) {
		super(model, pageName);
		setTitle(Messages.Export_PageTitle);
		setDescription(Messages.Export_PageDescription);
		//setImageDescriptor(ESBSharedImages.getImageDescriptor(ESBSharedImages.WIZARD_NEW_PROJECT));
		this.selection = selection;
		if( selection != null && selection.getFirstElement() != null ) {
			Object o = selection.getFirstElement();
			if( o instanceof IProject ) {
				model.setProperty(IJ2EEComponentExportDataModelProperties.PROJECT_NAME, ((IProject)o).getName());
			}
		}
	}

	@Override
	protected String[] getValidationPropertyNames() {
		return new String[]{IJ2EEComponentExportDataModelProperties.PROJECT_NAME, 
				IJ2EEComponentExportDataModelProperties.ARCHIVE_DESTINATION, 
				IJ2EEComponentExportDataModelProperties.OVERWRITE_EXISTING};
	}

	@Override
	protected Composite createTopLevelComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		createSourceAndDestinationGroup(composite);
		createOptionsGroup(composite);

		setupInfopop(composite);
		restoreWidgetValues();
	    Dialog.applyDialogFont(parent);
		return composite;
	}
	
	private void createSourceAndDestinationGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(3, false);
		composite.setLayout(layout);
		createExportComponentGroup(composite);
		createDestinationGroup(composite);
	}

    protected void createExportComponentGroup(Composite parent) {
        //Project label
        Label projectLabel = new Label(parent, SWT.NONE);
        projectLabel.setText(Messages.Export_LabelProject);
        //Project combo
        resourceNameCombo = new Combo(parent, SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        resourceNameCombo.setLayoutData(data);
        synchHelper.synchCombo(resourceNameCombo, IJ2EEComponentExportDataModelProperties.PROJECT_NAME, null);
        new Label(parent, SWT.NONE);//Pad label
    }

    protected void createDestinationGroup(org.eclipse.swt.widgets.Composite parent) {

        //Destination label
        Label destinationLabel = new Label(parent, SWT.NONE);
        destinationLabel.setText(Messages.Export_LabelDestination);
        // destination name combo field
        destinationNameCombo = new Combo(parent, SWT.SINGLE | SWT.BORDER);
        destinationNameCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        synchHelper.synchCombo(destinationNameCombo, IJ2EEComponentExportDataModelProperties.ARCHIVE_DESTINATION, null);

        // destination browse button
        destinationBrowseButton = new Button(parent, SWT.PUSH);
        destinationBrowseButton.setText(Messages.Export_LabelBrowse); 
        destinationBrowseButton.setLayoutData((new GridData(GridData.FILL_HORIZONTAL)));
        destinationBrowseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleDestinationBrowseButtonPressed();
            }
        });
        destinationBrowseButton.setEnabled(true);
    }

	protected void handleDestinationBrowseButtonPressed() {

		FileDialog dialog = new FileDialog(destinationNameCombo.getShell(), SWT.SAVE);
		String fileName = getDataModel().getStringProperty(IJ2EEComponentExportDataModelProperties.PROJECT_NAME);
		String[] filters = getFilterExpression();
		if (filters.length != 0 && filters[0] != null && filters[0].indexOf('.') != -1) {
			fileName += filters[0].substring(filters[0].indexOf('.'));
		}
		dialog.setFileName(fileName);
		if (isWindows) {
			dialog.setFilterExtensions(filters);
		}
		String filename = dialog.open();
		if (filename != null)
			destinationNameCombo.setText(filename);
	}

    /**
     * Return the most likely suffix for this archive
     * @return
     */
	protected String[] getFilterExpression() {
		return new String[0];
	}
	
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] sourceNames = settings.getArray(STORE_PREFIX + getModuleFacet());
			if (sourceNames == null)
				return; // ie.- no settings stored
			for (int i = 0; i < sourceNames.length; i++) {
				if (sourceNames[i] == null)
					sourceNames[i] = ""; //$NON-NLS-1$
			}
			destinationNameCombo.setItems(sourceNames);
			boolean overwrite = settings.getBoolean(STORE_PREFIX + STORE_OVERWRITE);
			model.setBooleanProperty(IJ2EEComponentExportDataModelProperties.OVERWRITE_EXISTING, overwrite);
		}
	}

	protected void createOptionsGroup(Composite parent) {

        // options group
        Composite optionsGroup = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, false);
        optionsGroup.setLayout(layout);

        // overwrite... checkbox
        createOverwriteExistingFilesCheckbox(optionsGroup);
    }
    
	protected void createOverwriteExistingFilesCheckbox(Composite optionsGroup) {
		//Overwrite checkbox
		overwriteExistingFilesCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		overwriteExistingFilesCheckbox.setText(Messages.Export_OverwriteCheckbox);
		overwriteExistingFilesCheckbox.setEnabled(true);
		synchHelper.synchCheckbox(overwriteExistingFilesCheckbox, IJ2EEComponentExportDataModelProperties.OVERWRITE_EXISTING, null);
	}

	protected String getModuleFacet() {
		Object o = selection.getFirstElement();
		if( o instanceof IProject ) {
			IProject proj = (IProject)o;
			IProjectFacet facet = VCFUtil.getModuleFacet(proj);
			return facet == null ? null : facet.getId();
		}
		return null;
	}
	
}
