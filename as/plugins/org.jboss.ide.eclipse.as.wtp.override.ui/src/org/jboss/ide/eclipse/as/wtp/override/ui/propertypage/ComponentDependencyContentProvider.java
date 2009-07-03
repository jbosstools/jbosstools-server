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

import java.io.File;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;


/*
 *  The only valid elements this content provider (should) provide
 *  are IProject or IVirtualComponent objects. The runtime paths portion is 
 *  shared with the preference page itself where they can both modify the data. 
 * 
 * This provider no longer "meddles" in to the content as it used to, 
 * but rather serves as only a view of it. 
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
			if (columnIndex == 0) {
				if( runtimePaths == null || runtimePaths.get(element) == null) {
					return new Path(PATH_SEPARATOR).toString();
				}
				return runtimePaths.get(element);
			} else if (columnIndex == 1) {
				if( comp.isBinary() && comp instanceof VirtualArchiveComponent) {
					IPath p = ((VirtualArchiveComponent)comp).getWorkspaceRelativePath();
					if( p == null )
						p = new Path(((VirtualArchiveComponent)comp).getUnderlyingDiskFile().getAbsolutePath());
					return p.toString();
				}
				return comp.getProject().getName();
			}
		} else if (element instanceof IProject){
			if (columnIndex == 0) {
				if( runtimePaths == null || runtimePaths.get(element) == null) {
					return new Path(PATH_SEPARATOR).toString();
				}
				return runtimePaths.get(element);
			} else {
				return ((IProject)element).getName();
			}
		}
		return null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
