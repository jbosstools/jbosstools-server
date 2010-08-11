package org.jboss.ide.eclipse.as.wtp.ui.propertypage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.SharedImages;
import org.eclipse.wst.common.componentcore.internal.DependencyType;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.wtp.core.vcf.OutputFoldersVirtualComponent;

public class OutputFolderReferenceWizardFragment extends WizardFragment implements IReferenceEditor {

	protected TreeViewer viewer;
	protected IPath[] paths;
	protected IWizardHandle handle;
	protected IProject selected = null;
	public OutputFolderReferenceWizardFragment() {
		super();
	}

	public boolean hasComposite() {
		return true;
	}
	
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		handle.setTitle("Add output folders");
		handle.setDescription("This reference will add in all source folder outputs from the selected project.");

		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FormLayout());
		viewer = new TreeViewer(c, SWT.SINGLE | SWT.BORDER);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(getLabelProvider());
		viewer.setInput(ResourcesPlugin.getWorkspace());

		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.top = new FormAttachment(0, 5);
		fd.right = new FormAttachment(100, -5);
		fd.bottom = new FormAttachment(100, -5);
		viewer.getTree().setLayoutData(fd);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
				selected = (IProject)sel.getFirstElement();
			}
		});
		
		IVirtualComponent vc = (IVirtualComponent)getTaskModel().getObject(NewReferenceWizard.COMPONENT);
		if( vc != null )
			selected = vc.getProject();
		return c;
	}
	
	private ITreeContentProvider getContentProvider() {
		return new ITreeContentProvider() {
			public Object[] getElements(Object inputElement) {
				return ResourcesPlugin.getWorkspace().getRoot().getProjects();
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
	
	private LabelProvider getLabelProvider() {
		return new LabelProvider() {
			public Image getImage(Object element) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
			}
			public String getText(Object element) {
				return element instanceof IProject ? ((IProject)element).getName() : element.toString();
			}
		};
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IVirtualComponent parentComp = (IVirtualComponent)getTaskModel().getObject(NewReferenceWizard.ROOT_COMPONENT);
		selected = selected == null ? parentComp.getProject() : selected;
		OutputFoldersVirtualComponent vc = new OutputFoldersVirtualComponent(selected, parentComp);
		getTaskModel().putObject(NewReferenceWizard.COMPONENT, vc);
		getTaskModel().putObject(NewReferenceWizard.DEPENDENCY_TYPE, DependencyType.CONSUMES_LITERAL);
		String s = 	(String)getTaskModel().getObject(NewReferenceWizard.COMPONENT_PATH);
		if( s == null ) 
			getTaskModel().putObject(NewReferenceWizard.COMPONENT_PATH, "/");
	}

	public boolean canEdit(IVirtualComponent vc) {
		if( vc instanceof OutputFoldersVirtualComponent )
			return true;
		return false;
	}
}
