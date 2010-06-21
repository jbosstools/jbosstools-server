/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.core.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

public class ProjectUtils {
	
	public static boolean addProjectNature(IPath path) {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for( int i = 0; i < allProjects.length; i++ ) {
			if( allProjects[i].getLocation().equals(path)) {
				return addProjectNature(allProjects[i], ArchivesNature.NATURE_ID);
			}
		}
		return false;
	}

	   public static boolean addProjectNature(IProject project, String natureId) {
		   boolean added = false;
		   try {
			   if (project != null && natureId != null) {
				   IProjectDescription desc = project.getDescription();
				   
				   if (!project.hasNature(natureId)) {
					   String natureIds[] = desc.getNatureIds();
					   String newNatureIds[] = new String[natureIds.length + 1];
					   
					   System.arraycopy(natureIds, 0, newNatureIds, 1, natureIds.length);
					   newNatureIds[0] = natureId;
					   desc.setNatureIds(newNatureIds);
					   
					   project.getProject().setDescription(desc, new NullProgressMonitor());
					   added = true;
				   }
			   }
		   } catch (CoreException e) {
			   e.printStackTrace();
		   }
		   return added;
	   }

}
