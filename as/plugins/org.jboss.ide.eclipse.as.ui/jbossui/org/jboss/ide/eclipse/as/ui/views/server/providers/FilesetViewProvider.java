package org.jboss.ide.eclipse.as.ui.views.server.providers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.JavaFileEditorInput;
import org.eclipse.ui.internal.util.SWTResourceUtil;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.core.model.ArchivesCore;
import org.jboss.ide.eclipse.archives.ui.util.composites.FilesetPreviewComposite;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.SimplePropertiesViewExtension;

public class FilesetViewProvider extends SimplePropertiesViewExtension {
	
	private static final String FILESET_KEY = "org.jboss.ide.eclipse.as.ui.views.server.providers.FilesetViewProvider.PropertyKey";
	
	private Action createFilter, deleteFilter, editFilter, deleteFileAction, editFileAction;
	
	private FilesetContentProvider contentProvider;
	private LabelProvider labelProvider;
	private Fileset[] filesets;
	private Object selection;

	public FilesetViewProvider() {
		contentProvider = new FilesetContentProvider();
		labelProvider = new FilesetLabelProvider();
		createActions();
	}
	
	protected void createActions() {
		createFilter =  new Action() { 
			public void run() {
				IDeployableServer server = (IDeployableServer)contentProvider.server.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
				if( server != null ) {
					FilesetDialog d = new FilesetDialog(new Shell(), server);
					if( d.open() == Window.OK ) {
						Fileset fs = d.getFileset();
						Fileset[] filesetsNew = new Fileset[filesets.length + 1];
						System.arraycopy(filesets, 0, filesetsNew, 0, filesets.length);
						filesetsNew[filesetsNew.length-1] = fs;
						filesets = filesetsNew;
						saveFilesets(true);
						refreshViewer();
					}
				}
			}
		};
		createFilter.setText("Create Filter");
		deleteFilter =  new Action() { 
			public void run() {
				if( selection instanceof Fileset ) {
					try {
						ArrayList asList = new ArrayList(Arrays.asList(filesets));
						asList.remove(selection);
						filesets = (Fileset[]) asList.toArray(new Fileset[asList.size()]);
						saveFilesets(true);
						removeElement(selection);
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}
			}
		};
		deleteFilter.setText("Delete Filter");
		editFilter =  new Action() { 
			public void run() {
				Fileset sel = (Fileset)selection;
				FilesetDialog d = new FilesetDialog(new Shell(), sel);
				if( d.open() == Window.OK ) {
					Fileset ret = d.getFileset();
					sel.setName(ret.getName());
					sel.setFolder(ret.getFolder());
					sel.setIncludesPattern(ret.getIncludesPattern());
					sel.setExcludesPattern(ret.getExcludesPattern());
					saveFilesets(true);
					refreshViewer(sel);
				}
			}
		};
		editFilter.setText("Edit Filter");
		deleteFileAction =  new Action() { 
			public void run() {
				try {
					PathWrapper wrapper = (PathWrapper)selection;
					File file = wrapper.getPath().toFile();
					file.delete();
					refreshViewer();
				} catch( Exception e ) {
				}
			}
		};
		deleteFileAction.setText("Delete File");
		editFileAction =  new Action() { 
			public void run() {
				try {
					PathWrapper wrapper = (PathWrapper)selection;
					File file = wrapper.getPath().toFile();
					IWorkbench wb = PlatformUI.getWorkbench();
					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
					IWorkbenchPage page = win.getActivePage();
					IFileStore fileStore= EFS.getLocalFileSystem().fromLocalFile(file);
					if( fileStore != null ) {
						IEditorInput input = new JavaFileEditorInput(fileStore);
						IEditorDescriptor desc = PlatformUI.getWorkbench().
							getEditorRegistry().getDefaultEditor(file.getName());
					   page.openEditor(input, desc.getId());
					} 
				} catch( Exception e ) {
					
				}
			}
		};
		editFileAction.setText("Edit File");
	}
	
	public static class PathWrapper {
		private IPath path;
		private String folder;
		public PathWrapper(IPath path, String folder) {
			this.path = path;
			this.folder = folder;
		}
		/**
		 * @return the folder
		 */
		public String getFolder() {
			return folder;
		}
		/**
		 * @return the path
		 */
		public IPath getPath() {
			return new Path(folder).append(path);
		}
		
		public String getLocalizedResourceName() {
			//return path.toOSString().substring(new Path(folder).toOSString().length());
			return path.toOSString();
		}
	}
	public class FilesetContentProvider implements ITreeContentProvider {
		public IServer server;
		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof ServerViewProvider ) {
				return filesets == null ? new Object[]{} : filesets;
			} else if( parentElement instanceof Fileset ) {
				Fileset fs = (Fileset)parentElement;
				IPath[] paths = ArchivesCore.findMatchingPaths(
						new Path(fs.getFolder()), fs.getIncludesPattern(), fs.getExcludesPattern());
				PathWrapper[] wrappers = new PathWrapper[paths.length];
				for( int i = 0; i < wrappers.length; i++ ) {
					wrappers[i] = new PathWrapper(paths[i], fs.getFolder());
				}
				return wrappers;
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false;
		}

		public Object[] getElements(Object inputElement) {
			return null;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if( newInput instanceof IServer ) {
				server = (IServer)newInput;
				loadFilesets();
			}
		}
	}

