package org.jboss.ide.eclipse.as.wtp.ui.propertypage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.wtp.ui.propertypage.DependencyPageExtensionManager.ReferenceExtension;

public class NewReferenceRootWizardFragment extends WizardFragment {
	protected Map<String, WizardFragment> fragmentMap = 
		new HashMap<String, WizardFragment>();
	private IWizardHandle wizard;
	private TreeViewer viewer;
	private ReferenceExtension[] extensions = null;
	public NewReferenceRootWizardFragment(ReferenceExtension[] extensions) {
		if( extensions == null )
			this.extensions = DependencyPageExtensionManager.getManager().getReferenceExtensions();
		else
			this.extensions = extensions;
	}
	public boolean hasComposite() {
		return true;
	}

	public Composite createComposite(Composite parent, IWizardHandle wizard) {
		this.wizard = wizard;
		wizard.setTitle("Select a reference type");
		wizard.setDescription("Here you can select one of many types of references to add");
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FillLayout());
		viewer = new TreeViewer(c, SWT.SINGLE | SWT.BORDER);
		viewer.setLabelProvider(getLabelProvider());
		viewer.setContentProvider(getContentProvider());
		viewer.setInput(ResourcesPlugin.getWorkspace());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				viewerSelectionChanged();
			}
		});
		return c;
	}

	protected void viewerSelectionChanged() {
		wizard.update();
	}
	
	protected WizardFragment getWizardFragment(String extensionPointID) {
		try {
			WizardFragment fragment = fragmentMap.get(extensionPointID);
			if (fragment != null)
				return fragment;
		} catch (Exception e) {
			// ignore
		}
		
		WizardFragment fragment = DependencyPageExtensionManager.getManager().loadReferenceWizardFragment(extensionPointID);
		if (fragment != null)
			fragmentMap.put(extensionPointID, fragment);
		return fragment;
	}

	public List getChildFragments() {
		List<WizardFragment> listImpl = new ArrayList<WizardFragment>();
		createChildFragments(listImpl);
		return listImpl;
	}

	protected void createChildFragments(List<WizardFragment> list) {
		// Instantiate and add the fragment for the current ID
		if( viewer != null ) {
			IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
			ReferenceExtension selected = (ReferenceExtension)sel.getFirstElement();
			if( selected != null ) {
				WizardFragment child = getWizardFragment(selected.getId());
				if( child != null )
					list.add(child);
			}
		}
	}

	public boolean isComplete() {
		return true;
	}

	
	private LabelProvider labelProvider = null;
	private ITreeContentProvider contentProvider = null;
	protected LabelProvider getLabelProvider() {
		if( labelProvider == null ) {
			labelProvider = new LabelProvider() {
				public Image getImage(Object element) {
					if( element instanceof ReferenceExtension)
						return ((ReferenceExtension)element).getImage();
					return null;
				}
				public String getText(Object element) {
					if( element instanceof ReferenceExtension)
						return ((ReferenceExtension)element).getName();
					return element == null ? "" : element.toString();//$NON-NLS-1$
				}
			   public void dispose() {
			    	super.dispose();
			    	if( extensions != null ) {
			    		for( int i = 0; i < extensions.length; i++) {
			    			extensions[i].disposeImage();
			    		}
			    	}
			    }
			};
		}
		return labelProvider;
	}
	
	protected ITreeContentProvider getContentProvider() {
		if( contentProvider == null ) {
			contentProvider = new ITreeContentProvider() {
				public Object[] getElements(Object inputElement) {
					return extensions;
				}
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
				public void dispose() {
				}
				public boolean hasChildren(Object element) {
					return false;
				}
				public Object getParent(Object element) {
					return null;
				}
				public Object[] getChildren(Object parentElement) {
					return null;
				}
			};
		}
		return contentProvider;
	}
}
