package org.jboss.ide.eclipse.as.ui.packages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.packages.ui.PackagesUIPlugin;
import org.jboss.ide.eclipse.ui.util.ActionWithDelegate;

public class NewWARAction extends ActionWithDelegate implements IViewActionDelegate {

	public void run() {
		NewWARWizard wizard = new NewWARWizard();
		wizard.init(PlatformUI.getWorkbench(), getSelection());
		
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		int response = dialog.open();
		if (response == Dialog.OK)
		{
			
		}
	}
	
	public ImageDescriptor getImageDescriptor() {
		return PackagesUIPlugin.getImageDescriptor(PackagesUIPlugin.IMG_WAR);
	}
	
	public String getText() {
		return "WAR";
	}
	
	public String getToolTipText() {
		return "Create a new WAR package";
	}
	
	public void init(IViewPart view) {
		
	}
}
