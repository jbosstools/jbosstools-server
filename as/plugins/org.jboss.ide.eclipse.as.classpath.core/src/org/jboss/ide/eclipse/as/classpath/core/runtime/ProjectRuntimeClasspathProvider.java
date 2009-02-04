/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.classpath.core.runtime;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.common.project.facet.core.IClasspathProvider;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathConstants;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.runtime.WebtoolsProjectJBossClasspathContainerInitializer.WebtoolsProjectJBossClasspathContainer;

/**
 * This class acts as a front to add whatever entries are available 
 * in the client all runtime classpath provider, unless it's 
 * a java facet being added in which case it does the right thing.
 * @author rob
 *
 */
public class ProjectRuntimeClasspathProvider implements IClasspathProvider {
	public static final String CONTAINER_ID = "org.jboss.ide.eclipse.as.classpath.core.runtime.ProjectInitializer"; //$NON-NLS-1$
	private IRuntimeComponent rc;

	public ProjectRuntimeClasspathProvider() {
	}
	
	public ProjectRuntimeClasspathProvider(final IRuntimeComponent rc) {
		this.rc = rc;
	}

	public List getClasspathEntries(final IProjectFacetVersion fv) {
		IPath path;
		if( fv.getProjectFacet().getId().equals(ClasspathConstants.FACET_JST_JAVA) ) {
			path = new Path(CONTAINER_ID);
			path = path.append(rc.getProperty("id")); //$NON-NLS-1$
			path = path.append(fv.getProjectFacet().getId());
			path = path.append(fv.getVersionString());
			IClasspathEntry[] entries =
				new WebtoolsProjectJBossClasspathContainer(path).getClasspathEntries();
			return Arrays.asList(entries);
		} else {
			String id = rc.getProperty("id"); //$NON-NLS-1$
			IPath containerPath = new Path("org.eclipse.jst.server.core.container").append("org.jboss.ide.eclipse.as.core.server.runtime.runtimeTarget"); //$NON-NLS-1$ //$NON-NLS-2$
			path = containerPath.append(id);
		}

		IClasspathEntry cpentry = JavaCore.newContainerEntry(path);
		return Collections.singletonList(cpentry);
	}
	
	public static final class Factory implements IAdapterFactory {
		private static final Class[] ADAPTER_TYPES = { IClasspathProvider.class };

		public Object getAdapter(final Object adaptable, final Class adapterType) {
			IRuntimeComponent rc = (IRuntimeComponent) adaptable;
			return new ProjectRuntimeClasspathProvider(rc);
		}

		public Class[] getAdapterList() {
			return ADAPTER_TYPES;
		}
	}
	
	
	
	public static class RuntimeClasspathContainerInitializer extends ClasspathContainerInitializer {
		public void initialize(IPath containerPath, IJavaProject project)
				throws CoreException {
			RuntimeClasspathContainer container = new RuntimeClasspathContainer(containerPath);
			
			JavaCore.setClasspathContainer(containerPath, 
					new IJavaProject[] {project}, new IClasspathContainer[] {container}, null);
		}
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
								"Runtime " + path.segment(1) + " does not exist. This classpath container cannot be initialized. "));
		}

		public IClasspathEntry[] getClasspathEntries() {
			return new ClientAllRuntimeClasspathProvider().resolveClasspathContainer(null, rt);
		}

		public String getDescription() {
			return "All JBoss Libraries [" + (rt == null ? "null" : rt.getName()) + "]";
		}

		public int getKind() {
			return K_APPLICATION;
		}

		public IPath getPath() {
			return path;
		}
		
	}
}
