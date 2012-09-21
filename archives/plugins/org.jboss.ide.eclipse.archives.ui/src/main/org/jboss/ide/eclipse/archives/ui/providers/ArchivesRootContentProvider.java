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
package org.jboss.ide.eclipse.archives.ui.providers;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jboss.ide.eclipse.archives.ui.PrefsInitializer;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate.WrappedProject;
import org.jboss.ide.eclipse.archives.ui.views.ProjectArchivesCommonView;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class ArchivesRootContentProvider implements ITreeContentProvider {
	public static final Object NO_PROJECT = new Object();

	private ArchivesContentProviderDelegate delegate;
	public ArchivesRootContentProvider() {
		delegate = new ArchivesContentProviderDelegate(WrappedProject.NAME);
	}

	public Object[] getChildren(Object parentElement) {
		return delegate.getChildren(parentElement);
	}

	public Object getParent(Object element) {
		return delegate.getParent(element);
	}

	public boolean hasChildren(Object element) {
		return delegate.hasChildren(element);
	}

	public Object[] getElements(Object inputElement) {
		IProject cp = ProjectArchivesCommonView.getInstance().getCurrentProject();
		if( showProjectRoot() ) {
			if( showAllProjects() ) {
				IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				ArrayList<IProject> tmp = new ArrayList<IProject>();
				for( int i = 0; i < projects.length; i++ ) {
					if( projects[i].isAccessible())
						tmp.add(projects[i]);
				}
				return wrap((IProject[]) tmp.toArray(new IProject[tmp.size()]));
			}
			if( cp != null && cp.isAccessible())
				return wrap(new IProject[]{cp});
		} else if( cp != null ){
			return getChildren(new WrappedProject(cp, WrappedProject.NAME));
		}
		return new Object[]{NO_PROJECT};
	}

	protected Object[] wrap(IProject[] objs) {
		WrappedProject[] projs = new WrappedProject[objs.length];
		for( int i = 0; i < projs.length; i++)
			projs[i] = new WrappedProject(objs[i], WrappedProject.NAME);
		return projs;
	}

	public void dispose() {
		delegate.dispose();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		delegate.inputChanged(viewer, oldInput, newInput);
	}

	private boolean showProjectRoot () {
		return PrefsInitializer.getBoolean(PrefsInitializer.PREF_SHOW_PROJECT_ROOT);
	}
	private boolean showAllProjects () {
		return PrefsInitializer.getBoolean(PrefsInitializer.PREF_SHOW_ALL_PROJECTS);
	}
}