	public void loadFilesets() {
		IServer server = contentProvider.server;
		if( server != null ) {
			IDeployableServer jbs = (IDeployableServer)server.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
			if( jbs != null ) {
				ServerAttributeHelper helper = jbs.getAttributeHelper();
				List tmp = helper.getAttribute(FILESET_KEY, new ArrayList());
				String[] asStrings = (String[]) tmp.toArray(new String[tmp.size()]);
				filesets = new Fileset[asStrings.length];
				for( int i = 0; i < asStrings.length; i++ ) {
					filesets[i] = new Fileset(asStrings[i]);
				}
			}
		}
	}
	
	public void saveFilesets(boolean suppressRefresh) {
		Runnable r = new Runnable() {
			public void run() {
				IServer server = contentProvider.server;
				if( server != null ) {
					ArrayList list = new ArrayList();
					for( int i = 0; i < filesets.length; i++ ) {
						list.add(filesets[i].toString());
					}
					IDeployableServer jbs = (IDeployableServer)server.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
					ServerAttributeHelper helper = jbs.getAttributeHelper();
					helper.setAttribute(FILESET_KEY, list);
					helper.save();
				}
			}
		};

		if( suppressRefresh ) {
			suppressingRefresh(r);
		} else {
			r.run();
		}
	}

	public class Fileset implements Cloneable {
		private String name, folder, includesPattern, excludesPattern;
		public Fileset() {
		}
		public Fileset(String string) {
			try {
				name = folder = includesPattern =excludesPattern = "";
				String[] parts = string.split("\n");
				name = parts[0];
				folder = parts[1];
				includesPattern = parts[2];
				excludesPattern = parts[3];
			} catch( ArrayIndexOutOfBoundsException aioobe) {}
		}
		
		public Fileset(String name, String folder, String inc, String exc) {
			this.name = name;
			this.folder = folder;
			includesPattern = inc;
			excludesPattern = exc;
		}
		public String toString() {
			return name + "\n" + folder + "\n" + includesPattern + "\n" + excludesPattern;
		}
		/**
		 * @return the folder
		 */
		public String getFolder() {
			return folder == null ? "" : folder;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name == null ? "" : name;
		}
		/**
		 * @return the excludesPattern
		 */
		public String getExcludesPattern() {
			return excludesPattern == null ? "" : excludesPattern;
		}
		/**
		 * @return the includesPattern
		 */
		public String getIncludesPattern() {
			return includesPattern == null ? "" : includesPattern;
		}

		/**
		 * @param excludesPattern the excludesPattern to set
		 */
		public void setExcludesPattern(String excludesPattern) {
			this.excludesPattern = excludesPattern;
		}

		/**
		 * @param folder the folder to set
		 */
		public void setFolder(String folder) {
			this.folder = folder;
		}

		/**
		 * @param includesPattern the includesPattern to set
		 */
		public void setIncludesPattern(String includesPattern) {
			this.includesPattern = includesPattern;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		
		public Object clone() {
			try {
				return super.clone();
			} catch( Exception e ) {}
			return null;
		}
		
		public boolean equals(Object other) {
			if( !(other instanceof Fileset)) return false;
			if( other == this ) return true;
			Fileset o = (Fileset)other;
			return o.getName().equals(getName()) && o.getFolder().equals(getFolder()) 
				&& o.getIncludesPattern().equals(getIncludesPattern()) && o.getExcludesPattern().equals(getExcludesPattern());
		}
		public int hashCode() {
			return (name + "::_::" +  folder + "::_::" +  includesPattern + "::_::" +  excludesPattern + "::_::").hashCode();
		}
	}
	
	public class FilesetLabelProvider extends LabelProvider {
	    public Image getImage(Object element) {
	    	if( element instanceof PathWrapper ) {
		    	String fileName = ((PathWrapper)element).getPath().toOSString();
		    	IContentTypeManager manager = Platform.getContentTypeManager();
		    	IContentTypeMatcher matcher = manager.getMatcher(null, null);
		    	IContentType contentType = matcher.findContentTypeFor(fileName);
		        ImageDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry()
		        	.getImageDescriptor(fileName, contentType);
			    if (descriptor == null) {
			    	descriptor = PlatformUI.getWorkbench().getSharedImages()
			                .getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
				}
		        Image image = (Image) SWTResourceUtil.getImageTable().get(descriptor);
		        if (image == null) {
		            image = descriptor.createImage();
		            SWTResourceUtil.getImageTable().put(descriptor, image);
		        }
		        return image;
	    	}
	        return null;
	    }

