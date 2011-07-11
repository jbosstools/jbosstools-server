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

package org.jboss.ide.eclipse.as.classpath.core.runtime;

import java.util.ArrayList;
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
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.common.project.facet.core.IClasspathProvider;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;

/**
 * This class acts as a front to add whatever entries are available 
 * in the client all runtime classpath provider, unless it's 
 * a java facet being added in which case it does the right thing.
 * @author rob
 *
 */
public class ProjectRuntimeClasspathProvider implements IClasspathProvider {
	private IRuntimeComponent rc;

	public ProjectRuntimeClasspathProvider() {
	}
	
	public ProjectRuntimeClasspathProvider(final IRuntimeComponent rc) {
		this.rc = rc;
	}

	public List<IClasspathEntry> getClasspathEntries(final IProjectFacetVersion fv) {
		if( fv.getProjectFacet().equals(JavaFacet.FACET)) {
			return getJavaClasspathEntries();
		} else if( isPrimaryFacet(fv.getProjectFacet()) || isSecondaryFacet(fv.getProjectFacet())) {
			return getClientAllClasspathEntry();
		}
		return Collections.emptyList();
	}
	
	private List<IClasspathEntry> getJavaClasspathEntries() {
		String runtimeId = rc.getProperty("id"); //$NON-NLS-1$
		if( runtimeId != null ) {
			IRuntime runtime = ServerCore.findRuntime(runtimeId);
			if( runtime != null ) {
				IJBossServerRuntime  jbsRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, null);
				IVMInstall vmInstall = jbsRuntime.getVM();
				if (vmInstall != null) {
					String name = vmInstall.getName();
					String typeId = vmInstall.getVMInstallType().getId();
					IClasspathEntry[] entries = new IClasspathEntry[] { 
							JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER)
								.append(typeId).append(name)) 
					};
					return Arrays.asList(entries);
				}
			}
		}
		return new ArrayList<IClasspathEntry>();
	}
	
	private List<IClasspathEntry> getClientAllClasspathEntry() {
		String id = rc.getProperty("id"); //$NON-NLS-1$
		IPath containerPath = new Path("org.eclipse.jst.server.core.container") //$NON-NLS-1$
			.append("org.jboss.ide.eclipse.as.core.server.runtime.runtimeTarget"); //$NON-NLS-1$
		IClasspathEntry cpentry = JavaCore.newContainerEntry(containerPath.append(id));
		return Collections.singletonList(cpentry);
	}
	
	// Bad name, I know, but checks if this is 
	// an ear, war, ejb, or other top level facet
	protected boolean isPrimaryFacet(IProjectFacet facet) {
		return facet.getId().equals(IWTPConstants.FACET_WEB)
			|| facet.getId().equals(IWTPConstants.FACET_EJB)
			|| facet.getId().equals(IWTPConstants.FACET_EAR)
			|| facet.getId().equals(IWTPConstants.FACET_CONNECTOR)
			|| facet.getId().equals(IWTPConstants.FACET_APP_CLIENT);
	}

	// Also a bad name, but facets the server automatically knows
	// how to provide classpath entries for
	protected boolean isSecondaryFacet(IProjectFacet facet) {
		return facet.getId().equals(IWTPConstants.FACET_JSF)
			|| facet.getId().equals(IWTPConstants.FACET_JPA); 
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
							Messages.bind(Messages.ProjectRuntimeClasspathProvider_runtime_does_not_exist,
							path.segment(1))));
		}

		public IClasspathEntry[] getClasspathEntries() {
			return new ClientAllRuntimeClasspathProvider().resolveClasspathContainer(null, rt);
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
