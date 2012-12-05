/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.mbeans.project.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectTemplate;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.web.ui.internal.wizards.NewProjectDataModelFacetWizard;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.ui.mbeans.Messages;
import org.jboss.ide.eclipse.as.ui.mbeans.SharedImages;
import org.jboss.ide.eclipse.as.ui.mbeans.project.IJBossSARFacetDataModelProperties;
import org.jboss.ide.eclipse.as.ui.mbeans.project.JBossSARFacetProjectCreationDataModelProvider;
import org.jboss.ide.eclipse.as.ui.mbeans.project.SarProjectRuntimeChangedDelegate;
import org.jboss.ide.eclipse.as.wtp.core.vcf.VCFClasspathCommand;

public class SARProjectWizard extends NewProjectDataModelFacetWizard implements
		INewWizard {

	public SARProjectWizard() {
		super();
		Set<IProjectFacetVersion> current = getFacetedProjectWorkingCopy().getProjectFacets();
		getFacetedProjectWorkingCopy().setProjectFacets(current);
		setWindowTitle(Messages.NewSarProject_FirstPageTitle);
		setDefaultPageImageDescriptor(SharedImages.getImageDescriptor(SharedImages.IMG_SAR_64));
	}

	public SARProjectWizard(IDataModel model) {
		super(model);
		setWindowTitle(Messages.NewSarProject_FirstPageTitle);
		setDefaultPageImageDescriptor(SharedImages.getImageDescriptor(SharedImages.IMG_SAR_64));
		
	}

	@Override
	protected IDataModel createDataModel() {
		return DataModelFactory.createDataModel(new JBossSARFacetProjectCreationDataModelProvider());
	}

    @Override
    public void setFacetedProjectWorkingCopy( final IFacetedProjectWorkingCopy fpjwc ) {
		super.setFacetedProjectWorkingCopy(fpjwc);
    }

	@Override
	protected IWizardPage createFirstPage() {
		return new SARProjectFirstPage(model, "first.page"); //$NON-NLS-1$
	}

	@Override
	protected ImageDescriptor getDefaultPageImageDescriptor() {
		return SharedImages.getImageDescriptor(SharedImages.IMG_SAR_64);
	}

	@Override
	protected IFacetedProjectTemplate getTemplate() {
		return ProjectFacetsManager.getTemplate(IJBossSARFacetDataModelProperties.SAR_PROJECT_FACET_TEMPLATE);
	}

	@Override
	protected void postPerformFinish() throws InvocationTargetException {
		super.postPerformFinish();
		String prjName = this.getProjectName();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(prjName);
		IRuntime runtime = null;
		if(project.exists()) {
			try {
				IFacetedProject fp = ProjectFacetsManager.create(project);
				runtime = fp.getPrimaryRuntime();
			} catch (CoreException e) {
				JBossServerCorePlugin.getDefault().getLog().log(e.getStatus());
			}
		}
		if(runtime != null) {
			IPath serverContainerPath = SarProjectRuntimeChangedDelegate.getContainerPath(runtime);
			VCFClasspathCommand.addContainerClasspathEntry(project, serverContainerPath);
		}
	}
}