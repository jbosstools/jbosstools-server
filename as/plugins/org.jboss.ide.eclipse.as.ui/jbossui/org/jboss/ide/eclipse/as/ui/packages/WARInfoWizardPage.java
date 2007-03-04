package org.jboss.ide.eclipse.as.ui.packages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.jboss.ide.eclipse.as.core.packages.ObscurelyNamedPackageTypeSuperclass;
import org.jboss.ide.eclipse.as.core.packages.WarPackageType;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.IPackageFileSet;
import org.jboss.ide.eclipse.packages.core.model.IPackageFolder;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;
import org.jboss.ide.eclipse.packages.core.model.types.IPackageType;
import org.jboss.ide.eclipse.packages.ui.PackagesUIPlugin;
import org.jboss.ide.eclipse.packages.ui.providers.PackagesContentProvider;
import org.jboss.ide.eclipse.packages.ui.providers.PackagesLabelProvider;
import org.jboss.ide.eclipse.ui.wizards.WizardPageWithNotification;

public class WARInfoWizardPage extends WizardPageWithNotification {

	private Group webinfGroup, previewGroup;
	private NewWARWizard wizard;
	private TreeViewer warPreview;
	private boolean hasCreated = false;
	private Text webinfFolders;
	private Button webinfFoldersButton;
	public WARInfoWizardPage (NewWARWizard wizard) {
		super("WAR information", "WAR Information", PackagesUIPlugin.getImageDescriptor(PackagesUIPlugin.IMG_NEW_WAR_WIZARD));
		this.wizard = wizard;
	}
	
	public void createControl(Composite parent) {
		setMessage("Information for the setup of your WAR. \n" + 
				"Later, you can customize this packaging structure further.");
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());
		
		layoutGroups(main);
		fillGroups();
		
