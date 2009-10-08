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
package org.jboss.ide.eclipse.as.wtp.ui.wizards.export;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizard;
import org.jboss.ide.eclipse.as.wtp.ui.Messages;

public class ProjectModuleExportWizard extends DataModelWizard
		implements IExportWizard {
	/**
	 * <p>
	 * Constant used to identify the key of the main page of the Wizard.
	 * </p>
	 */
	protected static final String MAIN_PG = "main"; //$NON-NLS-1$

	private IStructuredSelection currentSelection;

	public ProjectModuleExportWizard() {
		super();
		setWindowTitle(Messages.Export_WizardTitle);
	}
	
	public ProjectModuleExportWizard(IDataModel model) {
		super(model);
		setWindowTitle(Messages.Export_WizardTitle);
	}
    
    protected IDataModelProvider getDefaultProvider() {
        return new ProjectModuleExportDataModelProvider();
    }

    public void doAddPages() {
		addPage(new ProjectModuleExportPage(getDataModel(), MAIN_PG, getSelection()));
	}

	protected void doInit() {
		// do nothing
	}

	public final void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Messages.Export_WizardTitle);
		this.currentSelection = selection;
	}

	private void doDispose() {
		//dispose
	}

	public final void dispose() {
		super.dispose();
		doDispose();
		this.currentSelection = null;
	}

	protected final boolean prePerformFinish() {
		return super.prePerformFinish();
	}

	/**
	 * @return Returns the currentSelection.
	 */
	protected final IStructuredSelection getSelection() {
		return currentSelection;
	}

	/**
	 * @return
	 */
	protected final ProjectModuleExportPage getMainPage() {
		return (ProjectModuleExportPage) getPage(MAIN_PG);
	}

}
