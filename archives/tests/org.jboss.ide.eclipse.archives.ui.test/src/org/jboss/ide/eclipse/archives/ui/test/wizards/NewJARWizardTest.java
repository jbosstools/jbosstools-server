package org.jboss.ide.eclipse.archives.ui.test.wizards;

import junit.framework.TestCase;

import org.eclipse.core.commands.IParameter;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.ui.wizards.NewJARWizard;
import org.jboss.ide.eclipse.archives.ui.wizards.pages.ArchiveInfoWizardPage;
import org.jboss.tools.test.util.WorkbenchUtils;

public class NewJARWizardTest extends TestCase {
	
	public void testJarWizardIsOpened() {
		IWizard
		aWizard = new NewJARWizard();
	
		WizardDialog dialog = new WizardDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				aWizard);
		dialog.setBlockOnOpen(false);
		dialog.open();
		try {
			ArchiveInfoWizardPage page = (ArchiveInfoWizardPage)dialog.getSelectedPage();
		} finally {
			dialog.close();
		}
	}
}
