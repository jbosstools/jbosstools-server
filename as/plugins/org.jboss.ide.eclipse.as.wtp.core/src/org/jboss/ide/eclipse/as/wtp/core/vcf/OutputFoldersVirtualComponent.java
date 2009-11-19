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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;

public class OutputFoldersVirtualComponent extends AbstractFilesetVirtualComponent {

	public OutputFoldersVirtualComponent(IProject p, IVirtualComponent referencingComponent) {
		super(p, referencingComponent);
	}
	
	protected String getFirstIdSegment() {
		return OutputFolderReferenceResolver.OUTPUT_FOLDER_PROTOCOL;
	}

	protected IResource[] getLooseResources() {
		return new IResource[]{};
	}

	protected IContainer[] getUnderlyingContainers() {
		List<IContainer> result = new ArrayList<IContainer>();
		try {
			if (!project.hasNature(JavaCore.NATURE_ID))
				return new IContainer[] {};
		} catch (Exception e) {
		}
		IPackageFragmentRoot[] sourceContainers = getSourceContainers(project);
		for (int i = 0; i < sourceContainers.length; i++) {
			IContainer outputFolder = J2EEProjectUtilities.getOutputContainer(project, sourceContainers[i]);
			if (outputFolder != null && !result.contains(outputFolder))
				result.add(outputFolder);
		}
		return result.toArray(new IContainer[result.size()]);
	}

	public static IPackageFragmentRoot[] getSourceContainers(IProject project) {
		IJavaProject jProject = JavaCore.create(project);
		if (jProject == null)
			return new IPackageFragmentRoot[0];
		List<IPackageFragmentRoot> list = new ArrayList<IPackageFragmentRoot>();
		IPackageFragmentRoot[] roots;
		try {
			roots = jProject.getPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].getKind() != IPackageFragmentRoot.K_SOURCE)
					continue;
				list.add(roots[i]);
			}
		} catch( JavaModelException jme ) {
		}
		return list.toArray(new IPackageFragmentRoot[list.size()]);
	}
}
