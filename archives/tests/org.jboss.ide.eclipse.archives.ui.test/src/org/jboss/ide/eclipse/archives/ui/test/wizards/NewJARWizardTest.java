package org.jboss.ide.eclipse.archives.ui.test.wizards;

import junit.framework.TestCase;

import org.eclipse.core.commands.IParameter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.jboss.ide.eclipse.archives.ui.wizards.NewJARWizard;
import org.jboss.ide.eclipse.archives.ui.wizards.pages.ArchiveInfoWizardPage;
import org.jboss.tools.test.util.ResourcesUtils;
import org.jboss.tools.test.util.WorkbenchUtils;

public class NewJARWizardTest extends TestCase {
	
	IProject project = null;
	
	@Override
	protected void setUp() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("archives-example");
//		IWorkbenchPage page= getSite().getWorkbenchWindow().getActivePage();
//		IViewPart view= page.showView(IPageLayout.ID_RES_NAV);
//		if (view instanceof ISetSelectionTarget) {
//			ISelection selection= new StructuredSelection(resource);
//			((ISetSelectionTarget)view).selectReveal(selection);
//		}
	}

	public void testJarWizardIsOpened() {
		IWizard
		aWizard = new NewJARWizard();
	
		WizardDialog dialog = new WizardDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				aWizard);
		dialog.setBlockOnOpen(false);
		dialog.create();	
		try {
			ArchiveInfoWizardPage page = (ArchiveInfoWizardPage)dialog.getSelectedPage();
			
		} finally {
			dialog.close();
		}

	}
}
