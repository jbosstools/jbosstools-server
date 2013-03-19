/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.core.vcf;

import java.util.ArrayList;
import java.util.Arrays;

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
	 * @deprecated Please use addContainerClasspathEntry instead
	 * @see addContainerClasspathEntry
	 * @param project
	 * @param path
	 * @return
	 */
	@Deprecated
	public static IStatus addClassPath(IProject project, IPath path) {
		return addContainerClasspathEntry(project, path);
	}
	
	/**
	 * This can add any container path to a project
	 * @param project The project to add the path to
	 * @param path A container classpath path
	 * @return
	 */
	public static IStatus addContainerClasspathEntry(IProject project, IPath path) {
		IStatus status = Status.OK_STATUS;
		try {

			IClasspathEntry newClasspath = JavaCore.newContainerEntry(path);
			IJavaProject javaProject = JavaCore.create(project);

			IClasspathEntry[] oldClasspathEntries = javaProject
					.readRawClasspath();

			boolean isContainerInClassPathAlready = false;
			for (int i = 0; i < oldClasspathEntries.length
					&& !isContainerInClassPathAlready; i++) {
				if (oldClasspathEntries[i].getPath().equals(
						newClasspath.getPath())) {
					isContainerInClassPathAlready = true;
					break;
				}
			}

			if (!isContainerInClassPathAlready) {

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
	
	/**
	 * This can remove any container path from a project
	 * @param project The project to remove the path from
	 * @param path A container classpath path
	 * @return
	 */
	public static IStatus removeContainerClasspathEntry(IProject project, IPath path) {
		IStatus status = Status.OK_STATUS;
		try {

			IClasspathEntry toRemove = JavaCore.newContainerEntry(path);
			IJavaProject javaProject = JavaCore.create(project);

			IClasspathEntry[] oldClasspathEntries = javaProject.readRawClasspath();

			IClasspathEntry found = null;
			for (int i = 0; i < oldClasspathEntries.length && found == null; i++) {
				if (oldClasspathEntries[i].getPath().equals(toRemove.getPath())) {
					found = oldClasspathEntries[i];
				}
			}

			if(found != null) {
				// We found the entry; now remove it
				ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
				list.addAll(Arrays.asList(oldClasspathEntries));
				list.remove(found);
				
				IClasspathEntry[] newClasspathEntries = list.toArray(new IClasspathEntry[list.size()]);
				javaProject.setRawClasspath(newClasspathEntries, new NullProgressMonitor());
			}
		} catch (JavaModelException e) {
			status = new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, e.getLocalizedMessage(), e);
			return status;
		}

		return status;
	}


}
