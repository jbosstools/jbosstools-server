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

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.build.SaveArchivesJob;
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
	
	
	private MenuManager newPackageManager;
	private NodeContribution[]  nodePopupMenuContributions;
	private NewArchiveAction[] newPackageActions;
	private Action editAction, deleteAction, newFolderAction, newFilesetAction;
	private Action buildAction;
	private ICommonViewerSite site;

	public ArchivesActionProvider() {
	}

	public void init(ICommonActionExtensionSite aSite) {
		newPackageActions = ExtensionManager.findNewArchiveActions();
		nodePopupMenuContributions = ExtensionManager.findNodePopupMenuContributions();
		Arrays.sort(nodePopupMenuContributions);
		site = aSite.getViewSite();
		createActions();
		newPackageManager = new MenuManager(ArchivesUIMessages.ProjectPackagesView_newPackageMenu_label, NEW_PACKAGE_MENU_ID);
	}

	public void fillContextMenu(IMenuManager manager) {
		menuAboutToShow2(manager);
	}

	public void menuAboutToShow2(IMenuManager manager) {
		if (ProjectArchivesCommonView.getInstance() == null) {
			return;
		}
		IProject currentProject = ProjectArchivesCommonView.getInstance().getCurrentProject();
		if (currentProject == null || !currentProject.exists() || !currentProject.isOpen()) {
			return;
		}
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
		} else if( ProjectArchivesCommonView.getInstance().getCurrentProject() != null ){
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
				deleteSelectedNode();
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

	private void deleteSelectedNode () {
		IArchiveNode node = getSelectedNode(site);
		if (node != null) {
			final IArchiveNode parent = (IArchiveNode) node.getParent();
			parent.removeChild(node);
			SaveArchivesJob job = new SaveArchivesJob(parent.getProjectPath());
			job.schedule();
		}
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
