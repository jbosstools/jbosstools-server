package org.jboss.ide.eclipse.archives.jdt.integration.ui;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.jboss.ide.eclipse.archives.core.build.SaveArchivesJob;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.INamedContainerArchiveNode;
import org.jboss.ide.eclipse.archives.jdt.integration.model.IArchiveLibFileSet;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesActionProvider;
import org.jboss.ide.eclipse.archives.ui.wizards.FilesetWizard;

public class JDTArchivesActionProvider extends CommonActionProvider {
	private ICommonViewerSite site;
	private Action newLibFilesetAction, editLibFilesetAction, deleteAction;

	public JDTArchivesActionProvider() {
	}

	public void init(ICommonActionExtensionSite aSite) {
		site = aSite.getViewSite();
		createActions();
	}

	protected void createActions() {
		newLibFilesetAction = new Action(
				ArchivesUIMessages.ProjectPackagesView_newLibFilesetAction_label,
				JavaUI.getSharedImages().getImageDescriptor(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_LIBRARY)) {
			public void run() {
				createLibFileset();
			}
		};
		
		editLibFilesetAction = new Action(
				ArchivesUIMessages.ProjectPackagesView_editFilesetAction_label,
				JavaUI.getSharedImages().getImageDescriptor(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_LIBRARY)) {
			public void run() {
				editAction();
			}
		};

		deleteAction = new Action (ArchivesUIMessages.ProjectPackagesView_deleteFilesetAction_label, platformDescriptor(ISharedImages.IMG_TOOL_DELETE)) {
			public void run () {
				deleteSelectedNode();
			}
		};

	}

	public void fillContextMenu(IMenuManager manager) {
		IArchiveNode node = getSelectedNode();
		if( node instanceof INamedContainerArchiveNode )
			manager.insertBefore(ArchivesActionProvider.END_ADD_CHILD_SEPARATOR_ID, newLibFilesetAction);
		if( node instanceof IArchiveLibFileSet) {
			manager.insertAfter(ArchivesActionProvider.END_ADD_CHILD_SEPARATOR_ID, deleteAction);
			manager.insertAfter(ArchivesActionProvider.END_ADD_CHILD_SEPARATOR_ID, editLibFilesetAction);
		}
	}

	
	private void deleteSelectedNode () {
		IArchiveNode node = getSelectedNode();
		if (node != null) {
			final IArchiveNode parent = (IArchiveNode) node.getParent();
			parent.removeChild(node);
			SaveArchivesJob job = new SaveArchivesJob(parent.getProjectPath());
			job.schedule();
		}
	}

	
	private void createLibFileset() {
		IArchiveNode selected = getSelectedNode();
		WizardDialog dialog = new WizardDialog(site.getShell(),
				new LibFilesetWizard(null, selected));
		dialog.open();
	}

	private void editAction() {
		IArchiveNode node = getSelectedNode();
		if (node != null) {
			IArchiveLibFileSet fileset = (IArchiveLibFileSet) node;
			WizardDialog dialog = new WizardDialog(site.getShell(), new LibFilesetWizard(fileset, node.getParent()));
			dialog.open();
		}
	}
	
	private IArchiveNode getSelectedNode() {
		Object selected = getSelectedObject();
		if (selected instanceof IArchiveNode)
			return ((IArchiveNode) selected);
		return null;
	}

	private Object getSelectedObject() {
		IStructuredSelection selection = getSelection();
		if (selection != null && !selection.isEmpty())
			return selection.getFirstElement();
		return null;
	}

	private IStructuredSelection getSelection() {
		return (IStructuredSelection) site.getSelectionProvider()
				.getSelection();
	}

	private ImageDescriptor platformDescriptor(String desc) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(desc);
	}

}
