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

package org.jboss.ide.eclipse.as.classpath.core.runtime;

import java.text.MessageFormat;
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
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathConstants;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.Messages;
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
		IPath path = null;
		if( fv.getProjectFacet().getId().equals(ClasspathConstants.FACET_JST_JAVA) ) {
			path = new Path(CONTAINER_ID);
			path = path.append(rc.getProperty("id")); //$NON-NLS-1$
			path = path.append(fv.getProjectFacet().getId());
			path = path.append(fv.getVersionString());
			IClasspathEntry[] entries =
				new WebtoolsProjectJBossClasspathContainer(path).getClasspathEntries();
			return Arrays.asList(entries);
		} else if( isPrimaryFacet(fv.getProjectFacet())) {
			String id = rc.getProperty("id"); //$NON-NLS-1$
			IPath containerPath = new Path("org.eclipse.jst.server.core.container").append("org.jboss.ide.eclipse.as.core.server.runtime.runtimeTarget"); //$NON-NLS-1$ //$NON-NLS-2$
			path = containerPath.append(id);
		}
		if( path != null ) {
			IClasspathEntry cpentry = JavaCore.newContainerEntry(path);
			return Collections.singletonList(cpentry);
		}
		return Collections.emptyList();
	}
	
	// Bad name, I know, but checks if this is 
	// an ear, war, ejb, or other top level facet
	protected boolean isPrimaryFacet(IProjectFacet facet) {
		WebtoolsProjectJBossClasspathContainerInitializer del = new WebtoolsProjectJBossClasspathContainerInitializer();
		return facet.equals(del.WEB_FACET) 
			|| facet.equals(del.EJB_FACET) 
			|| facet.equals(del.EAR_FACET) 
			|| facet.equals(del.CONNECTOR_FACET) 
			|| facet.equals(del.APP_CLIENT_FACET);
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
								MessageFormat
										.format(
												Messages.ProjectRuntimeClasspathProvider_runtime_does_not_exist,
												path.segment(1))));
		}

		public IClasspathEntry[] getClasspathEntries() {
			return new ClientAllRuntimeClasspathProvider().resolveClasspathContainer(null, rt);
		}

		public String getDescription() {
			return MessageFormat.format(Messages.ProjectRuntimeClasspathProvider_all_jboss_libraries_description, (rt == null ? "null" : rt.getName())); //$NON-NLS-1$
		}

		public int getKind() {
			return K_APPLICATION;
		}

		public IPath getPath() {
			return path;
		}
		
	}
}
