/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.modules.manifest;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlot;
import org.jboss.ide.eclipse.as.classpath.core.runtime.internal.PathProviderResolutionUtil;

public class ModulesManifestEntryContainerInitializer extends
		ClasspathContainerInitializer {
	public static final String PATH_SEGMENT = "org.jboss.ide.eclipse.as.classpath.core.runtime.modules.manifest"; //$NON-NLS-1$
	
	public ModulesManifestEntryContainerInitializer() {
	}

	@Override
	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {
		int size = containerPath.segmentCount();
		if (size > 0) {
			ModulesManifestEntryContainer container = 
					createClasspathContainer(containerPath, project);
			JavaCore.setClasspathContainer(containerPath,
				new IJavaProject[] { project },
				new IClasspathContainer[] { container }, 
				null);
		}
	}
	protected ModulesManifestEntryContainer createClasspathContainer(
			IPath path, IJavaProject jp) {
		return new ModulesManifestEntryContainer(path, jp.getProject());
	}
	
	
	public static class ModulesManifestEntryContainer
		implements IClasspathContainer  {
		private IRuntime rt;
		private IPath path;
		private IProject p;
		
		public ModulesManifestEntryContainer(IPath path, IProject p) {
			this(getRuntime(path), p);
		}
		public ModulesManifestEntryContainer(IRuntime rt, IProject p) {
			this.rt = rt;
			this.p = p;
		}
		
		protected static IRuntime getRuntime(IPath p) {
			String rtName = p.segmentCount() > 1 ? p.segment(1) : null;
			if( rtName != null ) {
				IRuntime rt = ServerCore.findRuntime(rtName);
				return rt;
			}
			return null;
		}
		@Override
		public IClasspathEntry[] getClasspathEntries() {
			if( rt != null ) {
				IRuntimePathProvider[] sets = getRuntimePathProviders();
				IPath[] paths = PathProviderResolutionUtil.getAllPaths(rt, sets);
				return PathProviderResolutionUtil.getClasspathEntriesForResolvedPaths(paths);
			}
			// We have no runtime to resolve against
			return new IClasspathEntry[0];
		}
		
		public IRuntimePathProvider[] getRuntimePathProviders() {
			System.out.println("Inside getRuntimePathProviders");
			ModuleSlot[] all = new ModuleSlotManifestUtil().getAllModuleSlots(p);
			System.out.println("List of slits has length " + (all == null ? 0 : all.length));
			IRuntimePathProvider[] sets = ModuleSlotManifestUtil.moduleSlotsAsProviders(all);
			return sets;
		}
		
		
		@Override
		public String getDescription() {
			return "JBoss Modules - Manifest Entries"; //$NON-NLS-1$
		}

		@Override
		public int getKind() {
			return IClasspathContainer.K_APPLICATION;
		}

		@Override
		public IPath getPath() {
			return path;
		}
	}
}
