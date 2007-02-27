package org.jboss.ide.eclipse.as.ui.packages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jboss.ide.eclipse.packages.ui.PackagesUIPlugin;

public class WARInfoWizardPage extends WizardPage {

	public WARInfoWizardPage ()
	{
		super("WAR information", "WAR Information", PackagesUIPlugin.getImageDescriptor(PackagesUIPlugin.IMG_NEW_WAR_WIZARD));
	}
	
	public void createControl(Composite parent) {
		setMessage("Information for the setup of your WAR");
		Composite main = new Composite(parent, SWT.NONE);
		
		setControl(main);
	}

	public boolean isPageComplete() {
		return false;
	}
}
