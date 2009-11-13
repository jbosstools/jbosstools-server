package org.jboss.ide.eclipse.archives.ui.test.wizards;

import junit.framework.TestCase;

import org.eclipse.core.commands.IParameter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.jboss.ide.eclipse.archives.ui.views.ProjectArchivesCommonView;
import org.jboss.ide.eclipse.archives.ui.wizards.NewJARWizard;
import org.jboss.ide.eclipse.archives.ui.wizards.pages.ArchiveInfoWizardPage;
import org.jboss.tools.test.util.JobUtils;
import org.jboss.tools.test.util.ResourcesUtils;
import org.jboss.tools.test.util.WorkbenchUtils;

public class NewJARWizardTest extends TestCase {
	
	IProject project = null;
	
	@Override
	protected void setUp() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("archive-test");
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		IViewPart view= page.showView(IPageLayout.ID_RES_NAV);
		if (view instanceof ISetSelectionTarget) {
			ISelection selection= new StructuredSelection(project);
			((ISetSelectionTarget)view).selectReveal(selection);
		}
	}

	public void testJarWizardIsOpened() throws PartInitException {
		NewJARWizard
		aWizard = new NewJARWizard();
	
		WizardDialog dialog = new WizardDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				aWizard);
				dialog.setBlockOnOpen(false);
		aWizard.init(PlatformUI.getWorkbench(), new StructuredSelection(project));
		try {
			dialog.create();
			dialog.open();
			ArchiveInfoWizardPage page1 = (ArchiveInfoWizardPage)dialog.getSelectedPage();
			assertTrue(page1.isPageComplete());
			assertTrue(aWizard.canFinish());
			aWizard.performFinish();
		} finally {
			dialog.close();
		}

	}
}
