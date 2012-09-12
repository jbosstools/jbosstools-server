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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.ui.PrefsInitializer;
import org.jboss.ide.eclipse.archives.ui.PrefsInitializer.IArchivesPreferenceListener;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate.WrappedProject;

/**
 * This class is the contribution to the Project Explorer.
 * It returns a wrapped project suitable for adding archives to.
 * It delegates all further content to the common provider.
 *
 * @author rob.stryker@redhat.com
 *
 */
public class ArchivesRootBridgeContentProvider
	implements ITreeContentProvider, IArchivesPreferenceListener {
	private ArchivesContentProviderDelegate delegate;
	public ArchivesRootBridgeContentProvider() {
		delegate = new ArchivesContentProviderDelegate(WrappedProject.CATEGORY);
		PrefsInitializer.addListener(this);
	}

	public Object[] getChildren(Object parentElement) {
		if( parentElement instanceof IProject) {
			if(  ((IProject)parentElement).isOpen()) {
				IPath loc = ((IProject)parentElement).getLocation();
				boolean alwaysShow = PrefsInitializer.getBoolean(PrefsInitializer.PREF_ALWAYS_SHOW_PROJECT_EXPLORER_NODE); 
				boolean fileExists = ArchivesModel.instance().canReregister(loc);
				boolean nodeExists = ArchivesModel.instance().getRoot(loc) != null;
				if( alwaysShow || fileExists || nodeExists ) {
					return new Object[] { new WrappedProject((IProject)parentElement, WrappedProject.CATEGORY) };
				}
			}
			return new Object[]{};
		}
		return delegate.getChildren(parentElement);
	}

	public Object getParent(Object element) {
		return delegate.getParent(element);
	}

	public boolean hasChildren(Object element) {
		return delegate.hasChildren(element);
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		PrefsInitializer.removeListener(this);
	}

	private Viewer viewer;
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		delegate.inputChanged(viewer, oldInput, newInput);
	}

	public void preferenceChanged(String key, boolean val) {
		viewer.refresh();
	}

	public void preferenceChanged(String key, String val) {
		viewer.refresh();
	}
}
