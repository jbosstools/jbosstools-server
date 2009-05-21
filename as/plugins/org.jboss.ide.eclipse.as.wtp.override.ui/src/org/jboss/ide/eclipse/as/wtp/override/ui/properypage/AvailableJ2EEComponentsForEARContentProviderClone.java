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
package org.jboss.ide.eclipse.as.wtp.override.ui.properypage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jst.j2ee.application.internal.operations.ClassPathSelection;
import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
import org.eclipse.jst.j2ee.internal.J2EEConstants;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.model.IEARModelProvider;
import org.eclipse.jst.j2ee.model.IModelProvider;
import org.eclipse.jst.j2ee.model.ModelProviderManager;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.jst.javaee.application.Application;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.jboss.ide.eclipse.as.wtp.override.WTPOveridePlugin;

public class AvailableJ2EEComponentsForEARContentProviderClone implements IStructuredContentProvider, ITableLabelProvider {
	
	final static String PATH_SEPARATOR = String.valueOf(IPath.SEPARATOR);
	
	private int j2eeVersion;
	private IVirtualComponent earComponent;
	private boolean isEE5 = false;
	private String libDir = null;
	
	
	public AvailableJ2EEComponentsForEARContentProviderClone(IVirtualComponent aEarComponent, int j2eeVersion) {
		super();
		this.j2eeVersion = j2eeVersion;
		earComponent = aEarComponent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (null != earComponent){
			isEE5 = J2EEProjectUtilities.isJEEProject(earComponent.getProject());
		}
		Object[] empty = new Object[0];
		if (!(inputElement instanceof IWorkspaceRoot))
			return empty;
		IProject[] projects = ((IWorkspaceRoot) inputElement).getProjects();
		if (projects == null || projects.length == 0)
			return empty;
		List validCompList = new ArrayList();
		Map pathToComp = new HashMap();
		for (int i = 0; i < projects.length; i++) {
			// get flexible project
			IProject project = projects[i];
			if(ModuleCoreNature.isFlexibleProject(project)){
				IVirtualComponent component = ComponentCore.createComponent(project);
				if (J2EEProjectUtilities.isApplicationClientProject(project) ||
						J2EEProjectUtilities.isEJBProject(project) ||
						J2EEProjectUtilities.isDynamicWebProject(project) ||
						J2EEProjectUtilities.isJCAProject(project) ||
						J2EEProjectUtilities.isUtilityProject(project) ){
					int compJ2EEVersion = J2EEVersionUtil.convertVersionStringToInt(component);
					if( compJ2EEVersion <= j2eeVersion){
						validCompList.add(component);
					} else if(isEE5){
						validCompList.add(component);
					}
				}else if(null != earComponent && J2EEProjectUtilities.isEARProject(project)){
					//find the ArchiveComponent
					if (component.equals( earComponent )) {
						if (isEE5) {
							String earDDVersion = JavaEEProjectUtilities.getJ2EEDDProjectVersion(project);
							boolean isDDVersion5 = earDDVersion.equals(J2EEVersionConstants.VERSION_5_0_TEXT) ? true : false;
							if (isDDVersion5) {
								Application app = (Application)ModelProviderManager.getModelProvider(project).getModelObject();
								if (libDir == null)
									libDir = app.getLibraryDirectory();
								if (libDir == null)
									libDir = J2EEConstants.EAR_DEFAULT_LIB_DIR;
							}
						}
						IVirtualReference[] newrefs = component.getReferences();
						for( int k=0; k< newrefs.length; k++ ){
							IVirtualReference tmpref = newrefs[k];
							IVirtualComponent referencedcomp = tmpref.getReferencedComponent();		
							boolean isBinary = referencedcomp.isBinary();
							if( isBinary ){
								if (shouldShow(referencedcomp))
									validCompList.add(referencedcomp);
							} else {
								addClasspathComponentDependencies(validCompList, pathToComp, referencedcomp);
							}
						}	
					}
				}
			} else
				try {
					if (project.exists() && project.isAccessible() && project.hasNature("org.eclipse.jdt.core.javanature") ){ //$NON-NLS-1$
						if( !project.getName().startsWith(".") ) //$NON-NLS-1$
							validCompList.add(project);
					}
				} catch (CoreException e) {
					WTPOveridePlugin.log(e);
				}
		}
		return validCompList.toArray();
	}
	
	public void setCurrentLibDir(String libDir) {
		this.libDir = libDir;
	}
	
	private boolean shouldShow(IVirtualComponent component) {
		if (!(component instanceof VirtualArchiveComponent)) 
			return true;
		
		VirtualArchiveComponent comp = (VirtualArchiveComponent)component;
		IPath p = null;
		try {
			p = comp.getProjectRelativePath();
		} catch (IllegalArgumentException e) {
			return true;
		}
		if ((p == null) && (p.segmentCount() == 0))
			return true;	
		IContainer f  = earComponent.getRootFolder().getUnderlyingFolder();
		String rootFolderName = f.getProjectRelativePath().segment(0);
		if (!p.segment(0).equals(rootFolderName)) 
			return false;
		if (p.segmentCount() == 2)
			return true;
		if (isEE5) {
			String strippedLibDir = stripSeparators(libDir);
			String[] libDirSegs = strippedLibDir.split(PATH_SEPARATOR); 
			if (p.segmentCount() - 2 != libDirSegs.length)
				return false;
			for (int i = 0; i < libDirSegs.length; i++) 
				if (!libDirSegs[i].equals(p.segment(i + 1)))
					return false;
			return true;
		}
		return false;
	}
	
	private String stripSeparators(String dir) {
		if (dir.startsWith(PATH_SEPARATOR)) 
			dir = dir.substring(1);
		if (dir.endsWith(PATH_SEPARATOR))  
			dir = dir.substring(0, dir.length() - 1);
		return dir;
	}	

	public static void addClasspathComponentDependencies(final List componentList, final Map pathToComp, final IVirtualComponent referencedComponent) {
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
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IVirtualComponent) {
			IVirtualComponent comp = (IVirtualComponent)element;
			String name = ""; //$NON-NLS-1$
			if( columnIndex == 0 ){
				if (ClasspathDependencyUtil.isClasspathComponentDependency(comp)) {
					return ClasspathDependencyUtil.getClasspathComponentDependencyDisplayString(comp);
				}
				IModelProvider provider = ModelProviderManager.getModelProvider(earComponent.getProject());
				if (provider instanceof IEARModelProvider)
				{
					name = ((IEARModelProvider)provider).getModuleURI(comp);
				}
				if( name == null || name == "" ){ //$NON-NLS-1$
					name = comp.getName();
				}
				return name;
			} else if (columnIndex == 1) {
				return comp.getProject().getName();
			} else if (columnIndex == 2) {
				return ""; //$NON-NLS-1$
			}
		} else if (element instanceof IProject){
			if (columnIndex != 2) {
				return ((IProject)element).getName();
			} else {
				return ""; //$NON-NLS-1$
			}
		}		
		return null;
	}	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		//do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		//do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 *      java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}
}
