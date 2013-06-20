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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.build.SaveArchivesJob;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.core.model.INamedContainerArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveFileSetImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveFolderImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveImpl;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;
import org.jboss.ide.eclipse.archives.ui.ExtensionManager;
import org.jboss.ide.eclipse.archives.ui.NodeContribution;
import org.jboss.ide.eclipse.archives.ui.PackagesUIPlugin;
import org.jboss.ide.eclipse.archives.ui.actions.BuildAction;
import org.jboss.ide.eclipse.archives.ui.actions.NewArchiveAction;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate.WrappedProject;
import org.jboss.ide.eclipse.archives.ui.views.ProjectArchivesCommonView;
import org.jboss.ide.eclipse.archives.ui.wizards.FilesetWizard;
import org.jboss.ide.eclipse.archives.ui.wizards.NewJARWizard;

public class ArchivesActionProvider extends CommonActionProvider {
	public static final String NEW_PACKAGE_MENU_ID = "org.jboss.ide.eclipse.archives.ui.newPackageMenu"; //$NON-NLS-1$
	public static final String NODE_CONTEXT_MENU_ID = "org.jboss.ide.eclipse.archives.ui.nodeContextMenu"; //$NON-NLS-1$
	public static final String NEW_PACKAGE_ADDITIONS = "newPackageAdditions"; //$NON-NLS-1$
	public static final String INITIAL_SEPARATOR_ID = "org.jboss.ide.eclipse.archives.ui.providers.initialSeparator"; //$NON-NLS-1$
	public static final String END_ADD_CHILD_SEPARATOR_ID = "org.jboss.ide.eclipse.archives.ui.providers.endAddChildSeparator"; //$NON-NLS-1$
	
	private static final String DELETE_NODE_PERMISSION_TOGGLE = "ArchivesActionProvider.DeleteNodePreferenceKey"; //$NON-NLS-1$
	
	private MenuManager newPackageManager;
	private NodeContribution[]  nodePopupMenuContributions;
	private NewArchiveAction[] newPackageActions;
	private Action editAction, deleteAction, newFolderAction, newFilesetAction;
	private Action buildAction;
	private ICommonViewerSite site;
	private KeyListener deleteKeyListener;
	
