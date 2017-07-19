/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.internal.Messages;

/**
 * As best as I can discover, this class initializes a classpath
 * container for any path that starts with 
 *    org.jboss.ide.eclipse.as.classpath.core.runtime.ProjectRuntimeInitializer
 *    
 * I am unable to figure out if this path is from a legacy
 * version of JBossTools that must continue to function, or, 
 * if the extension point has simply been misused.
 * 
 * With this in mind, I have extracted out this initializer
 * and commented it without changing any of its behavior. 
 * 
 * THe behavior of this class is that it simply acts as a container
 * front for the "client-all" classpath container, returning
 * the full set of jars possible. 
 * 
 * This behavior should not be changed, or it could break
 * legacy workspaces where this classpath container path is 
 * possibly used. 
 * 
 */
public class UnknownLegacyClasspathContainerInitializer extends ClasspathContainerInitializer {
	
	/**
	 * This is the container path prefix that this class
	 * is equipped to handle via extension point. 
	 * 
	 * Any project that has a .classpath entry
	 * that includes this string will come to this class
	 * to resolve the entries. 
	 */
	public static final String CONTAINER_PATH_PREFIX = 
			"org.jboss.ide.eclipse.as.classpath.core.runtime.ProjectRuntimeInitializer"; //$NON-NLS-1$
	
	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {
		RuntimeClasspathContainer container = new RuntimeClasspathContainer(containerPath);
		
		JavaCore.setClasspathContainer(containerPath, 
				new IJavaProject[] {project}, new IClasspathContainer[] {container}, null);
	}
	
	public static class RuntimeClasspathContainer implements IClasspathContainer {
		private IPath path;
		private IRuntime rt;

		public RuntimeClasspathContainer(IPath path) throws CoreException {
			this.path = path;
			this.rt = ServerCore.findRuntime(path.segment(1));
			if( rt == null ) 
				throw new CoreException(
						new Status( IStatus.ERROR,  ClasspathCorePlugin.PLUGIN_ID, 
							Messages.bind(Messages.ProjectRuntimeClasspathProvider_runtime_does_not_exist,
							path.segment(1))));
		}

		public IClasspathEntry[] getClasspathEntries() {
			return new ProjectRuntimeClasspathProvider().resolveClasspathContainer(null, rt);
		}

		public String getDescription() {
			return Messages.bind(Messages.ProjectRuntimeClasspathProvider_all_jboss_libraries_description, (rt == null ? "null" : rt.getName())); //$NON-NLS-1$
		}

		public int getKind() {
			return K_APPLICATION;
		}

		public IPath getPath() {
			return path;
		}
		
	}
	
}