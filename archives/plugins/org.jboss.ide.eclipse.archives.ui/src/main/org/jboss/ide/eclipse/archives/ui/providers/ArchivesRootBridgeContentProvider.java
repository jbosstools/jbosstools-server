package org.jboss.ide.eclipse.archives.ui.providers;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
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
		if( parentElement instanceof IProject ) 
			return new Object[] { new WrappedProject((IProject)parentElement, WrappedProject.CATEGORY) };
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

}