	public ArchivesActionProvider() {
		deleteKeyListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if( e.keyCode == SWT.DEL ) {
					deleteSelectedNodes();
				}
			}
			public void keyReleased(KeyEvent e) {
			} 
		};
	}

	public void init(ICommonActionExtensionSite aSite) {
		newPackageActions = ExtensionManager.findNewArchiveActions();
		nodePopupMenuContributions = ExtensionManager.findNodePopupMenuContributions();
		Arrays.sort(nodePopupMenuContributions);
		site = aSite.getViewSite();
		createActions();
		newPackageManager = new MenuManager(ArchivesUIMessages.ProjectPackagesView_newPackageMenu_label, NEW_PACKAGE_MENU_ID);
		Control viewerControl = aSite.getStructuredViewer().getControl();
		viewerControl.addKeyListener(deleteKeyListener);
	}

	public void fillContextMenu(IMenuManager manager) {
		if (getContext() != null) {
			enableBuildAction(buildAction, getContext().getSelection());
		} else {
			buildAction.setEnabled(false);
		}
		menuAboutToShow2(manager);
	}

	public void menuAboutToShow2(IMenuManager manager) { 
		if( site == null || site.getAdapter(IViewSite.class) == null )
			return;
		addNewPackageActions(newPackageManager);

		IStructuredSelection selection = getSelection(site);
		if (selection != null && !selection.isEmpty()) {
			Object element = selection.getFirstElement();

			if (element instanceof IProject || element instanceof WrappedProject ) {
				manager.add(newPackageManager);
				manager.add(buildAction);
				buildAction.setText(ArchivesUIMessages.ProjectPackagesView_buildProjectAction_label);
			} else if( element instanceof IArchiveNode ){
				IArchiveNode node = (IArchiveNode)element;
				if( manager.find(INITIAL_SEPARATOR_ID) == null){
					Separator s = new Separator(INITIAL_SEPARATOR_ID);
					s.setVisible(false);
					manager.add(s);
				}
				if( manager.find(END_ADD_CHILD_SEPARATOR_ID) == null){
					Separator s = new Separator(END_ADD_CHILD_SEPARATOR_ID);
					s.setVisible(false);
					manager.add(s);
				}
				if (node instanceof INamedContainerArchiveNode ) {
					manager.insertAfter(INITIAL_SEPARATOR_ID, newFilesetAction);
					manager.insertAfter(INITIAL_SEPARATOR_ID, newFolderAction);
					manager.insertAfter(INITIAL_SEPARATOR_ID, newPackageManager);
					manager.insertBefore(END_ADD_CHILD_SEPARATOR_ID, new Separator(END_ADD_CHILD_SEPARATOR_ID));
				}

				
				if (node.getNodeType() == IArchiveNode.TYPE_ARCHIVE && node instanceof ArchiveImpl) {
					editAction.setText(ArchivesUIMessages.ProjectPackagesView_editPackageAction_label);
					deleteAction.setText(ArchivesUIMessages.ProjectPackagesView_deletePackageAction_label);
					editAction.setImageDescriptor(ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_PACKAGE_EDIT));
					buildAction.setText(ArchivesUIMessages.ProjectPackagesView_buildArchiveAction_label);
					manager.insertAfter(END_ADD_CHILD_SEPARATOR_ID, buildAction);
					manager.add(editAction);
					manager.add(deleteAction);
				} else if (node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FOLDER && node instanceof ArchiveFolderImpl) {
					editAction.setText(ArchivesUIMessages.ProjectPackagesView_editFolderAction_label);
					deleteAction.setText(ArchivesUIMessages.ProjectPackagesView_deleteFolderAction_label);
					editAction.setImageDescriptor(platformDescriptor(ISharedImages.IMG_OBJ_FOLDER));
					manager.add(editAction);
					manager.add(deleteAction);
				} else if (node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FILESET && node instanceof ArchiveFileSetImpl) {
					editAction.setText(ArchivesUIMessages.ProjectPackagesView_editFilesetAction_label);
					deleteAction.setText(ArchivesUIMessages.ProjectPackagesView_deleteFilesetAction_label);
					ImageDescriptor id = ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_MULTIPLE_FILES);
					editAction.setImageDescriptor(id);
					manager.add(editAction);
					manager.add(deleteAction);
				}
				addContextMenuContributions(node, manager);
			}
		} else {
			ProjectArchivesCommonView v = ProjectArchivesCommonView.getInstance();
			if( v != null && v.getCurrentProject() != null ) 
				manager.add(newPackageManager);
		}
	}

	protected void createActions() {
		newFolderAction = new Action(ArchivesUIMessages.ProjectPackagesView_newFolderAction_label, platformDescriptor(ISharedImages.IMG_OBJ_FOLDER)) {
			public void run () {
				createFolder();
			}
		};

		newFilesetAction = new Action(ArchivesUIMessages.ProjectPackagesView_newFilesetAction_label, ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_MULTIPLE_FILES)) {
			public void run () {
				createFileset();
			}
		};

		deleteAction = new Action (ArchivesUIMessages.ProjectPackagesView_deletePackageAction_label, platformDescriptor(ISharedImages.IMG_TOOL_DELETE)) {
			public void run () {
				deleteSelectedNodes();
			}
		};

		editAction = new Action (ArchivesUIMessages.ProjectPackagesView_editPackageAction_label, ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_PACKAGE_EDIT)) {
			public void run () {
				editSelectedNode();
			}
		};

		buildAction = new Action(ArchivesUIMessages.BuildArchivesNode, ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_BUILD_PACKAGES)) {
			public void run() {
				new BuildAction().run(getSelectedObject(site));
			}
		};
	}

	private void enableBuildAction(IAction action, ISelection selection) {
		Object selected = null;
		if( !selection.isEmpty() && selection instanceof IStructuredSelection ) {
			Object o = ((IStructuredSelection)selection).getFirstElement();
			if(o instanceof WrappedProject )
				o = ((WrappedProject)o).getElement();
			if( o instanceof IAdaptable ) {
				IResource res = (IResource)  ((IAdaptable)o).getAdapter(IResource.class);
				if( res != null ) {
					selected = res.getProject();
				}
			}
			if( o instanceof IArchiveNode )
				selected = o;
			boolean enabled = selected instanceof IArchiveNode
					|| ArchivesModel.instance().canReregister(
							((IProject) selected).getLocation());
			action.setEnabled(enabled);
		}
	}


	private void addContextMenuContributions (final IArchiveNode context, IMenuManager mgr) {

		for( int i = 0; i < nodePopupMenuContributions.length; i++ ) {
			final NodeContribution contribution = nodePopupMenuContributions[i];
			if ( contribution.getActionDelegate().isEnabledFor(context)) {
				Action action = new Action () {
					public String getId() {
						return contribution.getId();
					}

					public ImageDescriptor getImageDescriptor() {
						return contribution.getIcon();
					}

					public String getText() {
						return contribution.getLabel();
					}

					public void run() {
						contribution.getActionDelegate().run(context);
					}
				};
				mgr.add(action);
			}
		}
	}


	/**
	 * Adds the new package type actions (which come from an extension point)
	 * to the menu.
	 * @param manager
	 */
	private void addNewPackageActions (IMenuManager manager) {
       //workaround for JBIDE-3016
       manager.removeAll();
                		 							
		for( int i = 0; i < newPackageActions.length; i++ ) {
			NewArchiveAction action = newPackageActions[i];
			ActionWrapper wrapped = new ActionWrapper(action);
			wrapped.selectionChanged(getSelection(site));
			manager.add(wrapped);
		}
	}

	public class ActionWrapper extends Action {
		private NewArchiveAction action;
		public ActionWrapper(NewArchiveAction act) {
			this.action = act;
		}
		public String getId() {
			return action.getId();
		}

		public ImageDescriptor getImageDescriptor() {
			return action.getIconDescriptor();
		}

		public String getText() {
			return action.getLabel();
		}

		public void run() {
			action.getAction().run(null);
		}

		public void selectionChanged(IStructuredSelection sel) {
			action.getAction().selectionChanged(this, sel);
		}
	}


	/*
	 * Methods below are called from the standard actions,
	 * the implementations of the action, where the action does its work etc
	 */

	private void createFolder () {
		IInputValidator validator = new IInputValidator () {
			public String isValid(String newText) {
				IArchiveNode selected = getSelectedNode(site);

				boolean folderExists = false;
				IArchiveNode[] folders = selected.getChildren(IArchiveNode.TYPE_ARCHIVE_FOLDER);
				for (int i = 0; i < folders.length; i++) {
					IArchiveFolder folder = (IArchiveFolder) folders[i];
					if (folder.getName().equals(newText)) {
						folderExists = true; break;
					}
				}

				if (folderExists) {
					return NLS.bind(
						ArchivesUIMessages.ProjectPackagesView_createFolderDialog_warnFolderExists, newText);

				}
				if("".equals(newText)) { //$NON-NLS-1$
					return ArchivesUIMessages.ProjectPackagesView_createFolderDialog_blank;
				}
				return null;
			}
		};

		InputDialog dialog = new InputDialog(getShell(),
			ArchivesUIMessages.ProjectPackagesView_createFolderDialog_title,
			ArchivesUIMessages.ProjectPackagesView_createFolderDialog_message, "", validator); //$NON-NLS-1$

		int response = dialog.open();
		if (response == Dialog.OK) {
			String[] folderPaths = dialog.getValue().split("[\\\\/]"); //$NON-NLS-1$
			IArchiveNode selected = getSelectedNode(site);
			IArchiveFolder current = null;
			IArchiveFolder temp = null;

			for(int i = folderPaths.length-1; i >= 0 ; i-- ) {
				temp = ArchivesCore.getInstance().getNodeFactory().createFolder();
				temp.setName(folderPaths[i]);
				if( current == null )
					current = temp;
				else {
					temp.addChild(current);
					current = temp;
				}
			}

			selected.addChild(current);
			new SaveArchivesJob(selected.getProjectPath()).schedule();
		}
	}

	private void createFileset () {
		IArchiveNode selected = getSelectedNode(site);
		WizardDialog dialog = new WizardDialog(getShell(), new FilesetWizard(null, selected));
		dialog.open();
	}

	private void editSelectedNode () {
		IArchiveNode node = getSelectedNode(site);
		if (node != null) {
			if (node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FILESET && node instanceof IArchiveStandardFileSet ) {
				IArchiveStandardFileSet fileset = (IArchiveStandardFileSet) node;
				WizardDialog dialog = new WizardDialog(getShell(), new FilesetWizard(fileset, node.getParent()));
				dialog.open();
			} else if (node.getNodeType() == IArchiveNode.TYPE_ARCHIVE && node instanceof IArchive) {
				IArchive pkg = (IArchive) node;
				WizardDialog dialog = new WizardDialog(getShell(), new NewJARWizard(pkg));
				dialog.open();
			} else if (node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FOLDER && node instanceof IArchiveFolder) {
				// folder can do the model save here.
				IArchiveFolder folder = (IArchiveFolder) node;
				InputDialog dialog = new InputDialog(getShell(),
						ArchivesUIMessages.ProjectPackagesView_createFolderDialog_title,
						ArchivesUIMessages.ProjectPackagesView_createFolderDialog_message, folder.getName(), null);

				int response = dialog.open();
				if (response == Dialog.OK) {
					folder.setName(dialog.getValue());
					new SaveArchivesJob(folder.getProjectPath()).schedule();
				}
			}
		}
	}

	private void deleteSelectedNodes() {
		IArchiveNode[] node = getSelectedNodes(site);
		if (node != null && node.length > 0 && approveDeletion(node)) {
			ArrayList<IPath> paths = new ArrayList<IPath>();
			for( int i = 0; i < node.length; i++ ) {
				final IArchiveNode parent = (IArchiveNode) node[i].getParent();
				parent.removeChild(node[i]);
				if( !paths.contains(parent.getProjectPath()))
					paths.add(parent.getProjectPath());
			}
			Iterator<IPath> pit = paths.iterator();
			while(pit.hasNext()) {
				new SaveArchivesJob(pit.next()).schedule();
			}
		}
	}

	private boolean approveDeletion(IArchiveNode[] node) {
		if( node != null ) {
			IPreferenceStore store = PackagesUIPlugin.getDefault().getPreferenceStore();
			String current = store.getString(DELETE_NODE_PERMISSION_TOGGLE);
			boolean doNotPrompt = Boolean.parseBoolean(current);
			if( doNotPrompt )
				return true;
			MessageDialogWithToggle d = 
				MessageDialogWithToggle.openYesNoQuestion(site.getShell(),
						ArchivesUIMessages.deleteNodeMBTitle,
						ArchivesUIMessages.deleteNodeMBDesc,
						ArchivesUIMessages.deleteNodeMBToggle,
						false, store,  DELETE_NODE_PERMISSION_TOGGLE); 
			int ret = d.getReturnCode();
			boolean toggle = d.getToggleState();
			if( ret == 2 ) {  // ok pressed
				store.setValue(DELETE_NODE_PERMISSION_TOGGLE, new Boolean(toggle).toString());
				try {
					((ScopedPreferenceStore)store).save();
				} catch(IOException ioe) {
					// ignore
				}
				return true;
			}
		}
		return false;
	}

	public static IArchiveNode[] getSelectedNodes(ICommonViewerSite site) {
		ArrayList<IArchiveNode> list = new ArrayList<IArchiveNode>();
		IStructuredSelection selection = getSelection(site);
		if (selection != null && !selection.isEmpty()) {
			Iterator<Object> it = selection.iterator();
			while(it.hasNext() ) {
				Object o = it.next();
				if( o instanceof IArchiveNode )
					list.add((IArchiveNode)o);
			}
		}
		return (IArchiveNode[]) list.toArray(new IArchiveNode[list.size()]);
	}

	
	public static IArchiveNode getSelectedNode (ICommonViewerSite site) {
		Object selected = getSelectedObject(site);
		if( selected instanceof IArchiveNode )
			return ((IArchiveNode)selected);
		return null;
	}
	public static Object getSelectedObject(ICommonViewerSite site) {
		IStructuredSelection selection = getSelection(site);
		if (selection != null && !selection.isEmpty())
			return selection.getFirstElement();
		return null;
	}
	public static IStructuredSelection getSelection(ICommonViewerSite site) {
		return (IStructuredSelection) site.getSelectionProvider().getSelection();
	}

	public static ImageDescriptor platformDescriptor(String desc) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(desc);
	}
	private Shell getShell() {
		return site.getShell();
	}
}
