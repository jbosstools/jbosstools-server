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
import org.jboss.ide.eclipse.archives.webtools.archivetypes.EarArchiveType;
import org.jboss.ide.eclipse.archives.webtools.archivetypes.EjbArchiveType;

public class NewEJBJARAction implements IActionDelegate {
	private IStructuredSelection selection;
	public void run() {
		AbstractArchiveWizard wizard = new NewEJBWizard();
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

	public static class NewEJBWizard extends AbstractArchiveWizard {

		private EjbJarPreviewPage ejbInfoPage;

		public NewEJBWizard () {
			setWindowTitle(Messages.NewEjbJar);
		}

		public ImageDescriptor getImageDescriptor() {
			return ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_EJB_JAR);
		}

		public WizardPage[] createWizardPages() {
			ejbInfoPage = new EjbJarPreviewPage(this);

			return new WizardPage[] { ejbInfoPage };
		}

		public boolean performFinish(IArchive pkg) {
			pkg.setArchiveType(ArchivesCore.getInstance().getExtensionManager().getArchiveType(EjbArchiveType.ID));
			return true;
		}

		public String getArchiveExtension() {
			return "jar"; //$NON-NLS-1$
		}
	}


	protected static class EjbJarPreviewPage extends PreviewPage {
		protected EjbJarPreviewPage(NewEJBWizard wiz) {
			super(wiz, Messages.EjbJarPreview, Messages.EjbJarPreview,
					ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_NEW_JAR_WIZARD));
		}
		protected String getArchiveTypeId() {
			return EjbArchiveType.ID;
		}
		protected String getDescriptionMessage() {
			return 	Messages.EjbJarDescription;
		}
	}

}
