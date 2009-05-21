/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *	   David Schneider, david.schneider@unisys.com - [142500] WTP properties pages fonts don't follow Eclipse preferences
 *     Stefan Dimov, stefan.dimov@sap.com - bugs 207826, 222651
 *******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.override.ui.properypage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
import org.eclipse.jst.j2ee.application.internal.operations.RemoveComponentFromEnterpriseApplicationDataModelProvider;
import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualArchiveComponent;
import org.eclipse.jst.j2ee.internal.AvailableJ2EEComponentsForEARContentProvider;
import org.eclipse.jst.j2ee.internal.ICommonManifestUIConstants;
import org.eclipse.jst.j2ee.internal.J2EEConstants;
import org.eclipse.jst.j2ee.internal.J2EEDependenciesPage;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.internal.ManifestUIResourceHandler;
import org.eclipse.jst.j2ee.internal.SecondCheckBoxStateChangedEvent;
import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
import org.eclipse.jst.j2ee.internal.dialogs.ChangeLibDirDialog;
import org.eclipse.jst.j2ee.internal.dialogs.DependencyConflictResolveDialog;
import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.internal.ui.DoubleCheckboxTableItem;
import org.eclipse.jst.j2ee.internal.ui.DoubleCheckboxTableViewer;
import org.eclipse.jst.j2ee.model.IEARModelProvider;
import org.eclipse.jst.j2ee.model.ModelProviderManager;
import org.eclipse.jst.j2ee.project.EarUtilities;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.jst.j2ee.project.facet.EarFacetRuntimeHandler;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.jst.j2ee.project.facet.IJavaProjectMigrationDataModelProperties;
import org.eclipse.jst.j2ee.project.facet.JavaProjectMigrationDataModelProvider;
import org.eclipse.jst.javaee.application.Application;
import org.eclipse.jst.jee.project.facet.EarCreateDeploymentFilesDataModelProvider;
import org.eclipse.jst.jee.project.facet.ICreateDeploymentFilesDataModelProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
import org.eclipse.wst.common.componentcore.internal.operation.CreateReferenceComponentsDataModelProvider;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;


public class AddModulestoEARPropertiesPageClone implements /*IJ2EEDependenciesControl,*/ ICommonManifestUIConstants, Listener {
	
	protected final String PATH_SEPARATOR = String.valueOf(IPath.SEPARATOR);//AvailableJ2EEComponentsForEARContentProvider.PATH_SEPARATOR;	
	protected final IProject project;
	protected final J2EEDependenciesPage propPage; 
	protected IVirtualComponent earComponent = null;
	protected Text componentNameText;
	protected Label availableModules;
	protected CheckboxTableViewer availableComponentsViewer;
	protected Button selectAllButton;
	protected Button deselectAllButton;
	protected Button projectJarButton;
	protected Button externalJarButton;
	protected Button addVariableButton;
	protected Button changeLibPathButton;
	protected Composite buttonColumn;

	protected String libDir = null;
	protected String oldLibDir;
	protected List j2eeComponentList = new ArrayList();
	protected List javaProjectsList = new ArrayList();
	protected List j2eeLibElementList = new ArrayList();
	protected List javaLibProjectsList = new ArrayList();	
	protected static final IStatus OK_STATUS = IDataModelProvider.OK_STATUS;
	protected boolean isVersion5;
	protected Set libsToUncheck;
	protected Listener tableListener;
	protected Listener labelListener;
	
	//[Bug 238264] the cached list of jars selected using 'add jar' or 'add external jars'
	protected List<IVirtualComponent> addedJARComponents = new ArrayList<IVirtualComponent>();


	/**
	 * Constructor for AddModulestoEARPropertiesControl.
	 */
	public AddModulestoEARPropertiesPageClone(final IProject project, final J2EEDependenciesPage page) { 
		this.project = project;
		this.propPage = page;
		earComponent = ComponentCore.createComponent(project);
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
		libsToUncheck = new HashSet();
	}
	
	public Composite createContents(final Composite parent) { 
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        J2EEDependenciesPageClone.createDescriptionComposite(composite, ManifestUIResourceHandler.EAR_Modules_Desc);
		createListGroup(composite);
		refresh();
	    Dialog.applyDialogFont(parent);
		return composite;
	}

	protected void createListGroup(Composite parent) {
		Composite listGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		listGroup.setLayout(layout);
		GridData gData = new GridData(GridData.FILL_BOTH);
		gData.horizontalIndent = 5;
		listGroup.setLayoutData(gData);

		availableModules = new Label(listGroup, SWT.NONE);
		gData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		availableModules.setText(J2EEUIMessages.getResourceString("AVAILABLE_J2EE_COMPONENTS")); //$NON-NLS-1$ = "Available dependent JARs:"
		availableModules.setLayoutData(gData);
		createTableComposite(listGroup);
	}

	public boolean performOk() {
		NullProgressMonitor monitor = new NullProgressMonitor();
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
		removeModulesFromEAR(monitor);		
		addModulesToEAR(monitor);
		refresh();
		return true;
	}
	
	public void performDefaults() {
	}
	
	public boolean performCancel() {
		return true;
	}
	
