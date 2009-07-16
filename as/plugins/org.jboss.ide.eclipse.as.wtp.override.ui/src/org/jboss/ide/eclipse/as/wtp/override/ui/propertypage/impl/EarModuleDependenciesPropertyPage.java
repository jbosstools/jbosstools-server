package org.jboss.ide.eclipse.as.wtp.override.ui.propertypage.impl;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
import org.eclipse.jst.j2ee.application.internal.operations.RemoveComponentFromEnterpriseApplicationDataModelProvider;
import org.eclipse.jst.j2ee.internal.J2EEConstants;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
import org.eclipse.jst.j2ee.internal.dialogs.ChangeLibDirDialog;
import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
import org.eclipse.jst.j2ee.model.IEARModelProvider;
import org.eclipse.jst.j2ee.model.ModelProviderManager;
import org.eclipse.jst.j2ee.project.EarUtilities;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.jst.j2ee.project.facet.EarFacetRuntimeHandler;
import org.eclipse.jst.javaee.application.Application;
import org.eclipse.jst.jee.project.facet.EarCreateDeploymentFilesDataModelProvider;
import org.eclipse.jst.jee.project.facet.ICreateDeploymentFilesDataModelProperties;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.ide.eclipse.as.wtp.override.ui.propertypage.AddModuleDependenciesPropertiesPage;
import org.jboss.ide.eclipse.as.wtp.override.ui.propertypage.ModuleAssemblyRootPage;

