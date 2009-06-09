/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Dimov, stefan.dimov@sap.com - bug 207826
 *******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.override.ui.propertypage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jst.j2ee.application.internal.operations.ClassPathSelection;
import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;

public class ComponentDependencyContentProvider extends ViewerFilter implements IStructuredContentProvider, ITableLabelProvider {
	
	final static String PATH_SEPARATOR = String.valueOf(IPath.SEPARATOR);
	
	private IVirtualComponent rootComponent;
	private HashMap<Object, String> runtimePaths;
	
	public ComponentDependencyContentProvider(IVirtualComponent rootComponent) {
		super();
		this.rootComponent = rootComponent;
	}

	public void setRuntimePaths(HashMap<Object, String> paths) {
		this.runtimePaths = paths;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if( rootComponent == null )
			return new Object[]{};
		
		Object[] empty = new Object[0];
		if (!(inputElement instanceof IWorkspaceRoot))
			return empty;
		IProject[] projects = ((IWorkspaceRoot) inputElement).getProjects();
		if (projects == null || projects.length == 0)
			return empty;
		List validCompList = new ArrayList();
		HashMap pathToComp = new HashMap();
		for (int i = 0; i < projects.length; i++) {
			// get flexible project
			IProject project = projects[i];
			if(ModuleCoreNature.isFlexibleProject(project)){
				IVirtualComponent component = ComponentCore.createComponent(project);
				if( !component.equals(rootComponent))
					validCompList.add(component);
				else {
					IVirtualReference[] newrefs = component.getReferences();
					for( int k=0; k< newrefs.length; k++ ){
						IVirtualReference tmpref = newrefs[k];
						IVirtualComponent referencedcomp = tmpref.getReferencedComponent();		
						boolean isBinary = referencedcomp.isBinary();
						if( isBinary ){
							validCompList.add(referencedcomp);
						} else {
							addClasspathComponentDependencies(validCompList,pathToComp, referencedcomp);
						}
					}	
				}
			} else if (project.exists() && project.isAccessible() ){ 
				if( !project.getName().startsWith(".") ) //$NON-NLS-1$
					validCompList.add(project);
			}
		}
		return validCompList.toArray();
	}
	
	public static void addClasspathComponentDependencies(final List componentList, HashMap pathToComp, final IVirtualComponent referencedComponent) {
		if (referencedComponent instanceof J2EEModuleVirtualComponent) {
			J2EEModuleVirtualComponent j2eeComp = (J2EEModuleVirtualComponent) referencedComponent;
			IVirtualReference[] cpRefs = j2eeComp.getJavaClasspathReferences();
			for (int j=0; j < cpRefs.length; j++) {
				String unresolvedURI = null;
				// only ../ mappings supported at this level
				if (!cpRefs[j].getRuntimePath().equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH)) {
					continue;
				}
				// if the absolute path for this component already has a mapping, skip (the comp might be contributed by more than
				// one child module)
				final IPath path = ClasspathDependencyUtil.getClasspathVirtualReferenceLocation(cpRefs[j]);
				final IVirtualComponent comp = (IVirtualComponent) pathToComp.get(path);
				if (comp != null) {
					// replace with a temp VirtualArchiveComponent whose IProject is set to a new pseudo name that is
					// the concatenation of all project contributions for that archive
					if (comp instanceof VirtualArchiveComponent) {
						final VirtualArchiveComponent oldComp = (VirtualArchiveComponent) comp;
						componentList.remove(comp);
						final VirtualArchiveComponent newComponent = ClassPathSelection.updateDisplayVirtualArchiveComponent(oldComp, cpRefs[j]);
						pathToComp.put(path, newComponent);
						componentList.add(newComponent);
					}
					continue;
				} else {
					pathToComp.put(path, cpRefs[j].getReferencedComponent());
				}
				componentList.add(cpRefs[j].getReferencedComponent());
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/*
	 * Because the Dependency page modifies the rows by hand, 
	 * and also because there's a Cell modifier used, we need
	 * to know the most recent change, so we reference runtimePaths
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IVirtualComponent) {
			IVirtualComponent comp = (IVirtualComponent)element;
			String name = ""; //$NON-NLS-1$
			if( columnIndex == 0 ){
				if (ClasspathDependencyUtil.isClasspathComponentDependency(comp)) {
					return ClasspathDependencyUtil.getClasspathComponentDependencyDisplayString(comp);
				}
				name = comp.getName();
				return name;
			} else if (columnIndex == 1) {
				return comp.getProject().getName();
			} else if (columnIndex == 2) {
				if( runtimePaths == null || runtimePaths.get(element) == null) {
					return new Path(PATH_SEPARATOR).toString();
				}
				return runtimePaths.get(element);
			}
		} else if (element instanceof IProject){
			if (columnIndex != 2) {
				return ((IProject)element).getName();
			} else {
				if( runtimePaths == null || runtimePaths.get(element) == null) {
					return new Path(PATH_SEPARATOR).toString();
				}
				return runtimePaths.get(element);
			}
		}		
		return null;
	}

	/*
	 * Do we want this element to be shown or not? 
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return true;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
	public void dispose() {
	}
	
	public void addListener(ILabelProviderListener listener) {
	}
	
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}
	
	public void removeListener(ILabelProviderListener listener) {
	}	

}