	public void dispose() {
		Table table = null;
		if (availableComponentsViewer != null) {
		     table = availableComponentsViewer.getTable();
		     if (table == null)
		    	 return;
		}
		table.removeListener(SWT.Dispose, tableListener);
		table.removeListener(SWT.KeyDown, tableListener);
		table.removeListener(SWT.MouseMove, tableListener);
		table.removeListener(SWT.MouseHover, tableListener);		
	}

	public void setVisible(boolean visible) {
	}
	
	private List newJ2EEModulesToAdd(boolean inLibFolder){
		if (inLibFolder && !isVersion5) return null;
		List newComps = new ArrayList();
		List comps = inLibFolder ? j2eeLibElementList : j2eeComponentList;
		if (comps != null && !comps.isEmpty()){
			for (int i = 0; i < comps.size(); i++){
				IVirtualComponent handle = (IVirtualComponent)comps.get(i);
				if (ClasspathDependencyUtil.isClasspathComponentDependency(handle)) {
					continue;
				}
				if( !inEARAlready(handle))
					newComps.add(handle);
			}
		}
		return newComps;
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
	
	protected void createDD(IProgressMonitor monitor) {
		if( earComponent != null ){
			IDataModelOperation op = generateEARDDOperation();
			try {
				op.execute(monitor, null);
			} catch (ExecutionException e) {
				J2EEUIPlugin.logError(e);
			}
		}
	}
	
	private void execAddOp(IProgressMonitor monitor, List componentList, String path) throws CoreException {
		if (componentList == null || componentList.isEmpty()) return;
		IDataModel dm = DataModelFactory.createDataModel(new AddComponentToEnterpriseApplicationDataModelProvider());
		
		dm.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, earComponent);					
		dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, componentList);
		
