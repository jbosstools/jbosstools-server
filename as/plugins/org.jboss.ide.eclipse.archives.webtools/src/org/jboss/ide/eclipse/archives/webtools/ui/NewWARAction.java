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
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.wizards.AbstractArchiveWizard;
import org.jboss.ide.eclipse.archives.webtools.Messages;
import org.jboss.ide.eclipse.archives.webtools.archivetypes.WarArchiveType;

public class NewWARAction implements IActionDelegate {
	private IStructuredSelection selection;
	public void run() {
		NewWARWizard wizard = new NewWARWizard();
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


	public static class NewWARWizard extends AbstractArchiveWizard {

		private WarPreviewPage warInfoPage;

		public NewWARWizard () {
			setWindowTitle(Messages.NewWar);
		}

		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		public WizardPage[] createWizardPages() {
			warInfoPage = new WarPreviewPage(this);

			return new WizardPage[] { warInfoPage };
		}

		public boolean performFinish(IArchive pkg) {
			pkg.setArchiveType(ArchivesCore.getInstance().getExtensionManager().getArchiveType(WarArchiveType.WAR_PACKAGE_TYPE));
			return true;
		}

		public String getArchiveExtension() {
			return "war"; //$NON-NLS-1$
		}
	}

	protected static class WarPreviewPage extends PreviewPage {
		protected WarPreviewPage(NewWARWizard wiz) {
			super(wiz, Messages.WarPreview, Messages.WarPreview,
					ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_NEW_WAR_WIZARD));
		}
		protected void addToPackage() {
	    	IArchiveType type = ArchivesCore.getInstance().getExtensionManager().getArchiveType(WarArchiveType.WAR_PACKAGE_TYPE);
    		type.fillDefaultConfiguration(wizard.getProject().getName(), wizard.getArchive(), new NullProgressMonitor());
		}

		protected String getDescriptionMessage() {
			return 	Messages.WarDescription;
		}
	}
}
