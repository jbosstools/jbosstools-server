/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.views.server.providers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelCore;
import org.jboss.ide.eclipse.archives.ui.util.composites.FilesetPreviewComposite;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.SimplePropertiesViewExtension;
import org.jboss.tools.as.wst.server.ui.views.server.JBossServerView;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class FilesetViewProvider extends SimplePropertiesViewExtension {

	private static final String FILESET_KEY = "org.jboss.ide.eclipse.as.ui.views.server.providers.FilesetViewProvider.PropertyKey";
	
	private Action createFilter, deleteFilter, editFilter, deleteFileAction, editFileAction;
	
	private FilesetContentProvider contentProvider;
	private LabelProvider labelProvider;
	private Fileset[] filesets;
	private Object[] selection;

	public FilesetViewProvider() {
		contentProvider = new FilesetContentProvider();
		labelProvider = new FilesetLabelProvider();
		createActions();
	}
	
	protected boolean supports(IServer server) {
		return server != null && (isJBossDeployable(server) || server.getRuntime() != null);
	}

	protected void createActions() {
		createFilter =  new Action() { 
			public void run() {
				IDeployableServer server = (IDeployableServer)contentProvider.server.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
				String location = null;
				if( server != null ) 
					location = server.getDeployDirectory();
				else 
					location = contentProvider.server.getRuntime().getLocation().toOSString();

				if( location != null ) {
					FilesetDialog d = new FilesetDialog(new Shell(), location);
					if( d.open() == Window.OK ) {
						Fileset fs = d.getFileset();
						Fileset[] filesetsNew = new Fileset[filesets.length + 1];
						System.arraycopy(filesets, 0, filesetsNew, 0, filesets.length);
						filesetsNew[filesetsNew.length-1] = fs;
						filesets = filesetsNew;
						saveFilesets();
					}
				}
			}
		};
		createFilter.setText(Messages.FilesetsCreateFilter);
		deleteFilter =  new Action() { 
			public void run() {
				if( selection.length == 1 && selection[0] instanceof Fileset ) {
					try {
						ArrayList<Fileset> asList = new ArrayList<Fileset>(Arrays.asList(filesets));
						asList.remove(selection[0]);
						filesets = asList.toArray(new Fileset[asList.size()]);
						saveFilesets();
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}
			}
		};
		deleteFilter.setText(Messages.FilesetsDeleteFilter);
		editFilter =  new Action() { 
			public void run() {
				Fileset sel = selection.length == 1 && selection[0] instanceof Fileset ? (Fileset)selection[0] : null;
				if( sel == null ) return;
				FilesetDialog d = new FilesetDialog(new Shell(), sel);
				if( d.open() == Window.OK ) {
					Fileset ret = d.getFileset();
					sel.setName(ret.getName());
					sel.setFolder(ret.getFolder());
					sel.setIncludesPattern(ret.getIncludesPattern());
					sel.setExcludesPattern(ret.getExcludesPattern());
					saveFilesets();
				}
			}
		};
		editFilter.setText(Messages.FilesetsEditFilter);
		deleteFileAction =  new Action() { 
			public void run() {
				try {
					Shell shell = JBossServerView.getDefault().getSite().getShell();
					File[] files = getSelectedFiles();
					MessageBox mb = new MessageBox(shell,SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
					mb.setText("Delete Files?");
					mb.setMessage("Are you sure you want to delete the selected files?");
					if( mb.open() == SWT.OK) {
						for( int i = 0; i < files.length; i++ )
							FileUtil.safeDelete(files[i]);
						refreshViewer();
					}
				} catch( Exception e ) {
				}
			}
		};
		deleteFileAction.setText(Messages.FilesetsDeleteFile);
		editFileAction =  new Action() { 
			public void run() {
				File[] files = getSelectedFiles();
				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				IWorkbenchPage page = win.getActivePage();
				for( int i = 0; i < files.length; i++ ) {
					try {
						IFile eclipseFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(files[i].getAbsolutePath()));
						IFileStore fileStore= EFS.getLocalFileSystem().fromLocalFile(files[i]);
						if( eclipseFile != null ) {
							IEditorInput input = new FileEditorInput(eclipseFile);
							IEditorDescriptor desc = PlatformUI.getWorkbench().
								getEditorRegistry().getDefaultEditor(files[i].getName());
							if( desc != null ) 
								page.openEditor(input, desc.getId());
						} else if( fileStore != null ){
							IEditorInput input = new FileStoreEditorInput(fileStore);
							IEditorDescriptor desc = PlatformUI.getWorkbench().
									getEditorRegistry().getDefaultEditor(files[i].getName());
							if( desc != null ) 
								page.openEditor(input, desc.getId());
						}
					} catch( Exception e ) {
						IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, "Cannot open file", e);
						JBossServerUIPlugin.getDefault().getLog().log(status);
					}
				}
			}
		};
		editFileAction.setText(Messages.FilesetsEditFile);
	}
	
	protected File[] getSelectedFiles() {
		ArrayList<File> tmp = new ArrayList<File>();
		for( int i = 0; i < selection.length; i++ ) {
			tmp.add(((PathWrapper)selection[i]).getPath().toFile());
		}
		return (File[]) tmp.toArray(new File[tmp.size()]);
	}
	
	public static class PathWrapper {
		private IPath path;
		private IPath folder;
		public PathWrapper(IPath path, IPath folder) {
			this.path = path;
			this.folder = folder;
		}
		/**
		 * @return the folder
		 */
		public IPath getFolder() {
			return folder;
		}
		/**
		 * @return the path
		 */
		public IPath getPath() {
			return folder.append(path);
		}
		
		public String getLocalizedResourceName() {
			return path.toOSString();
		}
	}
	
	public static class FolderWrapper extends PathWrapper {
		private HashMap<String, FolderWrapper> childrenFolders;
		private ArrayList<PathWrapper> children;
		public FolderWrapper(IPath path, IPath folder) {
			super(path, folder);
			children = new ArrayList<PathWrapper>();
			childrenFolders = new HashMap<String, FolderWrapper>();
		}
		public void addChild(IPath path) {
			if( path.segmentCount() == 1 ) {
				children.add(new PathWrapper(path, getFolder().append(getLocalizedResourceName())));
			} else {
				addPath(children, childrenFolders, path, getFolder().append(getLocalizedResourceName()));				
			}
		}
		public Object[] getChildren() {
			return children.toArray(new Object[children.size()]);
		}
	}
	
	private static void addPath(ArrayList<PathWrapper> children, HashMap<String, FolderWrapper> folders, IPath path, IPath folder) {
		try {
		FolderWrapper fw = null;
		if( !folders.containsKey(path.segment(0))) {
			fw = new FolderWrapper(path.removeLastSegments(path.segmentCount()-1), folder);
			folders.put(path.segment(0), fw);
			children.add(fw);
		} else {
			fw = folders.get( path.segment(0));
		}
		fw.addChild(path.removeFirstSegments(1));
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	public class FilesetContentProvider implements ITreeContentProvider {
		public IServer server;
		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof ServerViewProvider ) {
				return filesets == null ? new Object[]{} : filesets;
			} else if( parentElement instanceof Fileset ) {
				Fileset fs = (Fileset)parentElement;
				IPath[] paths = null;
				try {
					paths = ArchivesModelCore.findMatchingPaths(
							new Path(fs.getFolder()), fs.getIncludesPattern(), fs.getExcludesPattern());
				} catch( BuildException be ) {
					return new Object[]{};
				}
					
				HashMap<String, FolderWrapper> folders = new HashMap<String, FolderWrapper>();
				ArrayList<PathWrapper> wrappers = new ArrayList<PathWrapper>();
				for( int i = 0; i < paths.length; i++ ) {
					if( paths[i].segmentCount() == 1 ) {
						wrappers.add(new PathWrapper(paths[i], new Path(fs.getFolder())));
					} else {
						addPath(wrappers, folders, paths[i], new Path(fs.getFolder()));
					}
				}
				return wrappers.toArray(new Object[wrappers.size()]);
			} else if( parentElement instanceof FolderWrapper ) {
				return ((FolderWrapper)parentElement).getChildren();
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
			ServerAttributeHelper helper = ServerAttributeHelper.createHelper(server);
			List tmp = helper.getAttribute(FILESET_KEY, new ArrayList());
			String[] asStrings = (String[]) tmp.toArray(new String[tmp.size()]);
			filesets = new Fileset[asStrings.length];
			for( int i = 0; i < asStrings.length; i++ ) {
				filesets[i] = new Fileset(asStrings[i]);
			}
		}
	}
	
	public void saveFilesets() {
		IServer server = contentProvider.server;
		if( server != null ) {
			ArrayList<String> list = new ArrayList<String>();
			for( int i = 0; i < filesets.length; i++ ) {
				list.add(filesets[i].toString());
			}
			ServerAttributeHelper helper = ServerAttributeHelper.createHelper(server);
			helper.setAttribute(FILESET_KEY, list);
			helper.save();
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
		
	    private LocalResourceManager resourceManager;

		public FilesetLabelProvider() {
			super();
			this.resourceManager = new LocalResourceManager(JFaceResources.getResources());
		}

		public Image getImage(Object element) {
	    	if( element instanceof Fileset ) {
	    		return PlatformUI.getWorkbench().getSharedImages()
                .getImage(ISharedImages.IMG_OBJ_FOLDER);
	    	} else if( element instanceof FolderWrapper ) {
	    		return PlatformUI.getWorkbench().getSharedImages()
                .getImage(ISharedImages.IMG_OBJ_FOLDER);
	    	} else if( element instanceof PathWrapper ) {
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
			    return resourceManager.createImage(descriptor);
	    	}
	        return null;
	    }

	    public String getText(Object element) {
	    	if( element instanceof PathWrapper ) return ((PathWrapper)element).getLocalizedResourceName();
	    	if( element instanceof Fileset ) return ((Fileset)element).getName() + "  " + ((Fileset)element).getFolder();
	        return element == null ? "" : element.toString();//$NON-NLS-1$
	    }

		
		public void dispose() {
			resourceManager.dispose();
			resourceManager = null;
			super.dispose();
		}

	}
	
	public void fillContextMenu(Shell shell, IMenuManager menu, Object[] selection) {
		this.selection = selection;
		if( selection.length == 1 && selection[0] instanceof ServerViewProvider ) {
			menu.add(createFilter);
		} else if( selection.length == 1 && selection[0] instanceof Fileset ) {
			menu.add(editFilter);
			menu.add(deleteFilter);
		} else if( allPathWrappers(selection) ) {
			editFileAction.setEnabled(canEdit(selection));
			deleteFileAction.setEnabled(canDelete(selection));
			menu.add(editFileAction);
			menu.add(deleteFileAction);
		}
	}
	
	protected boolean allPathWrappers(Object[] list) {
		boolean result = true;
		for( int i = 0; i < list.length; i++ )
			result &= list[i] instanceof PathWrapper;
		return result;
	}
	
	protected boolean canDelete(Object[] list ) {
		boolean result = true;
		for( int i = 0; i < list.length; i++ ) 
			result &= ((PathWrapper)selection[i]).getPath().toFile().exists();
		return result;
	}
	
	protected boolean canEdit(Object[] list) {
		for( int i = 0; i < list.length; i++ )
			if( canEdit(((PathWrapper)selection[i]).getPath().toFile()))
				return true;
		return false;
	}
	
	protected boolean canEdit(File file) {
		IFile eclipseFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.getAbsolutePath()));
		IFileStore fileStore= EFS.getLocalFileSystem().fromLocalFile(file);
		boolean editable = false;
		if( eclipseFile != null ) {
			IEditorInput input = new FileEditorInput(eclipseFile);
			IEditorDescriptor desc = PlatformUI.getWorkbench().
				getEditorRegistry().getDefaultEditor(file.getName());
			if( input != null && desc != null ) 
				editable = true;
		} else if( fileStore != null ){
			IEditorInput input = new FileStoreEditorInput(fileStore);
			IEditorDescriptor desc = PlatformUI.getWorkbench().
					getEditorRegistry().getDefaultEditor(file.getName());
			if( input != null && desc != null ) 
				editable = true;
		}
		return editable;
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
	
	protected class FilesetDialog extends TitleAreaDialog {
		protected Fileset fileset;
		private String name, dir, includes, excludes;
		private Button browse;
		private Text includesText, excludesText, folderText, nameText;
		private Composite main;
		private FilesetPreviewComposite preview;
		protected FilesetDialog(Shell parentShell, String defaultLocation) {
			super(parentShell);
			this.fileset = new Fileset();
			this.fileset.setFolder(defaultLocation);
			
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
			shell.setText(Messages.FilesetsNewFileset);
		}
		
		protected Control createDialogArea(Composite parent) {
			setTitle("File filter");
			setMessage("Creates a new file filter");
			
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
			nameLabel.setText(Messages.FilesetsNewName);
			
			nameText = new Text(main, SWT.BORDER);
			nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			Label folderLabel = new Label(main, SWT.NONE);
			folderLabel.setText(Messages.FilesetsNewRootDir);
			
			folderText = new Text(main, SWT.BORDER);
			folderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			browse = new Button(main, SWT.PUSH);
			browse.setText(Messages.FilesetsNewBrowse);
			
			Label includesLabel = new Label(main, SWT.NONE);
			includesLabel.setText(Messages.FilesetsNewIncludes);
			
			includesText = new Text(main, SWT.BORDER);
			includesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			Label excludeLabel= new Label(main, SWT.NONE);
			excludeLabel.setText(Messages.FilesetsNewExcludes);
			
			excludesText = new Text(main, SWT.BORDER);
			excludesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			Group previewWrapper = new Group(main, SWT.NONE);
			
			previewWrapper.setLayout(new GridLayout());
			GridLayout gridLayout = new GridLayout();
			//gridLayout.numColumns = 3;
			//gridLayout.verticalSpacing = 9;		
			
			GridData data = new GridData(GridData.FILL_BOTH);
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			data.horizontalSpan = 3;
			data.minimumHeight = 200;
			
			previewWrapper.setLayoutData(data);
			previewWrapper.setText(Messages.FilesetsNewPreview);
			
			previewWrapper.setLayout(new FillLayout());
			preview = new FilesetPreviewComposite(previewWrapper, SWT.NONE);
		}
		
		private void updatePreview() {
			IPath files[] = ArchivesModelCore.findMatchingPaths(new Path(dir), includesText.getText(), excludesText.getText());
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
