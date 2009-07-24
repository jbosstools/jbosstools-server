package org.jboss.ide.eclipse.as.wtp.override.ui.propertypage.impl;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
import org.eclipse.jst.j2ee.application.internal.operations.RemoveComponentFromEnterpriseApplicationDataModelProvider;
import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
import org.eclipse.jst.jee.project.facet.EarCreateDeploymentFilesDataModelProvider;
import org.eclipse.jst.jee.project.facet.ICreateDeploymentFilesDataModelProperties;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.jboss.ide.eclipse.as.wtp.override.ui.propertypage.AddModuleDependenciesPropertiesPage;
import org.jboss.ide.eclipse.as.wtp.override.ui.propertypage.ModuleAssemblyRootPage;

public class EarModuleDependenciesPropertyPage extends
		AddModuleDependenciesPropertiesPage {
	public EarModuleDependenciesPropertyPage(IProject project,
			ModuleAssemblyRootPage page) {
		super(project, page);
	}

	protected IDataModelOperation generateEARDDOperation() {
		IDataModel model = DataModelFactory.createDataModel(new EarCreateDeploymentFilesDataModelProvider());
		model.setProperty(ICreateDeploymentFilesDataModelProperties.GENERATE_DD, rootComponent);
		model.setProperty(ICreateDeploymentFilesDataModelProperties.TARGET_PROJECT, project);
		return model.getDefaultOperation();
	}
	
	public boolean postHandleChanges(IProgressMonitor monitor) {
		return true;
	}
	
	protected void handleRemoved(ArrayList<IVirtualComponent> removed) {
		super.handleRemoved(removed);
		J2EEComponentClasspathUpdater.getInstance().queueUpdateEAR(rootComponent.getProject());
	}
	
	protected IDataModelProvider getRemoveReferenceDataModelProvider(Object component) {
		return new RemoveComponentFromEnterpriseApplicationDataModelProvider();
	}

//	protected void postAddProjects(Set moduleProjects) throws CoreException {
//		EarFacetRuntimeHandler.updateModuleProjectRuntime(rootComponent.getProject(), moduleProjects, new NullProgressMonitor());
//	}

	protected IDataModelProvider getAddReferenceDataModelProvider(IVirtualComponent component) {
		return new AddComponentToEnterpriseApplicationDataModelProvider();
	}
}