	    public String getText(Object element) {
	    	if( element instanceof PathWrapper ) return ((PathWrapper)element).getLocalizedResourceName();
	    	if( element instanceof Fileset ) return ((Fileset)element).getName();
	        return element == null ? "" : element.toString();//$NON-NLS-1$
	    }

	}
	
	public Image createIcon() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	}

	
	public void fillContextMenu(Shell shell, IMenuManager menu, Object selection) {
		this.selection = selection;
		if( selection instanceof ServerViewProvider ) {
			menu.add(createFilter);
		} else if( selection instanceof Fileset ) {
			menu.add(editFilter);
			menu.add(deleteFilter);
		} else if( selection instanceof PathWrapper ) {
			menu.add(editFileAction);
			menu.add(deleteFileAction);
		}
	}

	public ITreeContentProvider getContentProvider() {
		return contentProvider;
	}

	public LabelProvider getLabelProvider() {
		return labelProvider;
	}

	public Properties getProperties(Object selected) {
		return null;
	}

	public String[] getPropertyKeys(Object selected) {
		return null;
	}
	
	
	
	protected class FilesetDialog extends Dialog {
		Fileset fileset;
		private String name, dir, includes, excludes;
		private Button browse;
		private Text includesText, excludesText, folderText, nameText;
		private Composite main;
		private FilesetPreviewComposite preview;
		protected FilesetDialog(Shell parentShell, IDeployableServer server) {
			super(parentShell);
			this.fileset = new Fileset();
			this.fileset.setFolder(server.getDeployDirectory());
		}
		protected FilesetDialog(Shell parentShell, Fileset fileset) {
			super(parentShell);
			this.fileset = (Fileset)fileset.clone();
		}
		protected Point getInitialSize() {
			//return new Point(400, 150);
			Point p = super.getInitialSize();
			return new Point(500, p.y);
		}
		protected int getShellStyle() {
			return super.getShellStyle() | SWT.RESIZE;
		}
		
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText("New Fileset");
		}
		
		protected Control createDialogArea(Composite parent) {
			Composite sup = (Composite) super.createDialogArea(parent);
			main = new Composite(sup, SWT.NONE);
			main.setLayout(new GridLayout(3, false));
			main.setLayoutData(new GridData(GridData.FILL_BOTH));
			fillArea(main);
			
			nameText.setText(fileset.getName());
			folderText.setText(fileset.getFolder());
			includesText.setText(fileset.getIncludesPattern());
			excludesText.setText(fileset.getExcludesPattern());
			
			addListeners();
			return sup;
		}

		protected void addListeners() {
			ModifyListener mListener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					textModified();
				} 
			};
			nameText.addModifyListener(mListener);
			folderText.addModifyListener(mListener);
			includesText.addModifyListener(mListener);
			excludesText.addModifyListener(mListener);
			
			browse.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					DirectoryDialog d = new DirectoryDialog(new Shell());
					d.setFilterPath(folderText.getText());
					String x = d.open();
					if( x != null ) 
						folderText.setText(x);
				} 
			});
		}
		
		protected void textModified() {
			name = nameText.getText();
			dir = folderText.getText();
			includes = includesText.getText();
			excludes = excludesText.getText();
			fileset.setName(name);
			fileset.setFolder(dir);
			fileset.setIncludesPattern(includes);
			fileset.setExcludesPattern(excludes);
			updatePreview();
		}
		protected void fillArea(Composite main) {
			Label nameLabel = new Label(main, SWT.NONE);
			nameLabel.setText("Name: ");
			
			nameText = new Text(main, SWT.BORDER);
			nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			Label folderLabel = new Label(main, SWT.NONE);
			folderLabel.setText("Root Directory: ");
			
			folderText = new Text(main, SWT.BORDER);
			folderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			browse = new Button(main, SWT.PUSH);
			browse.setText("Browse...");
			
			Label includesLabel = new Label(main, SWT.NONE);
			includesLabel.setText("Includes: ");
			
			includesText = new Text(main, SWT.BORDER);
			includesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			Label excludeLabel= new Label(main, SWT.NONE);
			excludeLabel.setText("Excludes: ");
			
			excludesText = new Text(main, SWT.BORDER);
			excludesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			Group previewWrapper = new Group(main, SWT.NONE);
			previewWrapper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 3, 3));
			previewWrapper.setText("Preview");
			
			previewWrapper.setLayout(new FillLayout());
			preview = new FilesetPreviewComposite(previewWrapper, SWT.NONE);
		}
		
		private void updatePreview() {
			preview.setRootFolder(new Path(dir));
			IPath files[] = ArchivesCore.findMatchingPaths(new Path(dir), includesText.getText(), excludesText.getText());
			preview.setInput(files);
		}
		
		public String getDir() {
			return dir;
		}
		public String getExcludes() {
			return excludes;
		}
		public String getIncludes() {
			return includes;
		}
		public String getName() {
			return name;
		}
		public Fileset getFileset() {
			return fileset;
		}
		
	}

}
