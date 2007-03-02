package org.jboss.ide.eclipse.as.ui.packages;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;
import org.jboss.ide.eclipse.packages.ui.wizards.AbstractPackageWizard;

public class NewWARWizard extends AbstractPackageWizard {

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

	public boolean performFinish(IPackage pkg) {
		pkg.setPackageType(PackagesCore.getPackageType("org.jboss.ide.eclipse.as.core.packages.warPackage"));
		return true;
	}
	
	public String getPackageExtension() {
		return "war";
	}

}
