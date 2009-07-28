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
package org.jboss.ide.eclipse.archives.webtools.ui;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.webtools.Messages;
import org.jboss.ide.eclipse.archives.webtools.ui.FilesetContentProvider.PathWrapper;
import org.jboss.ide.eclipse.archives.webtools.ui.FilesetContentProvider.ServerWrapper;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class FilesetActionProvider extends CommonActionProvider implements IDoubleClickListener {
	private ICommonActionExtensionSite actionSite;
	private Action createFilter, deleteFilter, editFilter, 
					deleteFileAction, editFileAction;
	private ITreeSelection treeSelection;
	private Object[] selected;

	public FilesetActionProvider() {
		super();
	}

	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		this.actionSite = aSite;
		ICommonViewerSite site = aSite.getViewSite();
		if( site instanceof ICommonViewerWorkbenchSite ) {
			StructuredViewer v = aSite.getStructuredViewer();
			v.addDoubleClickListener(this);
		}
		createActions();
	}
	
    public void dispose() {
		ICommonViewerSite site = actionSite.getViewSite();
		if( site instanceof ICommonViewerWorkbenchSite ) {
			StructuredViewer v = actionSite.getStructuredViewer();
			v.removeDoubleClickListener(this);
		}
    	super.dispose();
    }

	public void fillContextMenu(IMenuManager menu) {
		ICommonViewerSite site = actionSite.getViewSite();
		if (site instanceof ICommonViewerWorkbenchSite) {
			setSelection();
			if( selected == null )
				return;
			if( selected.length == 1 && selected[0] instanceof ServerWrapper ) {
				menu.add(createFilter);
			}else if( selected.length == 1 && selected[0] instanceof Fileset ) {
				menu.add(deleteFilter);
				menu.add(editFilter);
			} else if( allPathWrappers(selected) ) {
				editFileAction.setEnabled(canEdit(selected));
				deleteFileAction.setEnabled(canDelete(selected));
				menu.add(editFileAction);
				menu.add(deleteFileAction);
			}
		}
	}
	
	protected void setSelection() {
		ICommonViewerSite site = actionSite.getViewSite();
		if (site instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
			ITreeSelection selection = (ITreeSelection) wsSite.getSelectionProvider()
					.getSelection();
			this.treeSelection = selection;
			selected = selection.toArray();
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
			result &= ((PathWrapper)list[i]).getPath().toFile().exists();
		return result;
	}

	protected boolean canEdit(Object[] list) {
		for( int i = 0; i < list.length; i++ )
			if( canEdit(((PathWrapper)list[i]).getPath().toFile()))
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

	protected File[] getSelectedFiles() {
		ArrayList<File> tmp = new ArrayList<File>();
		for( int i = 0; i < selected.length; i++ ) {
			if( selected[i] instanceof PathWrapper)
				tmp.add(((PathWrapper)selected[i]).getPath().toFile());
		}
		return (File[]) tmp.toArray(new File[tmp.size()]);
	}
	
	protected ServerWrapper getServerWrapper(Fileset fs) {
		TreePath[] paths = treeSelection.getPathsFor(fs);
		if( paths.length == 1 ) {
			int count = paths[0].getSegmentCount();
			if( count > 1 ) {
				Object wrapper = paths[0].getSegment(count-2);
				if( wrapper != null && wrapper instanceof ServerWrapper ) {
					return ((ServerWrapper)wrapper);
				}
			}
		}
		return null;
	}
	
	protected void createActions() {
		createFilter = new Action() {
			public void run() {
				IServer iserver = null;
				ServerWrapper wrapper;
				if( !(selected[0] instanceof ServerWrapper))
						return;
				
				wrapper = (ServerWrapper)selected[0];
				iserver = wrapper.server;
				
				IDeployableServer server = (IDeployableServer) iserver
						.loadAdapter(IDeployableServer.class,
								new NullProgressMonitor());
				String location = null;
				if (server != null)
					location = server.getConfigDirectory();
				else
					location = iserver.getRuntime().getLocation().toOSString();

				if (location != null) {
					FilesetDialog d = new FilesetDialog(new Shell(), location);
					if (d.open() == Window.OK) {
						Fileset fs = d.getFileset();
						wrapper.addFileset(fs);
						actionSite.getStructuredViewer().refresh(wrapper);
					}
				}
			}
		};
		createFilter.setText(Messages.FilesetsCreateFilter);

		deleteFilter = new Action() {
			public void run() {
				Fileset fs = (Fileset)selected[0];
				ServerWrapper wrapper = getServerWrapper(fs);
				if( wrapper != null ) {
					wrapper.removeFileset(fs);
					actionSite.getStructuredViewer().refresh(wrapper);
				}
			}
		};
		deleteFilter.setText(Messages.FilesetsDeleteFilter);
		
		editFilter = new Action() {
			public void run() {
				Fileset sel = (Fileset)selected[0];
				ServerWrapper wrapper = getServerWrapper(sel);
				FilesetDialog d = new FilesetDialog(new Shell(), sel);
				if (d.open() == Window.OK) {
					Fileset ret = d.getFileset();
					sel.setName(ret.getName());
					sel.setFolder(ret.getFolder());
					sel.setIncludesPattern(ret.getIncludesPattern());
					sel.setExcludesPattern(ret.getExcludesPattern());
					wrapper.saveFilesets();
					actionSite.getStructuredViewer().refresh(wrapper);
				}
			}
		};
		editFilter.setText(Messages.FilesetsEditFilter);
		
		deleteFileAction = new Action() {
			public void run() {
				try {
					Shell shell = Display.getCurrent().getActiveShell();
					File[] files = getSelectedFiles();
					MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION
							| SWT.OK | SWT.CANCEL);
					mb.setText(Messages.DeleteFiles);
					mb.setMessage(Messages.DeleteFilesMessage);
					if (mb.open() == SWT.OK) {
						for (int i = 0; i < files.length; i++)
							FileUtil.safeDelete(files[i]);
						refreshViewer();
					}
				} catch (Exception e) {
				}
			}
		};
		deleteFileAction.setText(Messages.FilesetsDeleteFile);
		
		editFileAction = new Action() {
			public void run() {
				File[] files = getSelectedFiles();
				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				IWorkbenchPage page = win.getActivePage();
				for (int i = 0; i < files.length; i++) {
					try {
						IFile eclipseFile = ResourcesPlugin.getWorkspace()
								.getRoot().getFileForLocation(
										new Path(files[i].getAbsolutePath()));
						IFileStore fileStore = EFS.getLocalFileSystem()
								.fromLocalFile(files[i]);
						if (eclipseFile != null) {
							IEditorInput input = new FileEditorInput(
									eclipseFile);
							IEditorDescriptor desc = PlatformUI.getWorkbench()
									.getEditorRegistry().getDefaultEditor(
											files[i].getName());
							if (desc != null)
								page.openEditor(input, desc.getId());
						} else if (fileStore != null) {
							IEditorInput input = new FileStoreEditorInput(
									fileStore);
							IEditorDescriptor desc = PlatformUI.getWorkbench()
									.getEditorRegistry().getDefaultEditor(
											files[i].getName());
							if (desc != null)
								page.openEditor(input, desc.getId());
						}
					} catch (Exception e) {
						IStatus status = new Status(IStatus.ERROR,
								JBossServerUIPlugin.PLUGIN_ID,
								Messages.FilesetsCannotOpenFile, e);
						JBossServerUIPlugin.getDefault().getLog().log(status);
					}
				}
			}
		};
		editFileAction.setText(Messages.FilesetsEditFile);
	}
	
	protected void refreshViewer() {
		actionSite.getStructuredViewer().refresh();
	}

	public void doubleClick(DoubleClickEvent event) {
		setSelection();
		editFileAction.run();
	}

}