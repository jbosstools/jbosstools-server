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
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jst.server.core.internal.JavaServerPlugin;
import org.eclipse.jst.server.core.internal.RuntimeClasspathContainer;
import org.eclipse.jst.server.core.internal.RuntimeClasspathProviderWrapper;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectTemplate;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.web.ui.internal.wizards.NewProjectDataModelFacetWizard;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.ui.mbeans.project.IJBossSARFacetDataModelProperties;
import org.jboss.ide.eclipse.as.ui.mbeans.project.JBossSARFacetProjectCreationDataModelProvider;
import org.jboss.ide.eclipse.as.wtp.core.vcf.VCFClasspathCommand;

public class SARProjectWizard extends NewProjectDataModelFacetWizard implements
		INewWizard {

	public SARProjectWizard() {
		super();
		Set<IProjectFacetVersion> current = getFacetedProjectWorkingCopy().getProjectFacets();
//		getFacetedProjectWorkingCopy().addListener(new IFacetedProjectListener(){
//			public void handleEvent(IFacetedProjectEvent event) {
//				System.out.println("runtime changed" + event.getWorkingCopy().getPrimaryRuntime().getName());
//			}}, IFacetedProjectEvent.Type.PRIMARY_RUNTIME_CHANGED);
		IRuntime rt = getFacetedProjectWorkingCopy().getPrimaryRuntime();
		getFacetedProjectWorkingCopy().setProjectFacets(current);
		setWindowTitle("New Sar Project");
		//setDefaultPageImageDescriptor(ESBSharedImages.getImageDescriptor(ESBSharedImages.WIZARD_NEW_PROJECT));
	}

	public SARProjectWizard(IDataModel model) {
		super(model);
		setWindowTitle("New Sar Project");
		//setDefaultPageImageDescriptor(ESBSharedImages.getImageDescriptor(ESBSharedImages.WIZARD_NEW_PROJECT));
		
	}

	@Override
	protected IDataModel createDataModel() {
		return DataModelFactory.createDataModel(new JBossSARFacetProjectCreationDataModelProvider());
	}

    private IFacetedProjectWorkingCopy fpjwc;
    
    @Override
    public void setFacetedProjectWorkingCopy( final IFacetedProjectWorkingCopy fpjwc ) {
		super.setFacetedProjectWorkingCopy(fpjwc);
        this.fpjwc = fpjwc;
    }

	@Override
	protected IWizardPage createFirstPage() {
		return new SARProjectFirstPage(model, "first.page"); //$NON-NLS-1$
	}

	@Override
	protected ImageDescriptor getDefaultPageImageDescriptor() {
		return null; //ESBSharedImages.getImageDescriptor(ESBSharedImages.WIZARD_NEW_PROJECT);
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
		if(!project.exists()) return;
		
		try {
			String esbcontent = project.getPersistentProperty(IJBossSARFacetDataModelProperties.QNAME_SAR_CONTENT_FOLDER);
			IPath esbPath = new Path(esbcontent).append(IJBossSARFacetDataModelProperties.META_INF);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			
			// Add the server runtime as well
			IFacetedProject fp = ProjectFacetsManager.create(project);
			IRuntime runtime = fp.getPrimaryRuntime();
			
			if(runtime == null) return;
			
			String name = runtime.getName();
			org.eclipse.wst.server.core.IRuntime serverRuntime = ServerCore.findRuntime(name);
			RuntimeClasspathProviderWrapper rcpw = JavaServerPlugin.findRuntimeClasspathProvider(serverRuntime.getRuntimeType());
			IPath serverContainerPath = new Path(RuntimeClasspathContainer.SERVER_CONTAINER)
				.append(rcpw.getId()).append(serverRuntime.getId());
			VCFClasspathCommand.addClassPath(project, serverContainerPath);

		} catch (CoreException e) {
			JBossServerCorePlugin.getDefault().getLog().log(e.getStatus());
		}
		
	}
	
	

}