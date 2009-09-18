package org.jboss.ide.eclipse.as.wtp.ui.propertypage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
import org.eclipse.jst.j2ee.internal.classpathdep.UpdateClasspathAttributesDataModelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.SharedImages;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.wtp.core.vcf.ExportedClasspathFoldersVirtualComponent;

public class ExportedClasspathFoldersReferenceWizardFragment extends
		WizardFragment {

	protected TreeViewer viewer;
	protected IPath[] paths;
	protected IWizardHandle handle;
	protected IStructuredSelection selected = null;
	protected IStructuredSelection initialSelection = null;
	protected Object[] allElements;
	public ExportedClasspathFoldersReferenceWizardFragment() {
		super();
	}

	public boolean hasComposite() {
		return true;
	}
	
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		int x = 1;
		handle.setTitle("Add exported folders from the build pathx");
		handle.setDescription("This reference will allow you to add references to exported classpaths from this project's build page. Please select all entries you wish to be published.");
		IVirtualComponent parentComp = (IVirtualComponent)getTaskModel().getObject(NewReferenceWizard.ROOT_COMPONENT);
		IProject project = parentComp.getProject();
		IJavaProject javaProject = JavaCore.create(project);
		allElements = findAllClassFolderEntries(javaProject);

		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FormLayout());
		viewer = new TreeViewer(c, SWT.MULTI | SWT.BORDER);
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
				selected = (IStructuredSelection)viewer.getSelection();
			}
		});
		
		ArrayList<IClasspathEntry> withTag = new ArrayList<IClasspathEntry>();
		for( int i = 0; i < allElements.length; i++ ) {
			IClasspathAttribute attribute = ClasspathDependencyUtil.checkForComponentDependencyAttribute(
					((IClasspathEntry)allElements[i]),
					ClasspathDependencyUtil.DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY);
			
			if( attribute != null )
				withTag.add((IClasspathEntry)allElements[i]);
		}
		
		StructuredSelection newSel = new StructuredSelection(withTag);
		viewer.setSelection(newSel);
		initialSelection = newSel;
		return c;
	}
	
	protected IClasspathEntry[] findAllClassFolderEntries(IJavaProject javaProject) {
		List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		try {
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			for( int i = 0; i < entries.length; i++ ) {
				if( ClasspathDependencyUtil.isClassFolderEntry(entries[i]))
					list.add(entries[i]);
			}
		} catch( CoreException ce) {
		} 
		return list.toArray(new IClasspathEntry[list.size()]);
	}
	
	private ITreeContentProvider getContentProvider() {
		return new ITreeContentProvider() {
			public Object[] getElements(Object inputElement) {
				IVirtualComponent parentComp = (IVirtualComponent)getTaskModel().getObject(NewReferenceWizard.ROOT_COMPONENT);
				IProject project = parentComp.getProject();
				IJavaProject jp = JavaCore.create(project);
				if( jp != null ) {
					return findAllClassFolderEntries(jp);
				}
				return new Object[]{};
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
		try {
			Map<IClasspathEntry, IPath> toAdd = new HashMap<IClasspathEntry, IPath>();
			Map<IClasspathEntry, IPath> toRemove = new HashMap<IClasspathEntry, IPath>();

			List pre = Arrays.asList(initialSelection.toArray());
			List post = Arrays.asList(selected.toArray());
			for( int i = 0; i < allElements.length; i++ ) {
				if( pre.contains(allElements[i]) && !post.contains(allElements[i])) {
					toRemove.put((IClasspathEntry)allElements[i], getOriginalPath((IClasspathEntry)allElements[i]));
				} 
				else if( !pre.contains(allElements[i]) && post.contains(allElements[i])) {
					toAdd.put((IClasspathEntry)allElements[i], getNewPath((IClasspathEntry)allElements[i]));	
				}
			}
			
			UpdateClasspathAttributesDataModelProvider provider = new UpdateClasspathAttributesDataModelProvider();
			IDataModel model = DataModelFactory.createDataModel(provider);
			model.setProperty(UpdateClasspathAttributesDataModelProvider.PROJECT_NAME, parentComp.getProject().getName());
			model.setProperty(UpdateClasspathAttributesDataModelProvider.ENTRIES_TO_ADD_ATTRIBUTE, toAdd);
			model.getDefaultOperation().execute(new NullProgressMonitor(), null);

			provider = new UpdateClasspathAttributesDataModelProvider();
			model = DataModelFactory.createDataModel(provider);
			model.setProperty(UpdateClasspathAttributesDataModelProvider.PROJECT_NAME, parentComp.getProject().getName());
			model.setProperty(UpdateClasspathAttributesDataModelProvider.ENTRIES_TO_REMOVE_ATTRIBUTE, toRemove);
			model.getDefaultOperation().execute(new NullProgressMonitor(), null);

		} catch( ExecutionException ee) {
			
		}
		
		ExportedClasspathFoldersVirtualComponent vc = new ExportedClasspathFoldersVirtualComponent(parentComp.getProject(), parentComp);
		getTaskModel().putObject(NewReferenceWizard.COMPONENT, vc);
		getTaskModel().putObject(NewReferenceWizard.COMPONENT_PATH, "/");
	}
	
	protected IPath getOriginalPath(IClasspathEntry entry) {
	    final IClasspathAttribute[] attributes = entry.getExtraAttributes();
	    for (int i = 0; i < attributes.length; i++) {
	    	final IClasspathAttribute attribute = attributes[i];
	    	final String name = attribute.getName();
	    	if (name.equals(ClasspathDependencyUtil.CLASSPATH_COMPONENT_DEPENDENCY)) {
	    		return new Path(attribute.getValue());
    		}
	    }
	    return new Path("/");
	}
	
	protected IPath getNewPath(IClasspathEntry cpe) {
		return new Path("/");
	}
}
