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

public class ManifestChangeListener implements IResourceChangeListener {
	public static void register() {
		final ManifestChangeListener listener = new ManifestChangeListener();
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		ws.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_BUILD);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		final ArrayList<IProject> projectsWithChangedManifest = new ArrayList<IProject>();
		try {
			delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					IProject p = delta.getResource().getProject();
					if( !projectsWithChangedManifest.contains(p)) {
						String name = delta.getResource().getName();
						if (name.toLowerCase().equals("manifest.mf")) {
							projectsWithChangedManifest.add(p);
						}
						return true;
					} else {
						return false;
					}
				}
			});
		} catch (CoreException ce) {

		}
		
		IProject[] asArr = projectsWithChangedManifest.toArray(new IProject[projectsWithChangedManifest.size()]);
		for( int i = 0; i < asArr.length; i++ ) {
			resetProject(asArr[i]);
		}
	}

	
	private void resetProject(IProject p) {
		if (isJavaProject(p)) {
			// Update p's classpath
			IJavaProject jp = JavaCore.create(p);
			try {
				IClasspathEntry[] raw = jp.getRawClasspath();
				for( int i = 0; i < raw.length; i++ ) {
					if( ProjectRuntimeClasspathProvider.CONTAINER_PATH.isPrefixOf(raw[i].getPath())) {
						try {
							// Delegate to wtp to re-initialize this container
							new RuntimeClasspathContainerInitializer().initialize(raw[i].getPath(), jp);
						} catch(CoreException ce) {
							ce.printStackTrace();
						}
					}
				}
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
