package org.jboss.ide.eclipse.archives.ui.test.views;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.ui.views.ProjectArchivesCommonView;

import junit.framework.TestCase;

public class ProjectsArchiveViewTest extends TestCase {
	
	public void testArchivesViewIsOpened() throws PartInitException {
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		IViewPart view = page.showView(ProjectArchivesCommonView.ID);
		assertTrue(view instanceof ProjectArchivesCommonView);
	}
}
