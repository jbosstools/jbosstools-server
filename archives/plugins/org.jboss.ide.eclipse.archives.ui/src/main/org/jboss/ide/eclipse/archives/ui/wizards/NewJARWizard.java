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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class NewJARWizard extends AbstractArchiveWizard {
	public WizardPage[] createWizardPages() {
		return new WizardPage[0];
	}

	public NewJARWizard () {
		super();
		setWindowTitle(ArchivesUIMessages.NewJARWizard_windowTitle);
	}

	public NewJARWizard (IArchive existingPackage) {
		super(existingPackage);
		setWindowTitle(ArchivesUIMessages.NewJARWizard_windowTitle_editJAR);
	}

	public boolean performFinish(IArchive pkg) {
		pkg.setArchiveType(ArchivesCore.getInstance().getExtensionManager().getArchiveType("jar")); //$NON-NLS-1$
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_NEW_JAR_WIZARD);
	}

	public String getArchiveExtension() {
		return "jar"; //$NON-NLS-1$
	}
}
