package org.jboss.ide.eclipse.archives.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.ui.wizards.NewJARWizard;

public class NewJARAction implements IActionDelegate {
	public void run() {
		try {
			NewJARWizard wizard = new NewJARWizard();
			wizard.init(PlatformUI.getWorkbench(), selection);
			WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
			dialog.open();
		} catch( Exception e ) {
			// TODO
		}
	}
	
	private IStructuredSelection selection;
	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if( selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection)selection;
		}
	}
}
