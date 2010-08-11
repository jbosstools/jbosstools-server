/******************************************************************************* 
 * Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.vcf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;

public class ExportedClasspathFoldersVirtualComponent extends AbstractFilesetVirtualComponent {
	public ExportedClasspathFoldersVirtualComponent(IProject p, IVirtualComponent referencingComponent) {
		super(p,referencingComponent);
	}

	protected String getFirstIdSegment() {
		return ExportedClassFolderReferenceResolver.OUTPUT_FOLDER_PROTOCOL;
	}

	protected IResource[] getLooseResources() {
		return new IResource[]{};
	}
	
	protected IContainer[] getUnderlyingContainers() {
		IJavaProject jp = JavaCore.create(project);
		IClasspathEntry[] entries = findAllClassFolderEntries(jp);
		ArrayList<IContainer> results = new ArrayList<IContainer>();
		for( int i = 0; i < entries.length; i++ ) {
			IClasspathAttribute attribute = ClasspathDependencyUtil.checkForComponentDependencyAttribute(
					entries[i],
					ClasspathDependencyUtil.DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY);
			
			if( attribute != null ) {
				final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(entries[i].getPath());
				if (resource != null && resource instanceof IContainer ) {
					results.add((IContainer)resource);
				}
			}
		}
		return results.toArray(new IContainer[results.size()]);
	}
	
	protected IClasspathEntry[] findAllClassFolderEntries(IJavaProject javaProject) {
		List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		try {
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			for( int i = 0; i < entries.length; i++ ) {
				if( ClasspathDependencyUtil.isClassFolderEntry(entries[i]))
					list.add(entries[i]);
			}
		} catch( CoreException ce) {
		} 
		return list.toArray(new IClasspathEntry[list.size()]);
	}
}
