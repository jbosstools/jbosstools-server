package org.jboss.ide.eclipse.as.wtp.core.vcf;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

public class VCFClasspathCommand {
	/**
	 * This can add *any* container path
	 * @param project
	 * @param path
	 * @return
	 */
	public static IStatus addClassPath(IProject project, IPath path) {
		IStatus status = Status.OK_STATUS;
		try {

			IClasspathEntry newClasspath;
			IJavaProject javaProject = JavaCore.create(project);
			newClasspath = JavaCore.newContainerEntry(path);

			IClasspathEntry[] oldClasspathEntries = javaProject
					.readRawClasspath();

			boolean isFolderInClassPathAlready = false;
			for (int i = 0; i < oldClasspathEntries.length
					&& !isFolderInClassPathAlready; i++) {
				if (oldClasspathEntries[i].getPath().equals(
						newClasspath.getPath())) {
					isFolderInClassPathAlready = true;
					break;
				}
			}

			if (!isFolderInClassPathAlready) {

				IClasspathEntry[] newClasspathEntries = new IClasspathEntry[oldClasspathEntries.length + 1];
				for (int i = 0; i < oldClasspathEntries.length; i++) {
					newClasspathEntries[i] = oldClasspathEntries[i];
				}
				newClasspathEntries[oldClasspathEntries.length] = newClasspath;
				javaProject.setRawClasspath(newClasspathEntries,
						new NullProgressMonitor());
			}
		} catch (JavaModelException e) {
			status = new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, e.getLocalizedMessage(), e);
			return status;
		}

		return status;
	}


}
