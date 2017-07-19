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
package org.jboss.ide.eclipse.as.classpath.core.runtime.internal;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.server.core.internal.RuntimeClasspathContainerInitializer;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.runtime.modules.manifest.ModuleSlotManifestUtil;

public class ManifestChangeListener implements IResourceChangeListener {
	private static ManifestChangeListener listener;
	public static void register() {
		try {
			listener = new ManifestChangeListener();
			IWorkspace ws = ResourcesPlugin.getWorkspace();
			ws.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_BUILD);
		} catch(Exception e) {
			ClasspathCorePlugin.log("Unable to add manifest change listener", e); //$NON-NLS-1$
		}
	}

	public static void deregister() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		ws.removeResourceChangeListener(listener);
	}
	
	protected String getFileName() {
		return "manifest.mf"; //$NON-NLS-1$
	}
	
	protected void ensureInCache(IFile f) {
		new ModuleSlotManifestUtil().ensureInCache(f);
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		final ArrayList<IFile> changedManifests = new ArrayList<IFile>();
		final ArrayList<IProject> changedProjects = new ArrayList<IProject>();
		try {
			delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					String name = delta.getResource().getName();
					if (name.toLowerCase().equals(getFileName())) {
						if( delta.getResource() instanceof IFile) {
							changedManifests.add((IFile)delta.getResource());
						}
						if( !changedProjects.contains(delta.getResource().getProject())) {
							changedProjects.add(delta.getResource().getProject());
						}
					}
					return true;
				}
			});
		} catch (CoreException ce) {

		}
		// ensure in project cache
		IFile[] asArr = changedManifests.toArray(new IFile[changedManifests.size()]);
		for( int i = 0; i < asArr.length; i++ ) {
			ensureInCache(asArr[i]);
		}
		
		// reset classpath containers for affected projects
		IProject[] asArr2 = changedProjects.toArray(new IProject[changedProjects.size()]);
		for( int i = 0; i < asArr2.length; i++ ) {
			resetContainer(asArr2[i]);
		}
	}

	
	private void resetContainer(IProject p) {
		if (isJavaProject(p.getProject())) {
			// Update p's classpath
			IJavaProject jp = JavaCore.create(p.getProject());
			try {
				IClasspathEntry[] raw = jp.getRawClasspath();
				for( int i = 0; i < raw.length; i++ ) {
					if( ProjectRuntimeClasspathProvider.CONTAINER_PATH.isPrefixOf(raw[i].getPath())) {
						try {
							// Delegate to wtp to re-initialize this container
							new RuntimeClasspathContainerInitializer().initialize(raw[i].getPath(), jp);
						} catch(CoreException ce) {
							// Ignore, not critical
						}
					}
				}
			} catch (JavaModelException e) {
				//Not a java project, we don't care
			}
		}
	}
	
	private static boolean isJavaProject(final IProject pj) {
		try {
			return pj.getNature(JavaCore.NATURE_ID) != null;
		} catch (CoreException e) {
			return false;
		}
	}
}
