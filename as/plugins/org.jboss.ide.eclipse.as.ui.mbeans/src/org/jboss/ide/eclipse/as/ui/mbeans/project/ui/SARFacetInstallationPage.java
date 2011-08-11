/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.mbeans.project.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectListener;
import org.eclipse.wst.common.project.facet.ui.AbstractFacetWizardPage;
import org.eclipse.wst.common.project.facet.ui.IFacetWizardPage;
import org.jboss.ide.eclipse.as.ui.mbeans.Messages;
import org.jboss.ide.eclipse.as.ui.mbeans.project.IJBossSARFacetDataModelProperties;

public class SARFacetInstallationPage extends AbstractFacetWizardPage implements IFacetWizardPage, IJBossSARFacetDataModelProperties {

	private Text contentFolder;
	private Label contentRootLabel;
	private IDataModel model;
	private boolean hasValidContentFolder = true;
	private IFacetedProjectListener fpListerner;
	private IFacetedProjectWorkingCopy fpwc;
	
	public SARFacetInstallationPage() {
		super( "esb.facet.install.page"); //$NON-NLS-1$
		setTitle(Messages.NewSarProject_FacetInstallationPage);
		setDescription(Messages.NewSarProject_FacetInstallationPageDesc);
		
	}

	public void setConfig(Object config) {
		this.model = (IDataModel)config;
	}

	public void createControl(Composite parent) {
		setControl(createTopLevelComposite(parent));
	}
	
	private void changePageStatus(){
		if(!validFolderName(contentFolder.getText())){
			setErrorMessage(Messages.NewSarProject_FacetInstallationPage_ContentRootError);
			hasValidContentFolder = false;
			setPageComplete(isPageComplete());
		}
	}
	
	private boolean validFolderName(String folderName) {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		return ws.validateName(folderName, IResource.FOLDER).isOK();
	}

	
	protected Composite createTopLevelComposite(Composite parent) {
		//setInfopopID(IWstWebUIContextIds.NEW_STATIC_WEB_PROJECT_PAGE3);
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		createProjectGroup(composite);
//		//set page status
		changePageStatus();
		
		return composite;
	}

	private void createProjectGroup(Composite parent){
		Composite prjGroup = new Composite(parent, SWT.NONE);
		prjGroup.setLayout(new GridLayout(1, false));
		prjGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		this.contentRootLabel = new Label(prjGroup, SWT.NONE);
		this.contentRootLabel.setText(Messages.NewSarProject_ContentRootLabel);
		this.contentRootLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.contentFolder = new Text(prjGroup, SWT.BORDER);
		this.contentFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.contentFolder.setData("label", this.contentRootLabel); //$NON-NLS-1$ // wtf??
		this.contentFolder.setText(model.getStringProperty(SAR_CONTENT_FOLDER));
		contentFolder.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				 String content = contentFolder.getText();
				 if(content != null && !content.equals("")){ //$NON-NLS-1$
					 model.setProperty(SAR_CONTENT_FOLDER, content);
				 }
				 changePageStatus();
			}
		});
		
	}
}