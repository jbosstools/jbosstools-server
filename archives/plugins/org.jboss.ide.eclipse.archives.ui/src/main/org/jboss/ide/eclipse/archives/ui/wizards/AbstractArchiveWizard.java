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
package org.jboss.ide.eclipse.archives.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;
import org.jboss.ide.eclipse.archives.ui.PackagesUIPlugin;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate.WrappedProject;
import org.jboss.ide.eclipse.archives.ui.views.ProjectArchivesCommonView;
import org.jboss.ide.eclipse.archives.ui.wizards.pages.ArchiveInfoWizardPage;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public abstract class AbstractArchiveWizard extends WizardWithNotification implements INewWizard {
	private ArchiveInfoWizardPage firstPage;
	private WizardPage pages[];
	protected IProject project;
	protected IArchive existingPackage;
	protected String initialDestinationPath;
	protected boolean isPathWorkspaceRelative;
	protected IArchiveNode initialDestinationNode;

	public AbstractArchiveWizard () {
	}

	public AbstractArchiveWizard (IArchive existingPackage) {
		this.existingPackage = existingPackage;
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(existingPackage.getProjectName());
	}

	public void addPages() {
		firstPage = new ArchiveInfoWizardPage(this, existingPackage);
		addPage(firstPage);
		pages = createWizardPages();
		for (int i = 0; i < pages.length; i++) {
			addPage(pages[i]);
		}
	}

	public boolean canFinish() {
		if (firstPage.isPageComplete()) {
			for (int i = 0; i < pages.length; i++) {
				if (!pages[i].isPageComplete()) return false;
			}
			return true;
		}
		return false;
	}

	public boolean performFinish() {
		IWizardPage[] allPages = getPages();
		for( int i = 0; i < allPages.length; i++ ) {
			if (allPages[i] instanceof WizardPageWithNotification) {
				((WizardPageWithNotification)allPages[i]).pageExited(WizardWithNotification.FINISH);
			}
		}

		final boolean create = (this.existingPackage == null);
		final IArchive pkg = firstPage.getArchive();

		boolean performed = performFinish(pkg);

		if (performed) {
			try {
				getContainer().run(true, false, new IRunnableWithProgress () {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						IArchiveNode parent;
						IArchiveNode destNode = firstPage.getDestinationNode();
						if( destNode != null ) {
							// if we're modifying an existing package, remove old parentage
							if (!create && !destNode.equals(pkg.getParent())) {
								if (pkg.getParent() != null) {
									pkg.getParent().removeChild(pkg);
								}
							}
							parent = (IArchiveNode)destNode;
						} else {
							// parent is a String / path, so this is a top level node
							parent = ArchivesModel.instance().getRoot(project.getLocation());
							if( parent == null )
								parent = ArchivesModel.instance().registerProject(project.getLocation(), null);
						}

						try {
							if( create )
								parent.addChild(pkg);
							ArchivesModel.instance().save(project.getLocation(), monitor);
						} catch( ArchivesModelException ame ) {
							IStatus status = new Status(IStatus.ERROR, PackagesUIPlugin.PLUGIN_ID, ArchivesUIMessages.ErrorCompletingWizard, ame);
							PackagesUIPlugin.getDefault().getLog().log(status);
						}
					}
				});
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
		}
		return performed;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (selection == null) {
			ISelection sel = ProjectArchivesCommonView.getInstance().getCommonViewer().getSelection();
			if ( !(sel instanceof IStructuredSelection)) {
				return;
			}
			selection = (IStructuredSelection) sel;
		}
		init(selection);

		if (initialDestinationPath == null) {
			ISelection sel = ProjectArchivesCommonView.getInstance().getCommonViewer().getSelection();
			if ( !(sel instanceof IStructuredSelection)) {
				return;
			}
			selection = (IStructuredSelection) sel;
			init(selection);
		}
		setNeedsProgressMonitor(true);
	}

	private void init(IStructuredSelection selection) {
		project = null;

		Object selected = (selection.isEmpty() ? project : selection.getFirstElement());

		if (selected instanceof IArchiveNode) {
			IArchiveNode node = (IArchiveNode) selected;
			if (node.getNodeType() == IArchiveNode.TYPE_ARCHIVE || node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FOLDER) {
				initialDestinationNode = (IArchiveNode)selected;
			}
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(node.getProjectName());
		} else if (selected instanceof IContainer) {
			project = ((IContainer)selected).getProject();
			initialDestinationPath = ((IContainer)selected).getFullPath().toString();
			isPathWorkspaceRelative = true;
		} else if(selected instanceof WrappedProject) {
			project = ((WrappedProject)selected).getElement();
			initialDestinationPath = project.getFullPath().toString();
			isPathWorkspaceRelative = true;
		} else {
			project = ProjectArchivesCommonView.getInstance().getCurrentProject();
			if( project != null ) {
				initialDestinationPath = project.getFullPath().toString();
				isPathWorkspaceRelative = true;
			}
		}
	}

	public IArchiveNode getInitialNode() {
		return initialDestinationNode;
	}

	public String getInitialPath() {
		return initialDestinationPath;
	}

	public boolean isInitialPathWorkspaceRelative() {
		return isPathWorkspaceRelative;
	}

	public IProject getProject() {
		return project;
	}

	/**
	 * Returns the package created by this wizard.
	 * Note: This should only be called after the first page has been completed
	 * @return The package
	 */
	public IArchive getArchive () {
		return firstPage.getArchive();
	}

	public abstract boolean performFinish(IArchive pkg);
	public abstract WizardPage[] createWizardPages();
	public abstract ImageDescriptor getImageDescriptor();
	public abstract String getArchiveExtension();
}
