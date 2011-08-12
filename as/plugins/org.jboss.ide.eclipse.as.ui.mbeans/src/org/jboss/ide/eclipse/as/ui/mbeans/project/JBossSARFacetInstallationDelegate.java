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
package org.jboss.ide.eclipse.as.ui.mbeans.project;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.common.project.facet.WtpUtils;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.util.IComponentImplFactory;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.osgi.service.prefs.BackingStoreException;

public class JBossSARFacetInstallationDelegate implements IDelegate {

	
	private IDataModel model;
	//public static final String ESB_NATURE = "org.jboss.tools.esb.project.core.ESBNature";

	public void execute(IProject project, IProjectFacetVersion fv,
			Object config, IProgressMonitor monitor) throws CoreException {
		model = (IDataModel) config;
		final IJavaProject jproj = JavaCore.create(project);

		createProjectStructure(project);

		
		// Add WTP natures.
		WtpUtils.addNatures(project);

		// Setup the flexible project structure
		
		/* 
		 * This is necessary because at time, the project has NO facets
		 * So a call to createComponent(etc) returns a default implementation.
		 * Today, this WTP default implementation does not handle  
		 * new reference types in an acceptable fashion 
		 * (Does not use extension point). 
		 */
		IComponentImplFactory factory = new SARVirtualComponent();
		IVirtualComponent newComponent = factory.createComponent(project);

		String outputLoc = jproj.readOutputLocation().removeFirstSegments(1).toString();
		newComponent.create(0, null);
		newComponent.setMetaProperty("java-output-path", outputLoc); //$NON-NLS-1$
		
		final IVirtualFolder jbiRoot = newComponent.getRootFolder();

		// Map the sarcontent to root for deploy
		String resourcesFolder = model.getStringProperty(
				IJBossSARFacetDataModelProperties.SAR_CONTENT_FOLDER);
		jbiRoot.createLink(new Path("/" + resourcesFolder), 0, null); //$NON-NLS-1$
				
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		final IClasspathEntry[] cp = jproj.getRawClasspath();
		for (int i = 0; i < cp.length; i++) {
			final IClasspathEntry cpe = cp[i];
			if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				if( cpe.getPath().removeFirstSegments(1).segmentCount() > 0 ){
					try{
						IFolder srcFolder = ws.getRoot().getFolder(cpe.getPath());
						
						IVirtualResource[] virtualResource = ComponentCore.createResources(srcFolder);
						//create link for source folder only when it is not mapped
						if( virtualResource.length == 0 ){
							jbiRoot.createLink(cpe.getPath().removeFirstSegments(1), 0, null);							
						}
					}
					catch(Exception e){
						// TODO 
					}
				}
			}
		}
		
//		IVirtualComponent outputFoldersComponent = new OutputFoldersVirtualComponent(project, newComponent);
//		VCFUtil.addReference(outputFoldersComponent, newComponent, "/", null); //$NON-NLS-1$
	}
	
	

	private void createProjectStructure(IProject project) throws CoreException{
		String strContentFolder = model.getStringProperty(IJBossSARFacetDataModelProperties.SAR_CONTENT_FOLDER);
		project.setPersistentProperty(IJBossSARFacetDataModelProperties.QNAME_SAR_CONTENT_FOLDER, strContentFolder);
		
		String qualifier = JBossServerCorePlugin.getDefault().getDescriptor().getUniqueIdentifier();
		IScopeContext context = new ProjectScope(project);
		IEclipsePreferences node = context.getNode(qualifier);
		if (node != null)
			node.putDouble(IJBossSARFacetDataModelProperties.SAR_PROJECT_VERSION, 1.0);
		
		try {
			node.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

		IFolder sarContent = project.getFolder(strContentFolder);
		IProgressMonitor monitor = new NullProgressMonitor();
		createFolder(sarContent.getFolder(IJBossSARFacetDataModelProperties.META_INF), monitor);
		project.refreshLocal(IResource.DEPTH_ZERO, null);
	}
	
	/**
	 * Creates the underlying folder if it doesn't exist.
	 * It also recursively creates parent folders if necessary
	 * @param folder the folder to create
	 * @throws CoreException 
	 */
	//TODO Check if that kind of method exists elsewhere to avoid duplication
	private void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
	    if(!folder.exists()) {
	        IContainer parent = folder.getParent();
	        if(parent != null && !parent.exists()) {
	          createFolder((IFolder) parent, monitor);
	        }
	        folder.create(true, true, monitor);
	    }
	}
}