		//[Bug 238264] the uri map needs to be manually set correctly
		Map uriMap = new HashMap();
		IVirtualComponent virtComp;
		String virtCompURIMapName;
		for(int i=0; i<componentList.size(); i++) {
			virtComp = (IVirtualComponent)componentList.get(i);
			virtCompURIMapName = getVirtualComponentNameWithExtension(virtComp);	
			uriMap.put(virtComp, virtCompURIMapName);
		}
		dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP, uriMap);
		
        if (isVersion5) dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_DEPLOY_PATH, path);

		IStatus stat = dm.validateProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
		if (stat != OK_STATUS)
			throw new CoreException(stat);
		try {
			dm.getDefaultOperation().execute(monitor, null);
		} catch (ExecutionException e) {
			J2EEUIPlugin.logError(e);
		}		
	}
	
	private void execAddOp1(IProgressMonitor monitor, List jProjList, List j2eeCompList, String path)
													throws CoreException {
		if (!jProjList.isEmpty()) {
			Set moduleProjects = new HashSet();
			for (int i = 0; i < jProjList.size(); i++) {
				try {
					IProject proj = (IProject) jProjList.get(i);
					moduleProjects.add(proj);
					IDataModel migrationdm = DataModelFactory.createDataModel(new JavaProjectMigrationDataModelProvider());
					migrationdm.setProperty(IJavaProjectMigrationDataModelProperties.PROJECT_NAME, proj.getName());
					migrationdm.getDefaultOperation().execute(monitor, null);


					IDataModel refdm = DataModelFactory.createDataModel(new CreateReferenceComponentsDataModelProvider());
					List targetCompList = (List) refdm.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);

					IVirtualComponent targetcomponent = ComponentCore.createComponent(proj);
					targetCompList.add(targetcomponent);

					refdm.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, earComponent);
					refdm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, targetCompList);
					if (isVersion5) refdm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_DEPLOY_PATH, path);
					

					// referenced java projects should have archiveName attribute
					((Map)refdm.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP)).put(targetcomponent, proj.getName().replace(' ', '_') + IJ2EEModuleConstants.JAR_EXT);

					refdm.getDefaultOperation().execute(monitor, null);
					j2eeCompList.add(targetcomponent);
				} catch (ExecutionException e) {
					J2EEUIPlugin.logError(e);
				}
			}
			EarFacetRuntimeHandler.updateModuleProjectRuntime(earComponent.getProject(), moduleProjects, new NullProgressMonitor());
		} // end 
		
	}
	
	private IStatus addModulesToEAR(IProgressMonitor monitor) {
		IStatus stat = OK_STATUS;
		try {
			if( earComponent != null ){
				final List list = newJ2EEModulesToAdd(false);
				final List bndList = newJ2EEModulesToAdd(true);
				final boolean shouldRun = (list != null && !list.isEmpty()) || !javaProjectsList.isEmpty();
				final boolean shouldBndRun = isVersion5 && 
											((bndList != null && !bndList.isEmpty()) || !javaLibProjectsList.isEmpty());
				if(shouldRun || shouldBndRun){
					IWorkspaceRunnable runnable = new IWorkspaceRunnable(){

						public void run(IProgressMonitor monitor) throws CoreException{
							if (shouldRun) {
								execAddOp(monitor, list, J2EEConstants.EAR_ROOT_DIR);
								execAddOp1(monitor, javaProjectsList, j2eeComponentList, J2EEConstants.EAR_ROOT_DIR);								
							} 
							if (shouldBndRun) {
								execAddOp(monitor, bndList, libDir);
								execAddOp1(monitor, javaLibProjectsList, j2eeLibElementList, libDir);																
							} 
						}
					};
					J2EEUIPlugin.getWorkspace().run(runnable, monitor);
				}
			}
		} catch (Exception e) {
			J2EEUIPlugin.logError(e);
		}
		
		//[Bug 238264] clear out the cache because they should all either be added as references now
		//	or no longer checked and therefore not wanted by the user
		this.addedJARComponents.clear();
		
		return OK_STATUS;
	}
	
	private void remComps(List list, String path) {
		if( !list.isEmpty()){
			try {
				// remove the components from the EAR
				IDataModelOperation op = removeComponentFromEAROperation(earComponent, list, path);
				op.execute(null, null);
				// if that succeeded, remove all EAR-scope J2EE dependencies on these components
				J2EEComponentClasspathUpdater.getInstance().queueUpdateEAR(earComponent.getProject());
			} catch (ExecutionException e) {
				J2EEUIPlugin.logError(e);
			}
		}	
	}
	
	private IStatus removeModulesFromEAR(IProgressMonitor monitor) {
		IStatus stat = OK_STATUS;
		if (!isVersion5) {
			if(earComponent != null && j2eeComponentList != null) {
				List list = getComponentsToRemove();
				remComps(list, J2EEConstants.EAR_ROOT_DIR);
			}			
		} else {	
			if( earComponent != null && j2eeComponentList != null) {
				List[] list = getComponentsToRemoveUpdate(!libDir.equals(oldLibDir)); 
				remComps(list[0], J2EEConstants.EAR_ROOT_DIR);
				
				remComps(list[1], oldLibDir);
			}
		}
		return stat;
	}		
	
	protected IDataModelOperation generateEARDDOperation() {		
		IDataModel model = DataModelFactory.createDataModel(new EarCreateDeploymentFilesDataModelProvider());
		model.setProperty(ICreateDeploymentFilesDataModelProperties.GENERATE_DD, earComponent);					
		model.setProperty(ICreateDeploymentFilesDataModelProperties.TARGET_PROJECT, project);		
		return model.getDefaultOperation();
	}
	
	protected IDataModelOperation removeComponentFromEAROperation(IVirtualComponent sourceComponent, List targetComponentsHandles, String dir) {
		IDataModel model = DataModelFactory.createDataModel(new RemoveComponentFromEnterpriseApplicationDataModelProvider());
		model.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, sourceComponent);
		List modHandlesList = (List) model.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
		modHandlesList.addAll(targetComponentsHandles);
		model.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, modHandlesList);
        model.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_DEPLOY_PATH, dir);		
		return model.getDefaultOperation();
	}
	
	protected List getComponentsToRemove(){
		//j2eeComponentList = getCheckedJ2EEElementsAsList();
		List list = new ArrayList();
		if( earComponent != null && list != null ){
			IVirtualReference[] oldrefs = earComponent.getReferences();
			for (int j = 0; j < oldrefs.length; j++) {
				IVirtualReference ref = oldrefs[j];
				IVirtualComponent handle = ref.getReferencedComponent();
				if(!j2eeComponentList.contains(handle) && (isVersion5 ? !j2eeLibElementList.contains(handle) : true)){
					if ((handle instanceof VirtualArchiveComponent) && (isPhysicallyAdded((VirtualArchiveComponent)handle)))
						continue;
					list.add(handle);
				}
			}
		}
		return list;		
	}
	
	// EAR5 case
	protected List[] getComponentsToRemoveUpdate(boolean dirUpdated){
		//j2eeComponentList = getCheckedJ2EEElementsAsList();
		List[] list = new ArrayList[2];
		list[0] = new ArrayList();
		list[1] = new ArrayList();
		if( earComponent != null && list != null ){
			IVirtualReference[] oldrefs = earComponent.getReferences();
			for (int j = 0; j < oldrefs.length; j++) {
				IVirtualReference ref = oldrefs[j];
				IVirtualComponent handle = ref.getReferencedComponent();
				if (handle instanceof VirtualArchiveComponent) {
					VirtualArchiveComponent comp = (VirtualArchiveComponent)handle;
					if (isPhysicallyAdded(comp))
						continue;
				}
				if(!j2eeComponentList.contains(handle) && ref.getRuntimePath().isRoot()) {
					list[0].add(handle);
				}
				if((!j2eeLibElementList.contains(handle) || dirUpdated) &&
						ref.getRuntimePath().toString().equals(oldLibDir)) {	
					list[1].add(handle);
				}
			}
		}
		return list;		
	}
	
	
	public void handleEvent(Event event) {
		if (event.widget == selectAllButton)
			handleSelectAllButtonPressed();
		else if (event.widget == deselectAllButton)
			handleDeselectAllButtonPressed();
		else if(event.widget == projectJarButton)
			handleSelectProjectJarButton();
		else if(event.widget == externalJarButton)
			handleSelectExternalJarButton();
		else if(event.widget == addVariableButton)
			handleSelectVariableButton();
		else if(event.widget == changeLibPathButton) {
			this.handleChangeLibDirButton(true);
		}
	}

	private void handleSelectAllButtonPressed() {
		availableComponentsViewer.setAllChecked(true);
		j2eeComponentList = getCheckedJ2EEElementsAsList(true);
		javaProjectsList = getCheckedJavaProjectsAsList(true);		
		if (isVersion5) {
			j2eeLibElementList = getCheckedJ2EEElementsAsList(false);
			javaLibProjectsList = getCheckedJavaProjectsAsList(false);			
		} 
	}

	private void handleDeselectAllButtonPressed() {
		availableComponentsViewer.setAllChecked(false);
		if (isVersion5) {
			((DoubleCheckboxTableViewer)availableComponentsViewer).setAllSecondChecked(false);
			libsToUncheck.clear();
		}
		j2eeComponentList = new ArrayList();
		javaProjectsList = new ArrayList();
		if (isVersion5) {
			j2eeLibElementList = new ArrayList();
			javaLibProjectsList = new ArrayList();			
		}
	}
	
	/**
	 * [Bug 238264]
	 * Add an archive as a potential new reference for this.earComponent
	 * NOTE1: the given archive will not be added as a potential reference if there is already a reference to it
	 * NOTE2: the given archive will only be added as an actual reference when this.performOk is invoked
	 * 
	 * @param archive the archive to add as a potential new reference in this.earComponent
	 * 			
	 */
	private void addPotentialNewReference(IVirtualComponent archive) {
		//check to see if a reference to the given archive already exists
		IVirtualReference [] existingRefs = earComponent.getReferences();
		IVirtualComponent referencedComponent;
		boolean refAlreadyExists = false;
		for(int i=0;i<existingRefs.length && !refAlreadyExists;i++){
			referencedComponent = existingRefs[i].getReferencedComponent();
			refAlreadyExists = referencedComponent.equals(archive);
		}
		
		//only add the archive as a potentialy new reference if it does not already exist
		if(!refAlreadyExists) {
			this.j2eeComponentList.add(archive);
			this.addedJARComponents.add(archive);
		} else {
			//TODO should inform user that they selected an already referenced archive?
		}
	}
	
	private void handleSelectExternalJarButton(){
		IPath[] selected= BuildPathDialogAccess.chooseExternalJAREntries(propPage.getShell());

		if (selected != null) {
			for (int i= 0; i < selected.length; i++) {
				
				String type = VirtualArchiveComponent.LIBARCHIVETYPE + IPath.SEPARATOR;
				IVirtualComponent archive = ComponentCore.createArchiveComponent( earComponent.getProject(), type +
							selected[i].toString());
				
				this.addPotentialNewReference(archive);
			}
			refresh();
		}
		
	}

	private void handleSelectVariableButton(){
		IPath existingPath[] = new Path[0];
		IPath[] paths =  BuildPathDialogAccess.chooseVariableEntries(propPage.getShell(), existingPath);
		
		if (paths != null) {
			refresh();
			for (int i = 0; i < paths.length; i++) {
				IPath resolvedPath= JavaCore.getResolvedVariablePath(paths[i]);

				java.io.File file = new java.io.File(resolvedPath.toOSString());
				if( file.isFile() && file.exists()){
					String type = VirtualArchiveComponent.VARARCHIVETYPE + IPath.SEPARATOR;
					
					IVirtualComponent archive = ComponentCore.createArchiveComponent( earComponent.getProject(), type +
								paths[i].toString());
					
					this.addPotentialNewReference(archive);
				}else{
					//display error
				}
			}
			refresh();
		}	
	}
	
	private void handleChangeLibDirButton(boolean warnBlank) {					
		IVirtualFile vFile = earComponent.getRootFolder().getFile(new Path(J2EEConstants.APPLICATION_DD_URI));
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
		setLibDirInContentProvider();
		refresh();
	}

	
	protected void createTableComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gData);
		fillComposite(composite);
	}

	public void fillComposite(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		createTable(parent);
		createButtonColumn(parent);
	}

	protected void createButtonColumn(Composite parent) {
		buttonColumn = createButtonColumnComposite(parent);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonColumn.setLayoutData(data);
		createPushButtons();
	}

	protected void createPushButtons() {
		selectAllButton = createPushButton(SELECT_ALL_BUTTON);
		deselectAllButton = createPushButton(DE_SELECT_ALL_BUTTON);
		projectJarButton = createPushButton(J2EEUIMessages.getResourceString(J2EEUIMessages.PROJECT_JAR));//$NON-NLS-1$
		externalJarButton = createPushButton(J2EEUIMessages.getResourceString(J2EEUIMessages.EXTERNAL_JAR));//$NON-NLS-1$
		addVariableButton = createPushButton(J2EEUIMessages.getResourceString(J2EEUIMessages.ADDVARIABLE));//$NON-NLS-1$
		if (isVersion5) changeLibPathButton = createPushButton(J2EEUIMessages.getResourceString(J2EEUIMessages.CHANGE_LIB_DIR));//$NON-NLS-1$
	}

	protected Button createPushButton(String label) {
		Button aButton = primCreatePushButton(label, buttonColumn);
		aButton.addListener(SWT.Selection, this);
		aButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return aButton;
	}

	public Button primCreatePushButton(String label, Composite aButtonColumn) {
		Button aButton = new Button(aButtonColumn, SWT.PUSH);
		aButton.setText(label);
		return aButton;
	}

	public Composite createButtonColumnComposite(Composite parent) {
		Composite aButtonColumn = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		aButtonColumn.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		aButtonColumn.setLayoutData(data);
		return aButtonColumn;
	}

	public Group createGroup(Composite parent) {
		return new Group(parent, SWT.NULL);
	}

	protected void createTable(Composite parent) {
		availableComponentsViewer = createavailableComponentsViewer(parent);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		availableComponentsViewer.getTable().setLayoutData(gd);

		if (earComponent != null) {
			int j2eeVersion = J2EEVersionUtil.convertVersionStringToInt(earComponent);
			AvailableJ2EEComponentsForEARContentProvider provider = new AvailableJ2EEComponentsForEARContentProvider(earComponent, j2eeVersion);
			availableComponentsViewer.setContentProvider(provider);
			availableComponentsViewer.setLabelProvider(provider);
			setLibDirInContentProvider();
			addTableListeners();
		}
	}
	
	private void setLibDirInContentProvider() {
		IContentProvider prov = availableComponentsViewer.getContentProvider();
		if (prov instanceof AvailableJ2EEComponentsForEARContentProvider) 
			((AvailableJ2EEComponentsForEARContentProvider)prov).setCurrentLibDir(libDir);
	}

	protected void addTableListeners() {
		addCheckStateListener();
		addHoverHelpListeners();
	}
	
	protected void addHoverHelpListeners() {
		final Table table = availableComponentsViewer.getTable();				
		createLabelListener(table);
		createTableListener(table);
		table.addListener(SWT.Dispose, tableListener);
		table.addListener(SWT.KeyDown, tableListener);
		table.addListener(SWT.MouseMove, tableListener);
		table.addListener(SWT.MouseHover, tableListener);		
	}
	
	protected void createLabelListener(final Table table) {
		labelListener = new Listener () {
			public void handleEvent (Event event) {
				Label label = (Label)event.widget;
				Shell shell = label.getShell ();
				switch (event.type) {
					case SWT.MouseDown:
						Event e = new Event ();
						e.item = (TableItem) label.getData ("_TABLEITEM");
						table.setSelection (new TableItem [] {(TableItem) e.item});
						table.notifyListeners (SWT.Selection, e);
						shell.dispose ();
						table.setFocus();
						break;
					case SWT.MouseExit:
						shell.dispose ();
						break;
				}
			}
		};
	}
	
	protected void createTableListener(final Table table) {
		tableListener = new Listener () {
			Shell tip = null;
			Label label = null;
			public void handleEvent (Event event) {
				switch (event.type) {
					case SWT.Dispose:
					case SWT.KeyDown:
					case SWT.MouseMove: {
						if (tip == null) break;
						tip.dispose ();
						tip = null;
						label = null;
						break;
					}
					case SWT.MouseHover: {
						TableItem item = table.getItem (new Point (event.x, event.y));
						if (item != null) {
							if (!item.getGrayed())
								return;
							if (tip != null  && !tip.isDisposed ()) tip.dispose ();
							tip = new Shell (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					                .getShell(), SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
							tip.setBackground (Display.getDefault().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
							FillLayout layout = new FillLayout ();
							layout.marginWidth = 2;
							tip.setLayout (layout);
							label = new Label (tip, SWT.WRAP);
							label.setForeground (Display.getDefault().getSystemColor (SWT.COLOR_INFO_FOREGROUND));
							label.setBackground (Display.getDefault().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
							label.setData ("_TABLEITEM", item);
							label.setText (J2EEUIMessages.getResourceString(J2EEUIMessages.HOVER_HELP_FOR_DISABLED_LIBS));
							label.addListener (SWT.MouseExit, labelListener);
							label.addListener (SWT.MouseDown, labelListener);
							Point size = tip.computeSize (SWT.DEFAULT, SWT.DEFAULT);
							Rectangle rect = item.getBounds (0);
							Point pt = table.toDisplay (rect.x, rect.y);
							tip.setBounds (pt.x, pt.y - size.y, size.x, size.y);
							tip.setVisible (true);
						}
					}
				}
			}
		};		
	}

	protected void addCheckStateListener() {
		availableComponentsViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				CheckboxTableViewer vr = (CheckboxTableViewer)event.getSource();
				Object element = event.getElement();
				if (vr.getGrayed(element)) 
					vr.setChecked(element, !vr.getChecked(element));
				Object o = event.getSource();
				if (!(event instanceof SecondCheckBoxStateChangedEvent) && (isVersion5)) {
					Object[] items = ((DoubleCheckboxTableViewer)vr).getUncheckedItems();					
					for (int i = 0; i < items.length; i++) {
						DoubleCheckboxTableItem item = (DoubleCheckboxTableItem)items[i];
						if (item.getSecondChecked()) {
							item.setSecondChecked(false);
							libsToUncheck.remove(event.getElement());
						}
					}
				}
				if ((event instanceof SecondCheckBoxStateChangedEvent)) {
					SecondCheckBoxStateChangedEvent evt = (SecondCheckBoxStateChangedEvent)event;
					DoubleCheckboxTableItem tblItem = evt.getTableItem();
					if (tblItem.getSecondChecked() && isConflict(tblItem.getData())) {
						DependencyConflictResolveDialog dlg = new DependencyConflictResolveDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				                .getShell(), DependencyConflictResolveDialog.DLG_TYPE_2);
						if (dlg.open() == DependencyConflictResolveDialog.BTN_ID_CANCEL) {
							tblItem.setSecondChecked(false);
							return;
						}
					}	
					if (tblItem.getSecondChecked()) {
						if (!tblItem.getChecked())
							tblItem.setChecked(true);						
						libsToUncheck.add(event.getElement());	
					} else {
						libsToUncheck.remove(event.getElement());
					}
				}
				j2eeComponentList = getCheckedJ2EEElementsAsList(true);
				javaProjectsList = getCheckedJavaProjectsAsList(true);		
				if (isVersion5) {
					j2eeLibElementList = getCheckedJ2EEElementsAsList(false);
					javaLibProjectsList = getCheckedJavaProjectsAsList(false);					
					
				} 
			}
		});
	}
	
	protected List getCPComponentsInEar(boolean inLibFolder) {
		List list = new ArrayList();
		Map pathToComp = new HashMap();
		IVirtualReference refs[] = earComponent.getReferences();
		for( int i=0; i< refs.length; i++){
			IVirtualReference ref = refs[i];
			if ((ref.getRuntimePath().isRoot() && !inLibFolder) ||
				(!ref.getRuntimePath().isRoot() && inLibFolder) ||
				!isVersion5) {
				
				IVirtualComponent comp = ref.getReferencedComponent();
				AvailableJ2EEComponentsForEARContentProvider.addClasspathComponentDependencies(list, pathToComp, comp);
			}
		}
		return list;
	}
	
	protected List getComponentsInEar(boolean inLibFolder) {
		List list = new ArrayList();
		IVirtualReference refs[] = earComponent.getReferences();
		for( int i=0; i< refs.length; i++){
			IVirtualReference ref = refs[i];
			if ((ref.getRuntimePath().isRoot() && !inLibFolder) ||
				(!ref.getRuntimePath().isRoot() && inLibFolder) ||
				!isVersion5) {
				
				IVirtualComponent comp = ref.getReferencedComponent();
				list.add(comp);
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @param componentHandle
	 * @return
	 * @description  returns true is a component is already in the EAR as a dependent
	 */
	protected boolean inEARAlready(IVirtualComponent component){
		IVirtualReference refs[] = earComponent.getReferences();
		for( int i=0; i< refs.length; i++){
			IVirtualReference ref = refs[i];
			if  ( ref.getReferencedComponent().equals( component ))
				return true;
		}	
		return false;
	}
		
	// The next two are used in EAR5 case
	protected List getCheckedJ2EEElementsAsList(boolean singleChecked) {		
		Object[] elements;
		if (isVersion5) {
			elements = singleChecked ? ((DoubleCheckboxTableViewer)availableComponentsViewer).getSingleCheckedElements():
									   ((DoubleCheckboxTableViewer)availableComponentsViewer).getDoubleCheckedElements();
				
		} else {
			elements = availableComponentsViewer.getCheckedElements();
		}
		List list;
		if (elements == null || elements.length == 0)
			list = new ArrayList(0); // Collections.EMPTY_LIST would cause UnsupportedOperationException when a later attempt to add to it is made
		else {
			list = new ArrayList();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] instanceof IVirtualComponent) {
					list.add(elements[i]);
				}
			}
		}
		return list;
	}
	
	protected List getCheckedJavaProjectsAsList(boolean single) {
		Object[] elements;
		if (isVersion5) {
			elements = single ? ((DoubleCheckboxTableViewer)availableComponentsViewer).getSingleCheckedElements() :
								((DoubleCheckboxTableViewer)availableComponentsViewer).getDoubleCheckedElements();
		} else {
			elements = availableComponentsViewer.getCheckedElements();
		}
		
		List list;
		if (elements == null || elements.length == 0)
			list = new ArrayList(0); // Collections.EMPTY_LIST would cause UnsupportedOperationException when a later attempt to add to it is made
		else {
			list = new ArrayList();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] instanceof IProject) {
					list.add(elements[i]);
				}
			}
		}
		return list;
	}
		
	protected List getLibFolderLibsAsList() {
		Object[] items = ((DoubleCheckboxTableViewer)availableComponentsViewer).getSecondCheckedItems();
		List list;
		if (items == null || items.length == 0)
			list = new ArrayList(0); // Collections.EMPTY_LIST would cause UnsupportedOperationException when a later attempt to add to it is made
		else {
			list = new ArrayList();
			for (int i = 0; i < items.length; i++) {
				Object element = ((DoubleCheckboxTableItem)items[i]).getData();
				if (element instanceof IProject) {
					list.add(element);
				}
			}
		}
		return list;
	}

	public CheckboxTableViewer createavailableComponentsViewer(Composite parent) {
		int flags = SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI;

		Table table = isVersion5 ? new Table(parent, flags) : new Table(parent, flags);
		availableComponentsViewer = isVersion5 ? new DoubleCheckboxTableViewer(table, 2) : new CheckboxTableViewer(table);

		// set up table layout
		TableLayout tableLayout = new org.eclipse.jface.viewers.TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(200, true));
		tableLayout.addColumnData(new ColumnWeightData(200, true));
		if (isVersion5) tableLayout.addColumnData(new ColumnWeightData(200, true));
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		availableComponentsViewer.setSorter(null);

		// table columns
		TableColumn fileNameColumn = new TableColumn(table, SWT.NONE, 0);
		fileNameColumn.setText(ManifestUIResourceHandler.JAR_Module_UI_); 
		fileNameColumn.setResizable(true);

		TableColumn projectColumn = new TableColumn(table, SWT.NONE, 1);
		projectColumn.setText(ManifestUIResourceHandler.Project_UI_); 
		projectColumn.setResizable(true);
		
		if (isVersion5) {
			TableColumn bndColumn = new TableColumn(table, SWT.NONE, 2);
			bndColumn.setText(ManifestUIResourceHandler.Packed_In_Lib_UI_); 
			bndColumn.setResizable(true);			
		}
			
		tableLayout.layout(table, true);
		return availableComponentsViewer;

	}

	private boolean secondShouldBeDisabled(IVirtualComponent component) {
		if(component.isBinary()) return false;
		if (JavaEEProjectUtilities.isApplicationClientComponent(component)) return true;
		if (JavaEEProjectUtilities.isEARProject(component.getProject()) && component.isBinary()) return false;
		if (JavaEEProjectUtilities.isEJBComponent(component)) return true;
		if (JavaEEProjectUtilities.isDynamicWebComponent(component)) return true;
		if (JavaEEProjectUtilities.isJCAComponent(component)) return true;
		if (JavaEEProjectUtilities.isStaticWebProject(component.getProject())) return true;
		if (JavaEEProjectUtilities.isProjectOfType(component.getProject(), IJ2EEFacetConstants.JAVA)) return false;
		return false;
	}
	
	private boolean isPhysicallyAdded(VirtualArchiveComponent component) {
		IPath p = null;
		try {
			p = component.getProjectRelativePath();
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}		
		
	private boolean isInLibDir(VirtualArchiveComponent comp) {
		IPath p = comp.getProjectRelativePath();
		if (p.segmentCount() == 2)
			return false;
		return true;
	}
	
	
	public void refresh() {

		IWorkspaceRoot input = ResourcesPlugin.getWorkspace().getRoot();
		availableComponentsViewer.setInput(input);
		GridData data = new GridData(GridData.FILL_BOTH);
		int numlines = Math.min(10, availableComponentsViewer.getTable().getItemCount());
		data.heightHint = availableComponentsViewer.getTable().getItemHeight() * numlines;
		availableComponentsViewer.getTable().setLayoutData(data);

		//[Bug 238264] for all the jars in the cache temparaly list them in the grid
		// until the user applys the changes
		for(IVirtualComponent jarComponent : this.addedJARComponents) {
			availableComponentsViewer.add(jarComponent);
		}
		
		TableItem [] items = availableComponentsViewer.getTable().getItems();
		List list = new ArrayList();
		//Object[] comps = getComponentsInEar();
		List cpComps;
		List cpLibComps = new LinkedList();
		HashSet j2eeComponentSet = new HashSet();
		HashSet j2eeLibComponentSet = new HashSet();		
		if (isVersion5) {
			if( j2eeComponentList.isEmpty() ){
				List comps = getComponentsInEar(false);
				j2eeComponentList.addAll(comps);
			}
			if( j2eeLibElementList.isEmpty() ){
				List comps = getComponentsInEar(true);
				j2eeLibElementList.addAll(comps);
			}			
			// get all Classpath contributions to the Ear
			cpComps = getCPComponentsInEar(false);
			j2eeComponentList.addAll(cpComps);	
			cpLibComps = getCPComponentsInEar(true);
			j2eeLibElementList.addAll(cpLibComps);	
			for (int i = 0; i < j2eeLibElementList.size(); i++) {
				j2eeLibComponentSet.add(j2eeLibElementList.get(i));
			}
				
		} else {
			if( j2eeComponentList.isEmpty() ){
				List comps = getComponentsInEar(false);
				j2eeComponentList.addAll(comps);
			}
			// get all Classpath contributions to the Ear
			cpComps = getCPComponentsInEar(false);
			j2eeComponentList.addAll(cpComps);			
		}
		for (int i = 0; i < j2eeComponentList.size(); i++) {
			j2eeComponentSet.add(j2eeComponentList.get(i));
		}
				
		for (int i = 0; i < items.length; i++) {
			Object element = items[i].getData();
			try {
				if (element instanceof IVirtualComponent || 
						(element instanceof IProject && ((IProject) element).hasNature(JavaCore.NATURE_ID))) {
					if (j2eeComponentSet.contains(element)) {
						list.add(element);
					}
					boolean shouldBeDisabled = false;
					if (element instanceof VirtualArchiveComponent) {
						shouldBeDisabled = isPhysicallyAdded((VirtualArchiveComponent)element);
						if (shouldBeDisabled) {
							items[i].setChecked(true);
							items[i].setGrayed(true);
						}
					}
					if (isVersion5) {
						DoubleCheckboxTableItem dcbItem = (DoubleCheckboxTableItem)items[i]; 
						boolean secondEnabled = true;
						if (element instanceof IVirtualComponent) 
							secondEnabled = !secondShouldBeDisabled((IVirtualComponent) element);
						if (shouldBeDisabled) {
							dcbItem.setSecondChecked(isInLibDir((VirtualArchiveComponent)element));
							dcbItem.setSecondEnabled(false);
						} else {
							dcbItem.setSecondChecked(j2eeLibComponentSet.contains(element));							
							dcbItem.setSecondEnabled(secondEnabled);
						}
						if (j2eeLibComponentSet.contains(element)) list.add(element);
					}
				}
			} catch (CoreException e) {
				J2EEUIPlugin.logError(0, e.getMessage(), e);
			}
		}		
		
		for (int i = 0; i < list.size(); i++) 
			availableComponentsViewer.setChecked(list.get(i), true);
		GridData btndata = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		buttonColumn.setLayoutData(btndata);

	}

	private boolean isConflict(Object lib) {
		IProject libProj = (lib instanceof IProject) ? (IProject)lib : ((IVirtualComponent)lib).getProject(); 
		IProject earProject = earComponent.getProject();	
		try {			
			IVirtualComponent cmp = ComponentCore.createComponent(earProject);
			IProject[] earRefProjects = earProject.getReferencedProjects();
			for (int i = 0; i < earRefProjects.length; i++) {	
				if (!J2EEProjectUtilities.isEARProject(earRefProjects[i]) &&
						!earRefProjects[i].equals(libProj)) {
					IVirtualComponent cmp1 = ComponentCore.createComponent(earRefProjects[i]);
					IVirtualReference[] refs = cmp1.getReferences();
					for (int j = 0; j < refs.length; j++) {
						if (refs[j].getReferencedComponent().getProject().equals(libProj)) return true;
					}
				}	
			}
			return false;
		} catch (CoreException ce) {
			J2EEUIPlugin.logError(ce);
		}		
		return false;
	}	
	
	private void handleSelectProjectJarButton(){
		IPath[] selected= BuildPathDialogAccess.chooseJAREntries(propPage.getShell(), project.getLocation(), new IPath[0]);
	
		if (selected != null) {
			for (int i= 0; i < selected.length; i++) {
				//IPath fullPath = project.getFile(selected[i]).getFullPath();	
				String type = VirtualArchiveComponent.LIBARCHIVETYPE + IPath.SEPARATOR;
				IVirtualComponent archive = ComponentCore.createArchiveComponent( earComponent.getProject(), type +
							selected[i].makeRelative().toString());
				
				this.addPotentialNewReference(archive);
			}
			refresh();
		}
		
	}
	
	/**
	 * [Bug 238264]
	 * determines a unique URI mapping name for a given component
	 * this is in case two components have the same name.
	 * 
	 * @return returns a valid (none duplicate) uri mapping name for the given component\
	 */
	private String getURIMappingName(IVirtualComponent archive) {
		
		//get the default uri map name for the given archive
		IPath componentPath = Path.fromOSString(archive.getName());
		String uriMapName = componentPath.lastSegment().replace(' ', '_');
		
		
		//check to be sure this uri mapping is not already in use by another reference
		boolean dupeArchiveName;
		String refedCompName;
		int lastDotIndex;
		String increment;
		IVirtualReference [] existingRefs = earComponent.getReferences();
		for(int i=0;i<existingRefs.length;i++){
			refedCompName = existingRefs[i].getReferencedComponent().getName();
			
			//if uri mapping names of the refed component and the given archive are the same
			//  find a new uri map name for the given archive
			if(existingRefs[i].getArchiveName().equals(uriMapName)){
				dupeArchiveName = true;
				//find a new uriMapName for the given component
				for(int j=1; dupeArchiveName; j++){
					lastDotIndex = uriMapName.lastIndexOf('.');
					increment = "_"+j; //$NON-NLS-1$
					
					//create the new potential name
					if(lastDotIndex != -1){
						uriMapName = uriMapName.substring(0, lastDotIndex) + increment + uriMapName.substring(lastDotIndex);
					} else {
						uriMapName = uriMapName.substring(0)+increment;
					}
					
					//determine if the new potential name is valid
					for(int k=0; k<existingRefs.length; k++) {
						dupeArchiveName = existingRefs[k].getArchiveName().equals(uriMapName);
						if(dupeArchiveName) {
							break;
						}
					}
				}
			}
		}
		
		return uriMapName;
	}
	
	/**
	 * Method returns the name of the given IVirtualComponent being sure the correct extension
	 * is on the end of the name, this is important for internal projects. Added for [Bug 241509]
	 * 
	 * @param virtComp the IVirtualComponent to get the name of with the correct extension
	 * @return the name of the given IVirtualComponent with the correct extension
	 */
	private String getVirtualComponentNameWithExtension(IVirtualComponent virtComp) {
		String virtCompURIMapName = this.getURIMappingName(virtComp);
		
		boolean linkedToEAR = true;
		try {
			if(virtComp.isBinary()){
				linkedToEAR = ((J2EEModuleVirtualArchiveComponent)virtComp).isLinkedToEAR();
				((J2EEModuleVirtualArchiveComponent)virtComp).setLinkedToEAR(false);
			}
			if(JavaEEProjectUtilities.isDynamicWebComponent(virtComp)) {
				if(!virtCompURIMapName.endsWith(IJ2EEModuleConstants.WAR_EXT)) {
					//web module URIs need to end in WAR
					virtCompURIMapName += IJ2EEModuleConstants.WAR_EXT;
				}
			} else if(JavaEEProjectUtilities.isJCAComponent(virtComp)) {
				if(!virtCompURIMapName.endsWith(IJ2EEModuleConstants.RAR_EXT)) {
					//connector module URIs need to end in RAR
					virtCompURIMapName += IJ2EEModuleConstants.RAR_EXT;
				}
			} else if(!virtCompURIMapName.endsWith(IJ2EEModuleConstants.JAR_EXT)) {
				//all other modules (EJB, AppClient, Utility) need to end in JAR
				virtCompURIMapName += IJ2EEModuleConstants.JAR_EXT;
			}
		} finally {
			if(virtComp.isBinary()){
				((J2EEModuleVirtualArchiveComponent)virtComp).setLinkedToEAR(linkedToEAR);
			}
		}
		return virtCompURIMapName;
	}
		
}