public class EarModuleDependenciesPropertyPage extends
		AddModuleDependenciesPropertiesPage {
	protected Button changeLibPathButton;
	protected String libDir = null;
	protected String oldLibDir;
	protected boolean isVersion5;
	public EarModuleDependenciesPropertyPage(IProject project,
			ModuleAssemblyRootPage page) {
		super(project, page);
		initMemberVariables();
	}

	protected void initMemberVariables() {
		boolean hasEE5Facet = false;
		try {
			IFacetedProject facetedProject = ProjectFacetsManager.create(project);
			if(facetedProject != null){
				IProjectFacetVersion facetVersion = facetedProject.getProjectFacetVersion(EarUtilities.ENTERPRISE_APPLICATION_FACET);
				if(facetVersion.equals(EarUtilities.ENTERPRISE_APPLICATION_50)){
					hasEE5Facet = true;
				}
			}
		} catch (CoreException e) {
			J2EEUIPlugin.logError(e);
		}
		
		if(hasEE5Facet){
			String earDDVersion = JavaEEProjectUtilities.getJ2EEDDProjectVersion(project);
			if (earDDVersion.equals(J2EEVersionConstants.VERSION_5_0_TEXT)) {
				isVersion5 = true;
				Application app = (Application)ModelProviderManager.getModelProvider(project).getModelObject();
				if (app != null)
					oldLibDir = app.getLibraryDirectory();
				if (oldLibDir == null) oldLibDir = J2EEConstants.EAR_DEFAULT_LIB_DIR;
				libDir = oldLibDir;
			}
		}
	}
	
	protected void createPushButtons() {
		super.createPushButtons();
		if (isVersion5) 
			changeLibPathButton = createPushButton(J2EEUIMessages.getResourceString(J2EEUIMessages.CHANGE_LIB_DIR));

	}

	public void handleEvent(Event event) {
		if( event.widget == changeLibPathButton)
			handleChangeLibDirButton(true);
		else
			super.handleEvent(event);
	}


	private void handleChangeLibDirButton(boolean warnBlank) {					
		IVirtualFile vFile = rootComponent.getRootFolder().getFile(new Path(J2EEConstants.APPLICATION_DD_URI));
		if (!vFile.exists()) {
			if (!MessageDialog.openQuestion(null, 
					J2EEUIMessages.getResourceString(J2EEUIMessages.NO_DD_MSG_TITLE), 
					J2EEUIMessages.getResourceString(J2EEUIMessages.GEN_DD_QUESTION))) return;
			createDD(new NullProgressMonitor());
		}
		Application app = (Application)ModelProviderManager.getModelProvider(project).getModelObject();
		if (libDir == null) {
			libDir = app.getLibraryDirectory();
			if (libDir == null) libDir = J2EEConstants.EAR_DEFAULT_LIB_DIR;
		}
		
		ChangeLibDirDialog dlg = new ChangeLibDirDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell(), libDir, warnBlank);
		if (dlg.open() == Dialog.CANCEL) return;
		libDir = dlg.getValue().trim();
		if (libDir.length() > 0) {
			if (!libDir.startsWith(J2EEConstants.EAR_ROOT_DIR)) libDir = IPath.SEPARATOR + libDir;
		}
	}
	
	protected void createDD(IProgressMonitor monitor) {
		if( rootComponent != null ){
			IDataModelOperation op = generateEARDDOperation();
			try {
				op.execute(monitor, null);
			} catch (ExecutionException e) {
				J2EEUIPlugin.logError(e);
			}
		}
	}

	protected IDataModelOperation generateEARDDOperation() {
		IDataModel model = DataModelFactory.createDataModel(new EarCreateDeploymentFilesDataModelProvider());
		model.setProperty(ICreateDeploymentFilesDataModelProperties.GENERATE_DD, rootComponent);
		model.setProperty(ICreateDeploymentFilesDataModelProperties.TARGET_PROJECT, project);
		return model.getDefaultOperation();
	}
	
	private void updateLibDir(IProgressMonitor monitor) {
		if (libDir.equals(oldLibDir)) return;
		final IEARModelProvider earModel = (IEARModelProvider)ModelProviderManager.getModelProvider(project);
		final Application app = (Application)ModelProviderManager.getModelProvider(project).getModelObject();
		oldLibDir = app.getLibraryDirectory();
		if (oldLibDir == null) oldLibDir = J2EEConstants.EAR_DEFAULT_LIB_DIR;
		earModel.modify(new Runnable() {
			public void run() {			
			app.setLibraryDirectory(libDir);
		}}, null);
	}
	
	public boolean postHandleChanges(IProgressMonitor monitor) {
		return true;
	}
	
	public boolean preHandleChanges(IProgressMonitor monitor) {
		if (isVersion5) {
			if (libDir.length() == 0) {
				
				MessageDialog dlg = new MessageDialog(null,  
						J2EEUIMessages.getResourceString(J2EEUIMessages.BLANK_LIB_DIR),
			            null, J2EEUIMessages.getResourceString(J2EEUIMessages.BLANK_LIB_DIR_WARN_QUESTION), 
			            MessageDialog.QUESTION, new String[] {J2EEUIMessages.YES_BUTTON, 
																J2EEUIMessages.NO_BUTTON, 
																J2EEUIMessages.CANCEL_BUTTON}, 1);
				switch (dlg.open()) {
					case 0: break; 
					case 1: {
						handleChangeLibDirButton(false);
						return false;
					}
					case 2: return false;
					default: return false;
				}			
			}
			updateLibDir(monitor);
		}
		return true;
	}

	protected void handleRemoved(ArrayList<IVirtualComponent> removed) {
		super.handleRemoved(removed);
		J2EEComponentClasspathUpdater.getInstance().queueUpdateEAR(rootComponent.getProject());
	}
	
	protected IDataModelProvider getRemoveReferenceDataModelProvider(Object component) {
		return new RemoveComponentFromEnterpriseApplicationDataModelProvider();
	}

	protected void postAddProjects(Set moduleProjects) throws CoreException {
		EarFacetRuntimeHandler.updateModuleProjectRuntime(rootComponent.getProject(), moduleProjects, new NullProgressMonitor());
	}

	protected IDataModelProvider getAddReferenceDataModelProvider(IVirtualComponent component) {
		return new AddComponentToEnterpriseApplicationDataModelProvider();
	}
}
