package org.jboss.ide.eclipse.as.wtp.ui.propertypage.impl;

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
import org.jboss.ide.eclipse.as.wtp.ui.propertypage.AddModuleDependenciesPropertiesPage;
import org.jboss.ide.eclipse.as.wtp.ui.propertypage.DependencyPageExtensionManager;
import org.jboss.ide.eclipse.as.wtp.ui.propertypage.ModuleAssemblyRootPage;
import org.jboss.ide.eclipse.as.wtp.ui.propertypage.DependencyPageExtensionManager.ReferenceExtension;

public class JEEModuleDependenciesPropertyPage extends
		AddModuleDependenciesPropertiesPage {
	public JEEModuleDependenciesPropertyPage(IProject project,
			ModuleAssemblyRootPage page) {
		super(project, page);
	}

	protected ReferenceExtension[] getReferenceExtensions() {
		ReferenceExtension[] parents = super.getReferenceExtensions();
		ArrayList<ReferenceExtension> l = new ArrayList<ReferenceExtension>();
		for( int i = 0; i < parents.length; i++ ) 
			if( shouldAddReferenceType(parents[i]))
				l.add(parents[i]);
		return (ReferenceExtension[]) l.toArray(new ReferenceExtension[l.size()]);
	}

	protected boolean shouldAddReferenceType(ReferenceExtension extension) {
		// approved types
		String NEW_PROJ = "org.jboss.ide.eclipse.as.wtp.ui.newProjectReference";
		String JAR = "org.jboss.ide.eclipse.as.wtp.ui.jarReference";
		String EXT_JAR = "org.jboss.ide.eclipse.as.wtp.ui.externalJarReference";
		String VAR = "org.jboss.ide.eclipse.as.wtp.ui.variableReference";
		String id = extension.getId();
		if( id.equals(NEW_PROJ) || id.equals(JAR) || id.equals(EXT_JAR) || id.equals(VAR) )
			return true;
		return false;
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
		if( isEar())
			J2EEComponentClasspathUpdater.getInstance().queueUpdateEAR(rootComponent.getProject());
	}
	
	protected IDataModelProvider getRemoveReferenceDataModelProvider(IVirtualComponent component) {
		if( isEar() )
			return new RemoveComponentFromEnterpriseApplicationDataModelProvider();
		return super.getRemoveReferenceDataModelProvider(component);
	}

//	protected void postAddProjects(Set moduleProjects) throws CoreException {
//		EarFacetRuntimeHandler.updateModuleProjectRuntime(rootComponent.getProject(), moduleProjects, new NullProgressMonitor());
//	}

	protected IDataModelProvider getAddReferenceDataModelProvider(IVirtualComponent component) {
		if( isEar() )
			return new OverrideAddComponentToEnterpriseApplicationDataModelProvider();
		return super.getAddReferenceDataModelProvider(component);
	}
	
	protected boolean isEar() {
		return false;
	}
}
