/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.ide.eclipse.as.core.eap.xp;

import org.apache.maven.model.Plugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.jboss.tools.common.jdt.core.buildpath.ClasspathContainersHelper;

public class ProjectUtils {
	public static Object getSelectedProject(Object selectedElement) {
		Object result = null;
		if (selectedElement instanceof IResource) {
			result = ((IResource) selectedElement).getProject();
		}
		return result;
	}
	
	public static boolean projectExists(String name) {
		return (name != null) 
				&& !"".equals(name) 
				&& ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists();
	}
	
	public static String getProjectLocationDefault() {
		return ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString();
	}
	
	public static boolean isJavaProject(IProject project) {
		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			return false;
		}
	}
	
	public static boolean isMavenProject(IProject project) {
		try {
			return project.hasNature("org.eclipse.m2e.core.maven2Nature");
		} catch (CoreException e) {
			return false;
		}
	}
	
	public static boolean isJBossXpProject(IProject project) {
		return isJavaProject(project) && isMavenProject(project) && isJBossXpProject(JavaCore.create(project));
	}

	public static boolean isJBossXpProject(IJavaProject javaProject) {
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().create( javaProject.getProject(), new NullProgressMonitor() );
	    try {
	    	if( facade == null ) {
	    		return false;
	    	}
			Plugin plugin = facade.getMavenProject(new NullProgressMonitor()).getPlugin("org.wildfly.plugins:wildfly-jar-maven-plugin");
			if (plugin != null) {
				Object o = plugin.getConfiguration();
	    		return true;
			}
	    } catch( Exception e ) {
	    	
	    }
	    return false;
	}

	/**
	 * @param project the Eclipse project
	 * @return the path to the JRE/JDK attached to the project
	 * @throws CoreException 
	 */
	public static String getJREEntry(IProject project) throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		for(IClasspathEntry cpe : javaProject.getRawClasspath()) {
			if (cpe.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				IClasspathContainer container = JavaCore.getClasspathContainer(cpe.getPath(), javaProject);
				if (ClasspathContainersHelper.applies(container, ClasspathContainersHelper.JRE_CONTAINER_ID)) {
					return cpe.getPath().toPortableString();
					
				}
			}
		}
		return null;
	}
}
