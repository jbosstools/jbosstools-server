package org.jboss.ide.eclipse.as.ui.packages;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.jboss.ide.eclipse.archives.core.model.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.ui.wizards.AbstractArchiveWizard;

public class NewWARWizard extends AbstractArchiveWizard {

	private WARInfoWizardPage warInfoPage;
	
	public NewWARWizard ()
	{
		setWindowTitle("New WAR");
	}
	
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public WizardPage[] createWizardPages() {
		warInfoPage = new WARInfoWizardPage(this);
		
		return new WizardPage[] { warInfoPage };
	}

	public boolean performFinish(IArchive pkg) {
		pkg.setArchiveType(ArchivesCore.getArchiveType("org.jboss.ide.eclipse.as.core.packages.warPackage"));
		return true;
	}
	
	public String getArchiveExtension() {
		return "war";
	}

}
