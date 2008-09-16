package org.jboss.ide.eclipse.archives.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;

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
		pkg.setArchiveType(ArchivesCore.getInstance().getExtensionManager().getArchiveType("jar"));
		return true;
	}
	
	public ImageDescriptor getImageDescriptor() {
		return ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_NEW_JAR_WIZARD);
	}
	
	public String getArchiveExtension() {
		return "jar";
	}
}
