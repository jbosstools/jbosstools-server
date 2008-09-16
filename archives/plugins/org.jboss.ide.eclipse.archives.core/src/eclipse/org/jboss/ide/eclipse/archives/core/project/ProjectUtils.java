package org.jboss.ide.eclipse.archives.core.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

public class ProjectUtils {
	
	public static boolean addProjectNature(IPath path) {
		String loc = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		if( path.toOSString().startsWith(loc)) {
			String proj = path.toOSString().substring(loc.length()+1);
			IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(proj);
			return addProjectNature(p, ArchivesNature.NATURE_ID);
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
