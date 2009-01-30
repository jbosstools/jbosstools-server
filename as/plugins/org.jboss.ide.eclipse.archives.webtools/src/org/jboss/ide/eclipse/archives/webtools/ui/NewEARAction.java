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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.wizards.AbstractArchiveWizard;
import org.jboss.ide.eclipse.archives.webtools.Messages;
import org.jboss.ide.eclipse.archives.webtools.archivetypes.EarArchiveType;

public class NewEARAction implements IActionDelegate {

	private IStructuredSelection selection;
	public void run() {
		AbstractArchiveWizard wizard = new NewEARWizard();
		wizard.init(PlatformUI.getWorkbench(), selection);
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.open();
	}


	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if( selection instanceof IStructuredSelection)
			this.selection = (IStructuredSelection)selection;
	}

	public void init(IViewPart view) {
	}

	public static class NewEARWizard extends AbstractArchiveWizard {

		private EARPreviewPage earInfoPage;

		public NewEARWizard () {
			setWindowTitle(Messages.NewEar);
		}

		public ImageDescriptor getImageDescriptor() {
			return ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_EAR);
		}

		public WizardPage[] createWizardPages() {
			earInfoPage = new EARPreviewPage(this);

			return new WizardPage[] { earInfoPage };
		}

		public boolean performFinish(IArchive pkg) {
			pkg.setArchiveType(ArchivesCore.getInstance().getExtensionManager().getArchiveType(EarArchiveType.ID));
			return true;
		}

		public String getArchiveExtension() {
			return "ear"; //$NON-NLS-1$
		}
	}


	protected static class EARPreviewPage extends PreviewPage {
		protected EARPreviewPage(NewEARWizard wiz) {
			super(wiz, Messages.EarPreview, Messages.EarPreview,
					ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_EAR));
		}
		protected void addToPackage() {
	    	IArchiveType type = ArchivesCore.getInstance().getExtensionManager().getArchiveType(EarArchiveType.ID);
    		type.fillDefaultConfiguration(wizard.getProject().getName(), wizard.getArchive(), new NullProgressMonitor());
		}

		protected String getDescriptionMessage() {
			return 	Messages.EarDescription;
		}
	}
}