		setControl(main);
	}
	protected void layoutGroups(Composite main) {
		webinfGroup = new Group(main, SWT.NONE);
		FormData webinfData = new FormData();
		webinfData.left = new FormAttachment(0,5);
		webinfData.right = new FormAttachment(100,-5);
		webinfData.top = new FormAttachment(0,5);
		webinfGroup.setLayoutData(webinfData);
		webinfGroup.setText("WEB-INF Folders");
		
		previewGroup = new Group(main, SWT.NONE);
		previewGroup.setText("Preview");
		FormData previewData = new FormData();
		previewData.left = new FormAttachment(0,5);
		previewData.right = new FormAttachment(100,-5);
		previewData.top = new FormAttachment(webinfGroup,5);
		previewData.bottom = new FormAttachment(100,-5);
		previewGroup.setLayoutData(previewData);
		previewGroup.setLayout(new FormLayout());
		warPreview = new TreeViewer(previewGroup);
		warPreview.setLabelProvider(new PackagesLabelProvider());
		warPreview.setContentProvider(new PackagesContentProvider(false));
		FormData warPreviewData = new FormData();
		warPreviewData.left = new FormAttachment(0,5);
		warPreviewData.right = new FormAttachment(100,-5);
		warPreviewData.top = new FormAttachment(0,5);
		warPreviewData.bottom = new FormAttachment(100,-5);
		warPreview.getTree().setLayoutData(warPreviewData);
		
	}

	protected void fillGroups() {
		webinfGroup.setLayout(new FormLayout());
		webinfFolders = new Text(webinfGroup, SWT.BORDER | SWT.READ_ONLY);
		webinfFoldersButton = new Button(webinfGroup, SWT.PUSH);
		webinfFoldersButton.setText(Messages.browse);
		
		FormData buttonData = new FormData();
		buttonData.right = new FormAttachment(100,-5);
		webinfFoldersButton.setLayoutData(buttonData);
		
		FormData textData = new FormData();
		textData.left = new FormAttachment(0,5);
		textData.right = new FormAttachment(webinfFoldersButton, -5);
		webinfFolders.setLayoutData(textData);
		
		webinfFoldersButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				WorkspaceFolderSelectionDialog dialog = new WorkspaceFolderSelectionDialog(new Shell(), true, webinfFolders.getText());
				if( dialog.open() == Window.OK) {
					ArrayList selectedFolders = new ArrayList();
					Object[] o = dialog.getResult();
					String imploded = "";
					for( int i = 0; i < o.length; i++ ) {
						selectedFolders.add(((IResource)o[i]).getFullPath().toOSString());
						imploded += ((IResource)o[i]).getFullPath() + ",";
					}
					IPackageFolder webinf = getFolder(wizard.getPackage(), ObscurelyNamedPackageTypeSuperclass.WEBINF);
					IPackageFileSet[] sets = webinf.getFileSets();
					for( int i = 0; i < sets.length; i++ ) {
						String path = sets[i].getSourceContainer().getFullPath().toOSString();
						if( selectedFolders.contains(path)) {
							selectedFolders.remove(path); // already added
						} else {
							// remove ir
							webinf.removeChild(sets[i]);
						}
					}
					// add whatever's left as new filesets
					for( int i = 0; i < selectedFolders.size(); i++ ) {
						ObscurelyNamedPackageTypeSuperclass.addFileset(wizard.getProject(), 
								webinf, (String)selectedFolders.get(i), "**/*");
					}
					fillWidgets(wizard.getPackage());
				}
			}  
		});
	}
	public boolean isPageComplete() {
		return true;
	}
    public void pageEntered(int button) {
    	if( !hasCreated ) {
    		addToPackage();
    		hasCreated = true;
    	}
    	fillWidgets(wizard.getPackage());
    }
    
    protected void addToPackage() {
    	// fill it
    	IPackageType type = PackagesCore.getPackageType("org.jboss.ide.eclipse.as.core.packages.warPackage");
    	if( type instanceof WarPackageType ) {
    		((WarPackageType)type).fillDefaultConfiguration(wizard.getProject(), wizard.getPackage(), new NullProgressMonitor());
    	}
    }
    protected void fillWidgets(IPackage pkg) {
    	System.out.println("filling widgets");
    	warPreview.setInput(new IPackage[] {pkg});
    	warPreview.expandAll();
    	
    	fillWebinfText(pkg);
    }
    
    protected void fillWebinfText(IPackage pkg) {
    	// set webinf text
    	IPackageFolder webinf = getFolder(pkg, ObscurelyNamedPackageTypeSuperclass.WEBINF);
    	String s = "";
    	if( webinf != null ) {
    		IPackageFileSet[] filesets = webinf.getFileSets();
    		for( int i = 0; i < filesets.length; i++ ) {
    			String path = filesets[i].getSourceContainer().getFullPath().toOSString();
    			s += path + ",";
    		}
    	}
    	webinfFolders.setText(s);
    }
    
    protected IPackageFolder getFolder(IPackage pkg, String folderName) {
    	IPackageFolder result = null;
    	IPackageFolder[] folders = pkg.getFolders();
    	for( int i = 0; i < folders.length; i++ ) {
    		if( folders[i].getName().equals(folderName)) {
    			result = folders[i];
    			break;
    		}
    	}
    	return result;
    }
    
    public void pageExited(int button) {}

    
    
    
    // stuff that can be extracted
    public static class WorkspaceFolderSelectionDialog extends ElementTreeSelectionDialog {
    	
   	 public WorkspaceFolderSelectionDialog(Shell parent, boolean allowMultiple, String selectedPaths) {
   		 super(parent, new FolderLabelProvider(), new FolderContentProvider());
   		 setAllowMultiple(allowMultiple);
   		 setupDestinationList();
   	}
   	 
   	 private void setupDestinationList () {
   		 List projects = Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());
   		 setInput(projects);
   	 }
   	 
   	 private static class FolderContentProvider implements ITreeContentProvider {
   		private static final Object[] NO_CHILDREN = new Object[0];
   		public Object[] getChildren(Object parentElement) {
   			if (parentElement instanceof IContainer) {
   				IContainer container = (IContainer) parentElement;
   				try {
   					IResource members[] = container.members();
   					List folders = new ArrayList();
   					for (int i = 0; i < members.length; i++) {
   						if (members[i].getType() == IResource.FOLDER) folders.add(members[i]);
   					}
   					return folders.toArray();
   				} catch (CoreException e) {
   					// TODO Auto-generated catch block
   					e.printStackTrace();
   				}
   			}
   			return NO_CHILDREN;
   		}

   		public Object getParent(Object element) {
   			if (element instanceof IContainer) {
   				return ((IContainer) element).getParent();
   			}
   			return null;
   		}

   		public boolean hasChildren(Object element) {
   			if (element instanceof IContainer) {
   				IContainer container = (IContainer) element;
   				try {
   					IResource members[] = container.members();
   					List folders = new ArrayList();
   					for (int i = 0; i < members.length; i++) {
   						if (members[i].getType() == IResource.FOLDER) folders.add(members[i]);
   					}
   					return folders.size() > 0;
   				} catch (CoreException e) {
   					// TODO Auto-generated catch block
   					e.printStackTrace();
   				}
   			}
   			return false;
   		}

   		public Object[] getElements(Object inputElement) {
   			if (inputElement instanceof Collection)
   				return ((Collection)inputElement).toArray();
   			
   			return NO_CHILDREN;
   		}

   		public void dispose() {}
   		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
   	 }
   	 
   	 public static class FolderLabelProvider implements ILabelProvider {
   		public FolderLabelProvider () {}
   		public Image getImage(Object element) {
   			if (element instanceof IProject) {
   				return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
   			} else if (element instanceof IFolder) {
   				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
   			}
   			return null;
   		}

   		public String getText(Object element) {
   			if (element instanceof IContainer) {
   				return ((IContainer)element).getName();
   			}
   			return "";
   		}

   		public void addListener(ILabelProviderListener listener) {
   		}

   		public void dispose() {
   		}

   		public boolean isLabelProperty(Object element, String property) {
   			return true;
   		}

   		public void removeListener(ILabelProviderListener listener) {
   		}
   		 
   	 }
   }

}
