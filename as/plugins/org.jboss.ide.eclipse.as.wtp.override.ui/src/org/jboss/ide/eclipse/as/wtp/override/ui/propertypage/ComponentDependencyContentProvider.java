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

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;


/*
 *  The only valid elements this content provider (should) provide
 *  are IProject or IVirtualComponent objects. The runtime paths portion is 
 *  shared with the preference page itself where they can both modify the data. 
 * 
 *  The pref page should initialize its data first so that this provider can 
 *  spit out the proper information.  
 */
public class ComponentDependencyContentProvider extends LabelProvider implements IStructuredContentProvider, ITableLabelProvider {
	
	final static String PATH_SEPARATOR = String.valueOf(IPath.SEPARATOR);
	
	private HashMap<Object, String> runtimePaths;
	
	public ComponentDependencyContentProvider() {
		super();
	}

	public void setRuntimePaths(HashMap<Object, String> paths) {
		this.runtimePaths = paths;
	}
	
	public Object[] getElements(Object inputElement) {
		Object[] empty = new Object[0];
		if( !(inputElement instanceof IWorkspaceRoot))
			return empty;
		return runtimePaths.keySet().toArray(); 
	}
	
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IVirtualComponent) {
			IVirtualComponent comp = (IVirtualComponent)element;
			if( columnIndex == 0 ){
				return comp.getName();
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

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
